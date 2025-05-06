package game;

/**
 * The Game class represents the state and progression of a game session
 * within a lobby. It tracks the game's status, associated lobby, number
 * of players, the current round, and the number of rounds voted on.
 * It also maintains the maximum number of rounds allowed in a game.
 */

public class Game {
    private String status;
    private String lobbyName;
    private Integer numberOfPlayers;
    private Integer roundNumber;
    private Integer numberOfRoundsVoted;
    private Integer maxNumberOfRounds;
    private Integer timer;

    /**
     * This method is the constructor and sets status, lobbyName and
     * values for the game state.
     *
     * @param lobbyName The name of the lobby
     */
    public Game(String lobbyName) {
        this.status = "open";
        this.lobbyName = lobbyName;
        this.numberOfPlayers = 0;
        this.numberOfRoundsVoted = 0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {this.status = status;}

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String newLobbyName) {this.lobbyName = newLobbyName;}

    public void plusOnePlayer() {this.numberOfPlayers++;}

    public void minusOnePlayer() {this.numberOfPlayers--;}

    public void plusOneVotedRound() {this.numberOfRoundsVoted++;}

    public void nextRound() {this.roundNumber++;}

    public Integer getNumberOfPlayers() {return numberOfPlayers;}

    public Integer getNumberOfRoundsVoted() {return numberOfRoundsVoted;}

    public void resetNumberOfRoundsVoted() {this.numberOfRoundsVoted = 0;}

    public Integer getMaxNumberOfRounds() {return maxNumberOfRounds;}

    public void setMaxNumberOfRounds(Integer maxNumberOfRounds) {this.maxNumberOfRounds = maxNumberOfRounds;}

    public Integer getRoundNumber() {return roundNumber;}

    public void resetRoundNumber() {this.roundNumber = 0;}

    public void setTimer(int timer) {this.timer = timer;}

    public int getTimer() {return timer;}
}