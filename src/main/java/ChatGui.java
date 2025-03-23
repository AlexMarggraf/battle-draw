import java.util.LinkedList;
import java.util.Queue;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ChatGui extends Application {

    private static ChatGui chatGui;
    private volatile Queue<String> userInputQueue;
    private volatile Queue<String> printMessageQueue;

    public static ChatGui getChatGui(){
        return chatGui;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public String getUserInput() {
        return userInputQueue.poll();
    }

    public void queuePrintMessage(String message) {
        printMessageQueue.add(message);
    }

    @Override
    public void start(Stage primaryStage) {
        chatGui = this;
        userInputQueue = new LinkedList<>();
        printMessageQueue = new LinkedList<>();

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        TextField messageField = new TextField();

        VBox.setVgrow(chatArea, Priority.ALWAYS);

        VBox vbox = new VBox(10);
        vbox.setPrefSize(300, 400);

        vbox.getChildren().addAll(chatArea, messageField);

        messageField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {

                String message = messageField.getText();
                if (!message.trim().isEmpty()) {
                    userInputQueue.add(message);
                    chatArea.appendText("You: " + message + "\n");
                    messageField.clear();
                }
                event.consume();
            }
        });

        Timeline messageCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            String message = printMessageQueue.poll();
            if (message != null) {
                chatArea.appendText(message + "\n");
            }
        }));
        messageCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        messageCheckTimeline.play();

        Scene scene = new Scene(vbox);

        primaryStage.setTitle("Chat GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}