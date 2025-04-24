package unit_test.message_test;

import message.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit tests for the {@link Message} class.
 * This class tests both constructors of the Message class:
 * - One using a message type and parameter array
 * - One using a full message string*
 * It verifies the proper parsing, storage, and string reconstruction for all valid protocol messages.
 */
class MessageTest {

    // ------------------- FIRST CONSTRUCTOR TESTS: Message(String type, String[] params) -------------------

    /**
     * Tests the {@link Message#Message(String, String[])} constructor with various valid protocol messages.
     * Ensures that the type and parameters are correctly stored and the full message string is reconstructed accurately.
     *
     * @param type     the message type
     * @param params   the message parameters
     * @param expected the expected string representation of the message
     */
    @ParameterizedTest(name = "Test type-param constructor: {0}")
    @MethodSource("allValidMessageTypeParams")
    @DisplayName("Message(String, String[]) constructor – full protocol coverage")
    void testValidMessagesWithParams(String type, String[] params, String expected) {
        Message msg = new Message(type, params);
        assertEquals(type, msg.getType());
        assertArrayEquals(params, msg.getParams());
        assertEquals(expected, msg.toString());
    }

    /**
     * Provides a stream of valid message types and parameters used to test the type-param constructor.
     *
     * @return a stream of test arguments for valid messages
     */
    static Stream<org.junit.jupiter.params.provider.Arguments> allValidMessageTypeParams() {
        return Stream.of(
                arguments("chatAll", new String[]{"37", "Alex", "Willkommen bei BattleDraw"}, "chatAll 37 Alex Willkommen bei BattleDraw"),
                arguments("chatTo", new String[]{"37", "Alex", "Noah", "Hallo"}, "chatTo 37 Alex Noah Hallo"),
                arguments("chatLobby", new String[]{"42", "Simon", "Lobby_von_Simon", "druck auf ready"}, "chatLobby 42 Simon Lobby_von_Simon druck auf ready"),
                arguments("signUp", new String[]{"37", "Alex", "thisIsMyPassword"}, "signUp 37 Alex thisIsMyPassword"),
                arguments("acceptSignup", new String[]{"37", "NotAlex"}, "acceptSignup 37 NotAlex"),
                arguments("logIn", new String[]{"37", "Alex", "thisIsMyPassword"}, "logIn 37 Alex thisIsMyPassword"),
                arguments("acceptLogin", new String[]{"37", "NotAlex"}, "acceptLogin 37 NotAlex"),
                arguments("rejectLogin", new String[]{"37", "NotAlex"}, "rejectLogin 37 NotAlex"),
                arguments("username", new String[]{"37", "NotAlex"}, "username 37 NotAlex"),
                arguments("changeUsername", new String[]{"37", "NotAlex"}, "changeUsername 37 NotAlex"),
                arguments("ping", new String[]{"37"}, "ping 37"),
                arguments("pong", new String[]{"37"}, "pong 37"),
                arguments("disconnect", new String[]{"37"}, "disconnect 37"),
                arguments("id", new String[]{"37"}, "id 37"),
                arguments("whoOnline", new String[]{"37"}, "whoOnline 37"),
                arguments("whatGames", new String[]{"31"}, "whatGames 31"),
                arguments("createLobby", new String[]{"37"}, "createLobby 37"),
                arguments("joinLobby", new String[]{"21", "Lobby_von_Simon"}, "joinLobby 21 Lobby_von_Simon"),
                arguments("lobbyJoined", new String[]{"22", "Lobby_von_Simon"}, "lobbyJoined 22 Lobby_von_Simon"),
                arguments("playerJoined", new String[]{"23", "Drikson", "31"}, "playerJoined 23 Drikson 31"),
                arguments("listLobbyPlayers", new String[]{"41", "Lobby_von_Simon"}, "listLobbyPlayers 41 Lobby_von_Simon"),
                arguments("leaveLobby", new String[]{"37", "Lobby_von_Alex"}, "leaveLobby 37 Lobby_von_Alex"),
                arguments("lobbyLeft", new String[]{"37", "Dirkson"}, "lobbyLeft 37 Dirkson"),
                arguments("online", new String[]{"31", "Alex", "Frederic"}, "online 31 Alex Frederic"),
                arguments("lobbyPlayers", new String[]{"30", "Alex", "Simon", "Noah"}, "lobbyPlayers 30 Alex Simon Noah"),
                arguments("broadcast", new String[]{"User Frederic is online"}, "broadcast User Frederic is online"),
                arguments("games", new String[]{"41", "Lobby_von_Drikson-open", "Lobby_von_Simon-finish"}, "games 41 Lobby_von_Drikson-open Lobby_von_Simon-finish"),
                arguments("image", new String[]{"32", "Lobby_von_Simon", "iVBORw0K..."}, "image 32 Lobby_von_Simon iVBORw0K..."),
                arguments("votingStarts", new String[]{"27", "3"}, "votingStarts 27 3"),
                arguments("drawingStarts", new String[]{"37", "Sonne"}, "drawingStarts 37 Sonne"),
                arguments("sendVote", new String[]{"45", "Lobby_von_Simon", "3"}, "sendVote 45 Lobby_von_Simon 3"),
                arguments("updatePoints", new String[]{"45", "Frederic", "3.0"}, "updatePoints 45 Frederic 3.0"),
                arguments("showLeaderboard", new String[]{"37"}, "showLeaderboard 37"),
                arguments("showEnding", new String[]{"45"}, "showEnding 45"),
                arguments("startNextRound", new String[]{"27"}, "startNextRound 27"),
                arguments("startDrawing", new String[]{"45", "Lobby_von_Noah"}, "startDrawing 45 Lobby_von_Noah"),
                arguments("startVoting", new String[]{"27", "Lobby_von_Noah"}, "startVoting 27 Lobby_von_Noah"),
                arguments("readyToVote", new String[]{"27", "Lobby_von_Alex"}, "readyToVote 27 Lobby_von_Alex"),
                arguments("nextRound", new String[]{"42", "Lobby_von_Alex"}, "nextRound 42 Lobby_von_Alex"),
                arguments("whatID", new String[]{}, "whatID")
        );
    }

