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
    public Message (String message) {
        String[] content = message.split("\\s+");
        String[] params = new String[content.length - 1];
        System.arraycopy(content, 1, params, 0, content.length - 1);
        this.type = content[0];
        this.params = params;
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
    public String getMsgString() {
        String message = type;

        for(String param : params) {
            message = message + " " + param;
        }

        return message;
    }
}