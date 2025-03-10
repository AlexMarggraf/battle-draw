/**
 * This class serves as the server for our game. It accepts any clients that want to connect to the server and initializes
 * the server threads.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

import java.io.*;
import java.net.*;
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
            serverThreadList.add(server);
            Thread serverThread = new Thread(server);
            serverThread.start();
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
            String message = st.getClientMessage();

            while (message != null) {
                receiveMessageQueue.add(message);
                message = st.getClientMessage();
            }
        }
    }

    /**
     * This method handles any message that is in the recieveMessageQueue.
     */
    public void handleReceiveMessage() {
        while (receiveMessageQueue.peek() != null) {
            String msgString = receiveMessageQueue.remove();
            System.out.println(msgString);

            Message msg = new Message(msgString);

            if(msg.getType().equals("ping")) {
                Message response = new Message("pong", new String[]{msg.getID() + ""});
                sendMessageQueue.add(response.getMsgString());
            }
        }
    }

    /**
     * This method queues all messages into the correct server.
     */
    public void handleSendMessage() {
        while (sendMessageQueue.peek() != null) {
            String message = sendMessageQueue.remove();
            Message msg = new Message(message);
            sendMessage(msg.getMsgString(), msg.getID());
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