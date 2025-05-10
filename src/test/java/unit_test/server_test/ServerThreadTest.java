package unit_test.server_test;

import message.MessageListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ServerThread;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ServerThread} class.
 * These tests verify server thread functionality such as:
 * - Message queuing
 * - Ping/Pong handling
 * - PrintWriter communication
 * - Message listening and client-server interaction
 */
class ServerThreadTest {

    private ServerThread serverThread;

    /**
     * Initializes a {@link ServerThread} instance before each test with a dummy socket.
     */
    @BeforeEach
    void setUp() {
        serverThread = new ServerThread(1, new DummySocket());
    }

    /**
     * A dummy implementation of {@link Socket} used to bypass real networking in tests.
     */
    static class DummySocket extends Socket {
    }

    /**
     * A stub for {@link MessageListener} that simulates incoming messages using a predefined queue.
     */
    static class DummyListener extends MessageListener {
        private final Queue<String> messages = new java.util.LinkedList<>();

        /**
         * Constructs the dummy listener with predefined messages.
         *
         * @param initialMessages the messages to queue
         */
        public DummyListener(String... initialMessages) {
            super(System.in); // Stream not used
            messages.addAll(Arrays.asList(initialMessages));
        }

        /**
         * Returns the next message in the queue, or null if empty.
         *
         * @return a message string or null
         */
        @Override
        public String getMessage() {
            return messages.poll();
        }
    }

    /**
     * Tests that the constructor correctly initializes the thread fields,
     * including message queues and connection status flags.
     */
    @Test
    void testConstructorInitializesFields() {
        assertEquals(1, serverThread.getId());
        assertNotNull(serverThread.getServerMessageQueue());
        assertNotNull(serverThread.getClientMessageQueue());
        assertTrue(serverThread.getServerMessageQueue().isEmpty());
        assertTrue(serverThread.getClientMessageQueue().isEmpty());
        assertTrue(serverThread.receivedPong);
        assertFalse(serverThread.disconnect);
    }

    /**
     * Tests the ID setter and getter methods.
     */
    @Test
    void testIdSetterGetter() {
        serverThread.setId(42);
        assertEquals(42, serverThread.getId());
    }

    /**
     * Tests setting the client username.
     * (No assertion because it sets an internal field — test for exception-free behavior.)
     */
    @Test
    void testSetClientUsername() {
        serverThread.setClientUsername("user123");
    }

    /**
     * Tests adding server messages to the queue and retrieving a client message.
     */
    @Test
    void testQueueServerMessageAndGetClientMessage() {
        serverThread.queueServerMessage("msg1");
        serverThread.queueServerMessage("msg2");

        assertEquals("msg1", serverThread.getServerMessageQueue().poll());
        assertEquals("msg2", serverThread.getServerMessageQueue().poll());

        serverThread.getClientMessageQueue().add("client-msg");
        assertEquals("client-msg", serverThread.getClientMessage());
    }

    /**
     * Tests that the {@link ServerThread#sendMessage()} method correctly writes queued messages to the output stream.
     */
    @Test
    void testSendMessageWritesToWriter() {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer, true);
        serverThread.setPrintWriter(printWriter);

        serverThread.queueServerMessage("hello");
        serverThread.queueServerMessage("world");

        serverThread.sendMessage();

        String output = writer.toString().trim();
        assertTrue(output.contains("hello"));
        assertTrue(output.contains("world"));
    }

    /**
     * Tests that the ping/pong mechanism sends a ping when the last ping was too long ago.
     */
    @Test
    void testHandlePingPongSendsPing() {
        serverThread.lastPing = System.currentTimeMillis() - 11000;
        serverThread.receivedPong = true;

        serverThread.handlePingPong();

        assertFalse(serverThread.receivedPong);
        String msg = serverThread.getServerMessageQueue().poll();
        assertTrue(msg.startsWith("ping"));
    }

    /**
     * Tests that the thread disconnects the client if no pong was received in time.
     */
    @Test
    void testHandlePingPongDisconnectsIfNoPong() {
        serverThread.lastPing = System.currentTimeMillis() - 11000;
        serverThread.receivedPong = false;

        serverThread.handlePingPong();

        assertTrue(serverThread.disconnect);
        assertTrue(serverThread.getClientMessageQueue().peek().startsWith("disconnect"));
    }

    /**
     * Tests that receiving a "ping" message results in a "pong" message being queued to the server.
     */
    @Test
    void testReceiveMessageProcessesPing() {
        DummyListener listener = new DummyListener("ping 1");

        serverThread.receiveMessage(listener);

        String response = serverThread.getServerMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.startsWith("pong"));
    }

    @Test
    void testReceiveMessage_Disconnect() {
        DummyListener listener = new DummyListener("disconnect -1");
        serverThread.receiveMessage(listener);

        assertTrue(serverThread.disconnect, "The server thread should set the disconnect flag.");

        String response = serverThread.getClientMessageQueue().poll();
        assertNotNull(response, "There should be a disconnect message queued.");
        assertTrue(response.startsWith("disconnect 1"), "The disconnect message should contain the client ID.");
    }

    @Test
    void testReceiveMessage_WhatID() {
        DummyListener listener = new DummyListener("whatID");
        serverThread.receiveMessage(listener);

        String response = serverThread.getServerMessageQueue().poll();
        assertNotNull(response, "Expected an ID response message.");
        assertTrue(response.startsWith("id 1"), "The ID response should contain the correct client ID.");
    }

    @Test
    void testReceiveMessage_Pong() {
        serverThread.receivedPong = false;

        DummyListener listener = new DummyListener("pong 1");
        serverThread.receiveMessage(listener);

        assertTrue(serverThread.receivedPong, "Receiving a pong message should set the receivedPong flag to true.");
    }
}
