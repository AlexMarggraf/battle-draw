package unit_test.server_test;

import org.junit.jupiter.api.Test;
import server.ConnectionListener;

import java.net.Socket;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ConnectionListener} class.
 * These tests verify that the connection listener initializes correctly
 * and that its socket queue behaves as expected.
 */
class ConnectionListenerTest {

    /**
     * Tests whether the constructor of {@link ConnectionListener}
     * properly initializes the socket queue.
     */
    @Test
    void testConstructorInitializesFields() {
        int testPort = 8989;
        ConnectionListener listener = new ConnectionListener(testPort);

        assertNotNull(listener.socketQueue, "Socket queue should be initialized");
        assertTrue(listener.socketQueue.isEmpty(), "Socket queue should be empty at start");
    }

    /**
     * Tests that sockets can be manually added to the queue,
     * and that the queue correctly stores and returns the socket.
     */
    @Test
    void testQueueAcceptsSocketManually() {
        ConnectionListener listener = new ConnectionListener(8989);
        Socket dummySocket = new Socket();
        listener.socketQueue.add(dummySocket);

        Queue<Socket> queue = listener.socketQueue;
        assertFalse(queue.isEmpty());
        assertEquals(dummySocket, queue.poll());
    }
}
