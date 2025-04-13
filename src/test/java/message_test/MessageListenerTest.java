package message_test;

import message.MessageListener;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class MessageListenerTest {

    @Test
    void testConstructorInitializesStreamAndQueue() {
        InputStream dummyStream = new ByteArrayInputStream("".getBytes());
        MessageListener listener = new MessageListener(dummyStream);

        assertSame(dummyStream, listener.iStream);
        assertNotNull(listener.messageQueue);
        assertTrue(listener.messageQueue.isEmpty());
    }

    @Test
    void testGetMessageReturnsMessage() {
        MessageListener listener = new MessageListener(new ByteArrayInputStream("".getBytes()));
        listener.messageQueue.add("hello");
        listener.messageQueue.add("world");

        assertEquals("hello", listener.getMessage());
        assertEquals("world", listener.getMessage());
        assertNull(listener.getMessage());
    }

    @Test
    void testRunAddsMessagesToQueue() throws InterruptedException {
        String simulatedInput = "First message\nSecond message\n\nThird message";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(simulatedInput.getBytes());

        MessageListener listener = new MessageListener(inputStream);
        Thread thread = new Thread(listener);
        thread.start();

        // Let it run a little bit
        Thread.sleep(100);

        Queue<String> q = listener.messageQueue;
        assertTrue(q.contains("First message"));
        assertTrue(q.contains("Second message"));
        assertTrue(q.contains("Third message"));

        thread.interrupt();
    }

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

        Thread.sleep(100);

        assertEquals("disconnect -1", listener.getMessage());
        thread.interrupt(); // cleanup
    }
}
