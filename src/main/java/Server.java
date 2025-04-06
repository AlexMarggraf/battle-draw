/**
 * This class serves as the server for our game. It accepts any clients that want to connect to the server and initializes
 * the server threads.
 *
 * @author Frederic Weyssow
 * @author Alexander Maggraf
 */

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
     * it sets the id to 0.
     */
    public Server() {
        this.receiveMessageQueue = new LinkedList<>();
        this.sendMessageQueue = new LinkedList<>();
        this.serverThreadList = new ArrayList<>();
        this.connection = -2;
        this.lobbyList = new LinkedList<>();
        this.gameList = new LinkedList<>();
        this.lobbyGameMap = new HashMap<>();
    }

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
                System.out.println(message);
                st.queueServerMessage(message);
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
            System.out.println(clientMessage);

            Message message = new Message(clientMessage);

            switch(message.getType()) {
                case "disconnect" -> {
                    int id = message.getID();
                    serverThreadList.removeIf(st -> st.getId() == id);
                    DatabaseHelper.setStatus(id, 0);

                    Message broadcast = new Message ("broadcast User " + DatabaseHelper.getPlayerName(message.getID()) + " disconnected");
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
                        Message response = new Message("chatTo", new String[]{0 + "", "Server",  DatabaseHelper.getPlayerName(Integer.parseInt(message.getParams()[0])), "Username is already in use"});
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

                        Message broadcast = new Message ("broadcast User " + username + " is online");
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

                    Message broadcast = new Message ("broadcast User " + username + " is online");
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
                    for(Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            List<String> playerList = new ArrayList<>();
                            playerList.add(message.getID() + "");

                            for (int id : players) {
                                playerList.add(DatabaseHelper.getPlayerName(id));
                            }

                            Message response = new Message("lobbyPlayers", playerList.toArray(new String[0]));
                            sendMessageQueue.add(response.toString());
                        }
                    }
                }
                case "createLobby" -> {
                    String lobbyName = "Lobby_von_" + DatabaseHelper.getPlayerName(message.getID());
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
                    Message response = new Message("chatTo", new String[]{0 + "", "Server",  DatabaseHelper.getPlayerName(Integer.parseInt(message.getParams()[0])), "Lobby doesn't exist"});
                    for(Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobby.addPlayer(message.getID());
                            response = new Message("lobbyJoined", new String[]{message.getID() + "", lobby.getLobbyName()});
                            Integer[] players = lobby.getPlayers();
                            for (int id : players) {
                                Message responseToLobby = new Message("playerJoined", new String[]{id + "", DatabaseHelper.getPlayerName(message.getID()), message.getID() + ""});
                                sendMessageQueue.add(responseToLobby.toString());
                            }
                            lobbyGameMap.get(lobby).plusOnePlayer();
                        }
                    }
                    sendMessageQueue.add(response.toString());
                }
                case "leaveLobby" -> {
                    for(Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            Integer[] players = lobby.getPlayers();
                            for (int id : players) {
                                Message response = new Message("lobbyLeft", new String[]{id + "", DatabaseHelper.getPlayerName(message.getID())});
                                sendMessageQueue.add(response.toString());
                            }
                            lobby.removePlayer(message.getID());
                            lobbyGameMap.get(lobby).minusOnePlayer();
                            if(lobby.isEmpty()) {
                                lobbyGameMap.remove(lobby);
                                lobbyList.remove(lobby);
                            }
                        }
                    }
                }
                case "startDrawing" -> {
                    for(Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobbyGameMap.get(lobby).setStatus("drawing");
                            String theme = DatabaseHelper.getRandomWords(1).toString();
                            Integer[] players = lobby.getPlayers();
                            for (int id : players) {
                                Message responseToLobby = new Message("drawingStarts", new String[]{id + "", theme});
                                sendMessageQueue.add(responseToLobby.toString());
                            }
                            if(lobbyGameMap.get(lobby).getRoundNumber() == 0) {
                                lobby.initialiseLeaderboard();
                            }
                        }
                    }
                }
                case "readyToVote" -> {
                    for (Lobby lobby : lobbyList) {
                        if (lobby.getLobbyName().equals(message.getParams()[1])) {
                            lobby.plusOnePlayerIsReady();
                            if (lobby.getNumberOfPlayers() == lobby.getHowManyPlayersAreReady()) {
                                Integer[] players = lobby.getPlayers();
                                Integer numberOfRoundsVotes = lobbyGameMap.get(lobby).getNumberOfRoundsVoted();
                                Integer numberOfPlayers = lobby.getNumberOfPlayers();
                                if (numberOfRoundsVotes >= numberOfPlayers) {
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
                                    lobbyGameMap.get(lobby).setStatus("leaderboard");
                                } else {
                                    if (numberOfRoundsVotes == 0) {
                                        lobby.resetVotesOfARound();
                                        lobby.resetPlayersToVoteFor();
                                        lobbyGameMap.get(lobby).setStatus("voting");
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
                                        Message response2 = new Message("votingStarts", new String[]{id + "", numberOfRoundsVotes + ""});
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
                            Integer[] players = lobby.getPlayers();
                            if (lobbyGameMap.get(lobby).getRoundNumber() == lobbyGameMap.get(lobby).getMaxNumberOfRounds()) {
                                lobbyGameMap.get(lobby).setStatus("ended");
                                for (int id : players) {
                                     Message response = new Message("showEnding", new String[]{id + ""});
                                     sendMessageQueue.add(response.toString());
                                }
                            } else {
                                for (int id : players) {
                                    Message response = new Message("startNextRound", new String[]{id + ""});
                                    sendMessageQueue.add(response.toString());
                                }
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
            }
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
                                if(id != message.getID()) {
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
        Thread.startVirtualThread(conListener);

        while (true) {
            server.startServerThread(conListener);
            server.fillReceiveMessageQueue();
            server.handleReceiveMessage();
            server.handleSendMessage();
        }
    }
}