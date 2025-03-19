/**
 * This class is used to structure messages according to the network protocoll.
 *
 * @author Alexander Marggraf
 * @author Frederic Weyssow
 */

public class Message {
    private String type;
    private String[] params;

    /**
     * This method is a constructor and sets type and parameters.
     *
     * @param type The type of the message
     * @param params The parameters of the message
     */
    public Message(String type, String[] params) {
        this.type = type;
        this.params = params;
    }

    /**
     * This method is a constructor and sets the type and parameters.
     *
     * @param message The string literal of the message
     */
    public Message(String message) {
        String[] content = message.split("\\s+");
        String type = content[0];

        switch (type) {
            case "chatAll", "chatTo" -> {
                if (content[2] == null) {
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
                    throw new IllegalArgumentException("Message of type " + type + " need exactly 3 parameters.");
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
            case "changeUsername", "acceptLogin", "rejectLogin", "acceptSignup", "rejectSignup"-> {
                if (content.length == 3) {
                    try {
                        Integer.parseInt(content[1]);

                        this.type = type;
                        this.params = new String[]{content[1], content[2]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("Invalid number format for the id");
                    }
                }
            }
            case "ping", "pong", "disconnect", "id" -> {
                if (content.length != 2) {
                    throw new IllegalArgumentException("Message of type " + type + " need exactly 1 parameters.");
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
            case "whatID"/*, "whatUsername"*/ -> {
                if (content.length == 1) {
                    this.type = type;
                    this.params = new String[0];
                }
            }
            default -> {
                throw new IllegalArgumentException("Not a valid message type: " + type);
            }
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
     * This method construcs a string according to the network protocoll.
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