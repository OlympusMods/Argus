package com.github.olympus.argus.client.ui;

import com.github.olympus.argus.client.Argus;
import com.github.olympus.argus.core.ArgusConfig;
import com.github.olympus.proteus.client.ui.ButtonKind;
import com.github.olympus.proteus.client.ui.Theme;
import com.github.olympus.proteus.client.ui.UiHost;
import com.github.olympus.proteus.client.ui.UiShell;
import java.util.Locale;
import net.minecraft.network.chat.Component;

public final class ArgusUi extends UiShell {
    private static final int PAD = 10;
    private static final int GAP = 6;
    private static final int ROW = 20;
    private static final int LINE = 24;

    private enum Tab {
        ZOOM,
        PULL,
        FREELOOK
    }

    private final ArgusConfig config = Argus.config();
    private Tab tab = Tab.ZOOM;
    private boolean dirty;
    private int rowX;
    private int rowW;
    private int stepperW;

    public ArgusUi(UiHost host) {
        super(host);
    }

    @Override
    public void removed() {
        flush();
    }

    @Override
    protected void onTick() {
        flush();
    }

    private void flush() {
        if (dirty) {
            dirty = false;
            Argus.save();
        }
    }

    @Override
    protected void renderContent(Theme theme) {
        int pw = windowWidth();
        int ph = windowHeight();
        int px = windowX(pw);
        int py = windowY(ph);
        theme.window(fade(), px, py, pw, ph);

        int ix = px + PAD;
        int iw = pw - 2 * PAD;
        int y = py + PAD;

        String title = text("argus.screen.title");
        if (theme.centeredTitle() && pw >= 340) {
            gui().centered(title, px + pw / 2, y + 5, theme.titleColor());
        } else {
            fade().push();
            fade().translate(ix, y + 2);
            fade().scale(1.25f, 1.25f);
            gui().text(title, 0, 0, theme.titleColor());
            fade().pop();
        }
        y += 26;

        String[] tabs = {text("argus.tab.zoom"), text("argus.tab.pull"), text("argus.tab.freelook")};
        String[] tips = {text("argus.tab.zoom.tooltip"), text("argus.tab.pull.tooltip"),
            text("argus.tab.freelook.tooltip")};
        int chosen = gui().segmented("tabs", ix, y, iw, 20, tabs, tab.ordinal(), tips, null);
        if (chosen >= 0) {
            tab = Tab.values()[chosen];
            restartContentFade();
        }
        y += 20 + GAP;

        int footerY = py + ph - PAD - ROW;
        int hintY = footerY - GAP - 10;
        int viewH = hintY - GAP - y;

        float before = beginContent();
        float scroll = gui().scrollBegin("options-" + tab.name(), ix, y, iw, viewH);
        rowX = ix + 6;
        rowW = iw - 22;
        stepperW = Math.min(124, rowW / 2);
        int top = y - Math.round(scroll);
        int end = switch (tab) {
            case ZOOM -> zoomRows(top);
            case PULL -> pullRows(top);
            case FREELOOK -> freelookRows(top);
        };
        gui().scrollEnd(end - top);
        endContent(before);

        gui().text(gui().fit(text("argus.screen.keys"), iw), ix, hintY, theme.textSecondary());
        if (gui().button("done", ix, footerY, iw, ROW, text("argus.screen.done"), ButtonKind.PRIMARY, false, true,
                null)) {
            flush();
            requestClose();
        }
    }

    private int zoomRows(int y) {
        config.zoomToggle = toggle("zoomToggle", y, config.zoomToggle);
        y += LINE;
        config.initialZoom = number("initialZoom", y, config.initialZoom, zoomStep(config.initialZoom), 1.0,
                config.zoomCeiling(), ArgusOverlay.factor(config.initialZoom));
        y += LINE;
        config.rememberZoom = toggle("rememberZoom", y, config.rememberZoom);
        y += LINE;
        config.unlimitedZoom = toggle("unlimitedZoom", y, config.unlimitedZoom);
        y += LINE;
        if (!config.unlimitedZoom) {
            config.maxZoom = number("maxZoom", y, config.maxZoom, zoomStep(config.maxZoom), 2.0,
                    ArgusConfig.MAX_ZOOM_LIMIT, ArgusOverlay.factor(config.maxZoom));
            y += LINE;
        }
        config.scrollStep = number("scrollStep", y, config.scrollStep, 0.05, 1.05, 3.0, multiplier(config.scrollStep));
        y += LINE;
        config.fineStep = number("fineStep", y, config.fineStep, 0.01, 1.01, 1.5, multiplier(config.fineStep));
        y += LINE;
        config.smoothZoom = toggle("smoothZoom", y, config.smoothZoom);
        y += LINE;
        if (config.smoothZoom) {
            config.zoomSpeed = number("zoomSpeed", y, config.zoomSpeed, 1.0, 1.0, 40.0, whole(config.zoomSpeed));
            y += LINE;
        }
        config.sensitivityScaling = (int) number("sensitivityScaling", y, config.sensitivityScaling, 5.0, 0.0, 100.0,
                percent(config.sensitivityScaling));
        y += LINE;
        config.cinematic = toggle("cinematic", y, config.cinematic);
        y += LINE;
        if (config.cinematic) {
            config.cinematicStrength = (int) number("cinematicStrength", y, config.cinematicStrength, 5.0, 5.0, 100.0,
                    percent(config.cinematicStrength));
            y += LINE;
        }
        config.middleClickReset = toggle("middleClickReset", y, config.middleClickReset);
        y += LINE;
        config.indicator = toggle("indicator", y, config.indicator);
        y += LINE;
        config.hideCrosshair = toggle("hideCrosshair", y, config.hideCrosshair);
        y += LINE;
        config.vignette = toggle("vignette", y, config.vignette);
        y += LINE;
        config.zoomHideHand = toggle("zoomHideHand", y, config.zoomHideHand);
        y += LINE;
        config.hideHud = toggle("hideHud", y, config.hideHud);
        y += LINE;
        if (config.initialZoom > config.zoomCeiling()) {
            config.initialZoom = config.zoomCeiling();
        }
        return y;
    }

