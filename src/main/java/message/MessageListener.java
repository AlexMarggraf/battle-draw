package message;

/**
 * This class listen continuously for any incoming messages.
 */

import java.io.*;
import java.util.*;
import game.Main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageListener implements Runnable {

    public volatile InputStream iStream;
    public volatile Queue<String> messageQueue;

    /**
     * This method is the constructor and sets the input Stream and the queue for storing messages.
     *
     * @param iStream The input stream from the connection
     */
    public MessageListener(InputStream iStream) {
        this.iStream = iStream;
        this.messageQueue = new LinkedList<>();
    }

    private static final Logger LOGGER = LogManager.getLogger(MessageListener.class);

    /**
     * This method gets and removes the first element from the message queue.
     *
     * @return Returns the first message of the queue
     */
    public String getMessage() {
        return messageQueue.poll();
    }

    /**
     * This method runs the thread which continuously reads the input stream and adds any incoming
     * messages to the queue.
     */
    public void run() {
        BufferedReader MessageReader = new BufferedReader(new InputStreamReader(iStream));
        String message = " ";

        try {
            while (true) {
                message = MessageReader.readLine();
                if (message != null && !message.isEmpty()) {
                    LOGGER.info("heard message {}", message);
                    //System.out.println(message);
                    messageQueue.add(message);
                }
            }
        } catch (IOException ioException) {
            messageQueue.add("disconnect -1");
        }
    }
}