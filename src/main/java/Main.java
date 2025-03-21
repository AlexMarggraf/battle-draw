/**
 * This class serves as the main class that coordinates everything.
 * It connects the client to the server, starts the GUI and runs the
 * game.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

import java.util.*;

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
                    System.out.println("Server isn't reachable");
                    client.disconnect();
                }
                case "chatAll", "chatTo" -> System.out.println(message.getParams()[1] + ": " + message.getParams()[2]);
                case "acceptLogin", "acceptSignup" -> {
                    username = message.getParams()[1];
                    serverID = message.getID();
                    loggedIn = true;
                    System.out.println("You're logged in as " + username);
                }
                case "rejectLogin" -> {
                    System.out.println("Username or password is wrong");
                    signInLogIn();
                }
                default -> System.out.println(message.getID() + ": " + message.getParams()[1]);
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

    /**
     * This method retrieves all unhandled user input and adds it to the client message queue.
     *
     * @param msgListener The object that listens to the users inputs
     */
    public void handleClientMessage(MessageListener msgListener) {
        String clientMessage = msgListener.getMessage();

        while (clientMessage != null) {
            String[] splitClientMessage = clientMessage.split("\\s+");

            if (splitClientMessage.length == 2 && splitClientMessage[0].equals("/nick")) {
                username = splitClientMessage[1];
                Message message = new Message("changeUsername", new String[]{serverID + "", username});
                clientMessageQueue.add(message.toString());
            } else {
                Message message = new Message("chatAll", new String[]{serverID + "", username, clientMessage});
                clientMessageQueue.add(message.toString());
            }

            clientMessage = msgListener.getMessage();
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
     * This method starts up the client.
     *
     * @param ip   The ip address it should connect to
     * @param port The port it should connect to
     */
    public void startClient(String ip, int port) {
        this.client = new Client(ip, port);
        Thread clientThread = new Thread(client);
        clientThread.start();
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
                System.out.println("Server not reachable");
                sendPing = false;
                client.disconnect();
            }
        }
    }

    /**
     * This method starts the client and retrieves the ID of the server.
     */
    public void initClient() {
        startClient("localhost", 1234);
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
        Scanner scanner = new Scanner(System.in);

        System.out.println("Do you want to sign up or log in? (su/li)");
        String answer = scanner.nextLine();

        while (!(answer.equals("su") || answer.equals("li"))) {
            System.out.println("Answer with 'su ' or 'li'");
            answer = scanner.nextLine();
        }

        if (answer.equals("su")) {
            String suggestedUsername = System.getProperty("user.name") + (int) (Math.random() * 100);
            suggestedUsername = suggestedUsername.replaceAll("\\s+", "");
            System.out.println("Do you want to use: " + suggestedUsername + "? (y/n)");
            answer = scanner.nextLine();

            while (!(answer.equals("y") || answer.equals("n"))) {
                System.out.println("Answer with 'y' or 'n'!");
                answer = scanner.nextLine();
            }

            if (answer.equals("y")) {
                username = suggestedUsername;
            } else {
                System.out.println("Enter username:");
                String usernameInput = scanner.nextLine().trim();

                while (usernameInput.contains(" ") || usernameInput.isEmpty()) {
                    System.out.println("Whitespaces or an empty Username are not allowed!");
                    System.out.println("Enter new username:");
                    usernameInput = scanner.nextLine().trim();
                }

                username = usernameInput;
            }

            System.out.println("Choose a password:");
            String[] passwordArray = scanner.nextLine().split("\\s+");
            String password = passwordArray[0];

            while (passwordArray.length != 1 || passwordArray[0].isEmpty() || password.length() < 6) {
                System.out.println("Choose a different password!");
                passwordArray = scanner.nextLine().split("\\s+");
                password = passwordArray[0];
            }

            clientMessageQueue.add("signUp " + serverID + " " + username + " " + password);

        } else {
            System.out.println("Enter your username:");
            String[] usernameArray = scanner.nextLine().split("\\s+");

            while (usernameArray.length != 1 || usernameArray[0].isEmpty()) {
                System.out.println("Whitespaces or an empty username are not allowed!");
                System.out.println("Enter your username:");
                usernameArray = scanner.nextLine().split("\\s+");
            }

            username = usernameArray[0];

            System.out.println("Enter your password:");
            String[] passwordArray = scanner.nextLine().split("\\s+");


            while (passwordArray.length != 1 || passwordArray[0].isEmpty()) {
                System.out.println("Whitespaces or an empty password are not allowed!");
                System.out.println("Enter your password:");
                passwordArray = scanner.nextLine().split("\\s+");
            }

            String password = passwordArray[0];
            clientMessageQueue.add("logIn " + serverID + " " + username + " " + password);
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
        main.initClient();
        main.signInLogIn();

        MessageListener msgListener = new MessageListener(System.in);
        Thread.startVirtualThread(msgListener);

        while (true) {
            main.handlePingPong();
            main.handleClientMessage(msgListener);
            main.receiveMessage();
            main.sendMessage();
            main.handleServerMessage();
        }
    }
}