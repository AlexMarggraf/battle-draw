/**
 * The HomeView class represents the main menu view of the Battle Draw application.
 * It provides options for creating or joining game lobbies and accessing tutorials.
 * This view serves as the entry point for players to start their game experience.
 *
 * @author Simon Zeugin
 */

package gui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static gui.GUIApp.createDevButtons;
import static gui.GUIApp.print;

public class HomeView {

    private final VBox view;

    /**
     * Constructs a new HomeView with all necessary UI components.
     * Initializes the main menu buttons and sets up the layout.
     */
    public HomeView() {
        Button joinButton = createJoinButton();
        Button createLobbyButton = createLobbyButton();
        Button tutorialButton = createTutorialButton();
        HBox hboxScene = createDevButtons();
        view = new VBox(20, new Label("Willkommen bei Battle Draw!"), new Label("Home"), hboxScene, createLobbyButton, joinButton, tutorialButton);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));
    }

    /**
     * Creates and configures the "Create Lobby" button.
     * When pressed, this button initiates lobby creation and switches to the lobby view.
     *
     * @return the configured Button instance for creating a lobby
     */
    public static Button createLobbyButton() {
        Button btn = new Button("Create Lobby");
        btn.setOnAction(event -> {
            GUIApp.addUserInputQueue("/createLobby");
            GUIApp.showLobbyView();
        });
        return btn;
    }

    /**
     * Creates and configures the "Join Lobby" button.
     * When pressed, this button opens a popup dialog for entering a lobby name
     * and initiates the lobby joining process.
     *
     * @return the configured Button instance for joining a lobby
     */
    public static Button createJoinButton() {
        Button btn = new Button("Join Lobby");

        btn.setOnAction(event -> {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Enter Lobby name");

            TextField usernameField = new TextField();
            usernameField.setPromptText("Lobby name");

            Button joinButton = new Button("Join");
            joinButton.setOnAction(e -> {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    GUIApp.addUserInputQueue("/joinLobby Lobby_von_" + username);
                    popup.close();
                } else {
                    print("Username cannot be empty");
                }
            });

            VBox layout = new VBox(10);
            layout.getChildren().addAll(usernameField, joinButton);

            Scene scene = new Scene(layout, 300, 150);
            popup.setScene(scene);

            popup.showAndWait();
        });

        return btn;
    }

    /**
     * Creates and configures the "Tutorial" button.
     * Currently a placeholder for future tutorial functionality.
     *
     * @return the configured Button instance for accessing tutorials
     */
    public static Button createTutorialButton() {
        Button btn = new Button("Tutorial");
        //todo later
        return btn;
    }

    public Parent getView() {
        return view;
    }
}