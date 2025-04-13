package gui_test;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;
import gui.GameView;

import java.io.IOException;

public class GameViewTest extends ApplicationTest {

    public GameView gameView;

    @BeforeEach
    public void setUp() {
        gameView = new GameView();
    }

    @Test public void testCanvasInitialization () {
        VBox root = (VBox) gameView.getView();
        boolean hasCanvas = root.getChildren().stream().anyMatch(node -> node instanceof Canvas);
        assertTrue(hasCanvas, "The view should contain a canvas");
    }

    @Test public void testColorComboBoxInitialization() {
        VBox root = (VBox) gameView.getView();
        long comboBoxAmount = 0;

        for (Node child : root.getChildren()) {
            if (child instanceof HBox) {
                comboBoxAmount = ((HBox) child).getChildren().stream()
                        .filter(node -> node instanceof ComboBox<?>)
                        .count();
            }
        }

        assertEquals(2, comboBoxAmount, "The view should contain 2 combo boxes");
    }

    @Test
    public void testProceedButtonInitialization() throws IOException {
        VBox root = (VBox) gameView.getView();
        boolean hasproceedBtn = false;

        for (Node child : root.getChildren()) {
            if (child instanceof HBox) {
                hasproceedBtn = ((HBox) child).getChildren().stream()
                        .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("Proceed"));
            }
        }

        assertTrue(hasproceedBtn, "Proceed button should be present");
    }

    @Test
    public void testClearButtonInitialization() throws IOException {
        VBox root = (VBox) gameView.getView();
        boolean hasclearBtn = false;

        for (Node child : root.getChildren()) {
            if (child instanceof HBox) {
                hasclearBtn = ((HBox) child).getChildren().stream()
                        .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("Clear"));
            }
        }

        assertTrue(hasclearBtn, "Clear button should be present");
    }

    @Test
    public void testControlsInitialization() throws IOException {
        VBox root = (VBox) gameView.getView();
        boolean hasControlsHBox = false;

        hasControlsHBox = root.getChildren().stream().anyMatch(node -> node instanceof HBox);
        assertTrue(hasControlsHBox, "Controls should be present");
    }

    @Test
    public void testCountdownInitialization() throws IOException {
        VBox root = (VBox) gameView.getView();
        boolean hasCountdown = false;

        hasCountdown = root.getChildren().stream().anyMatch(node -> node instanceof TextField);
        assertTrue(hasCountdown, "Controls should be present");
    }

    @Test
    public void testThemeLabelInitialization() throws IOException {
        VBox root = (VBox) gameView.getView();
        boolean hasThemeLabelHBox = false;

        hasThemeLabelHBox = root.getChildren().stream().anyMatch(node -> node instanceof Label);
        assertTrue(hasThemeLabelHBox, "Controls should be present");
    }
}