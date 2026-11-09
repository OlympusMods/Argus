package com.github.olympus.argus.core;

public final class FreelookModel {
    private final ArgusConfig config;
    private boolean held;
    private boolean detached;
    private double yaw;
    private double pitch;
    private boolean returning;
    private double yawGap;
    private double pitchGap;

    public FreelookModel(ArgusConfig config) {
        this.config = config;
    }

    public boolean held() {
        return held;
    }

    public boolean detached() {
        return detached;
    }

    public double yaw() {
        return yaw;
    }

    public double pitch() {
        return pitch;
    }

    public void start(double playerYaw, double playerPitch) {
        if (held) {
            return;
        }
        held = true;
        if (!detached) {
            detached = true;
            yaw = playerYaw;
            pitch = playerPitch;
        }
    }

    public void stop() {
        if (!held) {
            return;
        }
        held = false;
        if (config.release == ArgusConfig.Release.INSTANT) {
            detached = false;
        }
    }

    public void recenter() {
        returning = false;
        held = false;
        detached = false;
    }

    public void turn(double yawDegrees, double pitchDegrees) {
        if (!held) {
            return;
        }
        double scale = config.freelookSensitivity / 100.0;
        yaw += yawDegrees * scale;
        pitch += (config.invertPitch ? -pitchDegrees : pitchDegrees) * scale;
        pitch = Math.max(config.pitchMin, Math.min(config.pitchMax, pitch));
    }

    public void update(double seconds, double playerYaw, double playerPitch) {
        if (held || !detached || config.release == ArgusConfig.Release.STAY) {
            returning = false;
            return;
        }
        if (!returning) {
            returning = true;
            yawGap = wrap(yaw - playerYaw);
            pitchGap = pitch - playerPitch;
        }
        double time = Math.max(0.0, seconds);
        double keep = Math.exp(-time * config.returnSpeed);
        yawGap = shrink(yawGap * keep, time * config.returnSpeed * 2.0);
        pitchGap = shrink(pitchGap * keep, time * config.returnSpeed * 2.0);
        yaw = playerYaw + yawGap;
        pitch = playerPitch + pitchGap;
        if (yawGap == 0.0 && pitchGap == 0.0) {
            detached = false;
            returning = false;
        }
    }

    private static double shrink(double gap, double amount) {
        if (Math.abs(gap) <= amount) {
            return 0.0;
        }
        return gap - Math.signum(gap) * amount;
    }

    public static double wrap(double degrees) {
        double wrapped = degrees % 360.0;
        if (wrapped >= 180.0) {
            wrapped -= 360.0;
        }
        if (wrapped < -180.0) {
            wrapped += 360.0;
        }
        return wrapped;
    }
}
