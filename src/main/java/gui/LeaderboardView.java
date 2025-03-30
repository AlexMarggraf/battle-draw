/**
 * The LeaderboardView class displays the current game rankings and scores.
 * It shows player rankings and provides a ready button for proceeding to the next round.
 *
 * @author Simon Zeugin
 */

package gui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Map;

import static gui.GUIApp.*;


public class LeaderboardView {

    private final VBox view;
    private static final VBox vboxLeaderboard = new VBox(10);
    private static boolean ready = false;

    /**
     * Constructs a new LeaderboardView with all necessary UI components.
     * Initializes the leaderboard display, development buttons and ready button.
     */
    public LeaderboardView() {
        HBox hboxScene = createDevButtons();
        Button rdy = createRdyBtn();
        updateLeaderboard();

        view = new VBox(30, new Label("Leaderboard"), hboxScene, vboxLeaderboard, rdy);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));
    }

    /**
     * Updates and displays the current leaderboard rankings.
     * Clears the existing leaderboard and repopulates it with current player data,
     * showing each player's rank, name and score in descending order.
     */
    static void updateLeaderboard() {
        Map<String, Integer> players = GUIApp.getPlayers();
        vboxLeaderboard.getChildren().clear();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : players.entrySet()) {
            vboxLeaderboard.getChildren().add(new Text(rank + ". " + entry.getKey() + " with " + entry.getValue() + " points"));
            rank++;
        }
    }

    /**
     * Creates and configures the ready button for proceeding to the next round.
     * When pressed, sets the ready status to true indicating the player is prepared to continue.
     *
     * @return the configured Button instance for indicating readiness
     */
    public static Button createRdyBtn() {
        Button btn = new Button("Ready for next round?");
        btn.setOnAction(event -> {
            ready = true;
        });
        return btn;
    }

    public static boolean getReady() {
        return ready;
    }

    public static void setReadyFalse() {
        ready = false;
    }

    public Parent getView() {
        return view;
    }
}