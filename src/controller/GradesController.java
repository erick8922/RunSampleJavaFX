package controller;

import dao.GradeDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Grade;
import util.StatusBadge;
import util.UserSession;

public class GradesController {

    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, String> remarksColumn;

    @FXML
    private void initialize() {
        gradesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        gradesTable.setPlaceholder(new Label("No grades have been posted yet."));
        StatusBadge.setup(remarksColumn);

        try {
            int studentId = new StudentDAO().findStudentIdByUserId(UserSession.getUserId());
            gradesTable.setItems(FXCollections.observableArrayList(
                    new GradeDAO().findByStudentId(studentId)));
        } catch (Exception e) {
            e.printStackTrace();
            gradesTable.setPlaceholder(new Label("Unable to load your grades. Please make sure MySQL is running."));
        }
    }
}
