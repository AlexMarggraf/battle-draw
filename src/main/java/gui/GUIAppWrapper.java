/**
 * The GUIAppWrapper class is the entry point for launching the JavaFX application.
 * It calls the launch method of the {@link javafx.application.Application} class
 * to start the {@link GUIApp} class.
 */
package gui;

import javafx.application.Application;

public class GUIAppWrapper {

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        Application.launch(GUIApp.class, args);
    }
}