    // ------------------- SECOND CONSTRUCTOR TESTS: Message(String fullMessage) -------------------

    /**
     * Tests the {@link Message#Message(String)} constructor using full message strings.
     * Ensures the message is correctly parsed into its type and parameters and re-serialized accurately.
     *
     * @param fullMessage     the input string to parse
     * @param expectedType    the expected message type
     * @param expectedParams  the expected parameters array
     * @param expectedString  the expected string output from toString()
     */
    @ParameterizedTest(name = "Test full-message constructor: {0}")
    @MethodSource("allValidMessageStringInputs")
    @DisplayName("Message(String fullMessage) constructor – full protocol coverage")
    void testValidMessagesFromFullString(String fullMessage, String expectedType, String[] expectedParams, String expectedString) {
        Message msg = new Message(fullMessage);
        assertEquals(expectedType, msg.getType());
        assertArrayEquals(expectedParams, msg.getParams());
        assertEquals(expectedString, msg.toString());
    }

    /**
     * Provides a stream of valid full message strings and their expected components.
     *
     * @return a stream of test arguments for full message parsing
     */
    static Stream<org.junit.jupiter.params.provider.Arguments> allValidMessageStringInputs() {
        return Stream.of(
                arguments("chatAll 37 Alex Willkommen bei BattleDraw", "chatAll", new String[]{"37", "Alex", "Willkommen bei BattleDraw"}, "chatAll 37 Alex Willkommen bei BattleDraw"),
                arguments("chatTo 37 Alex Noah Hallo", "chatTo", new String[]{"37", "Alex", "Noah", "Hallo"}, "chatTo 37 Alex Noah Hallo"),
                arguments("chatLobby 42 Simon Lobby_von_Simon druck auf ready", "chatLobby", new String[]{"42", "Simon", "Lobby_von_Simon", "druck auf ready"}, "chatLobby 42 Simon Lobby_von_Simon druck auf ready"),
                arguments("whatID", "whatID", new String[]{}, "whatID"),
                arguments("broadcast User Frederic is online", "broadcast", new String[]{"User Frederic is online"}, "broadcast User Frederic is online"),
                arguments("online 31 Alex Frederic", "online", new String[]{"31", "Alex", "Frederic"}, "online 31 Alex Frederic"),
                arguments("signUp 37 Alex thisIsMyPassword", "signUp", new String[]{"37", "Alex", "thisIsMyPassword"}, "signUp 37 Alex thisIsMyPassword"),
                arguments("acceptSignup 37 NotAlex", "acceptSignup", new String[]{"37", "NotAlex"}, "acceptSignup 37 NotAlex"),
                arguments("logIn 37 Alex thisIsMyPassword", "logIn", new String[]{"37", "Alex", "thisIsMyPassword"}, "logIn 37 Alex thisIsMyPassword"),
                arguments("acceptLogin 37 NotAlex", "acceptLogin", new String[]{"37", "NotAlex"}, "acceptLogin 37 NotAlex"),
                arguments("rejectLogin 37 NotAlex", "rejectLogin", new String[]{"37", "NotAlex"}, "rejectLogin 37 NotAlex"),
                arguments("username 37 NotAlex", "username", new String[]{"37", "NotAlex"}, "username 37 NotAlex"),
                arguments("changeUsername 37 NotAlex", "changeUsername", new String[]{"37", "NotAlex"}, "changeUsername 37 NotAlex"),
                arguments("ping 37", "ping", new String[]{"37"}, "ping 37"),
                arguments("pong 37", "pong", new String[]{"37"}, "pong 37"),
                arguments("disconnect 37", "disconnect", new String[]{"37"}, "disconnect 37"),
                arguments("id 37", "id", new String[]{"37"}, "id 37"),
                arguments("createLobby 37", "createLobby", new String[]{"37"}, "createLobby 37"),
                arguments("joinLobby 21 Lobby_von_Simon", "joinLobby", new String[]{"21", "Lobby_von_Simon"}, "joinLobby 21 Lobby_von_Simon"),
                arguments("lobbyJoined 22 Lobby_von_Simon", "lobbyJoined", new String[]{"22", "Lobby_von_Simon"}, "lobbyJoined 22 Lobby_von_Simon"),
                arguments("playerJoined 23 Drikson 31", "playerJoined", new String[]{"23", "Drikson", "31"}, "playerJoined 23 Drikson 31"),
                arguments("listLobbyPlayers 41 Lobby_von_Simon", "listLobbyPlayers", new String[]{"41", "Lobby_von_Simon"}, "listLobbyPlayers 41 Lobby_von_Simon"),
                arguments("lobbyPlayers 30 Alex Simon Noah", "lobbyPlayers", new String[]{"30", "Alex", "Simon", "Noah"}, "lobbyPlayers 30 Alex Simon Noah"),
                arguments("leaveLobby 37 Lobby_von_Alex", "leaveLobby", new String[]{"37", "Lobby_von_Alex"}, "leaveLobby 37 Lobby_von_Alex"),
                arguments("lobbyLeft 37 Drikson", "lobbyLeft", new String[]{"37", "Drikson"}, "lobbyLeft 37 Drikson"),
                arguments("whoOnline 37", "whoOnline", new String[]{"37"}, "whoOnline 37"),
                arguments("online 31 Alex Frederic", "online", new String[]{"31", "Alex", "Frederic"}, "online 31 Alex Frederic"),
                arguments("whatGames 31", "whatGames", new String[]{"31"}, "whatGames 31"),
                arguments("games 41 Lobby_von_Drikson-open Lobby_von_Simon-finish", "games", new String[]{"41", "Lobby_von_Drikson-open", "Lobby_von_Simon-finish"}, "games 41 Lobby_von_Drikson-open Lobby_von_Simon-finish"),
                arguments("image 32 Lobby_von_Simon iVBORw0KGgoAAAANSUhEUgAA", "image", new String[]{"32", "Lobby_von_Simon", "iVBORw0KGgoAAAANSUhEUgAA"}, "image 32 Lobby_von_Simon iVBORw0KGgoAAAANSUhEUgAA"),
                arguments("votingStarts 27 3", "votingStarts", new String[]{"27", "3"}, "votingStarts 27 3"),
                arguments("drawingStarts 37 Sonne", "drawingStarts", new String[]{"37", "Sonne"}, "drawingStarts 37 Sonne"),
                arguments("sendVote 45 Lobby_von_Simon 3", "sendVote", new String[]{"45", "Lobby_von_Simon", "3"}, "sendVote 45 Lobby_von_Simon 3"),
                arguments("updatePoints 45 Frederic 3.0", "updatePoints", new String[]{"45", "Frederic", "3.0"}, "updatePoints 45 Frederic 3.0"),
                arguments("showLeaderboard 37", "showLeaderboard", new String[]{"37"}, "showLeaderboard 37"),
                arguments("showEnding 45", "showEnding", new String[]{"45"}, "showEnding 45"),
                arguments("startNextRound 27", "startNextRound", new String[]{"27"}, "startNextRound 27"),
                arguments("startDrawing 45 Lobby_von_Noah", "startDrawing", new String[]{"45", "Lobby_von_Noah"}, "startDrawing 45 Lobby_von_Noah"),
                arguments("startVoting 27 Lobby_von_Noah", "startVoting", new String[]{"27", "Lobby_von_Noah"}, "startVoting 27 Lobby_von_Noah"),
                arguments("readyToVote 27 Lobby_von_Alex", "readyToVote", new String[]{"27", "Lobby_von_Alex"}, "readyToVote 27 Lobby_von_Alex"),
                arguments("nextRound 42 Lobby_von_Alex", "nextRound", new String[]{"42", "Lobby_von_Alex"}, "nextRound 42 Lobby_von_Alex")
        );
    }
}
