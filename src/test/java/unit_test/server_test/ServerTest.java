package unit_test.server_test;

import message.Message;
import game.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.DatabaseHelper;
import server.Server;
import server.ServerThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Server} class.
 * This test suite verifies server functionality including:
 * - Initialization
 * - Message routing between threads
 * - Queuing and dequeuing of messages
 * - Handling of send/receive message flows
 */
class ServerTest {

    private Server server;

    /**
     * Sets up a fresh {@link Server} instance before each test.
     */
    @BeforeEach
    void setUp() {
        server = new Server();
    }

    /**
     * Dummy server thread used to simulate {@link ServerThread} behavior
     * without relying on actual network sockets.
     */
    static class DummyServerThread extends ServerThread {
        public Queue<String> clientQueue = new java.util.LinkedList<>();
        public Queue<String> serverQueue = new java.util.LinkedList<>();
        private String username;

        /**
         * Constructs a dummy server thread with a mock ID.
         *
         * @param id the ID for the thread
         */
        public DummyServerThread(int id) {
            super(id, null);
            this.username = "User" + id;
        }

        /**
         * Simulates fetching a client message.
         *
         * @return the next message from the client queue
         */
        @Override
        public String getClientMessage() {
            return clientQueue.poll();
        }

        @Override
        public void setClientUsername(String username) {
            this.username = username;
        }

        /**
         * Simulates queuing a message to be sent to the client.
         *
         * @param message the message to queue
         */
        @Override
        public void queueServerMessage(String message) {
            serverQueue.add(message);
        }
    }

    /**
     * Uses reflection to access a private field from the {@link Server} class.
     *
     * @param server    the server instance
     * @param fieldName the name of the private field
     * @return the value of the field, cast to the appropriate type
     * @param <T> the expected return type
     */
    @SuppressWarnings("unchecked")
    private <T> T getField(Server server, String fieldName) {
        try {
            var field = Server.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(server);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies that the server constructor initializes required internal structures.
     */
    @Test
    void testConstructorInitializesFields() {
        assertNotNull(server);
    }

    /**
     * Tests that sending a message using {@link Server#sendMessage(String, int)}
     * adds it to the correct {@link ServerThread}'s server message queue.
     */
    @Test
    void testSendMessageAddsToCorrectServerThread() {
        DummyServerThread st = new DummyServerThread(-2);
        server.getServerThreadList().add(st);

        server.sendMessage("test-msg", -2);

        assertEquals("test-msg", st.serverQueue.poll());
    }

    /**
     * Tests that {@link Server#fillReceiveMessageQueue()} correctly transfers messages
     * from all server threads' client queues to the central receive message queue.
     */
    @Test
    void testFillReceiveMessageQueueRetrievesMessages() {
        DummyServerThread st = new DummyServerThread(-2);
        st.clientQueue.add("message1");
        st.clientQueue.add("message2");

        server.getServerThreadList().add(st);
        server.fillReceiveMessageQueue();

        Queue<String> receiveQueue = getField(server, "receiveMessageQueue");
        assertEquals("message1", receiveQueue.poll());
        assertEquals("message2", receiveQueue.poll());
    }

    /**
     * Tests that {@link Server#handleReceiveMessage()} correctly interprets a "chatAll" message
     * and adds it to the send queue.
     */
    @Test
    void testHandleReceiveMessageProcessesChatAll() {
        Message message = new Message("chatAll", new String[]{"-2", "user", "Hello!"});
        Queue<String> receiveQueue = getField(server, "receiveMessageQueue");
        receiveQueue.add(message.toString());

        server.handleReceiveMessage();

        Queue<String> sendQueue = getField(server, "sendMessageQueue");
        assertEquals(message.toString(), sendQueue.poll());
    }

    @Test
    void testHandleReceiveMessage_chatAll() {
        server.getReceiveMessageQueue().add("chatAll 0 User1 Hello everyone");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertEquals("chatAll 0 User1 Hello everyone", response);
    }

    @Test
    void testHandleReceiveMessage_chatLobby() {
        server.getReceiveMessageQueue().add("chatLobby 0 User1 Lobby1 Hi Lobby");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertEquals("chatLobby 0 User1 Lobby1 Hi Lobby", response);
    }

    @Test
    void testHandleReceiveMessage_chatTo_SelfMessage() {
        server.getReceiveMessageQueue().add("chatTo 0 User1 User1 Hey");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.contains("You can't whisper yourself"));
    }

    @Test
    void testHandleReceiveMessage_chatTo_UserNotOnline() {
        FakeDatabaseHelper.setupLogin("User2", "password", false);
        server.getReceiveMessageQueue().add("chatTo 0 User1 User2 Hello!");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.contains("isn't online or doesn't exist"));
    }

