package game;
/**
 * This class represents a game lobby where players can join, leave, and interact.
 * It manages the list of players, tracks readiness, voting mechanics, and maintains a leaderboard.
 */

import server.DatabaseHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Lobby {
    private String lobbyName;
    private ArrayList<Integer> players;
    private Integer howManyPlayersAreReady;
    private ArrayList<Integer> playersToVoteFor;
    private Integer playerVotingOn;
    private Integer votesOfARound;
    private Map<String, Double> leaderboard;
    private Map<Integer, String> images;
    private ArrayList<Integer> disconnectedPlayers;
    private Map<Integer, Double> disconnectedPlayersPoints;

    /**
     * Constructs a Lobby with the given name.
     *
     * @param lobbyName the name of the lobby
     */
    public Lobby(String lobbyName) {
        this.lobbyName = lobbyName;
        this.players = new ArrayList<>();
        this.howManyPlayersAreReady = 0;
        this.leaderboard = new HashMap<>();
        this.votesOfARound = 0;
        this.images = new HashMap<>();
        this.disconnectedPlayers = new ArrayList<>();
        this.disconnectedPlayersPoints = new HashMap<>();
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String newName) {this.lobbyName = newName;}

    public Integer[] getPlayers() {
        return players.toArray(new Integer[0]);
    }

    public void addPlayer(Integer playerId) {
        players.add(playerId);
    }

    public void removePlayer(Integer playerId) {
        players.remove(playerId);
    }

    public Integer getHowManyPlayersAreReady() {
        return howManyPlayersAreReady;
    }

    public void plusOnePlayerIsReady() {
        howManyPlayersAreReady++;
    }

    public void minusOnePlayerIsReady() {
        howManyPlayersAreReady--;
    }

    public void resetHowManyPlayersAreReady() {
        howManyPlayersAreReady = 0;
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    /**
     * Initializes the list of players to vote for in the current round.
     */
    public void resetPlayersToVoteFor() {
        playersToVoteFor = new ArrayList<>(players);
    }

    /**
     * Sets the current player to be voted on from the list.
     * Removes that player from the voting list.
     */
    public void setPlayerVotingOn() {
        if (!playersToVoteFor.isEmpty()) {
            playerVotingOn = playersToVoteFor.removeLast();
        }
    }

    public Integer getPlayerVotingOn() {
        return playerVotingOn;
    }

    /**
     * Initializes or resets the leaderboard, assigning all players 0.0 points.
     */
    public void initialiseLeaderboard() {
        leaderboard.clear();
        for (Integer playerId : players) {
            leaderboard.put(DatabaseHelper.getPlayerName(playerId), 0.0);
        }
    }

    /**
     * Adds points to a player's score in the leaderboard based on voting results.
     * It calculates a running average over the rounds.
     *
     * @param playerName  the name of the player
     * @param roundNumber the current round number
     */
    public void addPointsToLeaderboard(String playerName, Integer roundNumber) {
        Double votesOfRoundAsDouble = votesOfARound.doubleValue();
        Double meanOfVotes = votesOfRoundAsDouble / (getNumberOfPlayers() - 1);
        leaderboard.merge(playerName, meanOfVotes, (existingMeans, newMean) ->
                (existingMeans * roundNumber + newMean) / (roundNumber + 1));
    }

    /**
     * Adds a vote value to the total votes of the current round.
     *
     * @param vote the vote value to add
     */
    public void addToVotesOfARound(Integer vote) {
        votesOfARound += vote;
    }

    /**
     * Resets the total votes of the current round to zero.
     */
    public void resetVotesOfARound() {
        votesOfARound = 0;
    }

    public Double getPointsOfAPlayer(String playerName) {
        return leaderboard.get(playerName);
    }

    public void removeFromLeaderboard(String playerName) {
        leaderboard.remove(playerName);
    }

    public void addToLeaderboard(String playerName, Double points) {
        leaderboard.put(playerName, points);
    }

    public HashMap<String, Double> getLeaderboard() {return (HashMap<String, Double>) leaderboard;}

    public void setImageOfPlayer(Integer id, String imageAsString) {
        images.put(id, imageAsString);
    }

    public String getImageOfPlayer() {
        return images.get(playerVotingOn);
    }

    public void addToDisconnectedPlayers(Integer playerId) {disconnectedPlayers.add(playerId);}

    public void removeDisconnectedPlayers(Integer playerId) {disconnectedPlayers.remove(playerId);}

    public Integer[] getDisconnectedPlayers() {return disconnectedPlayers.toArray(new Integer[0]);}

    public Boolean disconnectedPlayersIsEmpty() {return disconnectedPlayers.isEmpty();}

    public void addDisconnectedPlayersPoints(Integer id, Double points) {disconnectedPlayersPoints.put(id, points);}

    public void removeDisconnectedPlayersPoints(Integer id, Double points) {disconnectedPlayersPoints.remove(id);}

    public HashMap<Integer, Double> getDisconnectedPlayerPoints() {return (HashMap<Integer, Double>) disconnectedPlayersPoints;}
}