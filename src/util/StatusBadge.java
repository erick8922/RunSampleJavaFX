package util;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/** Shows a status (active, pending, approved...) as a colored badge. */
public class StatusBadge {

    public static String styleFor(String status) {
        String s = status == null ? "" : status.trim().toLowerCase();
        switch (s) {
            case "active":
            case "approved":
            case "released":
            case "issued":
            case "completed":
            case "enrolled":
            case "ready":
            case "passed":
            case "graduated":
                return "badge-success";
            case "pending":
            case "processing":
                return "badge-warning";
            case "rejected":
            case "declined":
            case "cancelled":
            case "canceled":
            case "inactive":
            case "dropped":
            case "expired":
            case "revoked":
            case "failed":
                return "badge-danger";
            default:
                return "badge-info";
        }
    }

    public static void apply(Label label, String status) {
        label.setText(Formats.capitalize(status));
        label.getStyleClass().removeAll("badge", "badge-success", "badge-warning", "badge-danger", "badge-info");
        label.getStyleClass().addAll("badge", styleFor(status));
    }

    /** Makes a table column draw its text as a status badge. */
    public static <T> void setup(TableColumn<T, String> column) {
        column.setCellFactory(col -> new TableCell<T, String>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    apply(badge, item);
                    setText(null);
                    setGraphic(badge);
                }
            }
        });
    }
}
