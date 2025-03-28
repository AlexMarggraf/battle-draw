import java.util.ArrayList;

public class Lobby {
    private String lobbyName;
    private ArrayList<Integer> players;

    public Lobby(String lobbyName) {
        this.lobbyName = lobbyName;
        this.players = new ArrayList<>();

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
