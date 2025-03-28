import java.util.ArrayList;
import java.util.LinkedList;

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

    public ArrayList<Integer> getPlayers() {
        return players;
    }

    public void addPlayer(Integer playerId) {
        players.add(playerId);
    }

    public void removePlayer(Integer playerId) {
        players.remove(playerId);
    }
}
