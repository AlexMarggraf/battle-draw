/**
 * This class represents a game lobby where players can join, leave, and interact.
 * It manages the list of players in the lobby and provides methods to modify them.
 */

import java.util.ArrayList;

public class Lobby {
    private String lobbyName;
    private ArrayList<Integer> players;

    public Lobby(String lobbyName) {
        this.lobbyName = lobbyName;
        this.players = new ArrayList<>();

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
}
