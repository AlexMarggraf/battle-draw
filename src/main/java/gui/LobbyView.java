/**
 * The LobbyView class represents the game lobby interface where players wait before starting a game.
 * It displays connected players and provides a ready button to indicate player readiness.
 *
 * @author Simon Zeugin
 */

package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Map;


public class LobbyView {
    private final VBox view;
    private VBox vboxUsers;
    public static boolean ready = false;
    public static int rounds = 3;
    public static int timer = 45;

    /**
     * Constructs a new LobbyView with player listing and ready functionality.
     * Initializes the UI components and sets up a timeline to periodically update the player list.
     */
    public LobbyView() {
        this.vboxUsers = new VBox(10);
        Button readyBtn = createReadyButton();
        Button leaderBtn = createLeaderButton();
        Button leaveBtn = createLeaveButton();
        Button listBtn = creatListPlayerButton();
        Button changeUsername = HomeView.createChangeUsername();
        ComboBox<Integer> roundsBox = createRoundsCombo();
        ComboBox<Integer> timerBox = createTimerCombo();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> createLobbyListing())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();


        view = new VBox(20, new Label("Lobby"), vboxUsers, leaveBtn, listBtn, changeUsername);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));
        if (GUIApp.getLobbyLeader()) {
            view.getChildren().add(leaderBtn);
            view.getChildren().add(roundsBox);
            view.getChildren().add(timerBox);
        }
    }

    /**
     * Updates and displays the current list of players in the lobby.
     * Clears the existing player list and repopulates it with current player data.
     */
    private void createLobbyListing() {
        Map<String, Double> players = GUIApp.getPlayers();
        vboxUsers.getChildren().clear();
        for (String username : players.keySet()) {
            Label label = new Label(username);
            label.getStyleClass().add("leaderboard-bubble");
            vboxUsers.getChildren().add(label);
        }
    }

    /**
     * Creates and configures the ready button that players use to indicate their readiness.
     * When pressed, sets the ready status to true.
     *
     * @return the configured Button instance for indicating readiness
     */
    public Button createReadyButton() {
        Button btn = new Button("Ready?");
        btn.setOnAction(event -> {
            ready = true;
        });
        return btn;
    }

    /**
     * Creates and configures the Force start button that the lobby leader can use.
     * When pressed, the game starts.
     *
     * @return the configured Button instance for starting the Game
     */

    public Button createLeaderButton() {
        Button btn = new Button("Force start");
        btn.setOnAction(event -> {
            ready = true;
            GUIApp.addClickedInput("/startGame");
        });
        return btn;
    }


    /**
     * Creates and configures the "Leave Lobby" button.
     * When pressed, this button will leave the current lobby (functionality pending).
     *
     * @return the configured Button instance for leaving the lobby
     */
    public Button createLeaveButton() {
        Button btn = new Button("Leave Lobby");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/leaveLobby");
            GUIApp.showHomeView();
        });
        return btn;
    }

    /**
     * Creates a ComboBox for selecting the number of rounds in the game.
     *
     * @return the configured ComboBox with round options
     */
    private ComboBox<Integer> createRoundsCombo() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.getItems().addAll(1, 2, 3, 5, 8, 100);
        combo.setValue(3);
        rounds = 3;
        combo.setOnAction(e -> rounds = combo.getValue());
        return combo;
    }

    /**
     * Creates a ComboBox for selecting the game timer duration.
     *
     * @return the configured ComboBox with timer options
     */
    private ComboBox<Integer> createTimerCombo() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.getItems().addAll(10, 30, 45, 60, 90);
        combo.setValue(45);
        timer = 45;
        combo.setOnAction(e -> timer = combo.getValue());
        return combo;
    }

    public Button creatListPlayerButton() {
        Button btn = new Button("List Players");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/listPlayers");
        });
        return btn;
    }

    public static void setReadyFalse() {
        ready = false;
    }

    public static boolean getReadyLobby() {
        return ready;
    }

    public Parent getView() {
        return view;
    }

    public static int getRounds() {
        return rounds;
    }

    public static int getTimer() {
        return timer;
    }
}