    private int pullRows(int y) {
        config.pullEnabled = toggle("pullEnabled", y, config.pullEnabled);
        y += LINE;
        if (config.pullEnabled) {
            config.pullMax = number("pullMax", y, config.pullMax, 1.0, 1.0, 64.0, blocks(config.pullMax));
            y += LINE;
            config.pullStep = number("pullStep", y, config.pullStep, 0.25, 0.25, 8.0, blocks(config.pullStep));
            y += LINE;
            config.pullSmooth = toggle("pullSmooth", y, config.pullSmooth);
            y += LINE;
            if (config.pullSmooth) {
                config.pullSpeed = number("pullSpeed", y, config.pullSpeed, 1.0, 1.0, 40.0, whole(config.pullSpeed));
                y += LINE;
            }
        }
        config.widenFov = toggle("widenFov", y, config.widenFov);
        y += LINE;
        if (config.widenFov) {
            config.widenMaxFov = (int) number("widenMaxFov", y, config.widenMaxFov, 5.0, 30.0, 160.0,
                    degrees(config.widenMaxFov));
            y += LINE;
        }
        return y;
    }

    private int freelookRows(int y) {
        config.freelookToggle = toggle("freelookToggle", y, config.freelookToggle);
        y += LINE;
        config.autoThirdPerson = toggle("autoThirdPerson", y, config.autoThirdPerson);
        y += LINE;

        label("release", y);
        String[] modes = {text("argus.release.smooth"), text("argus.release.instant"), text("argus.release.stay")};
        String[] tips = {text("argus.release.smooth.tooltip"), text("argus.release.instant.tooltip"),
            text("argus.release.stay.tooltip")};
        int width = Math.min(210, rowW * 3 / 5);
        int picked = gui().segmented("release", rowX + rowW - width, y, width, ROW, modes, config.release.ordinal(),
                tips, null);
        if (picked >= 0) {
            config.release = ArgusConfig.Release.values()[picked];
            dirty = true;
        }
        y += LINE;
        if (config.release == ArgusConfig.Release.SMOOTH) {
            config.returnSpeed = number("returnSpeed", y, config.returnSpeed, 1.0, 1.0, 40.0,
                    whole(config.returnSpeed));
            y += LINE;
        }
        config.freelookSensitivity = (int) number("freelookSensitivity", y, config.freelookSensitivity, 10.0, 10.0,
                300.0, percent(config.freelookSensitivity));
        y += LINE;
        config.invertPitch = toggle("invertPitch", y, config.invertPitch);
        y += LINE;
        config.pitchMax = (int) number("pitchMax", y, config.pitchMax, 5.0, 0.0, 90.0, degrees(config.pitchMax));
        y += LINE;
        config.pitchMin = -(int) number("pitchMin", y, -config.pitchMin, 5.0, 0.0, 90.0, degrees(-config.pitchMin));
        y += LINE;
        config.freelookHideHand = toggle("freelookHideHand", y, config.freelookHideHand);
        y += LINE;
        config.headFollows = toggle("headFollows", y, config.headFollows);
        y += LINE;
        return y;
    }

    private boolean toggle(String id, int y, boolean value) {
        if (gui().switchRow(id, rowX, y, rowW, ROW, text("argus.option." + id), value, true,
                text("argus.option." + id + ".tooltip"), text("argus.state.on"), text("argus.state.off"))) {
            dirty = true;
            return !value;
        }
        return value;
    }

    private void label(String id, int y) {
        gui().text(gui().fit(text("argus.option." + id), rowW - stepperW - 8), rowX, gui().textY(y, ROW),
                gui().theme().textPrimary());
    }

    private double number(String id, int y, double value, double step, double low, double high, String shown) {
        label(id, y);
        int change = gui().stepper(id, rowX + rowW - stepperW, y, stepperW, ROW, shown,
                text("argus.option." + id + ".tooltip"), value > low + 1.0e-9, value < high - 1.0e-9);
        if (change == 0) {
            return value;
        }
        dirty = true;
        double next = Math.round((value + change * step) / step) * step;
        return Math.max(low, Math.min(high, Math.round(next * 1000.0) / 1000.0));
    }

    private static double zoomStep(double value) {
        if (value < 10.0) {
            return 0.5;
        }
        if (value < 50.0) {
            return 5.0;
        }
        if (value < 200.0) {
            return 10.0;
        }
        return 50.0;
    }

    private static String multiplier(double value) {
        return String.format(Locale.ROOT, "x%.2f", value);
    }

    private static String whole(double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static String percent(int value) {
        return value + "%";
    }

    private static String degrees(int value) {
        return value + "°";
    }

    private static String blocks(double value) {
        return text("argus.unit.blocks", String.format(Locale.ROOT, "%.2f", value));
    }

    private static String text(String key, Object... arguments) {
        return Component.translatable(key, arguments).getString();
    }
}
