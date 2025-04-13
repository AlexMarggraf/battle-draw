/**
 * Unit tests for the EndingView class.
 * These tests ensure that the functionality of the EndingView is working as expected.
 *
 * @author Alexander Marggraf
 */

package gui_test;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;
import gui.EndingView;

public class EndingViewTest extends ApplicationTest {

    private EndingView endingView;

    /**
     * Initializes the EndingView instance and sets the 'Again' flag to false before each test.
     */
    @BeforeEach
    public void setUp() {
        endingView = new EndingView();
        EndingView.setAgainEnding(false);
    }

    /**
     * Tests that the 'Again' flag is initially set to false.
     */
    @Test
    public void testAgainFlagInitialization() {
        assertFalse(EndingView.getAgainEnding(), "Again flag should initially be false");
    }

    /**
     * Tests that the 'Again' flag is a Boolean object.
     * This ensures that the return type is an instance of Boolean.
     */
    @Test
    public void testGetAgainEnding() {
        assertTrue((Boolean)(EndingView.getAgainEnding()) instanceof Boolean, "Expected a Boolean object");
    }

    /**
     * Tests the functionality of setting the 'Again' flag.
     * Ensures that the flag can be correctly set to true or false.
     */
    @Test
    public void testSetAgainEnding() {
        EndingView.setAgainEnding(true);
        assertTrue(EndingView.getAgainEnding(), "Again flag should be true after setting");

        EndingView.setAgainEnding(false);
        assertFalse(EndingView.getAgainEnding(), "Again flag should not be true");
    }

    /**
     * Tests the functionality of the 'Again?' button.
     * Simulates a click on the button and checks if the 'Again' flag is updated.
     */
    @Test
    public void testCreateAgainBtn() {
        Button againBtn = endingView.createAgainBtn();
        interact(() -> againBtn.fire());
        assertTrue(EndingView.getAgainEnding(), "Again flag should be true after clicking 'Again?'");
    }

    /**
     * Tests the creation of the 'Leave' button.
     * Ensures that the button is not null and is created properly.
     */
    @Test
    public void testCreateLeaveBtn() {
        Button leaveBtn = endingView.createLeaveBtn();
        assertNotNull(leaveBtn, "Leave button should not be null");
    }

    /**
     * Tests the view of the EndingView.
     * Ensures that the view is an instance of VBox and contains exactly 2 buttons.
     */
    @Test
    public void testGetView() {
        Parent view = endingView.getView();
        assertTrue(view instanceof VBox, "View should be a VBox");

        VBox vbox = (VBox) view;
        long buttonCount = vbox.getChildren().stream()
                .filter(node -> node instanceof Button)
                .count();
        assertEquals(2, buttonCount, "VBox should contain 2 buttons");
    }
}
