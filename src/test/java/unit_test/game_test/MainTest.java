package unit_test.game_test;

import game.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.util.LinkedList;
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
        DummyClient dummyClient = new DummyClient();
        main.setClient(dummyClient);
    }

    /**
     * Dummy implementation of {@link client.Client} for testing message sending behavior
     * without requiring real network communication.
     */
    static class DummyClient extends client.Client {
        Queue<String> sentMessages = new LinkedList<>();
        public boolean disconnectCalled = false;
        public int receivedServerID = -1;
        private final Queue<String> fakeMessages = new LinkedList<>();

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

        @Override
        public void setServerID(int id) {
            this.receivedServerID = id;
        }

        public void addServerMessage(String msg) {
            fakeMessages.add(msg);
        }

        @Override
        public String getServerMessage() {
            return fakeMessages.poll(); // returns null when empty
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

        assertTrue(main.getReceivedPong());
    }

    @Test
    void testHandleServerMessage_setsServerID() {
        main.getServerMessageQueue().add("id 42");
        main.handleServerMessage();

        assertEquals(42, main.getServerID());

        DummyClient dummyClient = (DummyClient) main.getClient();
        assertEquals(42, dummyClient.receivedServerID);
    }

    @Test
    void testHandleServerMessage_sendsPongResponse() {
        main.getServerMessageQueue().add("ping 1");
        main.handleServerMessage();

        String response = main.getClientMessageQueue().poll();
        assertEquals("pong 1", response);
    }

    @Test
    void testReceiveMessage_addsAllMessagesToQueue() {
        DummyClient dummyClient = new DummyClient();

        dummyClient.addServerMessage("msg1");
        dummyClient.addServerMessage("msg2");

        setPrivateField(main, "client", dummyClient);

        main.receiveMessage();

        Queue<String> serverQueue = main.getServerMessageQueue();
        assertEquals("msg1", serverQueue.poll());
        assertEquals("msg2", serverQueue.poll());
        assertTrue(serverQueue.isEmpty());
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

    @Test
    void testSendImage_addsEncodedImageFromFileToQueue() throws Exception {
        DummyClient dummyClient = new DummyClient();

        setPrivateField(main, "client", dummyClient);
        setPrivateField(main, "serverID", 123);
        setPrivateField(main, "lobbyName", "TestLobby");

        String imagePath = "src/main/resources/test-image.png";
        BufferedImage image = ImageIO.read(new File(imagePath));
        assertNotNull(image, "Image file should be loaded");

        main.sendImage(image);

        Queue<String> clientQueue = main.getClientMessageQueue();
        assertFalse(clientQueue.isEmpty());
        String queuedMessage = clientQueue.poll();

        assertTrue(queuedMessage.startsWith("image 123 TestLobby "));

        String[] parts = queuedMessage.split(" ", 4);
        assertEquals("image", parts[0]);
        assertEquals("123", parts[1]);
        assertEquals("TestLobby", parts[2]);

        byte[] imageBytes = Base64.getDecoder().decode(parts[3]);
        BufferedImage decodedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        assertNotNull(decodedImage, "Decoded image should not be null");
        assertEquals(image.getWidth(), decodedImage.getWidth());
        assertEquals(image.getHeight(), decodedImage.getHeight());
    }



    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
