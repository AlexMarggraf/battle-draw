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

class ServerThreadTest {

    private ServerThread serverThread;

    @BeforeEach
    void setUp() {
        serverThread = new ServerThread(1, new DummySocket());
    }

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

    @Test
    void testIdSetterGetter() {
        serverThread.setId(42);
        assertEquals(42, serverThread.getId());
    }

    @Test
    void testSetClientUsername() {
        serverThread.setClientUsername("user123");
    }

    @Test
    void testQueueServerMessageAndGetClientMessage() {
        serverThread.queueServerMessage("msg1");
        serverThread.queueServerMessage("msg2");

        assertEquals("msg1", serverThread.getServerMessageQueue().poll());
        assertEquals("msg2", serverThread.getServerMessageQueue().poll());

        serverThread.getClientMessageQueue().add("client-msg");
        assertEquals("client-msg", serverThread.getClientMessage());
    }

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

    @Test
    void testHandlePingPongSendsPing() {
        serverThread.lastPing = System.currentTimeMillis() - 11000;
        serverThread.receivedPong = true;

        serverThread.handlePingPong();

        assertFalse(serverThread.receivedPong);
        String msg = serverThread.getServerMessageQueue().poll();
        assertTrue(msg.startsWith("ping"));
    }

    @Test
    void testHandlePingPongDisconnectsIfNoPong() {
        serverThread.lastPing = System.currentTimeMillis() - 11000;
        serverThread.receivedPong = false;

        serverThread.handlePingPong();

        assertTrue(serverThread.disconnect);
        assertTrue(serverThread.getClientMessageQueue().peek().startsWith("disconnect"));
    }

    @Test
    void testReceiveMessageProcessesPing() {
        DummyListener listener = new DummyListener("ping 1");

        serverThread.receiveMessage(listener);

        String response = serverThread.getServerMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.startsWith("pong"));
    }

    // --- Dummy classes ---

    static class DummySocket extends Socket {
    }

    static class DummyListener extends MessageListener {
        private final Queue<String> messages = new java.util.LinkedList<>();

        public DummyListener(String... initialMessages) {
            super(System.in);
            messages.addAll(Arrays.asList(initialMessages));
        }

        @Override
        public String getMessage() {
            return messages.poll();
        }
    }
}
    //receiveMessage
    //run();
    //get set method in serverThread

