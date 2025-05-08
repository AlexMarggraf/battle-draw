/**
 * The LeaderboardView class displays the current game rankings and scores.
 * It shows player rankings and provides a ready button for proceeding to the next round.
 *
 * @author Simon Zeugin
 */

package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


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
        updateLeaderboard();

        Button rdy = createRdyBtn();
        Button changeUsername = HomeView.createChangeUsername();

        GUIApp.addClickSound(rdy);
        GUIApp.addClickSound(changeUsername);

        vboxLeaderboard.setAlignment(Pos.CENTER);

        view = new VBox(30, new Label("Leaderboard"), vboxLeaderboard, changeUsername);
        if (GUIApp.getLobbyLeader()) {
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
            double points = player.getValue();
            if (Double.compare(lastPoints, points) != 0) {
                rank += numberOfSamePoints;
                numberOfSamePoints = 1;
            } else {
                numberOfSamePoints++;
            }

            String emoji = switch (rank) {
                case 1 -> "\uD83E\uDD47";
                case 2 -> "\uD83E\uDD48";
                case 3 -> "\uD83E\uDD49";
                default -> "";
            };

            String leaderboardEntry = String.format("%s %d. %s with %.2f points",
                    emoji, rank, player.getKey(), points);

            Label bubble = new Label(leaderboardEntry);
            bubble.getStyleClass().add("leaderboard-bubble");

            switch (rank) {
                case 1 -> bubble.getStyleClass().add("first-place");
                case 2 -> bubble.getStyleClass().add("second-place");
                case 3 -> bubble.getStyleClass().add("third-place");
            }

            VBox.setMargin(bubble, new Insets(5));
            vboxLeaderboard.getChildren().add(bubble);
            lastPoints = points;
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

    public static void setReadyFalse() {
        ready = false;
    }

    public Parent getView() {
        return view;
    }
}