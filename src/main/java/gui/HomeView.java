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
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;


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
        Button lobbyListButton = createListLobby();
        Button lobbyPlayersButton = createListPlayers();
        Button changeUsernameButton = createChangeUsername();
        Button myHighscoreButton = createMyHighscoreButton();
        ImageView logo = createLogo();

        view = new VBox(20, new Label("Welcome to"), logo, createLobbyButton, joinButton, lobbyListButton, lobbyPlayersButton, changeUsernameButton, myHighscoreButton, tutorialButton);
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
            GUIApp.addClickedInput("/createLobby");
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
                    GUIApp.addClickedInput("/joinLobby Lobby_von_" + username);
                    popup.close();
                } else {
                    print("Username cannot be empty");
                }
            });

            usernameField.setOnAction(joinButton.getOnAction());

            VBox layout = new VBox(10);
            layout.getChildren().addAll(usernameField, joinButton);

            Scene scene = new Scene(layout, 300, 150);
            scene.getStylesheets().add(GUIApp.class.getResource("/style.css").toExternalForm());
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

    /**
     * Creates and configures the "List Lobbys" button.
     * When pressed, this button will list all available lobbies (functionality pending).
     *
     * @return the configured Button instance for listing lobbies
     */
    public Button createListLobby() {
        Button btn = new Button("List Lobbys");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/games");
        });
        return btn;
    }

    /**
     * Creates and configures the "Change username" button.
     * When pressed, this button opens a popup dialog for entering a new username
     * and initiates the process.
     *
     * @return the configured Button instance for changing the username
     */
    public static Button createChangeUsername() {
        Button btn = new Button("Change Username");

        btn.setOnAction(event -> {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Enter new Username");

            TextField usernameField = new TextField();
            usernameField.setPromptText("new username");

            Button confirmButton = new Button("Confirm");
            confirmButton.setOnAction(e -> {
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    GUIApp.addClickedInput("/nick " + username);
                    popup.close();
                } else {
                    print("Username cannot be empty");
                }
            });

            usernameField.setOnAction(confirmButton.getOnAction());

            VBox layout = new VBox(10);
            layout.getChildren().addAll(usernameField, confirmButton);

            Scene scene = new Scene(layout, 300, 150);
            scene.getStylesheets().add(GUIApp.class.getResource("/style.css").toExternalForm());
            popup.setScene(scene);

            popup.showAndWait();
        });

        return btn;
    }

    /**
     * Creates and configures the "My Highscore" Button
     */
    public static Button createMyHighscoreButton() {
        Button btn = new Button("My Highscore");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/myHighscore");
        });
        return btn;
    }


    /**
     * Creates and configures the "List Online Players" button.
     * When pressed, this button will list all online players (functionality pending).
     *
     * @return the configured Button instance for listing online players
     */
    public Button createListPlayers() {
        Button btn = new Button("List Online Players");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/online");
        });
        return btn;
    }

    public ImageView createLogo() {
        Image image = new Image("/logo.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
        return imageView;
    }


    public Parent getView() {
        return view;
    }
}