package controller;

import dao.AuditLogDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.Navigator;
import util.UserSession;

public class StudentDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private Label userLabel;
    @FXML private Label appSubtitleLabel;
    @FXML private Label menuCaption;
    @FXML private Button logoutButton;
    @FXML private StackPane contentArea;

    @FXML private ToggleButton homeButton;
    @FXML private ToggleButton profileButton;
    @FXML private ToggleButton gradesButton;
    @FXML private ToggleButton credentialsButton;
    @FXML private ToggleButton requestsButton;

    @FXML
    private void initialize() {
        userLabel.setText(UserSession.getFullName());
        setupResponsiveLayout();
        showHome();
    }

    // ---------------------------------------------------------
    // RESPONSIVE LAYOUT
    // Wide window  -> full sidebar (icon + text)
    // Narrow window -> slim sidebar (icons only, text shows on hover)
    // ---------------------------------------------------------
    private static final double COMPACT_BELOW = 820;

    private final java.util.Map<ButtonBase, String> fullText = new java.util.LinkedHashMap<>();
    private boolean compact = false;

    private void setupResponsiveLayout() {
        for (ToggleButton b : new ToggleButton[]{homeButton, profileButton, gradesButton,
                credentialsButton, requestsButton}) {
            fullText.put(b, b.getText());
            b.setTooltip(new Tooltip(b.getText().substring(b.getText().indexOf(' ')).trim()));
        }
        fullText.put(logoutButton, logoutButton.getText());
        logoutButton.setTooltip(new Tooltip("Log Out"));

        rootPane.widthProperty().addListener((obs, oldW, newW) -> applyLayout(newW.doubleValue()));
        // The width is not known yet on the first pass, so check again once the window is shown.
        javafx.application.Platform.runLater(() -> applyLayout(rootPane.getWidth()));
    }

    private void applyLayout(double width) {
        if (width <= 0) {
            return;
        }
        boolean shouldCompact = width < COMPACT_BELOW;
        if (shouldCompact == compact) {
            return;
        }
        compact = shouldCompact;

        if (compact) {
            sidebar.getStyleClass().add("sidebar-compact");
        } else {
            sidebar.getStyleClass().remove("sidebar-compact");
        }
        menuCaption.setVisible(!compact);
        menuCaption.setManaged(!compact);
        appSubtitleLabel.setVisible(!compact);
        appSubtitleLabel.setManaged(!compact);

        for (java.util.Map.Entry<ButtonBase, String> e : fullText.entrySet()) {
            String text = e.getValue();
            e.getKey().setText(compact ? text.substring(0, text.indexOf(' ')) : text);
        }
    }

    // ---------------------------------------------------------
    // HOME
    // ---------------------------------------------------------
    @FXML
    private void showHome() {
        homeButton.setSelected(true);

        Label welcome = new Label("Hello, " + UserSession.getFullName() + "!");
        welcome.getStyleClass().add("banner-title");
        welcome.setWrapText(true);

        Label hint = new Label("What would you like to do today? Tap one of the boxes below.");
        hint.getStyleClass().add("banner-text");
        hint.setWrapText(true);

        VBox banner = new VBox(8, welcome, hint);
        banner.getStyleClass().add("welcome-banner");

        FlowPane cards = new FlowPane(18, 18);
        cards.getChildren().addAll(
                actionCard("👤", "My Profile",
                        "See your personal details and update your contact number.",
                        this::showProfile),
                actionCard("📊", "Report Cards",
                        "Check your grades for each school year.",
                        this::showGrades),
                actionCard("🎓", "Credentials",
                        "See the certificates the school has given you.",
                        this::showCredentials),
                actionCard("📄", "Document Request",
                        "Ask the school for a document, like a Good Moral certificate.",
                        this::showDocumentRequests)
        );

        VBox page = new VBox(22, banner, cards);
        page.getStyleClass().add("page-pad");

        setContent(page);
    }

    /** One big, clickable box on the Home page. */
    private VBox actionCard(String icon, String title, String description, Runnable action) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("action-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("action-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("action-text");
        descLabel.setWrapText(true);

        VBox card = new VBox(8, iconLabel, titleLabel, descLabel);
        card.getStyleClass().add("action-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(250);
        card.setMinWidth(220);
        card.setFocusTraversable(true);
        card.setOnMouseClicked(e -> action.run());
        card.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER
                    || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                action.run();
            }
        });
        return card;
    }

    // ---------------------------------------------------------
    // MY PROFILE
    // ---------------------------------------------------------
    @FXML
    private void showProfile() {
        profileButton.setSelected(true);
        loadView("/view/StudentProfile.fxml");
    }

    // ---------------------------------------------------------
    // REPORT CARDS
    // ---------------------------------------------------------
    @FXML
    private void showGrades() {
        gradesButton.setSelected(true);
        loadView("/view/StudentGrades.fxml");
    }

    // ---------------------------------------------------------
    // CREDENTIALS
    // ---------------------------------------------------------
    @FXML
    private void showCredentials() {
        credentialsButton.setSelected(true);
        loadView("/view/StudentCredentials.fxml");
    }

    // ---------------------------------------------------------
    // DOCUMENT REQUEST
    // ---------------------------------------------------------
    @FXML
    private void showDocumentRequests() {
        requestsButton.setSelected(true);
        loadView("/view/StudentDocumentRequests.fxml");
    }

    // ---------------------------------------------------------
    // CONTENT HELPERS
    // ---------------------------------------------------------
    private void loadView(String fxml) {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource(fxml)
            );

            setContent(view);

        } catch (Exception e) {
            e.printStackTrace();

            showError(
                    "Something went wrong",
                    "Unable to load this page. Please try again."
            );
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
        page.getStyleClass().add("page-pad");

        setContent(page);
    }

    private void setContent(Node node) {
        util.ResponsiveForm.fit(node);
        contentArea.getChildren().setAll(node);
    }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------
    @FXML
    private void handleLogout() {

        AuditLogDAO.log(
                UserSession.getUserId(),
                "LOGOUT",
                "users",
                UserSession.getUserId()
        );

        UserSession.clear();

        try {
            Navigator.goTo(
                    userLabel,
                    "/view/Login.fxml",
                    "Login"
            );

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText(
                    "Unable to open the Login screen."
            );
            alert.showAndWait();
        }
    }
}
