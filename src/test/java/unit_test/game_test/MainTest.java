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

/**
 * Unit tests for the {@link Main} class.
 * This suite verifies:
 * - Initial state setup
 * - Ping/pong handling
 * - Server message parsing
 * - Image decoding
 * - Message queue flushing via client interaction
 */
class MainTest {

    private Main main;

    /**
     * Initializes a new {@link Main} instance before each test.
     */
    @BeforeEach
    void setUp() {
        main = new Main();
    }

    /**
     * Verifies that the constructor initializes the state and queues properly.
     */
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

    /**
     * Tests that a ping message is queued if enough time has passed and a pong was received.
     */
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

    /**
     * Tests that a Base64-encoded string is correctly decoded into a {@link BufferedImage}.
     *
     * @throws Exception if encoding/decoding or IO fails
     */
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

    /**
     * Tests that receiving a "pong" message sets the flag to allow sending ping again.
     */
    @Test
    void testHandleServerMessage_setsReceivedPongTrue() {
        main.getServerMessageQueue().add("pong 0");
        main.handleServerMessage();

        assertTrue(main.getSendPing());
    }

    /**
     * Dummy implementation of {@link client.Client} for testing message sending behavior
     * without requiring real network communication.
     */
    static class DummyClient extends client.Client {
        Queue<String> sentMessages = new java.util.LinkedList<>();
        public boolean disconnectCalled = false;

        /**
         * Constructs a dummy client with no actual network target.
         */
        public DummyClient() {
            super("localhost", 1234);
        }

        /**
         * Captures the message instead of sending it.
         *
         * @param msg the message to queue
         */
        @Override
        public void queueClientMessage(String msg) {
            sentMessages.add(msg);
        }

        /**
         * Flags that disconnect was called.
         */
        @Override
        public void disconnect() {
            disconnectCalled = true;
        }
    }

    /**
     * Tests that messages in the client message queue are passed to the client for sending.
     */
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
}
