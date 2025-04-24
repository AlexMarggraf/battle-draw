/**
 * The EndingView class represents the final screen displayed at the end of a game session.
 * It provides options for players to either play again or leave the game.
 *
 * @author Simon Zeugin
 */
package gui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class EndingView {

    private final VBox view;
    public static boolean again = false;

    /**
     * Constructs a new EndingView with game conclusion options.
     * Initializes the UI components including action buttons for replay or exit.
     */
    public EndingView() {
        Button againBtn = createAgainBtn();
        Button leaveBtn = createLeaveBtn();

        view = new VBox(20, new Label("Ending"), leaveBtn, againBtn);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));
    }

    /**
     * Creates and configures the "Again?" button for replaying the game.
     * When pressed, sets the again flag to true indicating the player wants to play again.
     *
     * @return the configured Button instance for replay option
     */
    public Button createAgainBtn() {
        Button btn = new Button("Again?");
        btn.setOnAction(event -> {
            GUIApp.showLobbyView();
            again = true;
        });
        return btn;
    }

    /**
     * Creates and configures the "Leave" button for exiting the game session.
     * When pressed, sends a leave command and returns to the home view.
     *
     * @return the configured Button instance for exit option
     */
    public Button createLeaveBtn() {
        Button btn = new Button("Leave");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/leaveLobby");
            GUIApp.showHomeView();
        });
        return btn;
    }

    public static boolean getAgainEnding() {
        return again;
    }

    public static void setAgainEnding(boolean bool) {
        again = bool;
    }

    public Parent getView() {
        return view;
    }
}