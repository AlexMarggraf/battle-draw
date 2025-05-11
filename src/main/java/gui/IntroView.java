package gui;

import animatefx.animation.FadeIn;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class IntroView {

    private boolean transitioned = false;

    public Node getView() {

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/logo.png")));
        logo.setPreserveRatio(true);
        logo.setFitWidth(600);

        StackPane logoContainer = new StackPane(logo);
        logoContainer.setMinSize(300, 300);
        logoContainer.setAlignment(Pos.CENTER);

        VBox introBox = new VBox(30, logoContainer);
        introBox.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(introBox);
        root.getStyleClass().add("intro-root");
        root.setAlignment(Pos.CENTER);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.5), logoContainer);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        scale.setOnFinished(event -> {
            Button starBtn = new Button();
            GUIApp.addClickSound(starBtn);
            starBtn.getStyleClass().add("star-button");
            starBtn.setOnAction(e -> {
                if (!transitioned) {
                    transitioned = true;
                    GUIApp.showLoginSignupView();
                }
            });

            Label pressToStart = new Label("Press to start");
            pressToStart.setStyle("-fx-font-size: 18px; -fx-text-fill: #2c3e50;");

            VBox startSection = new VBox(10, starBtn, pressToStart);
            startSection.setAlignment(Pos.CENTER);

            introBox.getChildren().add(startSection);
            new FadeIn(startSection).play();
        });

        scale.play();
        return root;
    }
}
