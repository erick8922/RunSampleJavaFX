package ManuelBGuñesSystem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField studentNoField;
    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField suffixField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField contactNoField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------
    @FXML
    private void handleRegister() {
        String studentNo = studentNoField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String suffix = suffixField.getText().trim();
        LocalDate birthDate = birthDatePicker.getValue();
        String gender = genderCombo.getValue();
        String contactNo = contactNoField.getText().trim();
        String address = addressField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // --- Validation ---
        if (studentNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                    "Please fill in all fields marked with an asterisk (*).");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Weak Password",
                    "The password must be at least 6 characters long.");
            return;
        }
        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING, "Passwords Do Not Match",
                    "The password and confirm password do not match.");
            return;
        }

        String fullName = firstName
                + (middleName.isEmpty() ? "" : " " + middleName)
                + " " + lastName
                + (suffix.isEmpty() ? "" : " " + suffix);

        // --- Save (Users + Students sa isang transaction) ---
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int roleId = getStudentRoleId(conn);
                int userId = insertUser(conn, roleId, username, password, fullName, email);
                insertStudent(conn, userId, studentNo, firstName, middleName, lastName,
                        suffix, birthDate, gender, address, contactNo);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                    "Registration successful! You can now log in.");
            handleBack();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                String msg = e.getMessage();
                if (msg.contains("uq_users_username")) {
                    showAlert(Alert.AlertType.ERROR, "Username Taken",
                            "This username is already taken. Please choose another one.");
                } else if (msg.contains("uq_users_email")) {
                    showAlert(Alert.AlertType.ERROR, "Email Already Registered",
                            "An account with this email address already exists.");
                } else if (msg.contains("uq_students_student_no")) {
                    showAlert(Alert.AlertType.ERROR, "Student No. Exists",
                            "Student Number Exists");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Duplicate", e.getMessage());
                }
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private int getStudentRoleId(Connection conn) throws SQLException {
        String sql = "SELECT role_id FROM Roles WHERE role_name = 'Student'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        }
        throw new SQLException("The 'Student' role was not found in the Roles table. Please run seed_login_data.sql first.");
    }

    private int insertUser(Connection conn, int roleId, String username, String password,
                           String fullName, String email) throws Exception {
        String sql = "INSERT INTO Users (role_id, username, password_hash, full_name, email, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'active')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roleId);
            ps.setString(2, username);
            ps.setString(3, LoginController.sha256(password));
            ps.setString(4, fullName);
            ps.setString(5, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve the new user_id.");
    }

    private void insertStudent(Connection conn, int userId, String studentNo, String firstName,
                               String middleName, String lastName, String suffix,
                               LocalDate birthDate, String gender, String address,
                               String contactNo) throws SQLException {
        String sql = "INSERT INTO Students (student_no, first_name, middle_name, last_name, suffix, "
                   + "birth_date, gender, address, contact_no, status, user_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            ps.setString(2, firstName);
            setNullable(ps, 3, middleName);
            ps.setString(4, lastName);
            setNullable(ps, 5, suffix);
            if (birthDate == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(birthDate));
            }
            setNullable(ps, 7, gender);
            setNullable(ps, 8, address);
            setNullable(ps, 9, contactNo);
            ps.setInt(10, userId);
            ps.executeUpdate();
        }
    }

    private void setNullable(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    // ---------------------------------------------------------
    // Bumalik sa Login
    // ---------------------------------------------------------
    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manuel B. Guiñes System");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to open the Login screen.");
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
