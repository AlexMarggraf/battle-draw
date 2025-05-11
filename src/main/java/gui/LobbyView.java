/**
 * The LobbyView class represents the game lobby interface where players wait before starting a game.
 * It displays connected players and provides a ready button to indicate player readiness.
 *
 * @author Simon Zeugin
 */

package gui;

import animatefx.animation.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Map;


public class LobbyView {
    private final VBox view;
    private VBox vboxUsers;
    public static boolean ready = false;
    public static int rounds = 3;
    public static int timer = 45;
    private final ArrayList<String> shownUsers = new ArrayList<>();
    private final VBox[] playerColumns = {new VBox(10), new VBox(10), new VBox(10)};
    private final HBox playerBox = new HBox(60, playerColumns);


    /**
     * Constructs a new LobbyView with player listing and ready functionality.
     * Initializes the UI components and sets up a timeline to periodically update the player list.
     */
    public LobbyView() {
        this.vboxUsers = new VBox(10);

        Button leaderBtn = createLeaderButton();
        Button leaveBtn = createLeaveButton();
        Button listBtn = creatListPlayerButton();
        Button changeUsername = HomeView.createChangeUsername();

        GUIApp.addClickSound(leaderBtn);
        GUIApp.addClickSound(leaveBtn);
        GUIApp.addClickSound(listBtn);
        GUIApp.addClickSound(changeUsername);

        ComboBox<Integer> roundsBox = createRoundsCombo();
        ComboBox<Integer> timerBox = createTimerCombo();

        VBox roundsBoxContainer = new VBox(5, new Label("Rounds:"), roundsBox);
        VBox timerBoxContainer = new VBox(5, new Label("Timer:"), timerBox);
        HBox settingsRow = new HBox(40, roundsBoxContainer, timerBoxContainer);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    createLobbyListing();
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        playerBox.setAlignment(Pos.CENTER);


        view = new VBox(25, new Label("Lobby"), playerBox, leaveBtn, listBtn, changeUsername);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));

        if (GUIApp.getLobbyLeader()) {
            view.getChildren().add(leaderBtn);
            settingsRow.setAlignment(Pos.CENTER);

            view.getChildren().add(settingsRow);
        }
    }

    /**
     * Updates the lobby view by adding new players with animation and removing absent ones (only GUI relevant not for the Server).
     */

    private void createLobbyListing() {
        Map<String, Double> lobbyPlayers = GUIApp.getPlayers();

        shownUsers.removeIf(name -> {
            if (!lobbyPlayers.containsKey(name)) {
                for (VBox column : playerColumns) {
                    column.getChildren().removeIf(node ->
                            node instanceof Label && ((Label) node).getText().equals(name)
                    );
                }
                return true;
            }
            return false;
        });

        int index = 0;
        for (String username : lobbyPlayers.keySet()) {
            if (!shownUsers.contains(username)) {
                Label label = new Label(username);
                label.getStyleClass().add("leaderboard-bubble");

                int columnIndex = index / 4;
                if (columnIndex < playerColumns.length) {
                    playerColumns[columnIndex].getChildren().add(label);
                }

                shownUsers.add(username);
                new BounceIn(label).setSpeed(1.5).play();
            }
            index++;
        }
    }

    /**
     * Creates and configures the Force start button that the lobby leader can use.
     * When pressed, the game starts.
     *
     * @return the configured Button instance for starting the Game
     */

    public Button createLeaderButton() {
        Button btn = new Button("Start");
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
        combo.getItems().addAll(15, 30, 45, 60, 90, 120, 180, 300);
        combo.setValue(45);
        timer = 45;
        combo.setOnAction(e -> timer = combo.getValue());
        return combo;
    }

    /**
     * Creates a ListPlayer Button that calls /listPlayers
     *
     * @return the configured ListPlayer Button
     */
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