package client_test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import client.Client;

class ClientTest {

    @Test
    void testDisconnect() {
        Client client = new Client("localhost", 8989);
        client.disconnect();
        assertEquals(true, client.disconnect, "Client should disconnect");
    }
}