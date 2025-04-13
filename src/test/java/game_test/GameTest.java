package game_test;

import game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game("TestLobby");
    }

    @Test
    void testConstructorSetsDefaults() {
        assertEquals("open", game.getStatus());
        assertEquals("TestLobby", game.getLobbyName());
        assertEquals(0, game.getNumberOfPlayers());
        assertEquals(0, game.getNumberOfRoundsVoted());
        assertEquals(0, game.getRoundNumber());
        assertEquals(1, game.getMaxNumberOfRounds());
    }

    @Test
    void testSetAndGetStatus() {
        game.setStatus("inProgress");
        assertEquals("inProgress", game.getStatus());
    }

    @Test
    void testGetLobbyName() {
        assertEquals("TestLobby", game.getLobbyName());
    }

    @Test
    void testPlusOnePlayerIncrements() {
        game.plusOnePlayer();
        assertEquals(1, game.getNumberOfPlayers());

        game.plusOnePlayer();
        assertEquals(2, game.getNumberOfPlayers());
    }

    @Test
    void testMinusOnePlayerDecrements() {
        game.plusOnePlayer();
        game.plusOnePlayer();
        game.minusOnePlayer();
        assertEquals(1, game.getNumberOfPlayers());
    }

    @Test
    void testGetNumberOfPlayers() {
        assertEquals(0, game.getNumberOfPlayers());
        game.plusOnePlayer();
        assertEquals(1, game.getNumberOfPlayers());
    }

    @Test
    void testPlusOneVotedRoundIncrements() {
        game.plusOneVotedRound();
        assertEquals(1, game.getNumberOfRoundsVoted());
    }

    @Test
    void testGetNumberOfRoundsVoted() {
        assertEquals(0, game.getNumberOfRoundsVoted());
        game.plusOneVotedRound();
        assertEquals(1, game.getNumberOfRoundsVoted());
    }

    @Test
    void testResetNumberOfRoundsVoted() {
        game.plusOneVotedRound();
        game.plusOneVotedRound();
        game.resetNumberOfRoundsVoted();
        assertEquals(0, game.getNumberOfRoundsVoted());
    }

    @Test
    void testNextRoundIncrementsRoundNumber() {
        game.nextRound();
        assertEquals(1, game.getRoundNumber());

        game.nextRound();
        assertEquals(2, game.getRoundNumber());
    }

    @Test
    void testMaxNumberOfRoundsDefault() {
        assertEquals(1, game.getMaxNumberOfRounds());
    }
}

