package controller;

import dao.AuditLogDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.Navigator;
import util.UserSession;

public class StudentDashboardController {

    @FXML private Label userLabel;
    @FXML private StackPane contentArea;
    @FXML private ToggleButton homeButton;
    @FXML private ToggleButton profileButton;
    @FXML private ToggleButton subjectsButton;
    @FXML private ToggleButton gradesButton;
    @FXML private ToggleButton transcriptButton;
    @FXML private ToggleButton credentialsButton;
    @FXML private ToggleButton requestsButton;

    @FXML
    private void initialize() {
        userLabel.setText(UserSession.getFullName());
        showHome();
    }

    // ---------------------------------------------------------
    // Menu
    // ---------------------------------------------------------
    @FXML
    private void showHome() {
        homeButton.setSelected(true);

        Label welcome = new Label("Welcome back, " + UserSession.getFullName() + "!");
        welcome.getStyleClass().add("banner-title");
        Label hint = new Label("Use the menu on the left to view your profile, subjects, and documents.");
        hint.getStyleClass().add("banner-text");
        VBox banner = new VBox(6, welcome, hint);
        banner.getStyleClass().add("welcome-banner");

        Label quickTitle = new Label("Quick Actions");
        quickTitle.getStyleClass().add("section-title");

        Button profileAction = new Button("View My Profile");
        profileAction.getStyleClass().add("btn-primary");
        profileAction.setOnAction(e -> showProfile());

        Button requestAction = new Button("Request a Document");
        requestAction.getStyleClass().add("btn-secondary");
        requestAction.setOnAction(e -> showDocumentRequests());

        VBox quickActions = new VBox(14, quickTitle, new HBox(12, profileAction, requestAction));
        quickActions.getStyleClass().add("card");

        VBox page = new VBox(18, banner, quickActions);
        page.setPadding(new Insets(25));
        setContent(page);
    }

    @FXML
    private void showProfile() {
        profileButton.setSelected(true);
        loadView("/view/StudentProfile.fxml");
    }

    @FXML
    private void showSubjects() {
        subjectsButton.setSelected(true);
        loadView("/view/StudentSubjects.fxml");
    }

    @FXML
    private void showGrades() {
        gradesButton.setSelected(true);
        loadView("/view/StudentGrades.fxml");
    }

    @FXML
    private void showTranscript() {
        transcriptButton.setSelected(true);
        loadView("/view/StudentTranscript.fxml");
    }

    @FXML
    private void showCredentials() {
        credentialsButton.setSelected(true);
        loadView("/view/StudentCredentials.fxml");
    }

    @FXML
    private void showDocumentRequests() {
        requestsButton.setSelected(true);
        loadView("/view/StudentDocumentRequests.fxml");
    }

    // ---------------------------------------------------------
    // Content helpers
    // ---------------------------------------------------------
    private void loadView(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            setContent(view);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Something went wrong", "Unable to load this page. Please try again.");
        }
    }

    private void showError(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("page-subtitle");
        messageLabel.setWrapText(true);

        VBox card = new VBox(messageLabel);
        card.getStyleClass().add("card");

        VBox page = new VBox(18, titleLabel, card);
        page.setPadding(new Insets(25));
        setContent(page);
    }

    private void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    // ---------------------------------------------------------
    // Logout
    // ---------------------------------------------------------
    @FXML
    private void handleLogout() {
        AuditLogDAO.log(UserSession.getUserId(), "LOGOUT", "users", UserSession.getUserId());
        UserSession.clear();
        try {
            Navigator.goTo(userLabel, "/view/Login.fxml", "Login");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Unable to open the Login screen.");
            alert.showAndWait();
        }
    }
}
