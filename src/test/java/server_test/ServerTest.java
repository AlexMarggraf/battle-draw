package server_test;

import message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import server.ServerThread;

import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server();
    }

    @Test
    void testConstructorInitializesFields() {
        assertNotNull(server);
    }

    @Test
    void testSendMessageAddsToCorrectServerThread() {
        DummyServerThread st = new DummyServerThread(-2);
        server.getServerThreadList().add(st);

        server.sendMessage("test-msg", -2);

        assertEquals("test-msg", st.serverQueue.poll());
    }

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

    @Test
    void testHandleReceiveMessageProcessesChatAll() {
        // Simulate a /chatAll message
        Message message = new Message("chatAll", new String[]{"-2", "user", "Hello!"});
        Queue<String> receiveQueue = getField(server, "receiveMessageQueue");
        receiveQueue.add(message.toString());

        server.handleReceiveMessage();

        Queue<String> sendQueue = getField(server, "sendMessageQueue");
        assertEquals(message.toString(), sendQueue.poll());
    }

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

        // st2 should receive the message, st1 is sender
        assertEquals(message.toString(), st2.serverQueue.poll());
        assertTrue(st1.serverQueue.isEmpty());
    }

    // ----------------- Helper Classes ------------------

    static class DummyServerThread extends ServerThread {
        public Queue<String> clientQueue = new java.util.LinkedList<>();
        public Queue<String> serverQueue = new java.util.LinkedList<>();

        public DummyServerThread(int id) {
            super(id, null); // no socket needed
        }

        @Override
        public String getClientMessage() {
            return clientQueue.poll();
        }

        @Override
        public void queueServerMessage(String message) {
            serverQueue.add(message);
        }
    }

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
/*handleReceiveMessage()	⚠️ Partially	Can simulate with test messages + stub DatabaseHelper
handleSendMessage()	⚠️ Partially	Same — fakes or stubs needed
startServerThread()	❌	Depends on real socket queue and threads
main()	❌	Integration only */

