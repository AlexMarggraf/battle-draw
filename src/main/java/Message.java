public class Message {
    private String type;
    private String[] params;

    public Message(String type, String[] params) {
        this.type = type;
        this.params = params;
    }

    public Message (String message) {
        String[] content = message.split("\\s+");
        this.type = content[0];
        String[] params = new String[content.length - 1];
        System.arraycopy(content, 1, params, 0, content.length - 1);
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public String[] getParams() {
        return params;
    }

    public int getID() {
        return Integer.parseInt(params[0]);
    }

    public String getMsgString() {
        String message = type;

        for(String param : params) {
            message = message  + " " + param;
        }

        return message;
    }
}