public class Game {
    private String status;
    private String lobbyName;

    public Game(String lobbyName) {
        this.status = "notStarted";
        this.lobbyName = lobbyName;
    }

    public String getStatus() {
        return status;
    }

    public String getLobbyName() {
        return lobbyName;
    }
}