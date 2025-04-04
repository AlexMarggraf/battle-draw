public class Game {
    private String status;
    private String lobbyName;
    private Integer numberOfPlayers;
    private Integer roundNumber;
    private Integer numberOfRoundsVoted;
    private Integer maxNumberOfRounds;

    
    public Game(String lobbyName) {
        this.status = "open";
        this.lobbyName = lobbyName;
        this.roundNumber = 0;
        this.numberOfPlayers = 0;
        this.numberOfRoundsVoted = 0;
        this.maxNumberOfRounds = 1;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {this.status = status;}

    public String getLobbyName() {
        return lobbyName;
    }

    public void plusOnePlayer() {this.numberOfPlayers++;}

    public void minusOnePlayer() {this.numberOfPlayers--;}

    public void plusOneVotedRound() {this.numberOfRoundsVoted++;}

    public void nextRound() {this.roundNumber++;}

    public Integer getNumberOfPlayers() {return numberOfPlayers;}

    public Integer getNumberOfRoundsVoted() {return numberOfRoundsVoted;}

    public void resetNumberOfRoundsVoted() {this.numberOfRoundsVoted = 0;}

    public Integer getMaxNumberOfRounds() {return maxNumberOfRounds;}

    public Integer getRoundNumber() {return roundNumber;}
}