package util;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

/**
 * Makes every FlowPane (rows of form fields, cards, buttons) fit the screen.
 * Each item keeps its normal width on a wide window, but becomes narrower
 * on a small window. When items cannot sit side by side they drop to the
 * next line, so on a phone-sized window the form becomes one column.
 */
public class ResponsiveForm {

    /** Call this on any screen or part of a screen after it is created. */
    public static void fit(Node node) {
        if (node instanceof FlowPane) {
            fitFlow((FlowPane) node);
        }
        if (node instanceof ScrollPane) {
            Node content = ((ScrollPane) node).getContent();
            if (content != null) {
                fit(content);
            }
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                fit(child);
            }
        }
    }

    private static void fitFlow(FlowPane flow) {
        if (flow.getProperties().containsKey("responsiveFit")) {
            return;
        }
        flow.getProperties().put("responsiveFit", Boolean.TRUE);

        for (Node child : flow.getChildren()) {
            if (!(child instanceof Region)) {
                continue;
            }
            Region item = (Region) child;

            // The item keeps its normal width (it is NOT locked to a number).
            // It is only stopped from being wider than the row it sits in, so on a
            // small window it becomes narrower, and its text wraps to the next line.
            item.setMinWidth(0);
            item.maxWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> {
                        double available = flow.getWidth();
                        return available <= 0 ? Double.MAX_VALUE : Math.max(120, available);
                    },
                    flow.widthProperty()));
        }
    }
}
