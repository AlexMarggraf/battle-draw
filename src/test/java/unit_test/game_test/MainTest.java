package unit_test.game_test;

import game.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private Main main;

    @BeforeEach
    void setUp() {
        main = new Main();
    }

    @Test
    void testConstructorInitialState() {
        assertNotNull(main.getClientMessageQueue());
        assertTrue(main.getClientMessageQueue().isEmpty());
        assertFalse(main.isLoggedIn());
        assertFalse(main.isInLobby());
        assertEquals("chatAll", main.getMessageType());
        assertEquals(-1, main.getServerID());
        assertTrue(main.getSendPing());
    }

    @Test
    void testSetReceivedPongAndAffectsPingHandling() {
        main.setSendPing(true);
        main.setReceivedPong(true);
        main.lastPing = System.currentTimeMillis() - 11000;

        main.handlePingPong();

        String queued = main.getClientMessageQueue().poll();
        assertNotNull(queued);
        assertTrue(queued.startsWith("ping"));
    }

    @Test
    void testDecodeImageReturnsBufferedImage() throws Exception {
        BufferedImage original = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(original, "png", stream);

        String encoded = Base64.getEncoder().encodeToString(stream.toByteArray());

        BufferedImage decoded = main.decodeImage(encoded);
        assertNotNull(decoded);
        assertEquals(5, decoded.getWidth());
        assertEquals(5, decoded.getHeight());
    }

    @Test
    void testHandleServerMessage_setsReceivedPongTrue() {
        main.getServerMessageQueue().add("pong 0");
        main.handleServerMessage();

        assertTrue(main.getSendPing());
    }

    @Test
    void testSendMessageFlushesToClientQueue() {
        DummyClient dummy = new DummyClient();
        main.setClient(dummy);

        main.getClientMessageQueue().add("msg1");
        main.getClientMessageQueue().add("msg2");

        main.sendMessage();

        assertEquals("msg1", dummy.sentMessages.poll());
        assertEquals("msg2", dummy.sentMessages.poll());
        assertTrue(dummy.sentMessages.isEmpty());
    }

    static class DummyClient extends client.Client {
        Queue<String> sentMessages = new java.util.LinkedList<>();

        public DummyClient() {
            super("localhost", 1234);
        }

        @Override
        public void queueClientMessage(String msg) {
            sentMessages.add(msg);
        }

        @Override
        public void disconnect() {
            disconnectCalled = true;
        }

        public boolean disconnectCalled = false;
    }
}
