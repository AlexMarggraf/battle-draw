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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;


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


        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        initializeRootLayout();

        primaryStage.setTitle("Battle Draw");
        Scene scene = new Scene(rootLayout, 1280, 960 );
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setScene(scene);
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
        chatPanel.setPrefWidth(300);
        rootLayout.setRight(chatPanel);
//        HBox devButtons = createDevButtons();
//        rootLayout.setTop(devButtons);
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
        vbox.setPrefSize(250, 300);
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
        Button bewertungButton = GUIApp.createReviewButton();
        Button homeButton = GUIApp.createHomeButton();
        Button loSiButton = GUIApp.createLoSiButton();
        Button lobbyBtn = GUIApp.createLobbyButton();
        Button endingBtn = GUIApp.createEndingButton();
        Button boardBtn = GUIApp.createLeaderboardButton();

        return new HBox(10, new Label("DevButtons: "), loSiButton, homeButton, lobbyBtn, gameButton, bewertungButton, boardBtn, endingBtn);
    }

    /**
     * Adds a click sound to the given button when it is pressed.
     *
     * @param button the button to attach the sound to
     */

    public static void addClickSound(Button button) {
        URL soundUrl = GUIApp.class.getResource("/buttonClick.mp3");
        if (soundUrl == null) {
            System.err.println("Sound file not found!");
            return;
        }

        EventHandler<ActionEvent> originalHandler = button.getOnAction();

        button.setOnAction(e -> {
            Media sound = new Media(soundUrl.toString());
            MediaPlayer player = new MediaPlayer(sound);
            player.play();

            if (originalHandler != null) {
                originalHandler.handle(e);
            }
        });
    }
    /**
     * Plays a short click sound using MediaPlayer.
     */
    public static void playClickSound() {
        URL soundUrl = GUIApp.class.getResource("/buttonClick.mp3");
        if (soundUrl == null) {
            System.err.println("Sound file not found!");
            return;
        }

        Media sound = new Media(soundUrl.toString());
        MediaPlayer player = new MediaPlayer(sound);
        player.play();
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
    public static void showReviewView() {
        ReviewView.resetVote();
        Platform.runLater(() -> {
            try {
                rootLayout.setCenter(new ReviewView().getView());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void setLobbyLeader(Boolean bool) {
        lobbyLeader = bool;
    }

    //LeaderboardView Methoden

    public static void showLeaderboardView() {
        LeaderboardView.setReadyFalse();
        Platform.runLater(() ->
                rootLayout.setCenter(new LeaderboardView().getView())
        );
    }

    //EndingView Methoden

    public static void showEndingView() {
        EndingView.setAgainEnding(false);
        Platform.runLater(() ->
                rootLayout.setCenter(new EndingView().getView())
        );
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

    static Button createReviewButton() {
        Button button = new Button("Bewertung");
        button.setOnAction(e -> GUIApp.showReviewView());
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

    public static Double getPoints(String username) {
        return players.get(username);
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

    public String getUserInput() {
        return userInputQueue.poll();
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

    public static void setTime(int timeInSeconds) {
        time = timeInSeconds;
    }

    public static int getTime() {
        return time;
    }

    public static void setUsername(String newUsername) {
        username = newUsername;
    }

    public static String getUsername() {
        return username;
    }
}