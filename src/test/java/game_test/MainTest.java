package game_test;

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
    void testHandlePingPong_DisconnectsIfNoPong() {
        main.setSendPing(true);
        main.setReceivedPong(false);
        main.lastPing = System.currentTimeMillis() - 11000;

        DummyClient dummyClient = new DummyClient();
        main.getClient(dummyClient); // inject dummy
        main.handlePingPong();

        assertFalse(main.getSendPing());
        assertTrue(dummyClient.disconnectCalled);
    }

    @Test
    void testSetSendPingAndGetSendPing() {
        main.setSendPing(false);
        assertFalse(main.getSendPing());

        main.setSendPing(true);
        assertTrue(main.getSendPing());
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
    void testHandleServerMessage_setsUsernameFromAcceptLogin() {
        String serverMessage = "acceptLogin 42 testUser";
        main.getServerMessageQueue().add(serverMessage);
        main.handleServerMessage();

        assertEquals("testUser", main.getUsername());
    }


    @Test
    void testGetClientMessageQueue() {
        Queue<String> queue = main.getClientMessageQueue();
        assertNotNull(queue);
        queue.add("testMessage");
        assertEquals("testMessage", queue.poll());
    }

    @Test
    void testGetClientReturnsSameClient() {
        DummyClient dummy = new DummyClient();
        assertSame(dummy, main.getClient(dummy));
    }

    static class DummyClient extends client.Client {
        public boolean disconnectCalled = false;

        public DummyClient() {
            super("localhost", 1234);
        }

        @Override
        public void disconnect() {
            disconnectCalled = true;
        }
    }

}

class DummyGUIApp extends gui.GUIApp {
    @Override
    public void queuePrintMessage(String msg) {
    }
}
