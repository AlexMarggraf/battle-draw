/**
 * This class serves as the server for our game. It accepts any clients that want to connect to the server and initializes
 * the server threads.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

import java.io.*;
import java.net.*;

public class Server {


    /**
     * This method listens for anny connections and starts a new server thread if someone
     * connects to the server.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {
        int connection = 0;

        try {
            ServerSocket listen = new ServerSocket(1234);

            while (true) {
                Socket socket = listen.accept();

                ServerThread server = new ServerThread(++connection, socket);
                Thread serverThread = new Thread(server);
                serverThread.start();
            }
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }
}