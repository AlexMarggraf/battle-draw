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
            case "chatTo" -> {
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
            case "changeUsername", "username", "acceptLogin", "rejectLogin", "acceptSignup", "rejectSignup" -> {
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
            case "ping", "pong", "disconnect", "id" -> {
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
            case "chatTo" -> {
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
            case "changeUsername", "username", "acceptLogin", "rejectLogin", "acceptSignup", "rejectSignup"-> {
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
            case "ping", "pong", "disconnect", "id" -> {
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