package server;

/**
 * This class serves as the server for our game. It accepts any clients that want to connect to the server and initializes
 * the server threads.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

import game.Game;
import game.Lobby;
import game.Main;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.*;

public class Server {

    private volatile Queue<String> receiveMessageQueue;
    private volatile Queue<String> sendMessageQueue;
    private ArrayList<ServerThread> serverThreadList;
    private int connection;
    private LinkedList<Lobby> lobbyList;
    private LinkedList<Game> gameList;
    private HashMap<Lobby, Game> lobbyGameMap;

    /**
     * This method is the constructor and initializes the message queues and the server list and
     * it sets the id to 0. In case the database doesn't exist it will create it.
     */
    public Server() {
        this.receiveMessageQueue = new LinkedList<>();
        this.sendMessageQueue = new LinkedList<>();
        this.serverThreadList = new ArrayList<>();
        this.connection = -2;
        this.lobbyList = new LinkedList<>();
        this.gameList = new LinkedList<>();
        this.lobbyGameMap = new HashMap<>();

        DatabaseHelper.createDB();
    }

    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    /**
     * This method starts a new server thread.
     *
     * @param conListener The object that listens to any incoming connections
     */
    public void startServerThread(ConnectionListener conListener) {
        while (conListener.socketQueue.peek() != null) {
            ServerThread server = new ServerThread(connection, conListener.socketQueue.remove());
            Thread serverThread = new Thread(server);
            serverThread.start();

            serverThreadList.add(server);
            connection--;
        }
    }

    /**
     * This method queues a message into the server thread which is
     * identified by the id provided.
     *
     * @param message The message that is supposed to be queued
     * @param id      The id of the server that the message is supposed to be queued in
     */
    public void sendMessage(String message, int id) {
        for (ServerThread st : serverThreadList) {
            if (st.getId() == id) {
                st.queueServerMessage(message);
                LOGGER.info("Message sent {}", message);
            }
        }
    }

    /**
     * This method checks all server threats and retrieves any undealt messages.
     */
    public void fillReceiveMessageQueue() {
        for (ServerThread st : serverThreadList) {
            String clientMessage = st.getClientMessage();

            while (clientMessage != null) {
                receiveMessageQueue.add(clientMessage);
                clientMessage = st.getClientMessage();
            }
        }
    }

    /**
     * This method handles any message that is in the receiveMessageQueue.
     */
    public void handleReceiveMessage() {
        while (receiveMessageQueue.peek() != null) {
            String clientMessage = receiveMessageQueue.remove();

            Message message = new Message(clientMessage);

            switch (message.getType()) {
                case "disconnect" -> {
                    int id = message.getID();

                    for (Lobby lobby : lobbyList) {
                        if (lobby.isEmpty()) {
                            Game game = lobbyGameMap.get(lobby);
                            lobbyGameMap.remove(lobby);
                            lobbyList.remove(lobby);
                            game.setStatus("ended");
                        } else {
                            Integer[] players = lobby.getPlayers();
                            if (players[0] == id) {
                                String name = DatabaseHelper.getPlayerName(players[1]);
                                String newLobbyName = "Lobby_of_" + name;
                                Game game = lobbyGameMap.get(lobby);
                                lobby.setLobbyName(newLobbyName);
                                game.setLobbyName(newLobbyName);
                                for (int player : players) {
                                    if (players[0] != player) {
                                        Message response = new Message("newHost", new String[]{player + "", newLobbyName, players[1] + ""});
                                        sendMessageQueue.add(response.toString());
                                    }
                                }
                            }
                            for (int player : players) {
                                if (player == id) {
                                    lobby.removePlayer(player);
                                    lobby.addToDisconnectedPlayers(player);
                                    String playerName = DatabaseHelper.getPlayerName(player);
                                    if (lobby.getLeaderboard().containsKey(playerName)) {
                                        Double points = lobby.getPointsOfAPlayer(playerName);
                                        lobby.removeFromLeaderboard(playerName);
                                        lobby.addDisconnectedPlayersPoints(player, points);
                                    }
                                    Integer[] playersNew = lobby.getPlayers();
                                    Message response;
                                    for (int playerID : playersNew) {
                                        response = new Message("hasDisconnected", new String[]{playerID + "", playerName});
                                        sendMessageQueue.add(response.toString());
                                    }
                                }
                            }
                        }
                    }

                    serverThreadList.removeIf(st -> st.getId() == id);
                    DatabaseHelper.setStatus(id, 0);

                    Message broadcast = new Message("broadcast User " + DatabaseHelper.getPlayerName(message.getID()) + " disconnected");
                    sendMessageQueue.add(broadcast.toString());
                }
                case "chatAll", "chatLobby" -> sendMessageQueue.add(message.toString());
                case "chatTo" -> {
                    String recipientUsername = message.getParams()[2];
                    if (message.getParams()[1].equals(message.getParams()[2])) {
                        Message response = new Message("chatTo", new String[]{0 + "", "Server", message.getParams()[1], "You can't whisper yourself"});
                        sendMessageQueue.add(response.toString());
                    } else if (!DatabaseHelper.checkUsername(message.getParams()[2]) && DatabaseHelper.getStatus(DatabaseHelper.getPlayerId(recipientUsername))) {
                        sendMessageQueue.add(message.toString());
                    } else {
                        Message response = new Message("chatTo", new String[]{0 + "", "Server", message.getParams()[1], "The user " + message.getParams()[2] + " isn't online or doesn't exist"});
                        sendMessageQueue.add(response.toString());
                    }
                }
                case "changeUsername" -> {
                    if (DatabaseHelper.checkUsername(message.getParams()[1])) {
                        DatabaseHelper.changeUsername(message.getParams()[1], Integer.parseInt(message.getParams()[0]));
                        Message response = new Message("username", new String[]{message.getID() + "", message.getParams()[1]});
                        sendMessageQueue.add(response.toString());
                    } else {
                        Message response = new Message("chatTo", new String[]{0 + "", "Server", DatabaseHelper.getPlayerName(Integer.parseInt(message.getParams()[0])), "Username is already in use"});
                        sendMessageQueue.add(response.toString());
                    }
                }
                case "logIn" -> {
                    String username = message.getParams()[1];

                    if (!DatabaseHelper.checkCredentials(username, message.getParams()[2]) || DatabaseHelper.getStatus(DatabaseHelper.getPlayerId(username))) {
                        Message response = new Message("rejectLogin", new String[]{message.getID() + "", username});
                        sendMessageQueue.add(response.toString());
                    } else {
                        DatabaseHelper.setStatus(DatabaseHelper.getPlayerId(username), 1);
                        Message response = new Message("acceptLogin", new String[]{DatabaseHelper.getPlayerId(username) + "", username});
                        sendMessageQueue.add(response.toString());
                        connection++;

                        for (ServerThread st : serverThreadList) {
                            if (st.getId() == message.getID()) {
                                st.setId(DatabaseHelper.getPlayerId(username));
                                st.setClientUsername(username);
                            }
                        }

                        Message broadcast = new Message("broadcast User " + username + " is online");
                        sendMessageQueue.add(broadcast.toString());
                    }
                }
                case "signUp" -> {
                    String username = message.getParams()[1];

                    if (!DatabaseHelper.checkUsername(username)) {
                        int counter = 1;
                        String originalUsername = username;

                        while (!DatabaseHelper.checkUsername(username)) {
                            username = originalUsername + String.format("%03d", counter);
                            counter++;
                        }
                    }

                    DatabaseHelper.insertPlayer(username, message.getParams()[2]);
                    DatabaseHelper.setStatus(DatabaseHelper.getPlayerId(username), 1);

                    for (ServerThread st : serverThreadList) {
                        if (st.getId() == message.getID()) {
                            st.setId(DatabaseHelper.getPlayerId(username));
                            st.setClientUsername(username);
                        }
                    }

                    Message response = new Message("acceptSignup", new String[]{DatabaseHelper.getPlayerId(username) + "", username});
                    sendMessageQueue.add(response.toString());
                    connection++;

                    Message broadcast = new Message("broadcast User " + username + " is online");
                    sendMessageQueue.add(broadcast.toString());
                }
                case "whoOnline" -> {
                    List<String> onlineList = new ArrayList<>();
                    onlineList.add(message.getID() + "");
                    for (ServerThread st : serverThreadList) {
                        if (DatabaseHelper.getStatus(st.getId())) {
                            onlineList.add(DatabaseHelper.getPlayerName(st.getId()));
                        }
                    }
                    Message response = new Message("online", onlineList.toArray(new String[0]));
                    sendMessageQueue.add(response.toString());
                }
                case "whatGames" -> {
                    List<String> gamesList = new ArrayList<>();
                    gamesList.add(message.getID() + "");
                    for (Game game : gameList) {
                        gamesList.add(game.getLobbyName());
                        gamesList.add(game.getStatus());
                    }
                    Message response = new Message("games", gamesList.toArray(new String[0]));
                    sendMessageQueue.add(response.toString());
                }
                case "listLobbyPlayers" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            List<String> playerList = new ArrayList<>();
                            playerList.add(message.getID() + "");

                            for (int id : players) {
                                if (DatabaseHelper.getStatus(id)) {
                                    playerList.add(DatabaseHelper.getPlayerName(id));
                                }
                            }

                            Message response = new Message("lobbyPlayers", playerList.toArray(new String[0]));
                            sendMessageQueue.add(response.toString());
                        }
                    }
                }
                case "createLobby" -> {
                    String lobbyName = "Lobby_of_" + DatabaseHelper.getPlayerName(message.getID());
                    Lobby newLobby = new Lobby(lobbyName);
                    newLobby.addPlayer(message.getID());
                    Game game = new Game(lobbyName);
                    game.plusOnePlayer();
                    lobbyList.add(newLobby);
                    gameList.add(game);
                    lobbyGameMap.put(newLobby, game);
                    Message response = new Message("lobbyJoined", new String[]{message.getID() + "", lobbyName});
                    sendMessageQueue.add(response.toString());
                    Message response2 = new Message("playerJoined", new String[]{message.getID() + "", lobbyName, message.getID() + ""});
                    sendMessageQueue.add(response2.toString());
                }
                case "joinLobby" -> {
                    Message response = new Message("chatTo", new String[]{0 + "", "Server", DatabaseHelper.getPlayerName(Integer.parseInt(message.getParams()[0])), "Lobby doesn't exist"});
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            boolean wasInLobby = false;
                            if (!lobby.disconnectedPlayersIsEmpty()) {
                                Integer[] disconnectedPlayers = lobby.getDisconnectedPlayers();
                                for (int id : disconnectedPlayers) {
                                    if (id == message.getID()) {
                                        wasInLobby = true;
                                    }
                                }
                            }
                            if ((lobbyGameMap.get(lobby).getStatus().equals("ongoing") || lobbyGameMap.get(lobby).getStatus().equals("ended")) && !wasInLobby) {
                                response = new Message("chatTo", new String[]{0 + "", "Server", DatabaseHelper.getPlayerName(Integer.parseInt(message.getParams()[0])), lobby.getLobbyName() + " is already playing. You cannot join ongoing or ended lobbies."});
                            } else {
                                if (wasInLobby) {
                                    Double points = lobby.getDisconnectedPlayerPoints().get(message.getID());
                                    lobby.removeDisconnectedPlayers(message.getID());
                                    lobby.removeDisconnectedPlayersPoints(message.getID());
                                    String playerName = DatabaseHelper.getPlayerName(message.getID());
                                    lobby.addToLeaderboard(playerName, points);
                                    ArrayList<Integer> haveBeenVotedOn = lobby.getHaveBeenVotedOn();
                                    ArrayList<Integer> playerToVoteFor = lobby.getPlayersToVoteFor();
                                    if (!haveBeenVotedOn.contains(message.getID()) && !playerToVoteFor.contains(message.getID())) {
                                        lobby.addPlayerToVoteFor(message.getID());
                                    }
                                    String picture = lobby.getDisconnectedPicture();
                                    lobby.setImageOfPlayer(message.getID(), picture);
                                    if (lobby.getNeedsReadyToVote()) {
                                        lobby.plusOnePlayerIsReady();
                                        if (!haveBeenVotedOn.getLast().equals(message.getID())) {
                                            lobby.plusJoinedDuringVote();
                                        }
                                    }
                                }
                                lobby.addPlayer(message.getID());
                                response = new Message("lobbyJoined", new String[]{message.getID() + "", lobby.getLobbyName()});
                                Integer[] players = lobby.getPlayers();
                                for (int id : players) {
                                    Message responseToLobby = new Message("playerJoined", new String[]{id + "", DatabaseHelper.getPlayerName(message.getID()), message.getID() + ""});
                                    sendMessageQueue.add(responseToLobby.toString());
                                }
                                lobbyGameMap.get(lobby).plusOnePlayer();
                                if (wasInLobby) {
                                    ArrayList<Integer> haveBeenVotedOn = lobby.getHaveBeenVotedOn();
                                    String playerName = DatabaseHelper.getPlayerName(message.getID());
                                    if (haveBeenVotedOn.contains(message.getID())) {
                                        for (Integer id : players) {
                                            Message message2 = new Message("updatePoints", new String[]{id + "", playerName, lobby.getPointsOfAPlayer(playerName) + ""});
                                            sendMessageQueue.add(message2.toString());
                                        }
                                    }
                                    if (!haveBeenVotedOn.contains(-20)) {
                                        for (Integer id : haveBeenVotedOn) {
                                            String lobbyPlayerName = DatabaseHelper.getPlayerName(id);
                                            Message message3 = new Message("updatePoints", new String[]{message.getID() + "", lobbyPlayerName, lobby.getPointsOfAPlayer(lobbyPlayerName) + ""});
                                            sendMessageQueue.add(message3.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    sendMessageQueue.add(response.toString());
                }
                case "leaveLobby" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            for (int id : players) {
                                Message response = new Message("lobbyLeft", new String[]{id + "", DatabaseHelper.getPlayerName(message.getID())});
                                sendMessageQueue.add(response.toString());
                            }
                            lobby.removePlayer(message.getID());
                            lobbyGameMap.get(lobby).minusOnePlayer();
                            if (lobby.isEmpty()) {
                                Game game = lobbyGameMap.get(lobby);
                                lobbyGameMap.remove(lobby);
                                lobbyList.remove(lobby);
                                game.setStatus("ended");
                            }
                        }
                    }
                }
                case "startDrawing" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobbyGameMap.get(lobby).setStatus("ongoing");
                            String theme = DatabaseHelper.getRandomWords(1).toString();
                            String numberOfRounds = message.getParams()[2];
                            lobbyGameMap.get(lobby).setMaxNumberOfRounds(Integer.parseInt(numberOfRounds));
                            lobbyGameMap.get(lobby).resetRoundNumber();
                            String time = message.getParams()[3];
                            lobbyGameMap.get(lobby).setTimer(Integer.parseInt(time));
                            Integer[] players = lobby.getPlayers();
                            for (int id : players) {
                                Message responseToLobby = new Message("drawingStarts", new String[]{id + "", theme, numberOfRounds, time});
                                sendMessageQueue.add(responseToLobby.toString());
                            }
                            if (lobbyGameMap.get(lobby).getRoundNumber() == 0) {
                                lobby.initialiseLeaderboard();
                            }
                            lobby.needsReadyToVoteTrue();
                        }
                    }
                }
                case "readyToVote" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobby.plusOnePlayerIsReady();
                            if (lobby.getNumberOfPlayers() == lobby.getHowManyPlayersAreReady()) {
                                Integer[] players = lobby.getPlayers();
                                Integer numberOfRoundsVoted = lobbyGameMap.get(lobby).getNumberOfRoundsVoted();
                                Integer numberOfImages = lobby.getImagesSize();
                                if (numberOfRoundsVoted >= numberOfImages) {
                                    String lastPlayer = DatabaseHelper.getPlayerName(lobby.getPlayerVotingOn());
                                    lobby.addPointsToLeaderboard(lastPlayer, lobbyGameMap.get(lobby).getRoundNumber());
                                    Double pointsOfLastPlayer = lobby.getPointsOfAPlayer(lastPlayer);
                                    for (int id : players) {
                                        Message response = new Message("updatePoints", new String[]{id + "", lastPlayer, pointsOfLastPlayer + ""});
                                        sendMessageQueue.add(response.toString());
                                        Message response2 = new Message("showLeaderboard", new String[]{id + ""});
                                        sendMessageQueue.add(response2.toString());
                                    }
                                    lobbyGameMap.get(lobby).resetNumberOfRoundsVoted();
                                    lobby.resetHowManyPlayersAreReady();
                                    lobbyGameMap.get(lobby).setStatus("ongoing");
                                    lobby.needsReadyToVoteFalse();
                                } else {
                                    if (numberOfRoundsVoted == 0) {
                                        lobby.resetVotesOfARound();
                                        lobby.resetPlayersToVoteFor();
                                        lobbyGameMap.get(lobby).setStatus("ongoing");
                                        lobby.resetJoinedDuringVote();
                                    } else {
                                        String lastPlayer = DatabaseHelper.getPlayerName(lobby.getPlayerVotingOn());
                                        lobby.addPointsToLeaderboard(lastPlayer, lobbyGameMap.get(lobby).getRoundNumber());
                                        Double pointsOfLastPlayer = lobby.getPointsOfAPlayer(lastPlayer);
                                        for (int id : players) {
                                            Message response = new Message("updatePoints", new String[]{id + "", lastPlayer, pointsOfLastPlayer + ""});
                                            sendMessageQueue.add(response.toString());
                                        }
                                        lobby.resetVotesOfARound();
                                    }
                                    lobby.setPlayerVotingOn();
                                    for (int id : players) {
                                        Message response2 = new Message("votingStarts", new String[]{id + "", numberOfRoundsVoted + ""});
                                        Message response3 = new Message("image", new String[]{id + "", DatabaseHelper.getPlayerName(lobby.getPlayerVotingOn()), lobby.getImageOfPlayer()});
                                        sendMessageQueue.add(response3.toString());
                                        sendMessageQueue.add(response2.toString());
                                    }
                                    lobby.resetHowManyPlayersAreReady();
                                    lobbyGameMap.get(lobby).plusOneVotedRound();
                                }
                            }
                        }
                    }
                }
                case "sendVote" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobby.addToVotesOfARound(Integer.parseInt(message.getParams()[2]));
                        }
                    }
                }
                case "nextRound" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobbyGameMap.get(lobby).nextRound();
                            lobby.resetImages();
                            Integer[] players = lobby.getPlayers();
                            if (lobbyGameMap.get(lobby).getRoundNumber().equals(lobbyGameMap.get(lobby).getMaxNumberOfRounds())) {
                                lobbyGameMap.get(lobby).setStatus("ended");
                                for (int id : players) {
                                    Message response = new Message("showEnding", new String[]{id + ""});
                                    sendMessageQueue.add(response.toString());
                                    double highscore = DatabaseHelper.getHighscore(id);
                                    String playerName = DatabaseHelper.getPlayerName(id);
                                    double currentScore = lobby.getPointsOfAPlayer(playerName);
                                    if (highscore < currentScore) {
                                        DatabaseHelper.setHighscore(id, currentScore);
                                    }
                                }
                            } else {
                                String theme = DatabaseHelper.getRandomWords(1).toString();
                                Integer maxRounds = lobbyGameMap.get(lobby).getMaxNumberOfRounds();
                                Integer timer = lobbyGameMap.get(lobby).getTimer();
                                for (int id : players) {
                                    Message response = new Message("drawingStarts", new String[]{id + "", theme, maxRounds + "", timer + ""});
                                    sendMessageQueue.add(response.toString());
                                }
                                lobby.needsReadyToVoteTrue();
                            }
                        }
                    }
                }
                case "image" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobby.setImageOfPlayer(message.getID(), message.getParams()[2]);
                        }
                    }
                }
                case "whatHighscore" -> {
                    Double highscore = DatabaseHelper.getHighscore(message.getID());
                    Message response = new Message("highscore", new String[]{message.getID() + "", highscore + ""});
                    sendMessageQueue.add(response.toString());
                }
                case "changeHost" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            String name = DatabaseHelper.getPlayerName(players[0]);
                            String newLobbyName = "Lobby_of_" + name;
                            Game game = lobbyGameMap.get(lobby);
                            lobby.setLobbyName(newLobbyName);
                            game.setLobbyName(newLobbyName);
                            game.setStatus("open");
                            for (int id : players) {
                                Message response = new Message("newHost", new String[]{id + "", newLobbyName, players[0] + ""});
                                sendMessageQueue.add(response.toString());
                            }
                        }
                    }
                }
                case "newUsername" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            String newUsername = DatabaseHelper.getPlayerName(message.getID());
                            String oldUsername = message.getParams()[2];
                            Double Points = lobby.getPointsOfAPlayer(oldUsername);
                            lobby.removeFromLeaderboard(oldUsername);
                            lobby.addToLeaderboard(newUsername, Points);
                            for (Integer id : players) {
                                Message response = new Message("newUsername", new String[]{id + "", oldUsername, newUsername});
                                sendMessageQueue.add(response.toString());
                            }
                        }
                    }
                }
                case "changeLobbyName" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            lobby.setLobbyName(message.getParams()[2]);
                            lobbyGameMap.get(lobby).setLobbyName(message.getParams()[2]);
                            for (int id : players) {
                                Message response = new Message("changeLobbyName", new String[]{id + "", message.getParams()[1], message.getParams()[2]});
                                sendMessageQueue.add(response.toString());
                            }
                        }
                    }
                }
                case "lobbyStatusOpen" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Game game = lobbyGameMap.get(lobby);
                            game.setStatus("open");
                        }
                    }
                }
            }

            LOGGER.info("handled and received message {}", clientMessage);
        }
    }

    /**
     * This method queues all messages into the correct server.
     */
    public void handleSendMessage() {
        while (sendMessageQueue.peek() != null) {
            String serverMessage = sendMessageQueue.remove();
            Message message = new Message(serverMessage);

            switch (message.getType()) {
                case "chatAll" -> {
                    for (ServerThread st : serverThreadList) {
                        if (st.getId() != message.getID()) {
                            sendMessage(message.toString(), st.getId());
                        }
                    }
                }
                case "chatLobby" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[2])) {
                            Integer[] playerId = lobby.getPlayers();
                            for (Integer id : playerId) {
                                if (id != message.getID()) {
                                    sendMessage(message.toString(), id);
                                }
                            }
                        }
                    }
                }
                case "broadcast" -> {
                    for (ServerThread st : serverThreadList) {
                        sendMessage(message.toString(), st.getId());
                    }
                }
                case "chatTo" -> {
                    sendMessage(message.toString(), DatabaseHelper.getPlayerId(message.getParams()[2]));
                }
                default -> sendMessage(message.toString(), message.getID());
            }
            LOGGER.info("handled and sent message {}", serverMessage);
        }
    }

    /**
     * This method starts a connection listener and handles any incoming and outgoing messages.
     *
     * @param args Nothing yet
     */
    public static void main(String[] args) {
        Server server = new Server();
        DatabaseHelper.setOffline();

        ConnectionListener conListener = new ConnectionListener(Integer.parseInt(args[0]));
        Thread conListenerThread = new Thread(conListener);
        conListenerThread.start();

        while (true) {
            server.startServerThread(conListener);
            server.fillReceiveMessageQueue();
            server.handleReceiveMessage();
            server.handleSendMessage();
        }
    }

    public ArrayList<ServerThread> getServerThreadList() {
        return serverThreadList;
    }

    public Queue<String> getSendMessageQueue() {
        return sendMessageQueue;
    }

    public Queue<String> getReceiveMessageQueue() {
        return receiveMessageQueue;
    }

    public LinkedList<Lobby> getLobbyList() {
        return lobbyList;
    }
}