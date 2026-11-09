package com.github.olympus.argus.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FreelookModelTest {
    @Test
    void turningMovesOnlyTheCamera() {
        FreelookModel look = new FreelookModel(new ArgusConfig());
        look.start(10.0, 5.0);
        look.turn(30.0, 200.0);
        assertEquals(40.0, look.yaw(), 1.0e-9);
        assertEquals(90.0, look.pitch(), 1.0e-9);
    }

    @Test
    void pitchLimitsAndInvertApply() {
        ArgusConfig config = new ArgusConfig();
        config.pitchMin = -30;
        config.invertPitch = true;
        FreelookModel look = new FreelookModel(config);
        look.start(0.0, 0.0);
        look.turn(0.0, 80.0);
        assertEquals(-30.0, look.pitch(), 1.0e-9);
    }

    @Test
    void smoothReleaseReturnsTheShortWay() {
        FreelookModel look = new FreelookModel(new ArgusConfig());
        look.start(170.0, 0.0);
        look.turn(30.0, 0.0);
        look.stop();
        assertTrue(look.detached());
        look.update(0.01, 170.0, 0.0);
        assertTrue(look.yaw() < 200.0 && look.yaw() > 170.0);
        for (int i = 0; i < 2000 && look.detached(); i++) {
            look.update(0.016, 170.0, 0.0);
        }
        assertFalse(look.detached());
    }

    @Test
    void instantReleaseSnapsBack() {
        ArgusConfig config = new ArgusConfig();
        config.release = ArgusConfig.Release.INSTANT;
        FreelookModel look = new FreelookModel(config);
        look.start(0.0, 0.0);
        look.turn(90.0, 0.0);
        look.stop();
        assertFalse(look.detached());
    }

    @Test
    void stayKeepsTheCameraUntilRecentered() {
        ArgusConfig config = new ArgusConfig();
        config.release = ArgusConfig.Release.STAY;
        FreelookModel look = new FreelookModel(config);
        look.start(0.0, 0.0);
        look.turn(90.0, 0.0);
        look.stop();
        look.update(5.0, 0.0, 0.0);
        assertTrue(look.detached());
        assertEquals(90.0, look.yaw(), 1.0e-9);
        look.recenter();
        assertFalse(look.detached());
    }

    @Test
    void smoothReleaseEndsWhileThePlayerKeepsTurning() {
        FreelookModel look = new FreelookModel(new ArgusConfig());
        look.start(0.0, 0.0);
        look.turn(120.0, 40.0);
        look.stop();
        double playerYaw = 0.0;
        int frames = 0;
        while (look.detached() && frames < 600) {
            playerYaw += 3.0;
            look.update(0.016, playerYaw, 0.0);
            frames++;
        }
        assertFalse(look.detached());
        assertTrue(frames < 90);
    }
}
