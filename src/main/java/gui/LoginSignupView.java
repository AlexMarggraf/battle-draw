/**
 * not finished and not used for now
 *
 * @author Simon Zeugin
 */
package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginSignupView {

    private final VBox view;
    static String usernameText;
    String passwordText;
    private static String errorText;

    /**
     * Constructs the login/signup view with a labeled input form containing
     * username and password fields, along with login and signup buttons.
     */

    public LoginSignupView() {

        TextField username = createUsernameField();
        PasswordField password = createPasswordField();
        Text textPrompted = new Text(errorText);
        textPrompted.setFill(Color.RED);

        Button loginBtn = createLoginButton(username, password);
        Button signupBtn = createSignUpButton(username, password);
        Button tutorialBtn = createTutorialButton();


        VBox inputBox = new VBox(10, username, password);
        inputBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(20, loginBtn, signupBtn, tutorialBtn);
        buttonBox.setAlignment(Pos.CENTER);

        view = new VBox(20, new Label("Please Login or Signup"), textPrompted, inputBox, buttonBox);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(50));

    }

    /**
     * Creates and returns a pre-filled TextField for entering the username.
     * Adds a listener to update the static {@code usernameText} whenever the field changes.
     *
     * @return the configured TextField for username input
     */

    public TextField createUsernameField() {
        TextField field = new TextField();
        field.setPromptText("Username");
        String username = GUIApp.getUsername();
        if (username != null) {
            field.setText(username);
        } else {
            field.setText(System.getProperty("user.name") + (int) (Math.random() * 100));
        }
        usernameText = field.getText();
        field.textProperty().addListener((observable, oldValue, newValue) -> usernameText = newValue);
        field.setMaxWidth(250);
        return field;
    }

    /**
     * Creates and returns a PasswordField for entering the user's password.
     * Adds a listener to update the {@code passwordText} whenever the field changes.
     *
     * @return the configured PasswordField for password input
     */

    public PasswordField createPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("Password");
        field.setMaxWidth(250);

        field.textProperty().addListener((observable, oldValue, newValue) -> passwordText = newValue);

        return field;
    }

    /**
     * Creates and returns a Button configured to handle user login.
     * Adds entered credentials to the input queue and clears the fields after submission.
     *
     * @param username the TextField containing the username
     * @param password the TextField containing the password
     * @return the configured login Button
     */


    public Button createLoginButton(TextField username, TextField password) {
        Button btn = new Button("Login?");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/setUsername " + usernameText);
            GUIApp.addClickedInput("/setPassword " + passwordText);
            GUIApp.addClickedInput("/logIn " + usernameText + " " + passwordText);

            username.clear();
            password.clear();

        });
        return btn;
    }

    /**
     * Creates and returns a Button configured to handle user signup.
     * Adds entered credentials to the input queue and clears the fields after submission.
     *
     * @param username the TextField containing the username
     * @param password the TextField containing the password
     * @return the configured signup Button
     */

    public Button createSignUpButton(TextField username, TextField password) {
        Button btn = new Button("Signup?");
        btn.setOnAction(event -> {
            GUIApp.addClickedInput("/setUsername " + usernameText);
            GUIApp.addClickedInput("/setPassword " + passwordText);
            GUIApp.addClickedInput("/signUp " + usernameText + " " + passwordText);

            username.clear();
            password.clear();

        });

        return btn;
    }

    /**
     * Creates and configures the "Tutorial" button.
     * When clicked a popup opens and explains the ins and outs of the game.
     *
     * @return the configured Button instance for accessing tutorials
     */
    public static Button createTutorialButton() {
        Button btn = new Button("Tutorial");
        btn.setOnAction(event -> {
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Tutorial");

            TextArea manualText = new TextArea();
            manualText.setEditable(false);
            manualText.setWrapText(true);
            manualText.setText(
                    "Welcome to the Game Battle Draw!\n\n"
                    + "We are pleased that you chose to play our game!\n"
                    + "If you've never played the game you have to create an account first."
                    + "Fill out the username and password, but you cannot use a space in either and click on \"Sign Up?\"."
                    + "Otherwise just use your username and password from the last time you played the game."
                    + "Then you get to see our home view. There you have different options. You can:\n"
                    + "- Create a lobby\n"
                    + "- Join a lobby\n"
                    + "- List all lobbies\n"
                    + "- List all online players\n"
                    + "- Change your username\n"
                    + "- check out your Highscore\n"
                    + "If you create a lobby you are the host. There you can set the number of rounds to play our game"
                    + "and the time to draw for a round or leave the lobby."
                    + "On the left side you can see all players which joined your lobby."
                    + "When all the players joined you can click on \"Start\". Then you can start to draw a picture to the theme."
                    + "You have different colors, different pen widths and if you're not happy you can clear the drawing."
                    + "After the timer runs out you take turns to vote for the pictures of the other players between 1 and 5 stars."
                    + "You cannot vote for yourself. But pay attention because if you've not voted in the 15 seconds your opponent gets 5 stars!"
                    + "At the end of a round you see the current leaderboard and you can click on \"Next Round\"."
                    + "After the game ends you have the choice to play again or leave the lobby."
                    + "Now if you want to join a lobby you can first list all lobbies and choose which to join."
                    + "You can only join a lobby which is open. You cannot join ongoing or ended lobbies."
                    + "When you click on \"Join Lobby\" a popup opens and you can enter just the username without \"Lobby_of_\"."
                    + "If you're not the host you can sit back an relax until the game starts."
                    + "After a game you can return to home by leaving to lobby and click on \"my Highscore\" and try to beat it."
                    + "You can also change your username at any time during a game if you want to experiment."
                    + "But pay attention! Your username is unique. If someone has taken your previous name you cannot switch back."
                    + "Now all that is left is to wish you luck playing our game. We wish you well!"
            );

            Button closeButton = new Button("Close");
            closeButton.setMaxWidth(Double.MAX_VALUE);
            closeButton.setOnAction(e -> popup.close());

            VBox layout = new VBox();
            layout.setPadding(new Insets(10));
            layout.setSpacing(10);
            VBox.setVgrow(manualText, Priority.ALWAYS);
            layout.getChildren().addAll(manualText, closeButton);

            Scene scene = new Scene(layout, 600, 500);
            scene.getStylesheets().add(GUIApp.class.getResource("/style.css").toExternalForm());
            popup.setScene(scene);
            popup.showAndWait();
        });
        return btn;
    }

    public static String getUsernameText() {
        return usernameText;
    }

    public Parent getView() {
        return view;
    }

    public static void setErrorText(String error) {
        errorText = error;
    }
}