package util;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

/**
 * Makes all text in a screen grow and shrink with the window width.
 * app.css writes every font size in "em" (a multiple of the base size),
 * so changing the base size here changes ALL text at once.
 */
public class ResponsiveFont {

    // ===== EDIT THESE TO CHANGE THE TEXT SIZE =====
    /** Normal text size in px when the window is wide. Try 16, 17 or 18. */
    private static final double BASE_PX = 16;
    /** Makes ALL text bigger or smaller. 1.0 = normal, 1.15 = 15% bigger, 1.3 = 30% bigger. */
    private static final double BASE_BOOST = 1.0;
    /** How small text may get when the window is small. 1.0 = never shrink, 0.9 = shrink up to 10%. */
    private static final double MIN_SCALE = 0.90;
    /** The base size is never smaller than this many px. */
    private static final double MIN_PX = 15;
    // ===============================================

    private static final double FULL_WIDTH = 1000;   // at this width or more: normal size
    private static final double SMALL_WIDTH = 400;   // at this width: smallest size
    private static final double COMPACT_BELOW = 600; // below this width: less empty space around things

    public static void bind(Parent root) {
        if (!(root instanceof Region)) {
            return;
        }
        Region region = (Region) root;
        region.widthProperty().addListener((obs, oldW, newW) -> apply(region, newW.doubleValue()));
        Platform.runLater(() -> apply(region, region.getWidth()));
    }

    private static void apply(Region region, double width) {
        if (width <= 0) {
            return;
        }
        // Small window (phone size): the CSS uses this class to shrink the spaces around things.
        boolean compact = width < COMPACT_BELOW;
        if (compact && !region.getStyleClass().contains("compact")) {
            region.getStyleClass().add("compact");
        } else if (!compact) {
            region.getStyleClass().remove("compact");
        }

        double t = (width - SMALL_WIDTH) / (FULL_WIDTH - SMALL_WIDTH);
        double scale = MIN_SCALE + (1 - MIN_SCALE) * Math.max(0, Math.min(1, t));

        double px = Math.max(MIN_PX, Math.round(BASE_PX * scale * BASE_BOOST));

        Object last = region.getProperties().get("responsiveFontPx");
        if (last != null && ((Double) last) == px) {
            return;   // size did not change, no need to restyle
        }
        region.getProperties().put("responsiveFontPx", px);
        region.setStyle("-fx-font-size: " + (int) px + "px;");
    }
}
