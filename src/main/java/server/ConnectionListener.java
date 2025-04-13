package server;
/**
 * This class listens for any clients that want to connect to a specific port.
 *
 * @author Alexander Marggraf
 * @Author Frederic Weyssow
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionListener implements Runnable {

    private final int port;
    public volatile Queue<Socket> socketQueue;

    /**
     * This method is the constructor and sets the port number and initializes
     * the queue for the sockets.
     *
     * @param port The port number
     */
    public ConnectionListener(int port) {
        this.port = port;
        this.socketQueue = new LinkedList<>();
    }

    /**
     * This method listens for any connections on the given port number
     * and adds the socket to the socket queue when someone connects.
     */
    public void run() {

        try {
            ServerSocket listen = new ServerSocket(port);

            while (true) {
                Socket socket = listen.accept();
                System.out.println("New connection from " + socket.getInetAddress().getHostAddress());
                socketQueue.add(socket);
            }
        } catch (
                IOException ioException) {
            System.out.println("IOException in class ConnectionListener in method run");
        }
    }
}