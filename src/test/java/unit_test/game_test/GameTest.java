package unit_test.game_test;

import game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Game} class.
 * These tests verify the core game logic including player count,
 * game round management, lobby status, and default initialization values.
 */
class GameTest {

    private Game game;

    /**
     * Initializes a new {@link Game} instance before each test.
     */
    @BeforeEach
    void setUp() {
        game = new Game("TestLobby");
    }

    /**
     * Tests whether the constructor initializes the game with default values.
     */
    @Test
    void testConstructorSetsDefaults() {
        assertEquals("open", game.getStatus());
        assertEquals("TestLobby", game.getLobbyName());
        assertEquals(0, game.getNumberOfPlayers());
        assertEquals(0, game.getNumberOfRoundsVoted());
    }

    /**
     * Tests the ability to set and retrieve the game status.
     */
    @Test
    void testSetAndGetStatus() {
        game.setStatus("inProgress");
        assertEquals("inProgress", game.getStatus());
    }

    /**
     * Tests that the lobby name is correctly stored and retrieved.
     */
    @Test
    void testGetLobbyName() {
        assertEquals("TestLobby", game.getLobbyName());
    }

    /**
     * Tests that the number of players increments correctly when a player joins.
     */
    @Test
    void testPlusOnePlayerIncrements() {
        game.plusOnePlayer();
        assertEquals(1, game.getNumberOfPlayers());

        game.plusOnePlayer();
        assertEquals(2, game.getNumberOfPlayers());
    }

    /**
     * Tests that the number of players decrements correctly when a player leaves.
     */
    @Test
    void testMinusOnePlayerDecrements() {
        game.plusOnePlayer();
        game.plusOnePlayer();
        game.minusOnePlayer();
        assertEquals(1, game.getNumberOfPlayers());
    }

    /**
     * Tests retrieval of the current number of players in the game.
     */
    @Test
    void testGetNumberOfPlayers() {
        assertEquals(0, game.getNumberOfPlayers());
        game.plusOnePlayer();
        assertEquals(1, game.getNumberOfPlayers());
    }

    /**
     * Tests that the number of voted rounds increments properly.
     */
    @Test
    void testPlusOneVotedRoundIncrements() {
        game.plusOneVotedRound();
        assertEquals(1, game.getNumberOfRoundsVoted());
    }

    /**
     * Tests retrieval of the current number of voted rounds.
     */
    @Test
    void testGetNumberOfRoundsVoted() {
        assertEquals(0, game.getNumberOfRoundsVoted());
        game.plusOneVotedRound();
        assertEquals(1, game.getNumberOfRoundsVoted());
    }

    /**
     * Tests that the number of voted rounds can be reset to zero.
     */
    @Test
    void testResetNumberOfRoundsVoted() {
        game.plusOneVotedRound();
        game.plusOneVotedRound();
        game.resetNumberOfRoundsVoted();
        assertEquals(0, game.getNumberOfRoundsVoted());
    }

    /**
     * Tests that the next round method correctly increments the round number after resetting.
     */
    @Test
    public void testNextRound() {
        game.resetRoundNumber();
        game.nextRound();
        assertEquals(1, game.getRoundNumber());
    }

    /**
     * Tests that the timer can be set to a specific value.
     */
    @Test
    public void testSetTimer() {
        game.setTimer(30);
        assertEquals(30, game.getTimer());
    }

    /**
     * Tests that the lobby name can be updated to a new value.
     */
    @Test
    public void testSetLobbyName() {
        game.setLobbyName("NewLobby");
        assertEquals("NewLobby", game.getLobbyName());
    }
}
