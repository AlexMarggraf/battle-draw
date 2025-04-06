/**
 * not finished and not used for now
 *
 * @author Simon Zeugin
 */
package gui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginSignupView {

    private final VBox view;
    static String usernameText;
    String passwordText;

    /**
     * Constructs the login/signup view with a labeled input form containing
     * username and password fields, along with login and signup buttons.
     */

    public LoginSignupView() {

        TextField username = createUsernameField();
        PasswordField password = createPasswordField();

        Button loginBtn = createLoginButton(username, password);
        Button signupBtn = createSignUpButton(username, password);

        HBox hbox = new HBox(10, username, password, loginBtn, signupBtn);

        view = new VBox(20, new Label("LogIn/Signup"), hbox);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));

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
        field.setText(System.getProperty("user.name") + (int) (Math.random() * 100));
        usernameText = field.getText();
        field.textProperty().addListener((observable, oldValue, newValue) -> usernameText = newValue);

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
            GUIApp.print(usernameText);
            GUIApp.print(passwordText);


            //tmp alarm wee woo
            GUIApp.addUserInputQueue("li");
            GUIApp.addUserInputQueue(usernameText);
            GUIApp.addUserInputQueue(passwordText);

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
        Button btn = new Button("SignUp?");
        btn.setOnAction(event -> {
            GUIApp.print(usernameText);
            GUIApp.print(passwordText);

            //tmp alarm wee woo
            GUIApp.addUserInputQueue("su");
            GUIApp.addUserInputQueue("n");
            GUIApp.addUserInputQueue(usernameText);
            GUIApp.addUserInputQueue(passwordText);

            username.clear();
            password.clear();

        });

        return btn;
    }

    public static String getUsernameText() {
        return usernameText;
    }

    public Parent getView() {
        return view;
    }
}