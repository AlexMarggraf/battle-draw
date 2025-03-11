/**
 * This class serves as the server for our game. It accepts any clients that want to connect to the server and initializes
 * the server threads.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

import java.util.*;

public class Server {

    private Queue<String> receiveMessageQueue;
    private Queue<String> sendMessageQueue;
    private ArrayList<ServerThread> serverThreadList;
    public int connection;

    /**
     * This method is the constructor and initializes the message queues and the server list and
     * it sets the id to 0.
     */
    public Server() {
        this.receiveMessageQueue = new LinkedList<>();
        this.sendMessageQueue = new LinkedList<>();
        this.serverThreadList = new ArrayList<>();
        this.connection = 0;
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
            connection++;
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
     * This method handles any message that is in the recieveMessageQueue.
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
                }
                case "chatAll" -> {
                    sendMessageQueue.add(message.toString());
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
                        if(st.getId() != message.getID()){
                            sendMessage(message.toString(), st.getId());
                        }
                    }
                }
                default -> {sendMessage(message.toString(), message.getID());}
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

        ConnectionListener conListener = new ConnectionListener(1234);
        Thread.startVirtualThread(conListener);

        while (true) {
            server.startServerThread(conListener);
            server.fillReceiveMessageQueue();
            server.handleReceiveMessage();
            server.handleSendMessage();
        }
    }
}