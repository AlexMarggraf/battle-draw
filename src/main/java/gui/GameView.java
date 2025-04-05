/**
 * The GameView class represents the drawing canvas interface where players create their artwork.
 * It provides drawing tools, color selection, line width options and a countdown timer.
 *
 * @author Simon Zeugin
 */

package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class GameView {

    private final VBox view;
    private static Canvas canvas;
    private static final int canvasHeight = 500;
    private static final int canvasWidth = 500;
    private Color selectedColor = Color.BLACK;
    private int selectedLineWidth = 8;
    private double lastX;
    private double lastY;
    private final GraphicsContext gc;
    private static boolean isTimeUp = false;

    /**
     * Constructs a new GameView with all drawing components and controls.
     * Initializes the canvas, tools and countdown timer.
     */
    public GameView() {
        isTimeUp = false;

        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();

        TextField countdownField = createCountdown(5);
        Label themeLabel = createThemeLabel();

        setupCanvas();
        setupEventHandlers();
        canvas.setTranslateY(-15);
        canvas.setTranslateX(-15);

        ComboBox<String> colorCombo = createColorCombo();
        ComboBox<Integer> widthCombo = createWidthCombo();
        Button clearBtn = createClearButton();
        Button proceedBtn = createNextBtn();

        HBox controls = new HBox(10, colorCombo, widthCombo, clearBtn, proceedBtn);
        controls.setTranslateY(-15);
        controls.setTranslateX(-15);
        view = new VBox(20, controls, countdownField, themeLabel, canvas);
        view.setPadding(new Insets(30));
    }

    /**
     * Initializes the canvas with default settings.
     * Clears the canvas and draws a border around it.
     */
    private void setupCanvas() {
        clearCanvas();
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, (canvas.getWidth()) + 5.0, (canvas.getHeight()) + 5.0);
    }

    /**
     * Sets up mouse event handlers for drawing on the canvas.
     * Handles both mouse press (for dots) and mouse drag (for lines) events.
     */
    private void setupEventHandlers() {
        canvas.setOnMousePressed(event -> {
            lastX = event.getX();
            lastY = event.getY();

            gc.setFill(selectedColor);
            gc.fillOval(
                    lastX - selectedLineWidth / 2.0,
                    lastY - selectedLineWidth / 2.0,
                    selectedLineWidth,
                    selectedLineWidth
            );
        });

        canvas.setOnMouseDragged(event -> {
            if (isTimeUp) {
                return;
            }
            double x = event.getX();
            double y = event.getY();

            gc.setStroke(selectedColor);
            gc.setLineWidth(selectedLineWidth);
            gc.setLineCap(StrokeLineCap.ROUND);

            gc.strokeLine(lastX, lastY, x, y);

            lastX = x;
            lastY = y;
        });
    }

    /**
     * Creates and configures a countdown timer display.
     * The timer counts down from the specified initial time and disables drawing when time expires.
     *
     * @param initialTime the starting time in seconds for the countdown
     * @return the configured TextField displaying the countdown
     */
    public TextField createCountdown(int initialTime) {
        TextField countdownField = new TextField(String.valueOf(initialTime));
        countdownField.setEditable(false);

        int[] currentTime = {initialTime};

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (currentTime[0] > 0) {
                        currentTime[0]--;
                        countdownField.setText(String.valueOf(currentTime[0]));
                    } else {
                        countdownField.setText("Time's up!");
                        canvas.setDisable(true);
                        timeline.stop();
                        GUIApp.addUserInputQueue("/timeIsUp");
                        //GUIApp.addUserInputQueue("/readyToVote");
                        isTimeUp = true;
                            }
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return countdownField;
    }

    /**
     * Creates a color selection combo box.
     * Allows users to choose between different drawing colors.
     *
     * @return the configured ComboBox for color selection
     */
    private ComboBox<String> createColorCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("BLACK", "RED", "BLUE", "GREEN", "WHITE");
        combo.setValue("BLACK");
        combo.setOnAction(e -> selectedColor = Color.valueOf(combo.getValue()));
        return combo;
    }

    /**
     * Creates a line width selection combo box.
     * Allows users to choose between different line thicknesses.
     *
     * @return the configured ComboBox for line width selection
     */
    private ComboBox<Integer> createWidthCombo() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.getItems().addAll(4, 8, 12, 16);
        combo.setValue(8);
        combo.setOnAction(e -> selectedLineWidth = combo.getValue());
        return combo;
    }

    /**
     * Creates a clear button with confirmation dialog.
     * Clears the canvas after user confirmation, unless time has expired.
     *
     * @return the configured Button for clearing the canvas
     */
    private Button createClearButton() {
        Button button = new Button("Clear");
        button.setOnAction(e -> {
            if (isTimeUp) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Action");
            alert.setHeaderText("Clear Canvas");
            alert.setContentText("Are you sure you want to clear the canvas?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                setupCanvas();
            }
        });
        return button;
    }

    /**
     * Creates the "Proceed to Vote" button for players to proceed.
     *
     *
     * @return the configured Button for navigation
     */
    private Button createNextBtn() {
        Button btn = new Button("Proceed");

        btn.setOnAction(e -> {
            GUIApp.addUserInputQueue("/readyToVote");
            btn.setDisable(true);
        });
        return btn;
    }

    /**
     * Clears the canvas contents while maintaining the border.
     * Fills the canvas with white and redraws the border rectangle.
     */
    private void clearCanvas() {
        gc.setLineWidth(1);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static BufferedImage getCanvasAsBufferedImage() {
        if (canvas == null) {
            System.err.println("Canvas is not initialized!");
            return null;
        }

        FutureTask<BufferedImage> task = new FutureTask<>(() -> {
            WritableImage snapshot = canvas.snapshot(null, null);
            return SwingFXUtils.fromFXImage(snapshot, null);
        });

        Platform.runLater(task);

        try {
            //WritableImage snapshot = canvas.snapshot(null, null);
            //return SwingFXUtils.fromFXImage(snapshot, null);
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Label createThemeLabel() {
        Label label = new Label("Thema: " + GUIApp.getTheme());
        return label;
    }

    public static int getCanvasHeight() {
        return canvasHeight;
    }

    public static int getCanvasWidth() {
        return canvasWidth;
    }

    public static void setIsTimeUp(Boolean bool) {
        isTimeUp = bool;
    }

    public Parent getView() {
        return view;
    }
}