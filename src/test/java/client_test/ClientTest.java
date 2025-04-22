package client_test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import client.Client;
import message.MessageListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
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
     *
     * @throws Exception if reflection fails
     */
    @Test
    void testSetServerID() throws Exception {
        client.setServerID(77);
        int id = getPrivateInt("serverID");
        assertEquals(77, id);
    }

    /**
     * Tests if a message can be correctly queued into the client's message queue.
     */
    @Test
    void testQueueClientMessage() {
        client.queueClientMessage("Hello Server");
        Queue<String> queue = getQueue("clientMessageQueue");
        assertEquals("Hello Server", queue.poll());
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
        getQueue("serverMessageQueue").add("Welcome Client");
        assertEquals("Welcome Client", client.getServerMessage());
    }

    /**
     * Tests if the sendMessage method correctly sends all queued client messages.
     *
     * @throws Exception if reflection fails
     */
    @Test
    void testSendMessageSendsAllMessages() throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outStream, true);

        setPrivateField("pWriter", writer);
        client.queueClientMessage("msg1");
        client.queueClientMessage("msg2");

        client.sendMessage();

        String output = outStream.toString();
        assertTrue(output.contains("msg1"));
        assertTrue(output.contains("msg2"));
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

    // ---------- HELPER METHODS ------------

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
            super(null); // InputStream unused
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
     * Uses reflection to retrieve a queue field from the {@link Client}.
     *
     * @param name the name of the queue field
     * @return the {@link Queue} object
     */
    @SuppressWarnings("unchecked")
    private Queue<String> getQueue(String name) {
        try {
            Field field = Client.class.getDeclaredField(name);
            field.setAccessible(true);
            return (Queue<String>) field.get(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uses reflection to set a private field in the {@link Client}.
     *
     * @param name  the name of the field
     * @param value the value to assign
     * @throws Exception if reflection access fails
     */
    private void setPrivateField(String name, Object value) throws Exception {
        Field field = Client.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }

    /**
     * Uses reflection to get a private integer field from the {@link Client}.
     *
     * @param name the name of the field
     * @return the integer value of the field
     * @throws Exception if reflection access fails
     */
    private int getPrivateInt(String name) throws Exception {
        Field field = Client.class.getDeclaredField(name);
        field.setAccessible(true);
        return (int) field.get(client);
    }
}