    @Test
    void testHandleReceiveMessage_chatAllMessageAddsToSendQueue() {
        server.getReceiveMessageQueue().add("chatAll 1 Alice Hello everyone");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertEquals("chatAll 1 Alice Hello everyone", response);
    }

    @Test
    void testHandleReceiveMessage_chatLobbyMessage() {
        server.getReceiveMessageQueue().add("chatLobby 2 Bob Hello Lobby");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertEquals("chatLobby 2 Bob Hello Lobby", response);
    }

    @Test
    void testHandleReceiveMessage_logInFailure() {
        Server server = new Server();
        FakeDatabaseHelper.setupLogin("Alice", "wrongpass", false);

        server.getReceiveMessageQueue().add("logIn 0 Alice wrongpass");
        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.startsWith("rejectLogin"));
        assertTrue(response.contains("Alice"));
    }

    @Test
    void testHandleReceiveMessage_signUp_NewUser() {
        server.getReceiveMessageQueue().add("signUp 0 User3 password");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.contains("acceptSignup"));
        assertTrue(response.contains("User3"));
    }

    @Test
    void testHandleReceiveMessage_whoOnline() {
        DatabaseHelper.insertPlayer("Alice", "password1");
        DatabaseHelper.insertPlayer("Bob", "password2");

        int clientId1 = DatabaseHelper.getPlayerId("Alice");
        int clientId2 = DatabaseHelper.getPlayerId("Bob");

        DatabaseHelper.setStatus(clientId1, 1);
        DatabaseHelper.setStatus(clientId2, 1);

        DummyServerThread client1 = new DummyServerThread(clientId1);
        DummyServerThread client2 = new DummyServerThread(clientId2);
        server.getServerThreadList().add(client1);
        server.getServerThreadList().add(client2);

        server.getReceiveMessageQueue().add("whoOnline 0");

        server.handleReceiveMessage();

        String response = server.getSendMessageQueue().poll();
        assertNotNull(response);
        assertTrue(response.startsWith("online 0"));
        assertTrue(response.contains("Alice"));
        assertTrue(response.contains("Bob"));

        DatabaseHelper.setStatus(clientId1, 0);
        DatabaseHelper.setStatus(clientId2, 0);
    }

    @Test
    void testHandleReceiveMessage_createAndJoinLobby() {
        String lobbyName = "Lobby_of_Alice";
        DatabaseHelper.insertPlayer("Alice", "password1");
        DatabaseHelper.insertPlayer("Bob", "password2");

        int clientId1 = DatabaseHelper.getPlayerId("Alice");
        int clientId2 = DatabaseHelper.getPlayerId("Bob");

        DatabaseHelper.setStatus(clientId1, 1);
        DatabaseHelper.setStatus(clientId2, 1);

        String createMessage = "createLobby " + clientId1;
        server.getReceiveMessageQueue().add(createMessage);
        server.handleReceiveMessage();

        String joinMessage = "joinLobby " + clientId2 + " " + lobbyName;
        server.getReceiveMessageQueue().add(joinMessage);
        server.handleReceiveMessage();

        Optional<Lobby> createdLobby = server.getLobbyList().stream()
                .filter(lobby -> lobby.getLobbyName().equals(lobbyName))
                .findFirst();
        assertTrue(createdLobby.isPresent(), "Lobby should be created and present in the lobby list");

        String response1 = server.getSendMessageQueue().poll();
        String response2 = server.getSendMessageQueue().poll();
        String response3 = server.getSendMessageQueue().poll();

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);

        assertTrue(response1.startsWith("lobbyJoined " + clientId1));
        assertTrue(response1.contains(lobbyName));

        assertTrue(response2.startsWith("playerJoined " + clientId1));
        assertTrue(response2.contains("Alice"));

        assertTrue(response3.startsWith("playerJoined " + clientId1));
        assertTrue(response3.contains("Bob"));
        assertTrue(response3.contains(String.valueOf(clientId2)));

        DatabaseHelper.setStatus(clientId1, 0);
        DatabaseHelper.setStatus(clientId2, 0);
    }





    /**
     * Tests that {@link Server#handleSendMessage()} sends the message to all other threads
     * except the original sender.
     */
    @Test
    void testHandleSendMessageSendsToCorrectThread() {
        DummyServerThread st1 = new DummyServerThread(1);
        DummyServerThread st2 = new DummyServerThread(2);
        server.getServerThreadList().add(st1);
        server.getServerThreadList().add(st2);

        Message message = new Message("chatAll", new String[]{"1", "Alice", "Hi!"});
        Queue<String> sendQueue = getField(server, "sendMessageQueue");
        sendQueue.add(message.toString());

        server.handleSendMessage();

        assertEquals(message.toString(), st2.serverQueue.poll());
        assertTrue(st1.serverQueue.isEmpty());
    }

    @Test
    void testHandleSendMessage_chatAll() {
        DummyServerThread sender = new DummyServerThread(1);
        DummyServerThread recipient = new DummyServerThread(2);
        server.getServerThreadList().add(sender);
        server.getServerThreadList().add(recipient);

        server.getSendMessageQueue().add("chatAll 1 Alice Hello Everyone");
        server.handleSendMessage();

        assertEquals("chatAll 1 Alice Hello Everyone", recipient.serverQueue.poll());
        assertNull(sender.serverQueue.poll());
    }

    @Test
    void testHandleSendMessage_chatLobby() {
        Lobby lobby = new Lobby("Lobby1");
        lobby.addPlayer(1);
        lobby.addPlayer(2);
        server.getLobbyList().add(lobby);

        DummyServerThread sender = new DummyServerThread(1);
        DummyServerThread recipient = new DummyServerThread(2);
        server.getServerThreadList().add(sender);
        server.getServerThreadList().add(recipient);

        server.getSendMessageQueue().add("chatLobby 1 Alice Lobby1 Hi Lobby");
        server.handleSendMessage();

        assertEquals("chatLobby 1 Alice Lobby1 Hi Lobby", recipient.serverQueue.poll());
        assertNull(sender.serverQueue.poll());
    }

    @Test
    void testHandleSendMessage_broadcast() {
        DummyServerThread client1 = new DummyServerThread(1);
        DummyServerThread client2 = new DummyServerThread(2);
        server.getServerThreadList().add(client1);
        server.getServerThreadList().add(client2);

        server.getSendMessageQueue().add("broadcast Hello Everyone");
        server.handleSendMessage();

        assertEquals("broadcast Hello Everyone", client1.serverQueue.poll());
        assertEquals("broadcast Hello Everyone", client2.serverQueue.poll());
    }

    @Test
    void testHandleSendMessage_chatTo() {
        DatabaseHelper.insertPlayer("Bob", "password");
        int bobId = DatabaseHelper.getPlayerId("Bob");

        DummyServerThread sender = new DummyServerThread(1);
        DummyServerThread recipient = new DummyServerThread(bobId);
        server.getServerThreadList().add(sender);
        server.getServerThreadList().add(recipient);

        server.getSendMessageQueue().add("chatTo 1 Alice Bob Hi Bob");
        server.handleSendMessage();

        assertEquals("chatTo 1 Alice Bob Hi Bob", recipient.serverQueue.poll());
        assertNull(sender.serverQueue.poll());
    }
}

class FakeDatabaseHelper {
    private static Map<String, String> users = new HashMap<>();
    private static Map<Integer, String> playerNames = new HashMap<>();
    private static Map<Integer, Boolean> playerStatus = new HashMap<>();

    static void setupLogin(String username, String password, boolean success) {
        if (success) {
            users.put(username, password);
        } else {
            users.put(username, "invalid");
        }
    }

    static void setPlayerName(int id, String name) {
        playerNames.put(id, name);
    }

    static void setStatus(int id, boolean status) {
        playerStatus.put(id, status);
    }

    static String getPlayerName(int id) {
        return playerNames.getOrDefault(id, "Unknown");
    }

    static boolean getStatus(int id) {
        return playerStatus.getOrDefault(id, false);
    }
}

