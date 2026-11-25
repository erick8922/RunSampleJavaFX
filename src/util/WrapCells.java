package util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;

/**
 * Makes a table column wrap long text onto the next line instead of
 * cutting it off with "...". The row grows taller to fit the text.
 */
public class WrapCells {

    public static <S, T> void apply(TableColumn<S, T> column) {
        column.setCellFactory(col -> new TableCell<S, T>() {
            private final Text text = new Text();

            {
                text.getStyleClass().add("wrap-text");
                // The text may be as wide as the column (minus a little space for padding).
                text.wrappingWidthProperty().bind(column.widthProperty().subtract(26));
                setGraphic(text);
                setPrefHeight(USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText("");
                    setGraphic(null);
                } else {
                    text.setText(item.toString());
                    setGraphic(text);
                }
            }
        });
    }
}
