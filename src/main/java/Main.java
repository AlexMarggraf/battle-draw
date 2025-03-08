/**
 * This class serves as the main class that coordinates everything.
 * It connects the client to the server, starts the GUI and runs the
 * game.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

import java.io.*;

public class Main {
    /**
     * This method connects the client to the server and parses the user
     * input.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {
        Client client = new Client("10.192.5.242", 1234);
        Thread clientThread = new Thread(client);
        clientThread.start();

        try {
            BufferedReader clientInputReader = new BufferedReader(new InputStreamReader(System.in));
            String clientMsg = " ";

            while (true) {

                clientMsg = clientInputReader.readLine();
                client.queueClientMessage(clientMsg);
            }

        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }
}