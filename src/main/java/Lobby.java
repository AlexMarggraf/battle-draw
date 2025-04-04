/**
 * This class represents a game lobby where players can join, leave, and interact.
 * It manages the list of players in the lobby and provides methods to modify them.
 */

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
    private Map<String, Integer> leaderboard;

    public Lobby(String lobbyName) {
        this.lobbyName = lobbyName;
        this.players = new ArrayList<>();
        this.howManyPlayersAreReady = 0;
        this.leaderboard = new HashMap<>();
        this.votesOfARound = 0;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public Integer[] getPlayers() {
        return players.toArray(new Integer[0]);
    }

    public void addPlayer(Integer playerId) {
        players.add(playerId);
    }

    public void removePlayer(Integer playerId) {
        players.remove(playerId);
    }

    public Integer getHowManyPlayersAreReady() {return howManyPlayersAreReady;}

    public void plusOnePlayerIsReady() {howManyPlayersAreReady++;}

    public void minusOnePlayerIsReady() {howManyPlayersAreReady--;}

    public void resetHowManyPlayersAreReady() {howManyPlayersAreReady = 0;}

    public int getNumberOfPlayers() {return players.size();}

    public void resetPlayersToVoteFor() {playersToVoteFor = new ArrayList<>(players);}

    public void setPlayerVotingOn() {
        if (!playersToVoteFor.isEmpty()) {
            playerVotingOn = playersToVoteFor.removeLast();
        }
    }

    public Integer getPlayerVotingOn() {return playerVotingOn;}

    public void initialiseLeaderboard() {
        leaderboard.clear();
        for (Integer playerId : players) {
            leaderboard.put(DatabaseHelper.getPlayerName(playerId), 0);
        }
    }
    public void addPointsToLeaderboard(String playerName) {
        Integer meanOfVotes = votesOfARound / getNumberOfPlayers();
        leaderboard.merge(playerName, meanOfVotes, (existingMeans, newMean) -> (existingMeans + newMean) / 2);
    }

    public void addToVotesOfARound(Integer vote) {
        votesOfARound += vote;
    }

    public Integer getPointsOfAPlayer(String playerName) {
        return leaderboard.get(playerName);
    }
}
