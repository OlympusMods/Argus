package com.github.olympus.argus.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZoomModelTest {
    private static ArgusConfig config() {
        ArgusConfig config = new ArgusConfig();
        config.smoothZoom = false;
        config.pullSmooth = false;
        return config;
    }

    @Test
    void startsAtTheInitialLevel() {
        ZoomModel zoom = new ZoomModel(config());
        zoom.start();
        zoom.update(0.0);
        assertEquals(4.0, zoom.shown(), 1.0e-9);
    }

    @Test
    void scrollMultipliesAndStopsAtTheMaximum() {
        ArgusConfig config = config();
        config.maxZoom = 10.0;
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        zoom.scroll(1.0, false, false, 70.0);
        assertEquals(5.0, zoom.level(), 1.0e-9);
        for (int i = 0; i < 50; i++) {
            zoom.scroll(1.0, false, false, 70.0);
        }
        assertEquals(10.0, zoom.level(), 1.0e-9);
    }

    @Test
    void fineStepIsSmaller() {
        ZoomModel zoom = new ZoomModel(config());
        zoom.start();
        zoom.scroll(1.0, true, false, 70.0);
        assertEquals(4.2, zoom.level(), 1.0e-9);
    }

    @Test
    void firstPersonStopsAtOne() {
        ZoomModel zoom = new ZoomModel(config());
        zoom.start();
        for (int i = 0; i < 40; i++) {
            zoom.scroll(-1.0, false, false, 70.0);
        }
        assertEquals(1.0, zoom.level(), 1.0e-9);
        assertEquals(0.0, zoom.pull(), 1.0e-9);
    }

    @Test
    void firstPersonWidensUpToTheLimit() {
        ArgusConfig config = config();
        config.widenFov = true;
        config.widenMaxFov = 110;
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        for (int i = 0; i < 40; i++) {
            zoom.scroll(-1.0, false, false, 70.0);
        }
        zoom.update(0.0);
        assertEquals(110.0, zoom.applyFov(70.0), 1.0e-6);
    }

    @Test
    void thirdPersonPullsBackAndComesBackIn() {
        ArgusConfig config = config();
        config.pullMax = 3.0;
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        for (int i = 0; i < 40; i++) {
            zoom.scroll(-1.0, false, true, 70.0);
        }
        assertEquals(1.0, zoom.level(), 1.0e-9);
        assertEquals(3.0, zoom.pull(), 1.0e-9);
        zoom.scroll(1.0, false, true, 70.0);
        assertEquals(2.0, zoom.pull(), 1.0e-9);
        zoom.scroll(3.0, false, true, 70.0);
        assertEquals(0.0, zoom.pull(), 1.0e-9);
        zoom.scroll(1.0, false, true, 70.0);
        assertEquals(1.25, zoom.level(), 1.0e-9);
    }

    @Test
    void remembersTheLastLevel() {
        ArgusConfig config = config();
        config.rememberZoom = true;
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        zoom.scroll(1.0, false, false, 70.0);
        assertTrue(zoom.stop());
        zoom.start();
        assertEquals(5.0, zoom.level(), 1.0e-9);
    }

    @Test
    void fovStaysFiniteAtExtremeZoom() {
        ArgusConfig config = config();
        config.unlimitedZoom = true;
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        for (int i = 0; i < 400; i++) {
            zoom.scroll(3.0, false, false, 70.0);
        }
        zoom.update(0.0);
        assertEquals(ArgusConfig.HARD_MAX_ZOOM, zoom.level(), 1.0e-6);
        double fov = zoom.applyFov(70.0);
        assertTrue(fov > 0.0 && fov < 0.01);
        assertFalse(Double.isNaN(fov));
        assertTrue((float) fov > 0.0f);
    }

    @Test
    void smoothZoomSettlesOnTheTarget() {
        ArgusConfig config = new ArgusConfig();
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        zoom.update(0.016);
        assertTrue(zoom.shown() > 1.0 && zoom.shown() < 4.0);
        for (int i = 0; i < 600; i++) {
            zoom.update(0.016);
        }
        assertEquals(4.0, zoom.shown(), 1.0e-9);
        zoom.stop();
        for (int i = 0; i < 600; i++) {
            zoom.update(0.016);
        }
        assertEquals(1.0, zoom.shown(), 1.0e-9);
        assertFalse(zoom.visible());
    }

    @Test
    void sensitivityFollowsTheZoom() {
        ArgusConfig config = config();
        ZoomModel zoom = new ZoomModel(config);
        zoom.start();
        zoom.update(0.0);
        assertEquals(0.25, zoom.sensitivity(), 1.0e-9);
        config.sensitivityScaling = 0;
        assertEquals(1.0, zoom.sensitivity(), 1.0e-9);
    }
}
