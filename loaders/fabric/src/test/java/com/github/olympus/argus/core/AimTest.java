package com.github.olympus.argus.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AimTest {
    @Test
    void tinyStepsAddUpInsteadOfBeingLost() {
        Aim aim = new Aim();
        float yaw = 3000.0f;
        float pitch = 0.0f;
        double input = 1.0e-5;
        double worst = 0.0;
        for (int i = 0; i < 20000; i++) {
            double steeredYaw = aim.steerYaw(yaw, input);
            double steeredPitch = aim.steerPitch(pitch, 0.0);
            yaw += (float) steeredYaw * 0.15f;
            pitch += (float) steeredPitch * 0.15f;
            aim.settle(yaw, pitch);
            worst = Math.max(worst, Math.abs(aim.residualYaw(yaw)));
        }
        double expected = 3000.0 + 20000 * input * 0.15;
        assertEquals(expected, yaw + aim.residualYaw(yaw), 1.0e-6);
        assertTrue(worst < 5.0e-4);
    }

    @Test
    void anOutsideTurnResyncs() {
        Aim aim = new Aim();
        aim.steerYaw(10.0f, 1.0);
        aim.settle(10.15f, 0.0f);
        assertEquals(0.0, aim.residualYaw(50.0f), 0.0);
        double steered = aim.steerYaw(50.0f, 0.0);
        assertEquals(0.0, steered, 1.0e-9);
    }
}
