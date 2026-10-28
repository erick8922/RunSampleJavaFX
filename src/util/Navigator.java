package util;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central place for switching screens, so every module
 * (Student, Registrar, Admin) navigates the same way.
 */
public class Navigator {

    public static final String APP_TITLE = "Manuel B. Guiñez System";

    /**
     * Replaces the current scene with the given FXML file.
     *
     * @param source   any node that is currently shown (used to find the window)
     * @param fxmlPath absolute classpath location, e.g. "/view/Login.fxml"
     * @param title    page title shown after the app name (can be empty)
     */
    public static void goTo(Node source, String fxmlPath, String title) throws IOException {
        URL location = Navigator.class.getResource(fxmlPath);
        if (location == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        Parent root = FXMLLoader.load(location);
        Stage stage = (Stage) source.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title == null || title.isEmpty() ? APP_TITLE : APP_TITLE + " - " + title);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }
}
