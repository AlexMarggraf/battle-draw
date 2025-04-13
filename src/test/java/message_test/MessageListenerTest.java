package message_test;

import message.MessageListener;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link MessageListener} class.
 * This suite verifies the correct functionality of:
 * - Constructor initialization
 * - Message queuing
 * - Threaded input stream reading
 * - IOException handling
 */
class MessageListenerTest {

    /**
     * Tests that the constructor properly initializes the input stream and the message queue.
     */
    @Test
    void testConstructorInitializesStreamAndQueue() {
        InputStream dummyStream = new ByteArrayInputStream("".getBytes());
        MessageListener listener = new MessageListener(dummyStream);

        assertSame(dummyStream, listener.iStream, "InputStream should be initialized correctly");
        assertNotNull(listener.messageQueue, "Message queue should be initialized");
        assertTrue(listener.messageQueue.isEmpty(), "Message queue should initially be empty");
    }

    /**
     * Tests that messages added to the queue are returned in the correct FIFO order via {@code getMessage()},
     * and that {@code null} is returned when the queue is empty.
     */
    @Test
    void testGetMessageReturnsMessage() {
        MessageListener listener = new MessageListener(new ByteArrayInputStream("".getBytes()));
        listener.messageQueue.add("hello");
        listener.messageQueue.add("world");

        assertEquals("hello", listener.getMessage(), "First call should return 'hello'");
        assertEquals("world", listener.getMessage(), "Second call should return 'world'");
        assertNull(listener.getMessage(), "Queue should be empty and return null");
    }

    /**
     * Tests that the {@code run()} method correctly reads multiple lines from the input stream
     * and adds them to the message queue.
     *
     * @throws InterruptedException if thread sleep is interrupted
     */
    @Test
    void testRunAddsMessagesToQueue() throws InterruptedException {
        String simulatedInput = "First message\nSecond message\n\nThird message";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());

        MessageListener listener = new MessageListener(inputStream);
        Thread thread = new Thread(listener);
        thread.start();

        // Give time for the thread to read input
        Thread.sleep(100);

        Queue<String> q = listener.messageQueue;
        assertTrue(q.contains("First message"), "Queue should contain 'First message'");
        assertTrue(q.contains("Second message"), "Queue should contain 'Second message'");
        assertTrue(q.contains("Third message"), "Queue should contain 'Third message'");

        thread.interrupt(); // Stop thread to clean up
    }

    /**
     * Tests that if an {@link IOException} occurs during reading,
     * the {@code run()} method adds a disconnect message to the queue.
     *
     * @throws InterruptedException if thread sleep is interrupted
     */
    @Test
    void testRunAddsDisconnectOnIOException() throws InterruptedException {
        InputStream faultyStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated exception");
            }
        };

        MessageListener listener = new MessageListener(faultyStream);
        Thread thread = new Thread(listener);
        thread.start();

        Thread.sleep(100); // Allow the run() to trigger the exception

        assertEquals("disconnect -1", listener.getMessage(), "IOException should trigger 'disconnect -1' message");
        thread.interrupt(); // cleanup
    }
}
