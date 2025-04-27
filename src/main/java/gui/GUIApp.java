/**
 * The main GUI application class for Battle Draw game.
 * Handles the primary application window, chat functionality and view management.
 *
 * @author Simon Zeugin
 */

package gui;

import game.Main;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class GUIApp extends Application {

    private static GUIApp guiApp;
    private static BorderPane rootLayout;
    private static TextArea chatArea = new TextArea();
    private TextField messageField;
    private static volatile Queue<String> userInputQueue;
    private volatile Queue<String> printMessageQueue;
    private volatile Queue<String> clientMessageQueue;
    private static volatile Queue<String> clickedInputQueue;
    private static boolean lobbyLeader = false;
    private static String theme;
    private static Map<String, Double> players;
    private static int time;
    private static String username;

    /**
     * The main entry point for the JavaFX application.
     * Initializes the application window, queues and sets up the message handler.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        guiApp = this;
        userInputQueue = new LinkedList<>();
        printMessageQueue = new LinkedList<>();
        clientMessageQueue = new LinkedList<>();
        clickedInputQueue = new LinkedList<>();
        players = new HashMap<>();

        primaryStage.setResizable(false);
        initializeRootLayout();

        primaryStage.setTitle("Battle Draw");
        primaryStage.setScene(new Scene(rootLayout, 1000, 700));
        primaryStage.show();

        setupMessageHandler();
    }

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * Initializes the root layout of the application.
     * Sets up the basic BorderPane structure with chat panel on the right.
     * Optionally with devButtons.
     */
    private void initializeRootLayout() {
        rootLayout = new BorderPane();

        VBox chatPanel = createChatPanel();
        rootLayout.setRight(chatPanel);
        HBox devButtons = createDevButtons();
        rootLayout.setTop(devButtons);
    }

    /**
     * Creates and configures the chat panel component.
     * Includes a read-only chat area and a message input field.
     *
     * @return the configured VBox containing chat components
     */
    private VBox createChatPanel() {
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        messageField = new TextField();
        messageField.setPromptText("Type Message");
        messageField.setOnAction(event -> handleUserInput());

        VBox.setVgrow(chatArea, Priority.ALWAYS);

        VBox vbox = new VBox(10);
        vbox.setPrefSize(300, 400);
        vbox.getChildren().addAll(chatArea, messageField);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    /**
     * Handles user input from the message field.
     * Adds the message to the user input queue and updates the chat display.
     */
    private void handleUserInput() {
        String message = messageField.getText();
        if (!message.trim().isEmpty()) {
            userInputQueue.add(message);
            chatArea.appendText("You: " + message + "\n");
            messageField.clear();
        }
    }

    /**
     * Sets up a timeline-based message handler that periodically checks for new messages
     * in the print message queue and displays them in the chat area.
     */
    private void setupMessageHandler() {
        Timeline messageCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            String message = printMessageQueue.poll();
            if (message != null) {
                chatArea.appendText(message + "\n");
            }
        }));
        messageCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        messageCheckTimeline.play();
    }

    /**
     * Creates a horizontal box containing navigation buttons for development purposes.
     * Includes buttons for all major application views.
     *
     * @return HBox containing development navigation buttons
     */
    static HBox createDevButtons() {
        Button gameButton = GUIApp.createGameButton();
        Button bewertungButton = GUIApp.createBewertungButton();
        Button homeButton = GUIApp.createHomeButton();
        //Button loSiButton = GUIApp.createLoSiButton();
        Button lobbyBtn = GUIApp.createLobbyButton();
        Button endingBtn = GUIApp.createEndingButton();
        Button boardBtn = GUIApp.createLeaderboardButton();

        return new HBox(10, new Label("DevButtons: "), homeButton, lobbyBtn, gameButton, bewertungButton, boardBtn, endingBtn);
    }

    //HomeView Methoden
    public static void showHomeView() {
        Platform.runLater(() ->
                rootLayout.setCenter(new HomeView().getView())
        );
    }

    public static void showLobbyView() {
        LobbyView.setReadyFalse();
        Platform.runLater(() ->
                rootLayout.setCenter(new LobbyView().getView())
        );
    }

    //GameView Methoden
    public static void showGameView() {
        Platform.runLater(() ->
                rootLayout.setCenter(new GameView().getView())
        );
    }

    public static BufferedImage getImage() {
        return GameView.getCanvasAsBufferedImage();
    }

    // BewertungView Methoden
    public static void showBewertungView() {
        BewertungView.setVoteZero();
        Platform.runLater(() -> {
            try {
                rootLayout.setCenter(new BewertungView().getView());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void setBewertungImage(BufferedImage image) {
        BewertungView.setImage(image);
    }

    public static int getBewertungVote() {
        return BewertungView.getVote();
    }

    public static void setLobbyLeader(Boolean bool) {
        lobbyLeader = bool;
    }

    public static Boolean getNextIndicatorBewertung() {
        return BewertungView.getNextIndicator();
    }

    //LeaderboardView Methoden

    public static void showLeaderboardView() {
        LeaderboardView.setReadyFalse();
        Platform.runLater(() ->
                rootLayout.setCenter(new LeaderboardView().getView())
        );
    }

    public static boolean getReadyLeaderboard() {
        return LeaderboardView.getReady();
    }

    public static void updateLeaderboard(Map<String, Integer> leaderboardData) {
        LeaderboardView.updateLeaderboard();
    }

    public static void setReadyFalseLeaderboard() {
        LeaderboardView.setReadyFalse();
    }

    //EndingView Methoden

    public static void showEndingView() {
        EndingView.setAgainEnding(false);
        Platform.runLater(() ->
                rootLayout.setCenter(new EndingView().getView())
        );
    }

    public static boolean getAgainEnding() {
        return EndingView.getAgainEnding();
    }

    //LoginSignUpView Methoden
    public static void showLoginSignupView() {
        Platform.runLater(() ->
                rootLayout.setCenter(new LoginSignupView().getView())
        );
    }


    //Knöpfe der einfachheit halber
    public static Button createGameButton() {
        Button button = new Button("Canvas");
        button.setOnAction(e -> GUIApp.showGameView());
        return button;
    }

    static Button createHomeButton() {
        Button button = new Button("Home");
        button.setOnAction(e -> GUIApp.showHomeView());
        return button;
    }

    static Button createLoSiButton() {
        Button button = new Button("LoSi");
        button.setOnAction(e -> GUIApp.showLoginSignupView());
        return button;
    }

    static Button createBewertungButton() {
        Button button = new Button("Bewertung");
        button.setOnAction(e -> GUIApp.showBewertungView());
        return button;
    }

    static Button createLobbyButton() {
        Button button = new Button("Lobby");
        button.setOnAction(e -> GUIApp.showLobbyView());
        return button;
    }

    public static Button createLeaderboardButton() {
        Button button = new Button("Leaderboard");
        button.setOnAction(e -> GUIApp.showLeaderboardView());
        return button;
    }

    public static Button createEndingButton() {
        Button button = new Button("Ending");
        button.setOnAction(e -> GUIApp.showEndingView());
        return button;
    }

    public static void print(String text) {
        chatArea.appendText(text + "\n");
    }

    public static void setSetLobbyLeader(Boolean bool) {
        lobbyLeader = bool;
    }

    public static Boolean getLobbyLeader() {
        return lobbyLeader;
    }

    public static void setTheme(String s) {
        theme = s;
    }

    public static String getTheme() {
        return theme;
    }

    public static void setPlayers(Map<String, Double> map) {
        players = map;
    }

    public static Map<String, Double> getPlayers() {
        return players;
    }

    public static void addPlayer(String username, Double points) {
        players.put(username, points);
    }

    public static void removePlayer(String username) {
        players.remove(username);
    }

    public static void addPointsPlayer(String username, Double points) {
        //players.merge(username, points, Double::sum);
        if (points.isNaN()) {
            points = 0.0;
        }
        players.put(username, points);
    }


    public static void clearPlayers() {
        players.clear();
    }

    public void setReadyFalseLobby() {
        LobbyView.setReadyFalse();
    }

    public boolean getReadyLobby() {
        return LobbyView.getReadyLobby();
    }

    public String getUserInput() {
        return userInputQueue.poll();
    }

    public String getClientMessageQueue() {
        return clientMessageQueue.poll();
    }

    public static void addUserInputQueue(String input) {
        userInputQueue.add(input);
    }

    public void queuePrintMessage(String message) {
        printMessageQueue.add(message);
    }

    public static GUIApp getGuiApp() {
        return guiApp;
    }

    public String getClickedInput() {
        return clickedInputQueue.poll();
    }

    public static void addClickedInput(String input) {
        clickedInputQueue.add(input);
        LOGGER.info("added input {}", input);
    }

    public static void setTime(int timeInSeconds) { time = timeInSeconds;}

    public static int getTime() {return time;}

    public static void setUsername(String newUsername) {username = newUsername;}

    public static String getUsername() {return username;}
}