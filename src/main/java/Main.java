/**
 * This class serves as the main class that coordinates everything.
 * It connects the client to the server, starts the GUI and runs the
 * game.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

import java.io.*;
import java.util.*;

public class Main {

    private Queue<String> serverMessageQueue;
    private Queue<String> clientMessageQueue;
    private Client client;
    public long lastPing;
    private int serverID;

    /**
     * This method is the constructor and initializes the queues.
     */
    public Main() {
        this.serverMessageQueue = new LinkedList<>();
        this.clientMessageQueue = new LinkedList<>();
        this.lastPing = 0;
        this.serverID = -1;
    }

    /**
     * This method properly handles any message in the serverMessageQueue.
     */
    public void handleServerMessage() {
        while (serverMessageQueue.peek() != null) {
            String msgString = serverMessageQueue.remove();
            System.out.println(msgString);

            Message msg = new Message(msgString);

            if(msg.getType().equals("id")) {
                this.serverID = Integer.parseInt(msg.getID() + "");
            } else if(msg.getType().equals("ping")) {
                Message response = new Message("pong", new String[]{msg.getID() + ""});
                clientMessageQueue.add(response.getMsgString());
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
     * This method retreives all unhandled user input and adds it to the client message queue.
     *
     * @param msgListener The object that listens to the users inputs
     */
    public void handleClientMessage(MessageListener msgListener) {
        String clientMsg = msgListener.getMessage();

        while (clientMsg != null) {
            clientMessageQueue.add(clientMsg);
            clientMsg = msgListener.getMessage();
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
        this.client = new Client("localhost", 1234);
        Thread clientThread = new Thread(client);
        clientThread.start();
    }

    /**
     * This method connects the client to the server and parses the user
     * input.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {

        Main main = new Main();
        main.startClient("localhost", 1234);

        main.clientMessageQueue.add((new Message("whatID")).getMsgString());
        main.sendMessage();

        while(main.serverID == -1) {
            main.receiveMessage();
            main.handleServerMessage();
        }

        MessageListener msgListener = new MessageListener(System.in);
        Thread.startVirtualThread(msgListener);


        while (true) {

            if(System.currentTimeMillis() - main.lastPing > 10000) {
                Message ping = new Message("ping", new String[]{main.serverID + ""});
                main.clientMessageQueue.add(ping.getMsgString());
                main.lastPing = System.currentTimeMillis();
            }

            main.handleClientMessage(msgListener);
            main.receiveMessage();
            main.sendMessage();
            main.handleServerMessage();
        }
    }
}