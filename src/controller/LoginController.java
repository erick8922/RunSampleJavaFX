package controller;

import config.DBConnection;
import dao.AuditLogDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import util.Navigator;
import util.PasswordUtil;
import util.UserSession;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please enter your username and password.");
            return;
        }

        String sql = "SELECT u.user_id, u.full_name, u.status, r.role_name "
                   + "FROM users u "
                   + "JOIN roles r ON u.role_id = r.role_id "
                   + "WHERE u.username = ? AND u.password_hash = ?";

        int userId = 0;
        String fullName = null;
        String role = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, PasswordUtil.sha256(password));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Login Failed",
                            "Wrong username or password.");
                    return;
                }

                if (!"active".equalsIgnoreCase(rs.getString("status"))) {
                    showAlert(Alert.AlertType.ERROR, "Account Inactive",
                            "This account is inactive. Please contact the administrator.");
                    return;
                }

                userId = rs.getInt("user_id");
                fullName = rs.getString("full_name");
                role = rs.getString("role_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Unable to connect to the database. Please make sure MySQL is running in XAMPP.\n\n"
                    + e.getMessage());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong: " + e.getMessage());
            return;
        }

        UserSession.start(userId, fullName, role);
        AuditLogDAO.log(userId, "LOGIN", "users", userId);
        openDashboard(role);
    }

    // ---------------------------------------------------------
    // Open the registration form
    // ---------------------------------------------------------
    @FXML
    private void handleRegister() {
        try {
            Navigator.goTo(loginButton, "/view/Register.fxml", "Register");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open the registration form.");
        }
    }

    // ---------------------------------------------------------
    // Go to the dashboard that matches the user's role.
    // Registrar and Admin dashboards are built by the other members:
    // just add AdminDashboard.fxml / RegistrarDashboard.fxml to the view package.
    // ---------------------------------------------------------
    private void openDashboard(String role) {
        String fxml;
        String title;

        switch (role.toLowerCase()) {
            case "admin":
                fxml = "/view/AdminDashboard.fxml";
                title = "Admin";
                break;
            case "registrar":
                fxml = "/view/RegistrarDashboard.fxml";
                title = "Registrar";
                break;
            case "student":
                fxml = "/view/StudentDashboard.fxml";
                title = "Student";
                break;
            default:
                UserSession.clear();
                showAlert(Alert.AlertType.ERROR, "Unknown Role",
                        "No dashboard is available for the role: " + role);
                return;
        }

        if (getClass().getResource(fxml) == null) {
            UserSession.clear();
            showAlert(Alert.AlertType.INFORMATION, "Dashboard Not Available",
                    "The " + title + " dashboard is not available yet.");
            return;
        }

        try {
            Navigator.goTo(loginButton, fxml, title);
        } catch (Exception e) {
            e.printStackTrace();
            UserSession.clear();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open the dashboard.\n\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
