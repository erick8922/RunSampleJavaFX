package controller;

import dao.EnrollmentDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.EnrolledSubject;
import util.StatusBadge;
import util.UserSession;

public class SubjectsController {

    @FXML private TableView<EnrolledSubject> subjectsTable;
    @FXML private TableColumn<EnrolledSubject, String> statusColumn;

    @FXML
    private void initialize() {
        subjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        subjectsTable.setPlaceholder(new Label("You are not enrolled in any subjects yet."));
        StatusBadge.setup(statusColumn);

        try {
            int studentId = new StudentDAO().findStudentIdByUserId(UserSession.getUserId());
            subjectsTable.setItems(FXCollections.observableArrayList(
                    new EnrollmentDAO().findByStudentId(studentId)));
        } catch (Exception e) {
            e.printStackTrace();
            subjectsTable.setPlaceholder(new Label("Unable to load your subjects. Please make sure MySQL is running."));
        }
    }
}
