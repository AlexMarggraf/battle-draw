import java.util.LinkedList;

public class Lobby {
    private int lobbyId;
    private String lobbyName;
    private LinkedList<Integer> players;

    public Lobby(int lobbyId, String lobbyName) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.players = new LinkedList<Integer>();

    }

    public int getLobbyId() {
        return lobbyId;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public LinkedList<Integer> getPlayers() {
        return players;
    }

    public void addPlayer(Integer playerId) {
        players.add(playerId);
    }

    public void removePlayer(Integer playerId) {
        players.remove(playerId);
    }

    public String listPlayers(){
        return "Lobby " + lobbyId + " (" + lobbyName + ") Players: " + players.toString();
    }
}
