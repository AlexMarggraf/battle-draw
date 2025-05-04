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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class LeaderboardView {

    private final VBox view;
    private static final VBox vboxLeaderboard = new VBox(10);
    private static boolean ready = false;

    /**
     * Constructs a new LeaderboardView with all necessary UI components.
     * Initializes the leaderboard display, development buttons and ready button.
     */
    public LeaderboardView() {
        setReadyFalse();
        Button rdy = createRdyBtn();
        updateLeaderboard();

        view = new VBox(30, new Label("Leaderboard"), vboxLeaderboard);
        if(GUIApp.getLobbyLeader()){
            view.getChildren().add(rdy);
        }
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));
    }

    /**
     * Updates and displays the current leaderboard rankings.
     * Clears the existing leaderboard and repopulates it with current player data,
     * showing each player's rank, name and score in descending order.
     */
    static void updateLeaderboard() {
        Map<String, Double> players = GUIApp.getPlayers();
        vboxLeaderboard.getChildren().clear();

        HashMap<String, Double> playersSorted = players.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        int rank = 0;
        double lastPoints = -1;
        int numberOfSamePoints = 1;
        for (Map.Entry<String, Double> player : playersSorted.entrySet()) {
            if (lastPoints != player.getValue()) {
                rank += numberOfSamePoints;
                numberOfSamePoints = 1;
            } else {
                numberOfSamePoints++;
            }
            String leaderboardEntry = String.format("%d. %s with %.2f points", rank, player.getKey(), player.getValue());
            vboxLeaderboard.getChildren().add(new Text(leaderboardEntry));
            lastPoints = player.getValue();
        }

    }

    /**
     * Creates and configures the ready button for proceeding to the next round.
     * When pressed, sets the ready status to true indicating the player is prepared to continue.
     *
     * @return the configured Button instance for indicating readiness
     */
    public static Button createRdyBtn() {
        Button btn = new Button("Force next round (leader)");
        btn.setOnAction(event -> {
            ready = true;
            GUIApp.addClickedInput("/nextRound");

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