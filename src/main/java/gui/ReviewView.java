/**
 * The BewertungView class handles the rating interface where players evaluate drawings.
 * It displays the artwork to be rated and provides voting controls.
 *
 * @author Simon Zeugin
 */
package gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;

import java.awt.image.BufferedImage;
import java.io.IOException;


public class ReviewView {

    private final VBox view;
    private static final ImageView imageView = new ImageView();
    private static int vote = 1;
    private static boolean next = false;
    private static boolean isOwnPicture = true;

    /**
     * Constructs a new BewertungView with rating components.
     * Initializes the image display, voting controls and navigation buttons.
     *
     * @throws IOException if there's an error loading image resources
     */
    public ReviewView() throws IOException {
        resetVote();
        setNextFalse();

        StackPane picturePane = new StackPane(imageView);
        picturePane.setMinSize(0, 0);
        picturePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(picturePane, Priority.ALWAYS);

        imageView.setPreserveRatio(true);

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            double size = Math.min(picturePane.getWidth(), picturePane.getHeight());
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
        };

        picturePane.widthProperty().addListener(sizeListener);
        picturePane.heightProperty().addListener(sizeListener);

        Button changeUsername = HomeView.createChangeUsername();
        Button nextBtn = createNextBtn();

        GUIApp.addClickSound(nextBtn);
        GUIApp.addClickSound(changeUsername);

        HBox voteButtons = createVoteButtonsWithSend();
        voteButtons.setAlignment(Pos.CENTER);

        Label label = new Label("Please rate the pictures!");

        HBox controls = new HBox(10, label);

        VBox view2 = new VBox(10, controls, changeUsername, picturePane);
        VBox.setVgrow(view2, Priority.ALWAYS);

        if (isOwnPicture) {
            view = new VBox(10, view2, nextBtn);
            nextBtn.setMaxWidth(Double.MAX_VALUE);
        } else {
            view = new VBox(10, view2, voteButtons);
        }
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(50));
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
    }

    /**
     * Creates the "Next" button for player with own Image displayed to proceed.
     * Automatically sets the next indicator when created.
     *
     * @return the configured Button for navigation
     */
    private Button createNextBtn() {
        Image image = new Image(getClass().getResourceAsStream("/images/skip.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);

        Button btn = new Button();
        btn.setGraphic(imageView);
        btn.getStyleClass().add("skip-button");

        Tooltip tooltip = new Tooltip("Skip to next");
        Tooltip.install(btn, tooltip);

        btn.setOnAction(e -> {
            GUIApp.addClickedInput("/readyToVote");
            btn.setDisable(true);
        });
        return btn;
    }

    /**
     * Creates an HBox with five selectable star buttons 1–5 and a "Send Vote" button.
     * Highlights the selected star and locks all buttons after voting.
     *
     * @return HBox containing the vote and send buttons
     */
    private HBox createVoteButtonsWithSend() {
        HBox voteButtons = new HBox(10);
        voteButtons.setSpacing(10);

        Button[] starButtons = new Button[5];

        for (int i = 1; i <= 5; i++) {
            int value = i;
            Button voteBtn = new Button(String.valueOf(i));
            voteBtn.getStyleClass().add("star-button");


            int index = i - 1;
            starButtons[index] = voteBtn;

            voteBtn.setOnAction(e -> {
                vote = value;


                for (int j = 0; j < 5; j++) {
                    starButtons[j].getStyleClass().remove("selected-star");
                }
                voteBtn.getStyleClass().add("selected-star");
            });
            GUIApp.addClickSound(voteBtn);
            voteButtons.getChildren().add(voteBtn);
        }

        Image image = new Image(getClass().getResourceAsStream("/images/send.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);

        Button sendVoteBtn = new Button();
        GUIApp.addClickSound(sendVoteBtn);
        sendVoteBtn.setGraphic(imageView);
        sendVoteBtn.getStyleClass().add("send-button");

        Tooltip tooltip = new Tooltip("Send Vote");
        Tooltip.install(sendVoteBtn, tooltip);

        sendVoteBtn.setOnAction(e -> {
            GUIApp.playClickSound();
            GUIApp.addClickedInput("/sendVote");
            sendVoteBtn.setDisable(true);

            for (Button btn : starButtons) {
                btn.setDisable(true);
            }
        });

        voteButtons.getChildren().add(sendVoteBtn);

        return voteButtons;
    }

    public static int getVote() {
        return vote;
    }

    public static void resetVote() {
        vote = 1;
    }

    public static void setNextFalse() {
        next = false;
    }

    public Parent getView() {
        return view;
    }

    public static void setIsOwnPicture(String ownUsername, String ownerOfPictureUsername) {
        isOwnPicture = ownUsername.equals(ownerOfPictureUsername);
    }
}