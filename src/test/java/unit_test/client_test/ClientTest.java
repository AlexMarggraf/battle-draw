package unit_test.client_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import client.Client;
import message.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Queue;

/**
 * Unit tests for the {@link Client} class.
 * These tests validate core functionalities such as message sending/receiving,
 * connection handling, and internal queue management using reflection to access private members.
 */
class ClientTest {

    private Client client;

    /**
     * Sets up a new {@link Client} instance before each test.
     */
    @BeforeEach
    void setUp() {
        client = new Client("localhost", 8989);
    }

    /**
     * Tests if the client correctly marks itself as disconnected.
     */
    @Test
    void testDisconnect() {
        client.disconnect();
        assertTrue(client.disconnect, "Client should be marked as disconnected");
    }

    /**
     * Tests whether the server ID can be set via a setter method.
     */
    @Test
    void testSetServerID() {
        client.setServerID(77);
        assertEquals(77, client.getServerID());
    }

    /**
     * Tests if a message can be correctly queued into the client's message queue.
     */
    @Test
    void testQueueClientMessage() {
        client.queueClientMessage("Hello Server");
        Queue<String> queue = client.getClientMessageQueue();
        assertEquals("Hello Server", queue.poll());
    }

    /**
     * Tests that queuing and sending an empty string results in a single newline being sent
     * through the client's PrintWriter. This verifies that empty messages are not filtered
     * and are still transmitted properly as a newline.
     */
    @Test
    void testQueueEmptyMessageAndSend() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outStream, true);
        client.setPrintWriter(writer);

        client.queueClientMessage("");
        assertEquals("", client.getClientMessageQueue().peek(), "Message queue should contain the empty string");

        client.sendMessage();

        String output = outStream.toString();
        assertEquals(System.lineSeparator(), output, "Empty message should result in just a newline");
    }

    /**
     * Tests if getting a server message returns null when the queue is empty.
     */
    @Test
    void testGetServerMessageWhenEmpty() {
        assertNull(client.getServerMessage());
    }

    /**
     * Tests if getting a server message retrieves the message from the queue when it's not empty.
     */
    @Test
    void testGetServerMessageWhenNotEmpty() {
        client.getServerMessageQueue().add("Welcome Client");
        assertEquals("Welcome Client", client.getServerMessage());
    }

    /**
     * Tests if the sendMessage method correctly sends all queued client messages.
     */
    @Test
    void testSendMessageSendsAllMessages() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outStream, true);

        client.setPrintWriter(writer);
        client.queueClientMessage("msg1");
        client.queueClientMessage("msg2");

        client.sendMessage();

        String output = outStream.toString();
        assertTrue(output.contains("msg1"));
        assertTrue(output.contains("msg2"));
    }

    /**
     * Stub class that simulates the {@link MessageListener} for controlled message delivery.
     */
    private static class StubMessageListener extends MessageListener {
        private final String[] messages;
        private int index = 0;

        /**
         * Constructs a stub with predefined messages.
         *
         * @param messages variable number of messages to simulate
         */
        public StubMessageListener(String... messages) {
            super(null);
            this.messages = messages;
        }

        /**
         * Returns the next message from the predefined list or null if none remain.
         */
        @Override
        public String getMessage() {
            return index < messages.length ? messages[index++] : null;
        }
    }

    /**
     * Tests the receiveMessage method for handling regular server messages.
     */
    @Test
    void testReceiveMessageHandlesRegularMessages() {
        StubMessageListener stub = new StubMessageListener("hi there", null);
        client.receiveMessage(stub);

        assertEquals("hi there", client.getServerMessage());
    }

    /**
     * Tests if the receiveMessage method rewrites disconnect messages to include the client server ID.
     */
    @Test
    void testReceiveMessageHandlesDisconnectMessage() {
        client.setServerID(10);
        StubMessageListener stub = new StubMessageListener("disconnect -1", null);
        client.receiveMessage(stub);

        assertEquals("disconnect 10", client.getServerMessage());
    }

    /**
     * Tests that the {@link Client#receiveMessage(MessageListener)} method throws a
     * {@link NullPointerException} when passed a null MessageListener.
     * This verifies that the method does not silently accept invalid input and fails fast.
     */
    @Test
    void testReceiveMessageWithNullListener() {
        assertThrows(NullPointerException.class, () -> {
            client.receiveMessage(null);
        }, "Passing null MessageListener should throw NullPointerException");
    }

}
