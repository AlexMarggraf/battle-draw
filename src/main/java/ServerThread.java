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
    protected volatile Queue<String> serverMessageQueue;
    protected volatile Queue<String> clientMessageQueue;
    public Boolean disconnect;
    public long lastPing;
    public Boolean receivedPong;

    /**
     * This method is the constructor and sets the id of the client, the socket, queues for messages
     * and the disconnect flag.
     *
     * @param id     The identification number for any clients
     * @param socket The socket used for ipc.
     */
    public ServerThread(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
        this.serverMessageQueue = new LinkedList<>();
        this.clientMessageQueue = new LinkedList<>();
        this.disconnect = false;
        this.receivedPong = true;
        this.lastPing = 0;
    }

    /**
     * This method provides identification to other objects.
     *
     * @return Returns the id of the client
     */
    public int getId() {
        return id;
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
     * This method gives access to the messages sent to the server.
     *
     * @return Returns any incoming message that hasn't been dealt with
     */
    public String getClientMessage() {
        return clientMessageQueue.poll();
    }

    /**
     * This method sends any message in the serverMessageQueue to the client.
     *
     * @param oStream The output stream to the client
     */
    public void sendMessage(OutputStream oStream) {
        while (serverMessageQueue.peek() != null) {
            try {
                String serverMessage = serverMessageQueue.remove();
                oStream.write(serverMessage.getBytes());
                oStream.write('\n');
            } catch (
                    IOException ioException) {
                System.out.println("IOException in ServerThread in method sendMessage");
            }
        }
    }

    /**
     * This method checks whether any messages have been received and puts them in the clientMessageQueue if they have.
     *
     * @param msgListener The object that catches any incoming messages
     */
    public void receiveMessage(MessageListener msgListener) {
        String clientMessage = msgListener.getMessage();

        while (clientMessage != null) {

            Message message = new Message(clientMessage);

            switch (message.getType()) {
                case "whatID" -> {
                    Message response = new Message("id", new String[]{id + ""});
                    serverMessageQueue.add(response.getMsgString());
                }
                case "disconnect" -> {
                    disconnect = true;
                    Message response = new Message("disconnect", new String[]{id + ""});
                    clientMessageQueue.add(response.getMsgString());
                }
                case "ping" -> {
                    Message response = new Message("pong", new String[]{id + ""});
                    serverMessageQueue.add(response.getMsgString());
                }
                case "pong" -> receivedPong = true;
                default -> clientMessageQueue.add(clientMessage);
            }

            clientMessage = msgListener.getMessage();
        }
    }

    public void handlePingPong(){
        if((System.currentTimeMillis() - lastPing > 10000)) {
            if(receivedPong) {
                Message ping = new Message("ping", new String[]{id + ""});
                serverMessageQueue.add(ping.getMsgString());
                lastPing = System.currentTimeMillis();
                receivedPong = false;
            } else {
                System.out.println("Client not reachable");
                disconnect = true;
            }
        }
    }

    /**
     * This method is invoked if the thread is started. It creates input/output-stream and MessageListener.
     * Afterward it continuously handles any incoming or outgoing messages.
     */
    public void run() {

        try {
            InputStream iStream = socket.getInputStream();
            OutputStream oStream = socket.getOutputStream();

            MessageListener msgListener = new MessageListener(iStream);
            Thread.startVirtualThread(msgListener);

            while (!disconnect) {
                handlePingPong();
                receiveMessage(msgListener);
                sendMessage(oStream);
            }

            socket.close();
            iStream.close();
            oStream.close();
        } catch (
                IOException ioException) {
            System.out.println("IOException in ServerThread in method run");
        }
    }
}