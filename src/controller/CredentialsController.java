package controller;

import dao.CredentialDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableView;
import model.Credential;
import util.Formats;
import util.StatusBadge;
import util.UserSession;

public class CredentialsController {

    @FXML private TableView<Credential> credentialsTable;
    @FXML private TableColumn<Credential, String> statusColumn;
    @FXML private TableColumn<Credential, String> credentialColumn;
    @FXML private TableColumn<Credential, String> dateColumn;

    @FXML
    private void initialize() {
        credentialsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        credentialsTable.setPlaceholder(new Label("No credentials found."));
        setupStatusIcons();
        util.WrapCells.apply(credentialColumn);
        util.WrapCells.apply(dateColumn);

        try {
            int studentId = new StudentDAO().findStudentIdByUserId(UserSession.getUserId());
            credentialsTable.setItems(FXCollections.observableArrayList(
                    new CredentialDAO().findByStudentId(studentId)));
        } catch (Exception e) {
            e.printStackTrace();
            credentialsTable.setPlaceholder(new Label("Unable to load your credentials. Please make sure MySQL is running."));
        }
    }

    /**
     * Shows the status as a big icon in the middle of the cell:
     * green check = still valid, red X = not valid, clock = waiting.
     * (Expired credentials are not loaded at all.)
     */
    private void setupStatusIcons() {
        statusColumn.setCellFactory(col -> new TableCell<Credential, String>() {
            private final Label icon = new Label();

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }
                String style = StatusBadge.styleFor(item);
                icon.getStyleClass().removeAll("status-icon-ok", "status-icon-bad", "status-icon-wait", "status-icon-info");
                switch (style) {
                    case "badge-success":
                        icon.setText("\u2714");
                        icon.getStyleClass().add("status-icon-ok");
                        break;
                    case "badge-danger":
                        icon.setText("\u2716");
                        icon.getStyleClass().add("status-icon-bad");
                        break;
                    case "badge-warning":
                        icon.setText("\u23F3");
                        icon.getStyleClass().add("status-icon-wait");
                        break;
                    default:
                        icon.setText("\u2022");
                        icon.getStyleClass().add("status-icon-info");
                        break;
                }
                setText(null);
                setGraphic(icon);
                setTooltip(new Tooltip(Formats.capitalize(item)));
            }
        });
        statusColumn.getStyleClass().add("centered-column");
    }
}
