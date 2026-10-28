package controller;

import dao.StudentDAO;
import dao.TranscriptDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.TranscriptRecord;
import util.StatusBadge;
import util.UserSession;

public class TranscriptController {

    @FXML private TableView<TranscriptRecord> transcriptTable;
    @FXML private TableColumn<TranscriptRecord, String> statusColumn;

    @FXML
    private void initialize() {
        transcriptTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transcriptTable.setPlaceholder(new Label("No transcript records found."));
        StatusBadge.setup(statusColumn);

        try {
            int studentId = new StudentDAO().findStudentIdByUserId(UserSession.getUserId());
            transcriptTable.setItems(FXCollections.observableArrayList(
                    new TranscriptDAO().findByStudentId(studentId)));
        } catch (Exception e) {
            e.printStackTrace();
            transcriptTable.setPlaceholder(new Label("Unable to load your transcript. Please make sure MySQL is running."));
        }
    }
}
