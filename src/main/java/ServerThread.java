/**
 * This class acts as the server thread. It connects to the client and sends and receives messages.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread implements Runnable {

    private int id;
    private Socket socket;
    private volatile Queue<String> serverMessageQueue;
    private volatile Queue<String> clientMessageQueue;
    public Boolean disconnect;

    /**
     * This method is the constructor and sets the id of the client, the socket, queues for messages
     * and the disconnect flag.
     *
     * @param id The identification number for any clients
     * @param socket The socket used for ipc.
     */
    public ServerThread(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
        this.serverMessageQueue = new LinkedList<>();
        this.clientMessageQueue = new LinkedList<>();
        this.disconnect = false;
    }

    /**
     * This method adds a message into the clientMessageQueue which will be handled by the server later.
     *
     * @param message The message that is supposed to handled
     */
    public void queueClientMessage(String message) {
        clientMessageQueue.add(message);
    }

    /**
     * This method adds a message into the ServerMessageQueue which will be sent to the client later.
     *
     * @param message the message that supposed to send
     */
    public void queueServerMessage(String message) {
        serverMessageQueue.add(message);
    }


    /**
     *  This method sends any message in the serverMessageQueue to the client.
     *
     * @param oStream The output stream to the client
     */
    public void sendMessage(OutputStream oStream) {
        while(serverMessageQueue.peek() != null) {
            try {
                String serverMessage = serverMessageQueue.remove();
                oStream.write(serverMessage.getBytes());
                oStream.write('\n');
            } catch(IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * This method checks whether any messages have been received and puts them in the clientMessageQueue if they have.
     *
     * @param msgListener The object that catches any incoming messages
     */
    public void receiveMessage(MessageListener msgListener) {
        String message = msgListener.getMessage();

        while(message != null) {
            clientMessageQueue.add(message);
            queueServerMessage("Ich habe deine Nachricht erhalten.");
            message = msgListener.getMessage();
        }
    }

    /**
     * This method properly handles any message in the clientMessageQueue
     */
    public void handleMessage() {
        while(clientMessageQueue.peek() != null) {
            System.out.println(clientMessageQueue.remove());
        }
    }

    /**
     * This method is invoked if the thread is started. It creates input/output-stream and MessageListener.
     * Afterward it continuously handles any incoming or outgoing messages.
     */
    public void run() {

        try{
            InputStream iStream = socket.getInputStream();
            OutputStream oStream = socket.getOutputStream();

            MessageListener msgListener = new MessageListener(iStream);
            Thread msgListenerThread = new Thread(msgListener);
            msgListenerThread.start();

            while(!disconnect) {

                receiveMessage(msgListener);
                sendMessage(oStream);
                handleMessage();

            }

            socket.close();
            iStream.close();
            oStream.close();

        } catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

}