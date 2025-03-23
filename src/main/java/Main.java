/**
 * This class serves as the main class that coordinates everything.
 * It connects the client to the server, starts the GUI and runs the
 * game.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;

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
    public ChatGui chatGui;

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
    }

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
                case "chatAll" -> print(message.getParams()[1] + ": " + message.getParams()[2]);
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
                    for (int i = 1; i < message.getParams().length; i++){
                        print(message.getParams()[i]);
                    }
                }
                default -> print(message.getID() + ": " + message.getParams()[1]);
            }
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
        chatGui.queuePrintMessage(message);
    }

    /**
     * This method retrieves all unhandled user input and adds it to the client message queue.
     */
    public void handleClientMessage() {
        String clientMessage = chatGui.getUserInput();

        while (clientMessage != null) {
            String[] splitClientMessage = clientMessage.split("\\s+");

            switch (splitClientMessage[0]) {
                case "/nick" -> {
                    Message message;
                    if (splitClientMessage.length == 2) {
                        String username = splitClientMessage[1];
                        message = new Message("changeUsername", new String[]{serverID + "", username});
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
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
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                    }
                    clientMessageQueue.add(message.toString());
                }
                case "/online" -> {
                    Message message;
                    if (splitClientMessage.length == 1) {
                        message = new Message("whoOnline", new String[]{serverID + ""});
                    } else {
                        message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                    }
                    clientMessageQueue.add(message.toString());
                }
                default -> {
                    Message message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                    clientMessageQueue.add(message.toString());
                }
            }

            clientMessage = chatGui.getUserInput();
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
    public void handlePingPong(){
        if((System.currentTimeMillis() - lastPing > 10000) && sendPing) {
            if(receivedPong) {
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

        while(serverID == -1) {
            receiveMessage();
            handleServerMessage();
        }
    }

    /**
     * This method handles the Sign-up and Log in processes.
     */
    public void signInLogIn() {
        Boolean useUnParam = true;
        String answer;


        print("Do you want to use " + username + " as your username? (y/n)");
        do {
            answer = chatGui.getUserInput();
        } while (answer == null);

        while (!(answer.equals("y") || answer.equals("n"))) {
            print("Answer with 'y' or 'n'!");

            do {
                answer = chatGui.getUserInput();
            } while (answer == null);
        }

        if(answer.equals("n")) {
            useUnParam = false;
        }

        print("Do you want to sign up or log in? (su/li)");

        while (answer == null) {
            answer = chatGui.getUserInput();
        }

        while (!(answer.equals("su") || answer.equals("li"))) {
            print("Answer with 'su ' or 'li'");
            do {
                answer = chatGui.getUserInput();
            } while (answer == null);
        }

        if (answer.equals("su")) {
            if (!useUnParam) {
                String suggestedUsername = System.getProperty("user.name") + (int) (Math.random() * 100);
                suggestedUsername = suggestedUsername.replaceAll("\\s+", "");
                print("Do you want to use: " + suggestedUsername + "? (y/n)");

                do
                {
                    answer = chatGui.getUserInput();
                } while (answer == null);

                while (!(answer.equals("y") || answer.equals("n"))) {
                    print("Answer with 'y' or 'n'!");

                    do
                    {
                        answer = chatGui.getUserInput();
                    } while (answer == null);
                }

                if (answer.equals("y")) {
                    username = suggestedUsername;
                } else {
                    print("Enter username:");

                    do
                    {
                        answer = chatGui.getUserInput();
                    } while (answer == null);
                    String usernameInput = answer.trim();

                    while (usernameInput.contains(" ") || usernameInput.isEmpty()) {
                        print("Whitespaces or an empty Username are not allowed!");
                        print("Enter new username:");
                        do
                        {
                            usernameInput = chatGui.getUserInput();
                        } while (usernameInput == null);
                    }

                    username = usernameInput;
                }
            }

            print("Choose a password:");
            do {
                answer = chatGui.getUserInput();
            } while (answer == null);

            answer = answer.trim();

            while (answer.contains(" ") || answer.length() < 6) {
                print("Choose a different password!");

                do {
                    answer = chatGui.getUserInput();
                } while (answer == null);

                answer = answer.trim();
            }

            clientMessageQueue.add("signUp " + serverID + " " + username + " " + answer);

        } else {
            if  (!useUnParam) {
                print("Enter your username:");
                do
                {
                    answer = chatGui.getUserInput();
                } while (answer == null);

                answer = answer.trim();

                while (answer.contains(" ") || answer.isEmpty()) {
                    print("Whitespaces or an empty username are not allowed!");
                    print("Enter your username:");
                    do
                    {
                        answer = chatGui.getUserInput();
                    } while (answer == null);

                    answer = answer.trim();
                }

                username = answer;
            }

            print("Enter your password:");
            do {
                answer = chatGui.getUserInput();
            } while (answer == null);

            answer = answer.trim();


            while (answer.contains(" ") || answer.isEmpty()) {
                print("Whitespaces or an empty password are not allowed!");
                print("Enter your password:");
                do {
                    answer = chatGui.getUserInput();
                } while (answer == null);

                answer = answer.trim();
            }

            clientMessageQueue.add("logIn " + serverID + " " + username + " " + answer);
        }

        sendMessage();
        while(!loggedIn) {
            receiveMessage();
            handleServerMessage();
        }
    }

    /**
     * This method connects the client to the server, handles the login/ sign up process and the continuously
     * handles the communication between users and the server.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.setUsername(args[2]);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> Application.launch(ChatGui.class, args));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        main.chatGui = ChatGui.getChatGui();

        main.initClient(args[0], Integer.parseInt(args[1]));
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