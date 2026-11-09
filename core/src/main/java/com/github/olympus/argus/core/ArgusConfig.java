package com.github.olympus.argus.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArgusConfig {
    public static final double HARD_MAX_ZOOM = 100000.0;
    public static final double MAX_ZOOM_LIMIT = 1000.0;

    private static final Logger LOGGER = LoggerFactory.getLogger("argus");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum Release {
        SMOOTH,
        INSTANT,
        STAY
    }

    public boolean zoomToggle = false;
    public double scrollStep = 1.25;
    public double fineStep = 1.05;
    public double maxZoom = 50.0;
    public boolean unlimitedZoom = false;
    public double initialZoom = 4.0;
    public boolean rememberZoom = false;
    public double lastZoom = 4.0;
    public boolean smoothZoom = true;
    public double zoomSpeed = 24.0;
    public int sensitivityScaling = 100;
    public boolean cinematic = false;
    public int cinematicStrength = 50;
    public boolean middleClickReset = true;
    public boolean hideCrosshair = false;
    public boolean vignette = false;
    public boolean zoomHideHand = false;
    public boolean hideHud = false;
    public boolean indicator = true;

    public boolean pullEnabled = true;
    public double pullMax = 16.0;
    public double pullStep = 1.0;
    public boolean pullSmooth = true;
    public double pullSpeed = 10.0;
    public boolean widenFov = false;
    public int widenMaxFov = 110;

    public boolean freelookToggle = false;
    public boolean autoThirdPerson = true;
    public Release release = Release.SMOOTH;
    public double returnSpeed = 12.0;
    public boolean invertPitch = false;
    public int freelookSensitivity = 100;
    public int pitchMin = -90;
    public int pitchMax = 90;
    public boolean freelookHideHand = true;
    public boolean headFollows = false;

    public double zoomCeiling() {
        return unlimitedZoom ? HARD_MAX_ZOOM : maxZoom;
    }

    public ArgusConfig sanitize() {
        scrollStep = clamp(scrollStep, 1.01, 4.0);
        fineStep = clamp(fineStep, 1.001, 2.0);
        maxZoom = clamp(maxZoom, 1.5, MAX_ZOOM_LIMIT);
        initialZoom = clamp(initialZoom, 1.0, zoomCeiling());
        lastZoom = clamp(lastZoom, 1.0, HARD_MAX_ZOOM);
        zoomSpeed = clamp(zoomSpeed, 1.0, 40.0);
        sensitivityScaling = (int) clamp(sensitivityScaling, 0, 100);
        cinematicStrength = (int) clamp(cinematicStrength, 5, 100);
        pullMax = clamp(pullMax, 1.0, 64.0);
        pullStep = clamp(pullStep, 0.25, 8.0);
        pullSpeed = clamp(pullSpeed, 1.0, 40.0);
        widenMaxFov = (int) clamp(widenMaxFov, 30, 160);
        returnSpeed = clamp(returnSpeed, 1.0, 40.0);
        freelookSensitivity = (int) clamp(freelookSensitivity, 10, 300);
        pitchMin = (int) clamp(pitchMin, -90, 0);
        pitchMax = (int) clamp(pitchMax, 0, 90);
        if (release == null) {
            release = Release.SMOOTH;
        }
        return this;
    }

    public static ArgusConfig load(Path file) {
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                ArgusConfig read = GSON.fromJson(reader, ArgusConfig.class);
                if (read != null) {
                    return read.sanitize();
                }
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not read {}: {}", file, exception.getMessage());
            }
        }
        return new ArgusConfig();
    }

    public void save(Path file) {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            LOGGER.warn("Could not save {}: {}", file, exception.getMessage());
        }
    }

    private static double clamp(double value, double low, double high) {
        if (Double.isNaN(value)) {
            return low;
        }
        return Math.max(low, Math.min(high, value));
    }
}
