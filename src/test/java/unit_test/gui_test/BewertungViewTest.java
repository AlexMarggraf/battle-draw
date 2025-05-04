/**
 * This class contains all necessary unit tests for the class BewertungView.java
 *
 * @author Alexander Marggraf
 */

package unit_test.gui_test;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.testfx.framework.junit5.ApplicationTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import gui.BewertungView;

public class BewertungViewTest extends ApplicationTest {

    private BewertungView bewertungView;

    /**
     * Initializes the BewertungView object for testing.
     *
     * @throws IOException if there is an error during initialization.
     */
    public void setUp() throws IOException {
        bewertungView = new BewertungView();
    }

    /**
     * Initializes the BewertungView object with the user's picture set to not their own.
     *
     * @throws IOException if there is an error during initialization.
     */
    public void setUpNotOwnPicture() throws IOException {
        BewertungView.setIsOwnPicture("user1", "user2");
        bewertungView = new BewertungView();
    }

    /**
     * Initializes the BewertungView object with the user's picture set to their own.
     *
     * @throws IOException if there is an error during initialization.
     */
    public void setUpOwnPicture() throws IOException {
        BewertungView.setIsOwnPicture("user1", "user1");
        bewertungView = new BewertungView();
    }

    /**
     * Tests the initialization of the vote when the user's picture is their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testVoteInitializationOwnPicture() throws IOException {
        setUpOwnPicture();
        assertEquals(0, BewertungView.getVote(), "Initial vote should be 0");
    }

    /**
     * Tests the initialization of the vote when the user's picture is not their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testVoteInitializationNotOwnPicture() throws IOException {
        setUpNotOwnPicture();
        assertEquals(0, BewertungView.getVote(), "Initial vote should be 0");
    }

    /**
     * Tests the initialization of the next flag when the user's picture is their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testNextFlagInitializationOwnPicture() throws IOException {
        setUpOwnPicture();
        assertTrue(BewertungView.getNextIndicator(), "Next should be false initially");
    }

    /**
     * Tests the initialization of the next flag when the user's picture is not their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testNextFlagInitializationNotOwnPicture() throws IOException {
        setUpNotOwnPicture();
        assertTrue(BewertungView.getNextIndicator(), "Next should be false initially");
    }

    /**
     * Tests the presence of a ComboBox when the user's picture is their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testVoteComboBoxPresenceOwnPicture() throws IOException {
        setUpOwnPicture();
        VBox root = (VBox) bewertungView.getView();
        boolean hasComboBox = root.getChildren().stream().anyMatch(node -> node instanceof ComboBox<?>);
        assertFalse(hasComboBox, "ComboBox shouldn't be present if it's the user's own picture");
    }

    /**
     * Tests the presence of a ComboBox when the user's picture is not their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testVoteComboBoxPresenceNotOwnPicture() throws IOException {
        setUpNotOwnPicture();
        VBox root = (VBox) bewertungView.getView();
        boolean hasComboBox = root.getChildren().stream().anyMatch(node -> node instanceof ComboBox<?>);
        assertTrue(hasComboBox, "ComboBox should be present if it's not the user's own picture");
    }

    /**
     * Tests the presence of the Next button when the user's picture is their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testNextButtonPresenceOwnPicture() throws IOException {
        setUpOwnPicture();
        VBox root = (VBox) bewertungView.getView();
        boolean hasNextBtn = root.getChildren().stream()
                .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("Next"));
        assertTrue(hasNextBtn, "Next button should be present if it's own picture");
    }

    /**
     * Tests the presence of the Next button when the user's picture is not their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testNextButtonPresenceNotOwnPicture() throws IOException {
        setUpNotOwnPicture();
        VBox root = (VBox) bewertungView.getView();
        boolean hasNextBtn = root.getChildren().stream()
                .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("Next"));
        assertFalse(hasNextBtn, "Next button should not be present if it's not own picture");
    }

    /**
     * Tests the presence of the Send Vote button when the user's picture is their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testSendVoteButtonPresenceOwnPicture() throws IOException {
        setUpOwnPicture();
        VBox root = (VBox) bewertungView.getView();
        boolean hasSendVote = root.getChildren().stream()
                .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("send Vote"));
        assertFalse(hasSendVote, "Send Vote button shouldn't be present if it's own picture");
    }

    /**
     * Tests the presence of the Send Vote button when the user's picture is not their own.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testSendVoteButtonPresenceNotOwnPicture() throws IOException {
        setUpNotOwnPicture();
        VBox root = (VBox) bewertungView.getView();
        boolean hasSendVote = root.getChildren().stream()
                .anyMatch(node -> node instanceof Button && ((Button) node).getText().equals("send Vote"));
        assertTrue(hasSendVote, "Send Vote button should be present if it's not own picture");
    }

    /**
     * Tests the functionality of getting the vote when the ComboBox is selected.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testGetVote() throws IOException {
        setUpNotOwnPicture();
        VBox root = (VBox) bewertungView.getView();

        for(int i = 0; i < 5; i++){
            final int index = i;
            root.getChildren().stream()
                    .filter(node -> node instanceof ComboBox<?>)
                    .map(node -> (ComboBox<Integer>) node)
                    .findFirst()
                    .ifPresent(combo -> {
                        combo.getSelectionModel().select(index);
                        if (combo.getOnAction() != null)
                            combo.getOnAction().handle(new ActionEvent());
                    });

            assertEquals(index + 1, BewertungView.getVote());
        }
    }

    /**
     * Tests setting and getting the vote and resetting it to zero.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testSetVote() throws IOException {
        setUpNotOwnPicture();
        VBox root = (VBox) bewertungView.getView();

        root.getChildren().stream()
                .filter(node -> node instanceof ComboBox<?>)
                .map(node -> (ComboBox<Integer>) node)
                .findFirst()
                .ifPresent(combo -> {
                    combo.getSelectionModel().select(2);
                    if (combo.getOnAction() != null)
                        combo.getOnAction().handle(new ActionEvent());
                });

        assertEquals(3, BewertungView.getVote());

        BewertungView.resetVote();
        assertEquals(0, BewertungView.getVote());
    }

    /**
     * Tests setting an image on the BewertungView.
     *
     * @throws IOException if there is an error during the test setup.
     */
    @Test
    public void testSetImage() throws IOException {
        setUpNotOwnPicture();
        BufferedImage bufferedImage = ImageIO.read(getClass().getResource("/test-image.png"));
        BewertungView.setImage(bufferedImage);
        ImageView imgView = getImageViewFromView();
        Image testImage = imgView.getImage();
        assertNotNull(testImage, "ImageView should have an image after setImage()");
    }

    /**
     * Helper method to retrieve the ImageView from the view.
     *
     * @return the ImageView object found in the BewertungView.
     */
    private ImageView getImageViewFromView() {
        VBox root = (VBox) bewertungView.getView();
        VBox view2 = (VBox) root.getChildren().get(0);
        return (ImageView) view2.getChildren().get(1);
    }
}
