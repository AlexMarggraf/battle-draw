package unit_test.server_test;

import message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import server.ServerThread;

import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Server} class.
 * This test suite verifies server functionality including:
 * - Initialization
 * - Message routing between threads
 * - Queuing and dequeuing of messages
 * - Handling of send/receive message flows
 */
class ServerTest {

    private Server server;

    /**
     * Sets up a fresh {@link Server} instance before each test.
     */
    @BeforeEach
    void setUp() {
        server = new Server();
    }

    /**
     * Verifies that the server constructor initializes required internal structures.
     */
    @Test
    void testConstructorInitializesFields() {
        assertNotNull(server);
    }

    /**
     * Tests that sending a message using {@link Server#sendMessage(String, int)}
     * adds it to the correct {@link ServerThread}'s server message queue.
     */
    @Test
    void testSendMessageAddsToCorrectServerThread() {
        DummyServerThread st = new DummyServerThread(-2);
        server.getServerThreadList().add(st);

        server.sendMessage("test-msg", -2);

        assertEquals("test-msg", st.serverQueue.poll());
    }

    /**
     * Tests that {@link Server#fillReceiveMessageQueue()} correctly transfers messages
     * from all server threads' client queues to the central receive message queue.
     */
    @Test
    void testFillReceiveMessageQueueRetrievesMessages() {
        DummyServerThread st = new DummyServerThread(-2);
        st.clientQueue.add("message1");
        st.clientQueue.add("message2");

        server.getServerThreadList().add(st);
        server.fillReceiveMessageQueue();

        Queue<String> receiveQueue = getField(server, "receiveMessageQueue");
        assertEquals("message1", receiveQueue.poll());
        assertEquals("message2", receiveQueue.poll());
    }

    /**
     * Tests that {@link Server#handleReceiveMessage()} correctly interprets a "chatAll" message
     * and adds it to the send queue.
     */
    @Test
    void testHandleReceiveMessageProcessesChatAll() {
        Message message = new Message("chatAll", new String[]{"-2", "user", "Hello!"});
        Queue<String> receiveQueue = getField(server, "receiveMessageQueue");
        receiveQueue.add(message.toString());

        server.handleReceiveMessage();

        Queue<String> sendQueue = getField(server, "sendMessageQueue");
        assertEquals(message.toString(), sendQueue.poll());
    }

    /**
     * Tests that {@link Server#handleSendMessage()} sends the message to all other threads
     * except the original sender.
     */
    @Test
    void testHandleSendMessageSendsToCorrectThread() {
        DummyServerThread st1 = new DummyServerThread(1);
        DummyServerThread st2 = new DummyServerThread(2);
        server.getServerThreadList().add(st1);
        server.getServerThreadList().add(st2);

        Message message = new Message("chatAll", new String[]{"1", "Alice", "Hi!"});
        Queue<String> sendQueue = getField(server, "sendMessageQueue");
        sendQueue.add(message.toString());

        server.handleSendMessage();

        assertEquals(message.toString(), st2.serverQueue.poll());
        assertTrue(st1.serverQueue.isEmpty());
    }

    // ----------------- Helper Classes ------------------

    /**
     * Dummy server thread used to simulate {@link ServerThread} behavior
     * without relying on actual network sockets.
     */
    static class DummyServerThread extends ServerThread {
        public Queue<String> clientQueue = new java.util.LinkedList<>();
        public Queue<String> serverQueue = new java.util.LinkedList<>();

        /**
         * Constructs a dummy server thread with a mock ID.
         *
         * @param id the ID for the thread
         */
        public DummyServerThread(int id) {
            super(id, null); // Socket not required
        }

        /**
         * Simulates fetching a client message.
         *
         * @return the next message from the client queue
         */
        @Override
        public String getClientMessage() {
            return clientQueue.poll();
        }

        /**
         * Simulates queuing a message to be sent to the client.
         *
         * @param message the message to queue
         */
        @Override
        public void queueServerMessage(String message) {
            serverQueue.add(message);
        }
    }

    /**
     * Uses reflection to access a private field from the {@link Server} class.
     *
     * @param server    the server instance
     * @param fieldName the name of the private field
     * @return the value of the field, cast to the appropriate type
     * @param <T> the expected return type
     */
    @SuppressWarnings("unchecked")
    private <T> T getField(Server server, String fieldName) {
        try {
            var field = Server.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(server);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
