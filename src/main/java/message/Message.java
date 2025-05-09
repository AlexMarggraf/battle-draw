package message;

/**
 * This class is used to structure messages according to the network protocol.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

public class Message {
    private String type;
    private String[] params;

    /**
     * This method is a constructor and sets type and parameters. Furthermore, it ensures that the Message object
     * created is valid.
     *
     * @param type The type of the message
     * @param params The parameters of the message
     */
    public Message(String type, String[] params) {
        switch (type) {
            case "chatTo", "chatLobby" -> {
                if(params.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 4 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The sender username has to be a string");
                } else if (params[2] == null) {
                    throw new IllegalArgumentException("The recipient username has to be a string");
                } else if (params[3] == null) {
                    throw new IllegalArgumentException("The message has to be a string");
                }
                else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "chatAll" -> {
                if(params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                }  else if (params[2] == null) {
                    throw new IllegalArgumentException("The message has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "signUp", "logIn" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if(params[1] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else if(params[2] == null) {
                    throw new IllegalArgumentException("The password has to be a string");
                } else {
                    try{
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "changeUsername", "username", "acceptLogin", "rejectLogin", "acceptSignup", "leaveLobby", "lobbyLeft", "rejectLobby", "hasDisconnected" -> {
                if (params.length != 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else if(params[1] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "ping", "pong", "disconnect", "id", "whoOnline", "whatGames", "createLobby", "showLeaderboard", "showEnding", "startNextRound", "whatHighscore" -> {
                if(params.length != 1) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 1 parameters.");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "listLobbyPlayers", "joinLobby", "lobbyJoined", "startVoting", "readyToVote", "nextRound", "changeHost", "lobbyStatusOpen" -> {
                if (params.length != 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "whatID" -> {
                if (params.length != 0) {
                    throw new IllegalArgumentException("Message of type " + type + " doesn't need parameters.");
                } else {
                    this.type = type;
                    this.params = new String[0];
                }
            }
            case "broadcast" -> {
                if (params.length != 1) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 1 parameters.");
                } else if(params[0] == null) {
                    throw new IllegalArgumentException("The message cannot be null");
                } else {
                        this.type = type;
                        this.params = params;
                }
            }
            case "online" -> {
                if (params.length < 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs at least 2 parameters.");
                } else {
                    for (int i = 1; i < params.length; i++) {
                        if (params[i] == null) {
                            throw new IllegalArgumentException("The username has to be a string");
                        }
                    }

                    try {
                        Integer.parseInt(params[0]);

                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "games", "lobbyPlayers" -> {
                if (params.length < 1) {
                    throw new IllegalArgumentException("Message of type " + type + " needs at least 1 parameter.");
                } else {
                    for (int i = 1; i < params.length; i++) {
                        if (params[i] == null) {
                            throw new IllegalArgumentException("The content has to be a string");
                        }
                    }

                    try {
                        Integer.parseInt(params[0]);

                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "playerJoined" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if(params[2] == null) {
                    throw new IllegalArgumentException("The username of the new player has to be a string");
                }
                try{
                    Integer.parseInt(params[0]);
                    Integer.parseInt(params[2]);
                    this.type = type;
                    this.params = params;
                } catch (NumberFormatException NFException) {
                    throw new IllegalArgumentException("The id and id of the new player have to be integers");
                }
            }
            case "image" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                }
                else if(params[2] == null) {
                    throw new IllegalArgumentException("The image has to be a string");
                }
                try {
                    Integer.parseInt(params[0]);
                    this.type = type;
                    this.params = params;
                } catch (NumberFormatException NFException) {
                    throw new IllegalArgumentException("The id has to be an integer");
                }
            }
            case "startDrawing" -> {
                if (params.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 4 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Integer.parseInt(params[2]);
                        Integer.parseInt(params[3]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id, number of rounds and time have to be integers");
                    }
                }
            }
            case "drawingStarts" -> {
                if (params.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 4 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The theme has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Integer.parseInt(params[2]);
                        Integer.parseInt(params[3]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id, number of rounds and time have to be integers");
                    }
                }
            }
            case "votingStarts" -> {
                if (params.length != 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Integer.parseInt(params[1]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id and number of voted rounds have to be integers");
                    }
                }
            }
            case "sendVote" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Integer.parseInt(params[2]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id and vote have to be integers");
                    }
                }
            }
            case "updatePoints" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Double.parseDouble(params[2]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer and points a double");
                    }
                }
            }
            case "highscore" -> {
                if (params.length != 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Double.parseDouble(params[1]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer and the highscore a double.");
                    }
                }
            }
            case "newHost" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        Integer.parseInt(params[2]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id and id of new host have to be an integers.");
                    }
                }
            }
            case "newUsername" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The old username has to be a string");
                } else if (params[2] == null) {
                    throw new IllegalArgumentException("The new username has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer.");
                    }
                }
            }
            case "changeLobbyName" -> {
                if (params.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (params[1] == null) {
                    throw new IllegalArgumentException("The old lobby name has to be a string");
                } else if (params[2] == null) {
                    throw new IllegalArgumentException("The new lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(params[0]);
                        this.type = type;
                        this.params = params;
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer.");
                    }
                }
            }
            default -> throw new IllegalArgumentException("Not a valid message type: " + type);
        }
    }

    /**
     * This method is a constructor and sets the type and parameters. Furthermore, it ensures that every String passed into this
     * will create a valid Message object.
     *
     * @param message The string literal of the message
     */
    public Message(String message) {
        String[] content = message.split("\\s+");
        String type = content[0];

        switch (type) {
            case "chatTo", "chatLobby" -> {
                if (content.length < 5) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 4 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The sender username has to be a string");
                } else if (content[3] == null) {
                    throw new IllegalArgumentException("The recipient username has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[4];
                        params[0] = content[1];

                        StringBuilder msgBuilder = new StringBuilder(content[4]);
                        for (int i = 5; i < content.length; i++) {
                            msgBuilder.append(" ").append(content[i]);
                        }

                        params[1] = content[2];
                        params[2] = content[3];
                        params[3] = msgBuilder.toString();
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "chatAll" -> {
                if (content.length < 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[3];
                        params[0] = content[1];

                        StringBuilder msgBuilder = new StringBuilder(content[3]);
                        for (int i = 4; i < content.length; i++) {
                            msgBuilder.append(" ").append(content[i]);
                        }

                        params[1] = content[2];
                        params[2] = msgBuilder.toString();
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "signUp", "logIn" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else if (content[3] == null) {
                    throw new IllegalArgumentException("The password has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);

                        this.params = new String[3];
                        params[0] = content[1];
                        this.type = type;

                        params[1] = content[2];
                        params[2] = content[3];
                    } catch (NumberFormatException  NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "changeUsername", "username", "acceptLogin", "rejectLogin", "acceptSignup", "leaveLobby", "lobbyLeft", "rejectLobby", "hasDisconnected" -> {
                if (content.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[]{content[1], content[2]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "ping", "pong", "disconnect", "id", "whoOnline", "whatGames", "createLobby", "showLeaderboard", "showEnding", "startNextRound", "whatHighscore" -> {
                if (content.length != 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 1 parameters.");
                } else {
                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[]{content[1]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "whatID" -> {
                if (content.length != 1) {
                    throw new IllegalArgumentException("Message of type " + type + " doesn't need parameters.");
                } else {
                    this.type = type;
                    this.params = new String[0];
                }
            }
            case "broadcast" -> {
                if (content.length < 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 1 parameters.");
                } else {
                    this.type = type;

                    StringBuilder msgBuilder = new StringBuilder(content[1]);
                    for (int i = 2; i < content.length; i++) {
                        msgBuilder.append(" ").append(content[i]);
                    }
                    this.params = new String[]{msgBuilder.toString()};
                }
            }
            case "online" -> {
                if (content.length < 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs at least 2 parameters.");
                } else {
                    for (int i = 2; i < content.length; i++) {
                        if (content[i] == null) {
                            throw new IllegalArgumentException("The username has to be a string");
                        }
                    }

                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[content.length - 1];

                        System.arraycopy(content, 1, params, 0, content.length - 1);
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "games", "lobbyPlayers" -> {
                if (content.length < 2) {
                    throw new IllegalArgumentException("Message of type " + type + " needs at least 1 parameter.");
                } else {
                    for (int i = 2; i < content.length; i++) {
                        if (content[i] == null) {
                            throw new IllegalArgumentException("The content has to be a string");
                        }
                    }

                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[content.length - 1];

                        System.arraycopy(content, 1, params, 0, content.length - 1);
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "joinLobby", "lobbyJoined", "listLobbyPlayers", "startVoting", "readyToVote", "nextRound", "changeHost", "lobbyStatusOpen" -> {
                if (content.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[]{content[1], content[2]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer");
                    }
                }
            }
            case "playerJoined" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[3] == null) {
                    throw new IllegalArgumentException("The username of the new player has to be a string");
                }
                try {
                    Integer.parseInt(content[1]);
                    Integer.parseInt(content[3]);
                    this.type = type;
                    this.params = new String[]{content[1], content[2], content[3]};
                } catch (NumberFormatException NFException) {
                    throw new IllegalArgumentException("The id and the id of the new player have to be integers");
                }
            }
            case "image" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                }
                else if (content[3] == null) {
                    throw new IllegalArgumentException("The image has to be a string");
                }
                try {
                    Integer.parseInt(content[1]);
                    this.type = type;
                    this.params = new String[]{content[1], content[2], content[3]};
                } catch (NumberFormatException NFException) {
                    throw new IllegalArgumentException("The id has to be an integer");
                }
            }
            case "startDrawing" -> {
                if (content.length != 5) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 4 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Integer.parseInt(content[3]);
                        Integer.parseInt(content[4]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3], content[4]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id, number of rounds and time have to be integers");
                    }
                }
            }
            case "drawingStarts" -> {
                if (content.length != 5) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 4 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The theme has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Integer.parseInt(content[3]);
                        Integer.parseInt(content[4]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3], content[4]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id, number of rounds and the time have to be integers");
                    }
                }
            }
            case "votingStarts" -> {
                if (content.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Integer.parseInt(content[2]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id and number of voted rounds have to be integers");
                    }
                }
            }
            case "sendVote" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The username lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Integer.parseInt(content[3]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id and vote have to be integers");
                    }
                }
            }
            case "updatePoints" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The username has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Double.parseDouble(content[3]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer and points a double");
                    }
                }
            }
            case "highscore" -> {
                if (content.length != 3) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 2 parameters.");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Double.parseDouble(content[2]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer and the highscore a double.");
                    }
                }
            }
            case "newHost" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        Integer.parseInt(content[3]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id and id of new host have to be an integers.");
                    }
                }
            }
            case "newUsername" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The old username has to be a string");
                } else if (content[3] == null) {
                    throw new IllegalArgumentException("The new username has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer.");
                    }
                }
            }
            case "changeLobbyName" -> {
                if (content.length != 4) {
                    throw new IllegalArgumentException("Message of type " + type + " needs exactly 3 parameters.");
                } else if (content[2] == null) {
                    throw new IllegalArgumentException("The old lobby name has to be a string");
                } else if (content[3] == null) {
                    throw new IllegalArgumentException("The new lobby name has to be a string");
                } else {
                    try {
                        Integer.parseInt(content[1]);
                        this.type = type;
                        this.params = new String[]{content[1], content[2], content[3]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("The id has to be an integer.");
                    }
                }
            }
            default -> throw new IllegalArgumentException("Not a valid message type: " + type);
        }
    }

    /**
     * This method gives information about the type of the message.
     *
     * @return Returns the type
     */
    public String getType() {
        return type;
    }

    /**
     * This method gives information about the parameters of the message.
     *
     * @return Returns the parameters
     */
    public String[] getParams() {
        return params;
    }

    /**
     * This method gives information about the id of the server the message is related to.
     *
     * @return Returns the id of the related server
     */
    public int getID() {
        return Integer.parseInt(params[0]);
    }

    /**
     * This method constructs a string according to the network protocol.
     *
     * @return Returns the message in string format
     */
    public String toString() {
        String message = type;

        for(String param : params) {
            message = message + " " + param;
        }

        return message;
    }
}