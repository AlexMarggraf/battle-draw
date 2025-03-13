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
                case "disconnect" -> client.disconnect();
                case "chatAll" -> System.out.println(message.getID() + ": " + message.getParams()[1]);
                default -> {System.out.println(message.getID() + ": " + message.getParams()[1]);}
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
            Message message;

            /* If you want to be able to use commands from the client chat, use this
            try {
                message = new Message(clientMessage);
            } catch (IllegalArgumentException e){
                message = new Message("chatAll", new String[]{serverID + "", clientMessage});
            } */

            message = new Message("chatAll", new String[]{serverID + "", clientMessage});

            clientMessageQueue.add(message.toString());
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
        this.client = new Client("localhost", 1234);
        Thread clientThread = new Thread(client);
        clientThread.start();
    }

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
     * This method connects the client to the server and parses the user
     * input.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.initClient();

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