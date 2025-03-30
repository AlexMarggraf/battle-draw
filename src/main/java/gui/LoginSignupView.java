/** not finished and not used for now
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

import static gui.GUIApp.createDevButtons;

public class LoginSignupView {

    private final VBox view;
    static String usernameText;
    String passwordText;

    public LoginSignupView() {

        TextField username = createUsernameField();
        PasswordField password = createPasswordField();

        Button loginBtn = createLoginButton(username, password);
        Button signupBtn = createSignUpButton(username, password);

        HBox hboxScene = createDevButtons();
        HBox hbox = new HBox(10, username, password, loginBtn, signupBtn);

        view = new VBox(20, new Label("LogIn/Signup"), hboxScene, hbox);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new javafx.geometry.Insets(50));

    }

    public TextField createUsernameField() {
        TextField field = new TextField();
        field.setPromptText("Username");
        field.setText(System.getProperty("user.name") + (int) (Math.random() * 100));
        usernameText = field.getText();
        field.textProperty().addListener((observable, oldValue, newValue) -> usernameText = newValue);

        return field;
    }

    public PasswordField createPasswordField() {
        PasswordField field = new PasswordField();
        field.setPromptText("Password");

        field.textProperty().addListener((observable, oldValue, newValue) -> passwordText = newValue);

        return field;
    }


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