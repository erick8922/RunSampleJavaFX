package controller;

import config.DBConnection;
import dao.AuditLogDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import util.Navigator;
import util.PasswordUtil;

public class RegisterController {

    @FXML private TextField studentNoField;      // now holds the 12-digit LRN
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
    // Validation rules
    // ---------------------------------------------------------
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // letters, with optional single space / hyphen / apostrophe between words, optional trailing period (Ma., Jr.)
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^\\p{L}+\\.?([ '\\-]\\p{L}+\\.?)*$");
    private static final Pattern REPEATED_LETTER = Pattern.compile("(?i)(\\p{L})\\1\\1");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{3,19}$");

    private static final int MIN_AGE = 10;
    private static final int MAX_AGE = 30;

    private static final Map<String, String> SUFFIXES = new HashMap<>();
    static {
        SUFFIXES.put("jr", "Jr.");
        SUFFIXES.put("sr", "Sr.");
        SUFFIXES.put("ii", "II");
        SUFFIXES.put("iii", "III");
        SUFFIXES.put("iv", "IV");
        SUFFIXES.put("v", "V");
    }

    // ---------------------------------------------------------
    // Setup: block wrong characters while typing + guide hints
    // ---------------------------------------------------------
    @FXML
    public void initialize() {
        // LRN and contact number: digits only, hard limit on length
        studentNoField.setTextFormatter(new TextFormatter<String>(digitsOnly(12)));
        contactNoField.setTextFormatter(new TextFormatter<String>(digitsOnly(11)));

        // Names: letters, space, hyphen, apostrophe and period only
        firstNameField.setTextFormatter(new TextFormatter<String>(nameChars(50)));
        middleNameField.setTextFormatter(new TextFormatter<String>(nameChars(50)));
        lastNameField.setTextFormatter(new TextFormatter<String>(nameChars(50)));
        suffixField.setTextFormatter(new TextFormatter<String>(nameChars(4)));

        // No spaces allowed in username / password / email
        usernameField.setTextFormatter(new TextFormatter<String>(noSpaces(20)));
        emailField.setTextFormatter(new TextFormatter<String>(noSpaces(150)));
        passwordField.setTextFormatter(new TextFormatter<String>(noSpaces(50)));
        confirmPasswordField.setTextFormatter(new TextFormatter<String>(noSpaces(50)));

        // Birth date: fixed format, and future dates cannot be picked
        birthDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate d) {
                return d == null ? "" : DATE_FMT.format(d);
            }
            @Override public LocalDate fromString(String s) {
                try {
                    return (s == null || s.trim().isEmpty()) ? null : LocalDate.parse(s.trim(), DATE_FMT);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        birthDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        if (genderCombo.getItems().isEmpty()) {
            genderCombo.getItems().setAll("Male", "Female");
        }

        // Hints
        studentNoField.setPromptText("12-digit LRN (e.g., 305012010037)");
        firstNameField.setPromptText("e.g., Maria Clara");
        middleNameField.setPromptText("Leave blank if none");
        lastNameField.setPromptText("e.g., Dela Cruz");
        suffixField.setPromptText("Jr., Sr., II, III (optional)");
        birthDatePicker.getEditor().setPromptText("MM/DD/YYYY");
        contactNoField.setPromptText("11 digits, e.g., 09171234567");
        addressField.setPromptText("Purok/Street, Barangay, City/Municipality, Province");
        emailField.setPromptText("e.g., juan.delacruz@gmail.com");
        usernameField.setPromptText("4-20 letters, numbers or _");
        passwordField.setPromptText("At least 8 characters, with letters and numbers");
    }

    private UnaryOperator<TextFormatter.Change> digitsOnly(int max) {
        return ch -> {
            if (!ch.isContentChange()) return ch;
            ch.setText(ch.getText().replaceAll("\\D", ""));      // pasted "0917-123 4567" becomes digits only
            return ch.getControlNewText().length() <= max ? ch : null;
        };
    }

    private UnaryOperator<TextFormatter.Change> nameChars(int max) {
        return ch -> {
            if (!ch.isContentChange()) return ch;
            ch.setText(ch.getText().replaceAll("[^\\p{L} .'\\-]", ""));
            return ch.getControlNewText().length() <= max ? ch : null;
        };
    }

    private UnaryOperator<TextFormatter.Change> noSpaces(int max) {
        return ch -> {
            if (!ch.isContentChange()) return ch;
            ch.setText(ch.getText().replaceAll("\\s", ""));
            return ch.getControlNewText().length() <= max ? ch : null;
        };
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------
    @FXML
    private void handleRegister() {
        List<String> errors = new ArrayList<>();
        List<Control> invalid = new ArrayList<>();

        // ----- LRN -----
        String studentNo = studentNoField.getText().trim();
        if (studentNo.isEmpty()) {
            fail(errors, invalid, studentNoField, "LRN is required. It is the 12-digit number on your report card (Form 138).");
        } else if (!studentNo.matches("\\d{12}")) {
            fail(errors, invalid, studentNoField, "LRN must be exactly 12 digits (numbers only). You typed " + studentNo.length() + " digit(s).");
        } else if (studentNo.matches("(\\d)\\1{11}")) {
            fail(errors, invalid, studentNoField, "LRN cannot be the same digit repeated 12 times. Please copy it from your report card.");
        }

        // ----- Names -----
        String firstName = checkName("First name", firstNameField, true, errors, invalid);
        String middleName = checkName("Middle name", middleNameField, false, errors, invalid);
        String lastName = checkName("Last name", lastNameField, true, errors, invalid);

        // ----- Suffix -----
        String suffix = "";
        String rawSuffix = suffixField.getText().trim();
        if (!rawSuffix.isEmpty()) {
            String key = rawSuffix.replace(".", "").toLowerCase();
            suffix = SUFFIXES.get(key);
            if (suffix == null) {
                suffix = "";
                fail(errors, invalid, suffixField, "Suffix must be one of: Jr., Sr., II, III, IV, V. Leave it blank if you have none.");
            }
        }

        // ----- Birth date -----
        LocalDate birthDate = null;
        String dateText = birthDatePicker.getEditor().getText() == null ? "" : birthDatePicker.getEditor().getText().trim();
        if (dateText.isEmpty()) {
            fail(errors, invalid, birthDatePicker, "Birth date is required. Use the format MM/DD/YYYY (e.g., 06/15/2011).");
        } else {
            try {
                birthDate = LocalDate.parse(dateText, DATE_FMT);
            } catch (DateTimeParseException e) {
                fail(errors, invalid, birthDatePicker, "Birth date is not a valid date. Use the format MM/DD/YYYY (e.g., 06/15/2011).");
            }
        }
        if (birthDate != null) {
            LocalDate today = LocalDate.now();
            if (birthDate.isAfter(today)) {
                fail(errors, invalid, birthDatePicker, "Birth date cannot be in the future.");
                birthDate = null;
            } else {
                int age = Period.between(birthDate, today).getYears();
                if (age < MIN_AGE || age > MAX_AGE) {
                    fail(errors, invalid, birthDatePicker, "The birth date gives an age of " + age
                            + ". Please check the year (accepted age is " + MIN_AGE + " to " + MAX_AGE + ").");
                    birthDate = null;
                }
            }
        }

        // ----- Gender -----
        String gender = genderCombo.getValue();
        if (gender == null || gender.trim().isEmpty()) {
            fail(errors, invalid, genderCombo, "Please select your gender.");
        }

        // ----- Contact number (optional, but must be valid when filled) -----
        String contactNo = contactNoField.getText().trim();
        if (!contactNo.isEmpty() && !contactNo.matches("09\\d{9}")) {
            fail(errors, invalid, contactNoField,
                    "Contact number must be 11 digits and start with 09 (e.g., 09171234567).");
        }

        // ----- Address (optional, but must look like an address when filled) -----
        String address = addressField.getText().trim().replaceAll("\\s+", " ");
        if (!address.isEmpty()) {
            if (address.length() < 8 || !address.matches(".*\\p{L}.*")) {
                fail(errors, invalid, addressField,
                        "Address is too short. Please include Purok/Street, Barangay, City/Municipality and Province.");
            } else if (address.length() > 255) {
                fail(errors, invalid, addressField, "Address is too long (maximum 255 characters).");
            }
        }

        // ----- Email -----
        String email = emailField.getText().trim().toLowerCase();
        if (email.isEmpty()) {
            fail(errors, invalid, emailField, "Email is required (e.g., juan.delacruz@gmail.com).");
        } else if (!EMAIL_PATTERN.matcher(email).matches() || email.contains("..")) {
            fail(errors, invalid, emailField,
                    "Email is not valid. It must look like name@example.com (no spaces, with @ and a domain such as .com).");
        }

        // ----- Username -----
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            fail(errors, invalid, usernameField, "Username is required.");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            fail(errors, invalid, usernameField,
                    "Username must be 4 to 20 characters, start with a letter, and contain only letters, numbers or underscore (_). No spaces.");
        }

        // ----- Password -----
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        if (password.isEmpty()) {
            fail(errors, invalid, passwordField, "Password is required.");
        } else if (password.length() < 8) {
            fail(errors, invalid, passwordField, "Password is too short. Use at least 8 characters.");
        } else if (!password.matches(".*\\p{L}.*") || !password.matches(".*\\d.*")) {
            fail(errors, invalid, passwordField, "Password must contain at least one letter and one number (e.g., Study2026).");
        } else if (password.equalsIgnoreCase(username) || password.equals(studentNo)) {
            fail(errors, invalid, passwordField, "Password must not be the same as your username or LRN.");
        } else if (!password.equals(confirm)) {
            fail(errors, invalid, confirmPasswordField, "Password and Confirm Password do not match. Please type them again.");
        }

        // ----- Show every problem at once -----
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Please fix the following before registering:\n");
            for (String e : errors) {
                sb.append("\n\u2022 ").append(e);
            }
            showAlert(Alert.AlertType.WARNING, "Please Check Your Information", sb.toString());
            invalid.get(0).requestFocus();
            return;
        }

        String fullName = firstName
                + (middleName.isEmpty() ? "" : " " + middleName)
                + " " + lastName
                + (suffix.isEmpty() ? "" : " " + suffix);
        if (fullName.length() > 150) {
            showAlert(Alert.AlertType.WARNING, "Name Too Long",
                    "Your full name is too long (maximum 150 characters). Please shorten it.");
            return;
        }

        int newUserId = 0;

        // --- Save (Users + Students in one transaction) ---
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int roleId = getStudentRoleId(conn);
                newUserId = insertUser(conn, roleId, username, password, fullName, email);
                insertStudent(conn, newUserId, studentNo, firstName, middleName, lastName,
                        suffix, birthDate, gender, address, contactNo);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            AuditLogDAO.log(newUserId, "REGISTER", "users", newUserId);
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
                    showAlert(Alert.AlertType.ERROR, "LRN Already Registered",
                            "This LRN is already registered. If you did not register it, please contact the registrar.");
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
    // Validation helpers
    // ---------------------------------------------------------
    private void fail(List<String> errors, List<Control> invalid, Control field, String message) {
        errors.add(message);
        invalid.add(field);
    }

    /** Validates a name, returns it cleaned (extra spaces removed, proper capitalization). */
    private String checkName(String label, TextField field, boolean required,
                             List<String> errors, List<Control> invalid) {
        String value = field.getText().trim().replaceAll("\\s+", " ");
        if (value.isEmpty()) {
            if (required) {
                fail(errors, invalid, field, label + " is required.");
            }
            return "";
        }
        if (value.length() < 2 || value.length() > 50) {
            fail(errors, invalid, field, label + " must be 2 to 50 characters long.");
            return "";
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            fail(errors, invalid, field, label + " must contain letters only (a space, hyphen, apostrophe or period between words is allowed). Numbers and symbols are not allowed.");
            return "";
        }
        if (REPEATED_LETTER.matcher(value).find()) {
            fail(errors, invalid, field, label + " looks incorrect (the same letter appears 3 times in a row). Please check the spelling.");
            return "";
        }
        return toTitleCase(value);
    }

    private String toTitleCase(String s) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : s.toLowerCase().toCharArray()) {
            if (capitalizeNext && Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
            if (c == ' ' || c == '-' || c == '\'' || c == '.') {
                capitalizeNext = true;
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------
    // Database helpers
    // ---------------------------------------------------------
    private int getStudentRoleId(Connection conn) throws SQLException {
        String sql = "SELECT role_id FROM roles WHERE role_name = 'Student'";
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
        String sql = "INSERT INTO users (role_id, username, password_hash, full_name, email, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'active')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, roleId);
            ps.setString(2, username);
            ps.setString(3, PasswordUtil.sha256(password));
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
        String sql = "INSERT INTO students (student_no, first_name, middle_name, last_name, suffix, "
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
    // Back to the login screen
    // ---------------------------------------------------------
    @FXML
    private void handleBack() {
        try {
            Navigator.goTo(registerButton, "/view/Login.fxml", "Login");
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
