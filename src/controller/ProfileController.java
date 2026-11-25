package controller;

import dao.AuditLogDAO;
import dao.StudentDAO;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import model.Student;
import util.Formats;
import util.StatusBadge;
import util.UserSession;

public class ProfileController {

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label studentNoLabel;
    @FXML private Label statusBadge;
    @FXML private Label genderValue;
    @FXML private Label birthDateValue;
    @FXML private Label gradeSectionValue;
    @FXML private Label schoolYearValue;
    @FXML private Label emailValue;
    @FXML private Label usernameValue;
    @FXML private TextField contactNoField;
    @FXML private TextField addressField;
    @FXML private Button saveButton;
    @FXML private Label messageLabel;

    private final StudentDAO studentDAO = new StudentDAO();
    private Student student;

    @FXML
    private void initialize() {
        // Contact number: digits only, maximum of 11
        contactNoField.setTextFormatter(new TextFormatter<String>(ch -> {
            if (!ch.isContentChange()) return ch;
            ch.setText(ch.getText().replaceAll("\\D", ""));
            return ch.getControlNewText().length() <= 11 ? ch : null;
        }));

        try {
            student = studentDAO.findByUserId(UserSession.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            saveButton.setDisable(true);
            showMessage("Unable to load your profile. Please make sure MySQL is running.", false);
            return;
        }

        if (student == null) {
            saveButton.setDisable(true);
            showMessage("No student record is linked to this account.", false);
            return;
        }

        // Summary
        nameLabel.setText(student.getFullName());
        avatarLabel.setText(initials(student));
        studentNoLabel.setText("LRN: " + Formats.text(student.getStudentNo()));
        StatusBadge.apply(statusBadge, student.getStatus());

        // Personal information
        genderValue.setText(Formats.text(student.getGender()));
        birthDateValue.setText(student.getBirthDate() == null
                ? "-" : student.getBirthDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        loadEnrollmentInfo();
        emailValue.setText(Formats.text(student.getEmail()));
        usernameValue.setText(Formats.text(student.getUsername()));

        // Editable fields
        contactNoField.setText(student.getContactNo() == null ? "" : student.getContactNo());
        addressField.setText(student.getAddress() == null ? "" : student.getAddress());
    }

    @FXML
    private void handleSave() {
        String contactNo = contactNoField.getText().trim();
        String address = addressField.getText().trim();

        if (!contactNo.isEmpty() && !contactNo.matches("09\\d{9}")) {
            showMessage("Contact number must be 11 digits and start with 09 (e.g., 09171234567).", false);
            return;
        }
        if (!address.isEmpty() && (address.length() < 8 || !address.matches(".*\\p{L}.*"))) {
            showMessage("Address is too short. Please include Purok/Street, Barangay, City/Municipality and Province.", false);
            return;
        }

        try {
            studentDAO.updateContactInfo(student.getStudentId(), contactNo, address);
            student.setContactNo(contactNo);
            student.setAddress(address);
            AuditLogDAO.log(UserSession.getUserId(), "UPDATE_PROFILE", "students", student.getStudentId());
            showMessage("Your changes have been saved.", true);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Unable to save your changes. Please try again.", false);
        }
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private void loadEnrollmentInfo() {
        String gradeSection = "-";
        String schoolYear = "-";
        try {
            String[] current = studentDAO.findCurrentEnrollment(student.getStudentId());
            if (current != null) {
                gradeSection = Formats.text(current[0]);
                schoolYear = Formats.text(current[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        gradeSectionValue.setText(gradeSection);
        schoolYearValue.setText(schoolYear);
    }

    private void showMessage(String text, boolean success) {
        messageLabel.getStyleClass().removeAll("message-success", "message-error");
        messageLabel.getStyleClass().add(success ? "message-success" : "message-error");
        messageLabel.setText(text);
    }

    private String initials(Student s) {
        String first = s.getFirstName() == null || s.getFirstName().isEmpty() ? "" : s.getFirstName().substring(0, 1);
        String last = s.getLastName() == null || s.getLastName().isEmpty() ? "" : s.getLastName().substring(0, 1);
        String result = (first + last).toUpperCase();
        return result.isEmpty() ? "?" : result;
    }
}
