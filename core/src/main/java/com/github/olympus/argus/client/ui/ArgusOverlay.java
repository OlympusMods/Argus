package com.github.olympus.argus.client.ui;

import com.github.olympus.argus.client.Argus;
import com.github.olympus.argus.core.ArgusConfig;
import com.github.olympus.argus.core.ZoomModel;
import com.github.olympus.proteus.client.ui.Canvas;
import com.github.olympus.proteus.client.ui.Overlay;
import java.util.Locale;

public final class ArgusOverlay implements Overlay {
    private static final int BANDS = 18;

    @Override
    public boolean visible() {
        ArgusConfig config = Argus.config();
        ZoomModel zoom = Argus.zoom();
        if (!zoom.visible()) {
            return false;
        }
        return (config.vignette && zoom.shown() > 1.0) || (config.indicator && zoom.active());
    }

    @Override
    public void render(Canvas canvas) {
        ArgusConfig config = Argus.config();
        ZoomModel zoom = Argus.zoom();
        if (config.vignette && zoom.shown() > 1.0) {
            vignette(canvas, Math.min(1.0, Math.log(zoom.shown()) / Math.log(3.0)));
        }
        if (config.indicator && zoom.active()) {
            String label = label(zoom);
            int x = (canvas.width() - canvas.textWidth(label)) / 2;
            int y = canvas.height() / 2 + 14;
            canvas.text(label, x, y, 0xFFFFFFFF, true);
        }
    }

    public static String label(ZoomModel zoom) {
        if (zoom.pull() > 0.0) {
            return String.format(Locale.ROOT, "-%.1f m", zoom.pull());
        }
        return factor(zoom.level());
    }

    public static String factor(double level) {
        if (level >= 100.0) {
            return String.format(Locale.ROOT, "%.0fx", level);
        }
        if (level >= 10.0) {
            return String.format(Locale.ROOT, "%.1fx", level);
        }
        return String.format(Locale.ROOT, "%.2fx", level);
    }

    private static void vignette(Canvas canvas, double strength) {
        int width = canvas.width();
        int height = canvas.height();
        int depth = Math.max(BANDS, Math.min(width, height) / 3);
        int band = Math.max(1, depth / BANDS);
        for (int i = 0; i < BANDS; i++) {
            double edge = 1.0 - i / (double) BANDS;
            int alpha = (int) Math.round(200.0 * strength * edge * edge / 3.0);
            if (alpha <= 0) {
                continue;
            }
            int color = alpha << 24;
            int reach = (i + 1) * band;
            canvas.fill(0, 0, width, reach, color);
            canvas.fill(0, height - reach, width, reach, color);
            canvas.fill(0, reach, reach, height - 2 * reach, color);
            canvas.fill(width - reach, reach, reach, height - 2 * reach, color);
        }
    }
}
