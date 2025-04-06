/**
 * This class is the client which connects to the server and serves
 * as the communication point between users. Furthermore, all game updates
 * are sent via this class.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable {

    private final String serverAddress;
    private final int port;
    private volatile Queue<String> clientMessageQueue;
    private volatile Queue<String> serverMessageQueue;
    public volatile Boolean disconnect;
    private volatile int serverID;
    private volatile InputStream iStream;
    private volatile OutputStream oStream;
    private PrintWriter pWriter;

    /**
     * This method is the constructor and sets serverAddress, port number,
     * queues for messages and the disconnect flag.
     *
     * @param serverAddress The ip address of the server
     * @param port          The port number of the server
     */
    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        clientMessageQueue = new LinkedList<>();
        serverMessageQueue = new LinkedList<>();
        disconnect = false;
        serverID = -1;
    }

    /**
     * This method causes the client to disconnect.
     */
    public void disconnect() {
        this.disconnect = true;
    }

    /**
     * This method sets the server ID.
     *
     * @param id The value of the ID
     */
    public void setServerID(int id) {
        this.serverID = id;
    }

    /**
     * This method adds a message into the clientMessageQueue which will
     * be sent to the server later.
     *
     * @param message The message that is supposed to be sent
     */
    public void queueClientMessage(String message) {
        clientMessageQueue.add(message);
    }

    /**
     * This method gives access to all incoming messages.
     *
     * @return Returns all undealt messages from the server
     */
    public String getServerMessage() {
        return serverMessageQueue.poll();
    }

    /**
     * This method sends any message in the clientMessageQueue to the server.
     *
     */
    public void sendMessage() {
        while (clientMessageQueue.peek() != null) {
            try {
                String clientMessage = clientMessageQueue.remove();
                //oStream.write(clientMessage.getBytes());
                //oStream.write('\n');
                pWriter.println(clientMessage);
            } catch (Exception e) {
                    System.out.println("IOException in class Client in method sendMessage");
            }
        }
    }

    /**
     * This method checks whether any messages have been received and puts
     * them in the serverMessageQueue if they have.
     *
     * @param msgListener The object that catches any incoming messages
     */
    public void receiveMessage(MessageListener msgListener) {
        String message = msgListener.getMessage();

        while (message != null) {
            if (message.equals("disconnect -1")){
                Message response = new Message("disconnect " + serverID);
                serverMessageQueue.add(response.toString());
            } else {
                serverMessageQueue.add(message);
            }
            message = msgListener.getMessage();
        }
    }

    /**
     * This method is invoked if the thread is started. It creates a new socket,
     * input/output-stream and MessageListener. Afterward it continuously handles
     * any incoming or outgoing messages.
     */
    public void run() {
        try {
            Socket socket = new Socket(serverAddress, port);
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            pWriter = new PrintWriter(oStream, true);

            MessageListener msgListener = new MessageListener(iStream);
            Thread msgListenerThread = new Thread(msgListener);
            msgListenerThread.start();

            while (!disconnect) {
                receiveMessage(msgListener);
                sendMessage();
            }

            socket.close();
            iStream.close();
            oStream.close();
        } catch (
                IOException ioException) {
            System.out.println("IOException in class Client in method run");
        }
    }
}