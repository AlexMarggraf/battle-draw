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
            case "chatAll" -> {
                this.type = type;
                this.params = new String[3];

                try {
                    Integer.parseInt(content[1]);
                    params[0] = content[1];
                } catch (NumberFormatException NFException) {
                    throw new IllegalArgumentException("Invalid number format for type: " + type);
                }

                params[1] = content[2];

                StringBuilder msgBuilder = new StringBuilder(content[3]);
                for (int i = 4; i < content.length; i++) {
                    msgBuilder.append(" ").append(content[i]);
                }

                params[2] = msgBuilder.toString();
            }
            case "ping", "pong", "disconnect", "id" -> {
                if (content.length == 2) {
                    try {
                        Integer.parseInt(content[1]);
                        this.type = type;
                        this.params = new String[]{content[1]};
                    } catch (NumberFormatException NFException) {
                        throw new IllegalArgumentException("Invalid number format for type: " + type);
                    }
                } else {
                    throw new IllegalArgumentException(type + " must have exactly one parameter.");
                }
            }
            case "whatID" -> {
                this.type = "whatID";
                this.params = new String[0];
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