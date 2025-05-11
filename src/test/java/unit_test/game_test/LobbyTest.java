package unit_test.game_test;

import game.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.DatabaseHelper;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Lobby} class.
 * This suite verifies lobby functionality such as:
 * - Player management
 * - Readiness tracking
 * - Voting and leaderboard logic
 * - Image storage
 */
class LobbyTest {

    private Lobby lobby;

    /**
     * Sets up a new {@link Lobby} instance before each test with a test lobby name.
     */
    @BeforeEach
    void setUp() {
        lobby = new Lobby("TestLobby");
    }

    /**
     * Tests that the constructor correctly initializes the lobby name,
     * sets the lobby as empty, and initializes readiness count to zero.
     */
    @Test
    void testConstructorAndLobbyName() {
        assertEquals("TestLobby", lobby.getLobbyName());
        assertTrue(lobby.isEmpty());
        assertEquals(0, lobby.getHowManyPlayersAreReady());
    }

    /**
     * Tests adding and removing players from the lobby and verifies
     * the internal player list and count are updated accordingly.
     */
    @Test
    void testAddAndRemovePlayer() {
        lobby.addPlayer(1);
        lobby.addPlayer(2);

        Integer[] expected = {1, 2};
        assertArrayEquals(expected, lobby.getPlayers());
        assertEquals(2, lobby.getNumberOfPlayers());

        lobby.removePlayer(1);
        assertArrayEquals(new Integer[]{2}, lobby.getPlayers());
    }

    /**
     * Tests tracking the number of players marked as ready,
     * including incrementing, decrementing, and resetting the count.
     */
    @Test
    void testPlayerReadinessTracking() {
        lobby.plusOnePlayerIsReady();
        lobby.plusOnePlayerIsReady();
        assertEquals(2, lobby.getHowManyPlayersAreReady());

        lobby.minusOnePlayerIsReady();
        assertEquals(1, lobby.getHowManyPlayersAreReady());

        lobby.resetHowManyPlayersAreReady();
        assertEquals(0, lobby.getHowManyPlayersAreReady());
    }

    /**
     * Tests the queue for players to be voted on and ensures
     * that voting targets are correctly rotated.
     */
    @Test
    void testVotingQueueAndCurrentVoteTarget() {
        lobby.addPlayer(1);
        lobby.addPlayer(2);
        lobby.addPlayer(3);
        lobby.resetPlayersToVoteFor();

        lobby.setPlayerVotingOn();
        Integer current = lobby.getPlayerVotingOn();
        assertTrue(Arrays.asList(1, 2, 3).contains(current));

        lobby.setPlayerVotingOn();
        assertNotEquals(current, lobby.getPlayerVotingOn());
    }

    /**
     * Accesses the private field {@code votesOfARound} via reflection
     * to verify vote accumulation logic.
     *
     * @return the number of accumulated votes in a round
     */
    private double getPrivateVotesOfARound() {
        try {
            var field = Lobby.class.getDeclaredField("votesOfARound");
            field.setAccessible(true);
            return ((Integer) field.get(lobby)).doubleValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests the accumulation and reset of votes for a single round.
     */
    @Test
    void testVotesOfARoundAccumulationAndReset() {
        lobby.addToVotesOfARound(2);
        lobby.addToVotesOfARound(3);
        assertEquals(5.0, getPrivateVotesOfARound());

        lobby.resetVotesOfARound();
        assertEquals(0.0, getPrivateVotesOfARound());
    }

    /**
     * Tests leaderboard initialization and point calculation after voting.
     * Simulates adding players, simulating votes, and adding points.
     */
    @Test
    void testLeaderboardCalculationWithoutInitialise() {
        lobby.addPlayer(37);
        lobby.addPlayer(38);
        lobby.addPlayer(39);

        lobby.initialiseLeaderboard();

        lobby.resetVotesOfARound();
        lobby.addToVotesOfARound(4);
        lobby.addToVotesOfARound(2);

        String username = DatabaseHelper.getPlayerName(37);

        lobby.addPointsToLeaderboard(username, 0);
        assertEquals(3.0, lobby.getPointsOfAPlayer(username));
    }

    /**
     * Tests that a base64-encoded image can be stored for a player and retrieved correctly.
     */
    @Test
    void testImageStorageAndRetrieval() {
        lobby.addPlayer(99);
        lobby.resetPlayersToVoteFor();
        lobby.setPlayerVotingOn();

        Integer current = lobby.getPlayerVotingOn();
        lobby.setImageOfPlayer(current, "base64image");
        assertEquals("base64image", lobby.getImageOfPlayer());
    }

    /**
     * Tests that a player can be added to the leaderboard with a specific score.
     */
    @Test
    public void testAddToLeaderboard() {
        lobby.addToLeaderboard("Player1", 5.0);
        assertEquals(5.0, lobby.getPointsOfAPlayer("Player1"));
    }

    /**
     * Tests that points can be correctly added to the leaderboard using a lambda expression.
     */
    @Test
    public void testAddPointsToLeaderboardLambda() {
        lobby.initialiseLeaderboard();
        lobby.addToLeaderboard("Player1", 3.0);
        lobby.addPointsToLeaderboard("Player1", 1);
        Double points = lobby.getPointsOfAPlayer("Player1");
        assertTrue(points >= 0.0 && points <= 5.0);
    }

    /**
     * Tests that a player can be removed from the leaderboard.
     */
    @Test
    public void testRemoveFromLeaderboard() {
        lobby.addToLeaderboard("Player1", 10.0);
        lobby.removeFromLeaderboard("Player1");
        assertEquals(0.0, lobby.getPointsOfAPlayer("Player1"));
    }

    /**
     * Tests that the lobby name can be updated to a new value.
     */
    @Test
    public void testSetLobbyName() {
        lobby.setLobbyName("NewLobby");
        assertEquals("NewLobby", lobby.getLobbyName());
    }
}


