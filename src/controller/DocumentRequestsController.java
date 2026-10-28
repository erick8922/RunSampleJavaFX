package controller;

import dao.AuditLogDAO;
import dao.DocumentRequestDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import model.DocumentRequest;
import util.StatusBadge;
import util.UserSession;

public class DocumentRequestsController {

    @FXML private ComboBox<String> documentTypeCombo;
    @FXML private TextArea purposeArea;
    @FXML private Button submitButton;
    @FXML private Label messageLabel;
    @FXML private TableView<DocumentRequest> requestsTable;
    @FXML private TableColumn<DocumentRequest, String> statusColumn;

    private final DocumentRequestDAO requestDAO = new DocumentRequestDAO();
    private int studentId = -1;

    @FXML
    private void initialize() {
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        requestsTable.setPlaceholder(new Label("You have not submitted any requests yet."));
        StatusBadge.setup(statusColumn);

        try {
            studentId = new StudentDAO().findStudentIdByUserId(UserSession.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            submitButton.setDisable(true);
            showMessage("Unable to connect to the database. Please make sure MySQL is running.", false);
            return;
        }

        if (studentId <= 0) {
            submitButton.setDisable(true);
            showMessage("No student record is linked to this account.", false);
            return;
        }

        loadRequests();
    }

    @FXML
    private void handleSubmit() {
        String documentType = documentTypeCombo.getValue();
        String purpose = purposeArea.getText().trim();

        if (documentType == null) {
            showMessage("Please select a document type.", false);
            return;
        }
        if (purpose.length() < 5) {
            showMessage("Please describe the purpose of your request.", false);
            return;
        }

        try {
            int requestId = requestDAO.create(studentId, documentType, purpose);
            AuditLogDAO.log(UserSession.getUserId(), "REQUEST_DOCUMENT", "document_requests", requestId);

            documentTypeCombo.getSelectionModel().clearSelection();
            purposeArea.clear();
            showMessage("Your request has been submitted. The registrar will review it.", true);
            loadRequests();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Unable to submit your request. Please try again.", false);
        }
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private void loadRequests() {
        try {
            requestsTable.setItems(FXCollections.observableArrayList(requestDAO.findByStudentId(studentId)));
        } catch (Exception e) {
            e.printStackTrace();
            requestsTable.setPlaceholder(new Label("Unable to load your requests. Please make sure MySQL is running."));
        }
    }

    private void showMessage(String text, boolean success) {
        messageLabel.getStyleClass().removeAll("message-success", "message-error");
        messageLabel.getStyleClass().add(success ? "message-success" : "message-error");
        messageLabel.setText(text);
    }
}
