package controller;

import dao.CredentialDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Credential;
import util.StatusBadge;
import util.UserSession;

public class CredentialsController {

    @FXML private TableView<Credential> credentialsTable;
    @FXML private TableColumn<Credential, String> statusColumn;

    @FXML
    private void initialize() {
        credentialsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        credentialsTable.setPlaceholder(new Label("No credentials found."));
        StatusBadge.setup(statusColumn);

        try {
            int studentId = new StudentDAO().findStudentIdByUserId(UserSession.getUserId());
            credentialsTable.setItems(FXCollections.observableArrayList(
                    new CredentialDAO().findByStudentId(studentId)));
        } catch (Exception e) {
            e.printStackTrace();
            credentialsTable.setPlaceholder(new Label("Unable to load your credentials. Please make sure MySQL is running."));
        }
    }
}
