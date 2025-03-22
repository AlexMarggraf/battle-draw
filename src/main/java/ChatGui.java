import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class ChatGui extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);

        TextField messageField = new TextField();
        Button sendButton = new Button("Send");

        VBox.setVgrow(chatArea, Priority.ALWAYS);

        VBox vbox = new VBox(10);
        vbox.setPrefSize(300, 400);

        vbox.getChildren().addAll(chatArea, messageField, sendButton);

        // Set up event handling for the send button
        sendButton.setOnAction(event -> {
            String message = messageField.getText();
            if (!message.trim().isEmpty()) {
                chatArea.appendText("You: " + message + "\n");  // Display the message
                messageField.clear();  // Clear the input field
            }
        });

        Scene scene = new Scene(vbox);

        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

