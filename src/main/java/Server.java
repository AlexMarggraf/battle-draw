/**
 * This class serves as the server for our game. It accepts any clients that want to connect to the server and initializes
 * the server threads.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

import javax.xml.crypto.Data;
import java.util.*;

public class Server {

    private Queue<String> receiveMessageQueue;
    private Queue<String> sendMessageQueue;
    private ArrayList<ServerThread> serverThreadList;
    public int connection;
    private LinkedList<Lobby> lobbyList;

    /**
     * This method is the constructor and initializes the message queues and the server list and
     * it sets the id to 0.
     */
    public Server() {
        this.receiveMessageQueue = new LinkedList<>();
        this.sendMessageQueue = new LinkedList<>();
        this.serverThreadList = new ArrayList<>();
        this.connection = -2;
        this.lobbyList = new LinkedList<Lobby>();
    }

    public void addLobby(Lobby lobby) {
        this.lobbyList.add(lobby);
    }

    /**
     * This method starts a new server thread.
     *
     * @param conListener The object that listens to any incoming connections
     */
    public void startServerThread(ConnectionListener conListener) {
        while (conListener.socketQueue.peek() != null) {
            ServerThread server = new ServerThread(connection, conListener.socketQueue.remove());
            Thread serverThread = new Thread(server);
            serverThread.start();

            serverThreadList.add(server);
            connection--;
        }
    }

    /**
     * This method queues a message into the server thread which is
     * identified by the id provided.
     *
     * @param message The message that is supposed to be queued
     * @param id      The id of the server that the message is supposed to be queued in
     */
    public void sendMessage(String message, int id) {
        for (ServerThread st : serverThreadList) {
            if (st.getId() == id) {
                st.queueServerMessage(message);
            }
        }
    }

    /**
     * This method checks all server threats and retrieves any undealt messages.
     */
    public void fillReceiveMessageQueue() {
        for (ServerThread st : serverThreadList) {
            String clientMessage = st.getClientMessage();

            while (clientMessage != null) {
                receiveMessageQueue.add(clientMessage);
                clientMessage = st.getClientMessage();
            }
        }
    }

    /**
     * This method handles any message that is in the receiveMessageQueue.
     */
    public void handleReceiveMessage() {
        while (receiveMessageQueue.peek() != null) {
            String clientMessage = receiveMessageQueue.remove();
            System.out.println(clientMessage);

            Message message = new Message(clientMessage);

            switch(message.getType()) {
                case "disconnect" -> {
                    int id = message.getID();
                    serverThreadList.removeIf(st -> st.getId() == id);
                    DatabaseHelper.setStatus(id, 0);
                }
                case "chatAll" -> sendMessageQueue.add(message.toString());
                case "chatTo" -> {
                    String recipientUsername = message.getParams()[2];
                    if (message.getParams()[1].equals(message.getParams()[2])) {
                        Message response = new Message("chatTo", new String[]{0 + "", "Server", message.getParams()[1], "You can't whisper yourself"});
                        sendMessageQueue.add(response.toString());
                    } else if (!DatabaseHelper.checkUsername(message.getParams()[2]) && DatabaseHelper.getStatus(DatabaseHelper.getPlayerId(recipientUsername))) {
                        sendMessageQueue.add(message.toString());
                    } else {
                        Message response = new Message("chatTo", new String[]{0 + "", "Server", message.getParams()[1], "The user " + message.getParams()[2] + " isn't online or doesn't exist"});
                        sendMessageQueue.add(response.toString());
                    }
                }
                case "changeUsername" -> {
                    if (DatabaseHelper.checkUsername(message.getParams()[1])) {
                        DatabaseHelper.changeUsername(message.getParams()[1], Integer.parseInt(message.getParams()[0]));
                        Message response = new Message("username", new String[]{message.getID() + "", message.getParams()[1]});
                        sendMessageQueue.add(response.toString());
                    } else {
                        Message response = new Message("chatTo", new String[]{0 + "", "Server",  DatabaseHelper.getPlayerName(Integer.parseInt(message.getParams()[0])), "Username is already in use"});
                        sendMessageQueue.add(response.toString());
                    }
                }
                case "logIn" -> {
                    String username = message.getParams()[1];

                    if (!DatabaseHelper.checkCredentials(username, message.getParams()[2]) || DatabaseHelper.getStatus(DatabaseHelper.getPlayerId(username))) {
                        Message response = new Message("rejectLogin", new String[]{message.getID() + "", username});
                        sendMessageQueue.add(response.toString());
                    } else {
                        DatabaseHelper.setStatus(DatabaseHelper.getPlayerId(username), 1);
                        Message response = new Message("acceptLogin", new String[]{DatabaseHelper.getPlayerId(username) + "", username});
                        sendMessageQueue.add(response.toString());
                        connection++;

                        for (ServerThread st : serverThreadList) {
                            if (st.getId() == message.getID()) {
                                st.setId(DatabaseHelper.getPlayerId(username));
                                st.setClientUsername(username);
                            }
                        }
                    }
                }
                case "signUp" -> {
                    String username = message.getParams()[1];

                    if (!DatabaseHelper.checkUsername(username)) {
                        int counter = 1;
                        String originalUsername = username;

                        while (!DatabaseHelper.checkUsername(username)) {
                            username = originalUsername + String.format("%03d", counter);
                            counter++;
                        }
                    }

                    DatabaseHelper.insertPlayer(username, message.getParams()[2]);
                    DatabaseHelper.setStatus(DatabaseHelper.getPlayerId(username), 1);

                    for (ServerThread st : serverThreadList) {
                        if (st.getId() == message.getID()) {
                            st.setId(DatabaseHelper.getPlayerId(username));
                            st.setClientUsername(username);
                        }
                    }

                    Message response = new Message("acceptSignup", new String[]{DatabaseHelper.getPlayerId(username) + "", username});
                    sendMessageQueue.add(response.toString());
                    connection++;
                }
                case "whoOnline" -> {
                    List<String> onlineList = new ArrayList<>();
                    onlineList.add(message.getID() + "");
                    for (ServerThread st : serverThreadList) {
                        if (DatabaseHelper.getStatus(st.getId())) {
                            onlineList.add(DatabaseHelper.getPlayerName(st.getId()));
                        }
                    }
                    Message response = new Message("online", onlineList.toArray(new String[0]));
                    sendMessageQueue.add(response.toString());
                }
            }
        }
    }

    /**
     * This method queues all messages into the correct server.
     */
    public void handleSendMessage() {
        while (sendMessageQueue.peek() != null) {
            String serverMessage = sendMessageQueue.remove();
            Message message = new Message(serverMessage);

            switch (message.getType()) {
                case "chatAll" -> {
                    for (ServerThread st : serverThreadList) {
                        if (st.getId() != message.getID()) {
                            sendMessage(message.toString(), st.getId());
                        }
                    }
                }
                case "broadcast" -> {
                    for (ServerThread st : serverThreadList) {
                        sendMessage(message.toString(), st.getId());
                    }
                }
                case "chatTo" -> {
                    sendMessage(message.toString(), DatabaseHelper.getPlayerId(message.getParams()[2]));
                }
                default -> sendMessage(message.toString(), message.getID());
            }
        }
    }

    /**
     * This method starts a connection listener and handles any incoming and outgoing messages.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {
        Server server = new Server();
        DatabaseHelper.setOffline();

        ConnectionListener conListener = new ConnectionListener(Integer.parseInt(args[0]));
        Thread.startVirtualThread(conListener);

        while (true) {
            server.startServerThread(conListener);
            server.fillReceiveMessageQueue();
            server.handleReceiveMessage();
            server.handleSendMessage();
        }
    }
}