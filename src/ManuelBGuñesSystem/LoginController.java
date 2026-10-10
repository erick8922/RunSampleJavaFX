package ManuelBGuñesSystem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

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
                   + "FROM Users u "
                   + "JOIN Roles r ON u.role_id = r.role_id "
                   + "WHERE u.username = ? AND u.password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, sha256(password));

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

                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role_name");

                UserSession.start(userId, fullName, role);
                writeAuditLog(conn, userId);
                openDashboard(role);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Unable to connect to the database. Please make sure MySQL is running in XAMPP.\n\n"
                    + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Buksan ang Registration form
    // ---------------------------------------------------------
    @FXML
    private void handleRegister() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Manuel B. Guiñes System - Register");
        stage.centerOnScreen();
        stage.show();
    }

    // ---------------------------------------------------------
    // Pagpunta sa dashboard ayon sa role
    // Palitan ang pangalan ng FXML ayon sa totoong files mo.
    // ---------------------------------------------------------
    private void openDashboard(String role) throws Exception {
        String fxml;
        String title;

        switch (role.toLowerCase()) {
            case "admin":
                fxml = "AdminDashboard.fxml";
                title = "Admin";
                break;
            case "registrar":
                fxml = "RegistrarDashboard.fxml";
                title = "Registrar";
                break;
            case "student":
                fxml = "StudentDashboard.fxml";
                title = "Student";
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Unknown Role",
                        "No dashboard is available for the role: " + role);
                return;
        }

        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Manuel B. Guiñes System - " + title);
        stage.show();
    }

    // ---------------------------------------------------------
    // Audit log (hindi hihinto ang login kahit mag-fail ito)
    // ---------------------------------------------------------
    private void writeAuditLog(Connection conn, int userId) {
        String sql = "INSERT INTO Audit_Logs (user_id, action, table_name, record_id, status) "
                   + "VALUES (?, 'LOGIN', 'Users', ?, 'success')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // SHA-256 (tugma sa SHA2(..., 256) ng MySQL)
    // ---------------------------------------------------------
    public static String sha256(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
