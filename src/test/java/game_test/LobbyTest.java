package game_test;

import game.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

    private Lobby lobby;

    @BeforeEach
    void setUp() {
        lobby = new Lobby("TestLobby");
    }

    @Test
    void testConstructorAndLobbyName() {
        assertEquals("TestLobby", lobby.getLobbyName());
        assertTrue(lobby.isEmpty());
        assertEquals(0, lobby.getHowManyPlayersAreReady());
    }

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

    @Test
    void testVotingQueueAndCurrentVoteTarget() {
        lobby.addPlayer(1);
        lobby.addPlayer(2);
        lobby.addPlayer(3);
        lobby.resetPlayersToVoteFor();

        lobby.setPlayerVotingOn();
        Integer current = lobby.getPlayerVotingOn();
        assertTrue(Arrays.asList(1, 2, 3).contains(current));

        lobby.setPlayerVotingOn(); // Next vote
        assertNotEquals(current, lobby.getPlayerVotingOn());
    }

    @Test
    void testVotesOfARoundAccumulationAndReset() {
        lobby.addToVotesOfARound(2);
        lobby.addToVotesOfARound(3);
        assertEquals(5.0, getPrivateVotesOfARound());

        lobby.resetVotesOfARound();
        assertEquals(0.0, getPrivateVotesOfARound());
    }

    @Test
    void testLeaderboardCalculation() {
        lobby.addPlayer(1);
        lobby.addPlayer(2);
        lobby.addPlayer(3);

        // Replace DatabaseHelper behavior for testing:
        // Assume all ids resolve to "Player1", "Player2", etc.
        lobby.initialiseLeaderboard();
        assertEquals(0.0, lobby.getPointsOfAPlayer("Player1"));

        // Simulate 2 votes and 1 round complete
        lobby.addToVotesOfARound(3);
        lobby.addToVotesOfARound(2);
        lobby.addPointsToLeaderboard("Player1", 0); // First round

        double expectedMean = 2.5; // (5 votes / 2 voters)
        assertEquals(expectedMean, lobby.getPointsOfAPlayer("Player1"));
    }

    @Test
    void testImageStorageAndRetrieval() {
        lobby.addPlayer(99);
        lobby.resetPlayersToVoteFor();
        lobby.setPlayerVotingOn();

        Integer current = lobby.getPlayerVotingOn();
        lobby.setImageOfPlayer(current, "base64image");
        assertEquals("base64image", lobby.getImageOfPlayer());
    }

    // ------------------ Helper ------------------

    // We can’t access votesOfARound directly since it’s private
    private double getPrivateVotesOfARound() {
        try {
            var field = Lobby.class.getDeclaredField("votesOfARound");
            field.setAccessible(true);
            return ((Integer) field.get(lobby)).doubleValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
