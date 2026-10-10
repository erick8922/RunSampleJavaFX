package ManuelBGuñesSystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StudentDashboardController {

    @FXML private Label userLabel;
    @FXML private Label contentTitle;
    @FXML private Label contentText;

    @FXML
    private void initialize() {
        userLabel.setText("Welcome, " + UserSession.getFullName());
        showHome();
    }

    // ---------------------------------------------------------
    // Menu (placeholder muna; dito ilalagay ang laman ng bawat page)
    // ---------------------------------------------------------
    @FXML
    private void showHome() {
        setContent("Home", "Welcome, " + UserSession.getFullName() + "!");
    }

    @FXML
    private void showProfile() {
        setContent("My Profile", "Your information from the Students table will appear here.");
    }

    @FXML
    private void showSubjects() {
        setContent("My Subjects", "sample Enrollment_Subject.");
    }

    @FXML
    private void showTranscript() {
        setContent("Transcript", "sample Transcript table.");
    }

    @FXML
    private void showCredentials() {
        setContent("Credentials", "sample Credentials table.");
    }

    @FXML
    private void showDocumentRequests() {
        setContent("Document Requests", "sample Document_Requests.");
    }

    private void setContent(String title, String text) {
        contentTitle.setText(title);
        contentText.setText(text);
    }

    // ---------------------------------------------------------
    // Logout
    // ---------------------------------------------------------
    @FXML
    private void handleLogout() throws Exception {
        UserSession.clear();
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Stage stage = (Stage) userLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Manuel B. Guiñes System");
        stage.centerOnScreen();
        stage.show();
    }
}
