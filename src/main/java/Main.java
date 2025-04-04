/**
 * This class serves as the main class that coordinates everything.
 * It connects the client to the server, starts the GUI and runs the
 * game.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

import gui.BewertungView;
import gui.GUIApp;
import javafx.application.Application;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.imageio.ImageIO;

public class Main {

    private Queue<String> serverMessageQueue;
    private Queue<String> clientMessageQueue;
    private Client client;
    public long lastPing;
    private int serverID;
    private Boolean receivedPong;
    private Boolean sendPing;
    private String username;
    private Boolean loggedIn;
    private Boolean inLobby;
    private GUIApp guiApp;
    private String lobbyName;
    private String messageType;
    private Game game;

    /**
     * This method is the constructor and initializes the queues.
     */
    public Main() {
        this.serverMessageQueue = new LinkedList<>();
        this.clientMessageQueue = new LinkedList<>();
        this.lastPing = 0;
        this.serverID = -1;
        this.receivedPong = true;
        this.sendPing = true;
        this.loggedIn = false;
        this.inLobby = false;
        this.messageType = "chatAll";
    }

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private void setUsername(String username) {
        this.username = username;
    }

    /**
     * This method properly handles any message in the serverMessageQueue.
     */
    public void handleServerMessage() {
        while (serverMessageQueue.peek() != null) {
            String serverMessage = serverMessageQueue.remove();
            Message message = new Message(serverMessage);

            switch (message.getType()) {
                case "id" -> {
                    this.serverID = Integer.parseInt(message.getID() + "");
                    client.setServerID(message.getID());
                }
                case "ping" -> {
                    Message response = new Message("pong", new String[]{message.getID() + ""});
                    clientMessageQueue.add(response.toString());
                }
                case "pong" -> receivedPong = true;
                case "disconnect" -> {
                    sendPing = false;
                    print("Server isn't reachable");
                    client.disconnect();
                }
                case "chatTo" -> print("(whisper) " + message.getParams()[1] + ": " + message.getParams()[3]);
                case "chatAll" -> print("(All) " + message.getParams()[1] + ": " + message.getParams()[2]);
                case "chatLobby" -> print("(Lobby) " + message.getParams()[1] + ": " + message.getParams()[3]);
                case "acceptLogin", "acceptSignup" -> {
                    username = message.getParams()[1];
                    serverID = message.getID();
                    loggedIn = true;
                    print("You're logged in as " + username);
                }
                case "rejectLogin" -> {
                    print("Username or password is wrong");
                    signInLogIn();
                }
                case "broadcast" -> {
                    print("Server: " + message.getParams()[0]);
                }
                case "username" -> {
                    username = message.getParams()[1];
                    print("Change username to:" + username);
                }
                case "online" -> {
                    print("Online:");
                    for (int i = 1; i < message.getParams().length; i++) {
                        print(message.getParams()[i]);
                    }
                }
                case "games" -> {
                    print("Games:");
                    for (int i = 1; i < message.getParams().length; i = i + 2) {
                        String game = message.getParams()[i] + " - " + message.getParams()[i + 1];
                        print(game);
                    }
                }
                case "lobbyPlayers" -> {
                    for (int i = 1; i < message.getParams().length; i++) {
                        GUIApp.addPlayer(message.getParams()[i], 0);
                        game.plusOnePlayer();
                    }
                }
                case "lobbyJoined" -> {
                    lobbyName = message.getParams()[1];
                    this.game = new Game(lobbyName);
                    messageType = "chatLobby";
                    inLobby = true;
                    print("You are in Lobby: " + message.getParams()[1]);
                    Message response = new Message("listLobbyPlayers", new String[]{message.getID() + "", lobbyName});
                    clientMessageQueue.add(response.toString());
                }
                case "lobbyLeft" -> {
                    inLobby = false;
                    messageType = "chatAll";
                    print("You left Lobby: " + message.getParams()[1]);
                }
                case "playerJoined" -> {
                    if (message.getParams()[0].equals(message.getParams()[2])) {
                        GUIApp.showLobbyView();
                    } else {
                        print(message.getParams()[1] + " has joined the lobby");
                        GUIApp.addPlayer(message.getParams()[1], 0);
                        game.plusOnePlayer();
                    }
                }
                case "drawingStarts" -> {
                    GUIApp.setTheme(message.getParams()[1]);
                    GUIApp.showGameView();
                    game.setStatus("drawing");
                }
                case "votingStarts" -> {
                    GUIApp.showBewertungView();
                    print("You would see a picture");
                    game.plusOneVotedRound();
                    game.setStatus("voting");
                }
                case "updatePoints" -> {
                    GUIApp.addPointsPlayer(message.getParams()[1], Integer.parseInt(message.getParams()[2]));
                }
                case "showLeaderboard" -> {
                    GUIApp.showLeaderboardView();
                }
                case "showEnding" -> {
                    GUIApp.showEndingView();
                }
                case "startNextRound" -> {
                    if(GUIApp.getLobbyLeader()) {
                        Message response = new Message("startDrawing", new String[]{serverID + "", lobbyName});
                        clientMessageQueue.add(response.toString());
                    }
                }
                default -> print(message.getID() + ": " + message.getParams()[1]);
            }
            LOGGER.info("received and handled Message {}", message.getType());
        }
    }

    /**
     * this method retrieves all unreceived messages from the client.
     */
    public void receiveMessage() {
        String serverMessage = client.getServerMessage();

        while (serverMessage != null) {
            serverMessageQueue.add(serverMessage);
            serverMessage = client.getServerMessage();
        }
    }

    public void print(String message) {
        guiApp.queuePrintMessage(message);
    }

    /**
     * This method retrieves all unhandled user input and adds it to the client message queue.
     */
    public void handleClientMessage() {
        String clientMessage = guiApp.getUserInput();

        while (clientMessage != null) {
            String[] splitClientMessage = clientMessage.split("\\s+");

            switch (splitClientMessage[0]) {
                case "/nick" -> {
                    Message message;
                    if (splitClientMessage.length == 2) {
                        String username = splitClientMessage[1];
                        message = new Message("changeUsername", new String[]{serverID + "", username});
                    } else {
                        if (messageType.equals("chatAll")) {
                            message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                        } else {
                            message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                        }
                    }
                    clientMessageQueue.add(message.toString());
                }
                case "/whisper" -> {
                    Message message;
                    if (splitClientMessage.length > 2) {
                        StringBuilder msgBuilder = new StringBuilder(splitClientMessage[2]);

                        for (int i = 3; i < splitClientMessage.length; i++) {
                            msgBuilder.append(" ").append(splitClientMessage[i]);
                        }

                        message = new Message("chatTo", new String[]{serverID + "", username, splitClientMessage[1], msgBuilder.toString()});
                    } else {
                        if (messageType.equals("chatAll")) {
                            message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                        } else {
                            message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                        }
                    }
                    clientMessageQueue.add(message.toString());
                }
                case "/online" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        message = new Message("whoOnline", new String[]{serverID + ""});
                    } else {
                        if (messageType.equals("chatAll")) {
                            message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                        } else {
                            message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                        }
                    }
                    clientMessageQueue.add(message.toString());
                }
                case "/games" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        message = new Message("whatGames", new String[]{serverID + ""});
                    } else {
                        if (messageType.equals("chatAll")) {
                            message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                        } else {
                            message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                        }
                    }
                    clientMessageQueue.add(message.toString());
                }
                case "/joinLobby" -> {
                    Message message;
                    if (splitClientMessage.length == 2) {
                        if (!inLobby) {
                            message = new Message("joinLobby", new String[]{serverID + "", splitClientMessage[1]});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're already in a lobby");
                        }
                    } else {
                        if (messageType.equals("chatAll")) {
                            message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                        } else {
                            message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                        }
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/createLobby" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (!inLobby) {
                            message = new Message("createLobby", new String[]{serverID + ""});
                            clientMessageQueue.add(message.toString());
                            GUIApp.setLobbyLeader(true);
                        } else {
                            print("You're already in a lobby");
                        }
                    } else {
                        if (messageType.equals("chatAll")) {
                            message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                        } else {
                            message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                        }
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/leaveLobby" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (inLobby) {
                            message = new Message("leaveLobby", new String[]{serverID + "", lobbyName});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're not in a lobby");
                        }
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/listPlayers" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (inLobby) {
                            message = new Message("listLobbyPlayers", new String[]{serverID + "", lobbyName});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're not in a lobby");
                        }
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/startGame" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (inLobby) {
                            print("This will start the game");
                            message = new Message("startDrawing", new String[]{serverID + "", lobbyName});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're not in a lobby");
                        }
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/timeIsUp" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (inLobby) {
                            print("Your picture will be uploaded");
                            message = new Message("readyToVote", new String[]{serverID + "", lobbyName});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're not in a lobby");
                        }
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/sendVote" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (inLobby) {
                            message = new Message("sendVote", new String[]{serverID + "", lobbyName, BewertungView.getVote() + ""});
                            clientMessageQueue.add(message.toString());
                            message = new Message("readyToVote", new String[]{serverID + "", lobbyName});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're not in a lobby");
                        }
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                        clientMessageQueue.add(message.toString());
                    }
                }
                case "/nextRound" ->  {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        if (inLobby) {
                            message = new Message("nextRound", new String[]{serverID + "", lobbyName});
                            clientMessageQueue.add(message.toString());
                        } else {
                            print("You're not in a lobby");
                        }
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                        clientMessageQueue.add(message.toString());
                    }
                }
                default -> {
                    Message message;
                    if (messageType.equals("chatAll")) {
                        message = new Message(messageType, new String[]{serverID + "", username, clientMessage});
                    } else {
                        message = new Message(messageType, new String[]{serverID + "", username, lobbyName, clientMessage});
                    }
                    clientMessageQueue.add(message.toString());
                }
            }

            clientMessage = guiApp.getUserInput();
        }
    }

    /**
     * This method queues any messages to the server into the client.
     */
    public void sendMessage() {
        while (clientMessageQueue.peek() != null) {
            client.queueClientMessage(clientMessageQueue.remove());
        }
    }

    /**
     * This method handles the Ping-Pong procedure on the client side.
     */
    public void handlePingPong() {
        if ((System.currentTimeMillis() - lastPing > 10000) && sendPing) {
            if (receivedPong) {
                Message ping = new Message("ping", new String[]{serverID + ""});
                clientMessageQueue.add(ping.toString());
                lastPing = System.currentTimeMillis();
                receivedPong = false;
            } else {
                print("Server not reachable");
                sendPing = false;
                client.disconnect();
            }
        }
    }

    /**
     * This method starts the client and retrieves the ID of the server.
     */
    public void initClient(String ip, int port) {
        this.client = new Client(ip, port);
        Thread clientThread = new Thread(client);
        clientThread.start();

        clientMessageQueue.add("whatID");
        sendMessage();

        while (serverID == -1) {
            receiveMessage();
            handleServerMessage();
        }
    }

    /**
     * This method handles the Sign-up and Log in processes.
     */
    public void signInLogIn() {
        boolean useUnParam = false;
        String answer = "";

        if (username != null) {
            print("Do you want to use " + username + " as your username? (y/n)");
            do {
                answer = guiApp.getUserInput();
            } while (answer == null);

            while (!(answer.equals("y") || answer.equals("n"))) {
                print("Answer with 'y' or 'n'!");

                do {
                    answer = guiApp.getUserInput();
                } while (answer == null);
            }

            if (answer.equals("y")) {
                useUnParam = true;
            }
        }

        print("Do you want to sign up or log in? (su/li)");

        while (answer == null) {
            answer = guiApp.getUserInput();
        }

        while (!(answer.equals("su") || answer.equals("li"))) {
            print("Answer with 'su ' or 'li'");
            do {
                answer = guiApp.getUserInput();
            } while (answer == null);
        }

        if (answer.equals("su")) {
            if (!useUnParam) {
                String suggestedUsername = System.getProperty("user.name") + (int) (Math.random() * 100);
                suggestedUsername = suggestedUsername.replaceAll("\\s+", "");
                print("Do you want to use: " + suggestedUsername + "? (y/n)");

                do {
                    answer = guiApp.getUserInput();
                } while (answer == null);

                while (!(answer.equals("y") || answer.equals("n"))) {
                    print("Answer with 'y' or 'n'!");

                    do {
                        answer = guiApp.getUserInput();
                    } while (answer == null);
                }

                if (answer.equals("y")) {
                    username = suggestedUsername;
                } else {
                    print("Enter username:");

                    do {
                        answer = guiApp.getUserInput();
                    } while (answer == null);
                    String usernameInput = answer.trim();

                    while (usernameInput.contains(" ") || usernameInput.isEmpty()) {
                        print("Whitespaces or an empty Username are not allowed!");
                        print("Enter new username:");
                        do {
                            usernameInput = guiApp.getUserInput();
                        } while (usernameInput == null);
                    }

                    username = usernameInput;
                }
            }

            print("Choose a password:");
            do {
                answer = guiApp.getUserInput();
            } while (answer == null);

            answer = answer.trim();

            while (answer.contains(" ") || answer.length() < 6) {
                print("Choose a different password!");

                do {
                    answer = guiApp.getUserInput();
                } while (answer == null);

                answer = answer.trim();
            }

            clientMessageQueue.add("signUp " + serverID + " " + username + " " + answer);
        } else {
            if (!useUnParam) {
                print("Enter your username:");
                do {
                    answer = guiApp.getUserInput();
                } while (answer == null);

                answer = answer.trim();

                while (answer.contains(" ") || answer.isEmpty()) {
                    print("Whitespaces or an empty username are not allowed!");
                    print("Enter your username:");
                    do {
                        answer = guiApp.getUserInput();
                    } while (answer == null);

                    answer = answer.trim();
                }

                username = answer;
            }

            print("Enter your password:");
            do {
                answer = guiApp.getUserInput();
            } while (answer == null);

            answer = answer.trim();

            while (answer.contains(" ") || answer.isEmpty()) {
                print("Whitespaces or an empty password are not allowed!");
                print("Enter your password:");
                do {
                    answer = guiApp.getUserInput();
                } while (answer == null);

                answer = answer.trim();
            }

            clientMessageQueue.add("logIn " + serverID + " " + username + " " + answer);
        }

        sendMessage();
        while (!loggedIn) {
            receiveMessage();
            handleServerMessage();
        }
        LOGGER.info("Logged in");
    }

    private void setChatGui() {
        while (this.guiApp == null) {
            this.guiApp = GUIApp.getGuiApp();
        }
    }

    /**
     * This method encodes a buffered image into a base 64 string and sends it to the server.
     *
     * @param image The image to be encoded
     */
    public void sendImage(BufferedImage image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            byte[] imageBytes = stream.toByteArray();

            String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
            clientMessageQueue.add("image " + serverID + " " + lobbyName + " " + encodedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method decodes a base 64 String into a buffered image.
     *
     * @param encodedImage The base 64 String of the encoded image
     * @return Returns the decoded image as a buffered image
     */
    public BufferedImage decodeImage(String encodedImage) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
            ByteArrayInputStream stream = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method connects the client to the server, handles the login/ sign up process and the continuously
     * handles the communication between users and the server.
     *
     * @param args Either server port or client ip port username depending on what class you want to start
     */
    public static void main(String[] args) {
        Configurator.setRootLevel(org.apache.logging.log4j.Level.INFO);

        if (args[0].equals("server")) {
            String[] args2 = Arrays.copyOfRange(args, 1, args.length);
            Server.main(args2);
        } else {
            String[] args2;

            if (args[0].equals("client")) {
                args2 = Arrays.copyOfRange(args, 1, args.length);
            } else {
                args2 = args;
            }

            Main main = new Main();
            main.setUsername(args2[2]);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> Application.launch(GUIApp.class, args));

            while (GUIApp.getGuiApp() == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            GUIApp.showHomeView();
            /*

            //bsp code
            GUIApp.addPlayer("Bob", 60);
            GUIApp.addPlayer("Andrew", 100);
            GUIApp.addPlayer("Peter", 44);
            GUIApp.setTheme("Test");

             */


            main.setChatGui();
            main.initClient(args2[0], Integer.parseInt(args2[1]));
            main.signInLogIn();

            MessageListener msgListener = new MessageListener(System.in);
            Thread.startVirtualThread(msgListener);

            while (true) {
                main.handlePingPong();
                main.handleClientMessage();
                main.receiveMessage();
                main.sendMessage();
                main.handleServerMessage();
            }
        }
    }
}