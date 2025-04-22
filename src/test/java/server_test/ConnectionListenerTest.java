package server_test;

import org.junit.jupiter.api.Test;
import server.ConnectionListener;

import java.net.Socket;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionListenerTest {

    @Test
    void testConstructorInitializesFields() {
        int testPort = 8989;
        ConnectionListener listener = new ConnectionListener(testPort);

        assertNotNull(listener.socketQueue, "Socket queue should be initialized");
        assertTrue(listener.socketQueue.isEmpty(), "Socket queue should be empty at start");
    }

    @Test
    void testQueueAcceptsSocketManually() {
        ConnectionListener listener = new ConnectionListener(8989);
        Socket dummySocket = new Socket();
        listener.socketQueue.add(dummySocket);

        Queue<Socket> queue = listener.socketQueue;
        assertFalse(queue.isEmpty());
        assertEquals(dummySocket, queue.poll());
    }

    /*
    integration test
    @Test
    void testRunAcceptsClientConnection() throws Exception {
        int port = 12346;
        ConnectionListener listener = new ConnectionListener(port);
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        // Simulate a client connecting
        Socket clientSocket = new Socket("localhost", port);

        Thread.sleep(100); // Let the listener accept it

        assertFalse(listener.socketQueue.isEmpty());
        Socket accepted = listener.socketQueue.poll();
        assertNotNull(accepted);

        clientSocket.close();
        accepted.close();
        listenerThread.interrupt();
    }

     */
}
