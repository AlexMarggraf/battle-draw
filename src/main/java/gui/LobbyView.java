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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Map;


public class LobbyView {
    private final VBox view;
    private VBox vboxUsers;
    public static boolean ready = false;

    /**
     * Constructs a new LobbyView with player listing and ready functionality.
     * Initializes the UI components and sets up a timeline to periodically update the player list.
     */
    public LobbyView() {
        this.vboxUsers = new VBox(10);
        HBox devBtns = GUIApp.createDevButtons();
        Button readyBtn = createReadyButton();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> createLobbyListing())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        view = new VBox(20, new Label("Lobby"), devBtns, vboxUsers, readyBtn);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));
    }

    /**
     * Updates and displays the current list of players in the lobby.
     * Clears the existing player list and repopulates it with current player data.
     */
    private void createLobbyListing() {
        Map<String, Integer> players = GUIApp.getPlayers();
        vboxUsers.getChildren().clear();
        for (String username : players.keySet()) {
            Label label = new Label(username);
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

    public static void setReadyFalse() {
        ready = false;
    }

    public static boolean getReadyLobby() {
        return ready;
    }

    public Parent getView() {
        return view;
    }
}