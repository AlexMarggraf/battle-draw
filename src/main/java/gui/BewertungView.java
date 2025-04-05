/**
 * The BewertungView class handles the rating interface where players evaluate drawings.
 * It displays the artwork to be rated and provides voting controls.
 *
 * @author Simon Zeugin
 */
package gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.io.IOException;


public class BewertungView {

    private final VBox view;
    private static final ImageView imageView = new ImageView();
    private static int vote = 0;
    private static boolean next = false;
    private static boolean isOwnPicture;

    /**
     * Constructs a new BewertungView with rating components.
     * Initializes the image display, voting controls and navigation buttons.
     *
     * @throws IOException if there's an error loading image resources
     */
    public BewertungView() throws IOException {
        setVoteZero();
        setNextFalse();

        ComboBox<Integer> voteBox = createVoteBox();
        Button nextBtn = createNextBtn();
        Button sendVoteBtn = createSendVoteButton();

        Label label = new Label("Bewertung");

        HBox controls = new HBox(10, label);

        VBox view2 = new VBox(10, controls, imageView);
        if (isOwnPicture) {
            view = new VBox(10, view2, nextBtn);
            view.setPadding(new javafx.geometry.Insets(50));
        } else {
            view = new VBox(10, view2, voteBox, sendVoteBtn);
            view.setPadding(new javafx.geometry.Insets(50));
        }
    }

    /**
     * Sets the image to be displayed for rating.
     * Converts the BufferedImage to JavaFX Image and configures the ImageView.
     *
     * @param bufferedImage the image to be displayed
     */
    public static void setImage(BufferedImage bufferedImage) {
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imageView.setImage(image);
        imageView.setFitHeight(GameView.getCanvasHeight());
        imageView.setFitWidth(GameView.getCanvasWidth());
    }

    /**
     * Creates and configures the rating combo box.
     * Provides options for rating the displayed artwork (1-5).
     *
     * @return the configured ComboBox for rating selection
     */
    private ComboBox<Integer> createVoteBox() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.setAccessibleText("Bewertung");
        combo.getItems().addAll(1, 2, 3, 4, 5);
        combo.setOnAction(e -> vote = combo.getValue());
        return combo;
    }

    /**
     * Creates the "Next" button for lobby leaders to proceed.
     * Automatically sets the next indicator when created.
     * (for the player whose picture is voted on to get to the next round.)
     *
     *
     * @return the configured Button for navigation
     */
    private Button createNextBtn() {
        Button btn = new Button("Next");
        next = true;
        btn.setOnAction(e -> {
            GUIApp.addUserInputQueue("/readyToVote");
            btn.setDisable(true);
        });
        return btn;
    }

    private Button createSendVoteButton(){
        Button btn = new Button("send Vote");
        btn.setOnAction( e -> {
            GUIApp.addUserInputQueue("/sendVote");
            btn.setDisable(true);
        });
        return btn;
    }

    public static int getVote() {
        return vote;
    }

    public static void setVoteZero() {
        vote = 0;
    }

    public static void setNextFalse() {
        next = false;
    }

    public static Boolean getNextIndicator() {
        return next;
    }

    public Parent getView() {
        return view;
    }

    public static void setIsOwnPicture(String ownUsername, String ownerOfPictureUsername) {
        isOwnPicture = ownUsername.equals(ownerOfPictureUsername);
    }
}