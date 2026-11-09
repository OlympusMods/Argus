package com.github.olympus.argus.core;

public final class ZoomModel {
    private static final double EPSILON = 1.0e-4;

    private final ArgusConfig config;
    private boolean active;
    private double level = 1.0;
    private double shown = 1.0;
    private double pull;
    private double pullShown;

    public ZoomModel(ArgusConfig config) {
        this.config = config;
    }

    public boolean active() {
        return active;
    }

    public double level() {
        return level;
    }

    public double shown() {
        return shown;
    }

    public double pull() {
        return pull;
    }

    public double pullShown() {
        return pullShown;
    }

    public boolean visible() {
        return active || Math.abs(shown - 1.0) > EPSILON;
    }

    public boolean pulled() {
        return pullShown > EPSILON || (active && pull > 0.0);
    }

    public void start() {
        if (active) {
            return;
        }
        active = true;
        pull = 0.0;
        double wanted = config.rememberZoom ? config.lastZoom : config.initialZoom;
        level = clamp(wanted, 1.0, config.zoomCeiling());
    }

    public boolean stop() {
        if (!active) {
            return false;
        }
        active = false;
        boolean changed = false;
        if (level >= 1.0 && config.lastZoom != level) {
            config.lastZoom = level;
            changed = config.rememberZoom;
        }
        return changed;
    }

    public void reset() {
        level = clamp(config.initialZoom, 1.0, config.zoomCeiling());
        pull = 0.0;
    }

    public void scroll(double amount, boolean fine, boolean canPull, double baseFov) {
        if (!active || amount == 0.0) {
            return;
        }
        double step = fine ? config.fineStep : config.scrollStep;
        double pullStep = fine ? config.pullStep * 0.25 : config.pullStep;
        boolean pulling = canPull && config.pullEnabled;
        if (!pulling) {
            pull = 0.0;
        }
        if (amount > 0.0) {
            if (pull > 0.0) {
                pull = Math.max(0.0, pull - pullStep * amount);
                return;
            }
            double raised = level * Math.pow(step, amount);
            if (level < 1.0 && raised > 1.0) {
                raised = 1.0;
            }
            level = Math.min(config.zoomCeiling(), raised);
            if (level > 1.0 - EPSILON && level < 1.0 + EPSILON) {
                level = 1.0;
            }
            return;
        }
        double floor = pulling ? 1.0 : widestLevel(baseFov);
        if (level > floor + EPSILON) {
            double next = level * Math.pow(step, amount);
            if (level > 1.0 && next < 1.0) {
                next = 1.0;
            }
            level = Math.max(floor, next);
            return;
        }
        level = Math.max(level, floor);
        if (pulling) {
            pull = Math.min(config.pullMax, pull - pullStep * amount);
        }
    }

    public double widestLevel(double baseFov) {
        if (!config.widenFov || baseFov <= 0.0 || config.widenMaxFov <= baseFov) {
            return 1.0;
        }
        double base = Math.tan(Math.toRadians(baseFov) / 2.0);
        double wide = Math.tan(Math.toRadians(Math.min(config.widenMaxFov, 170)) / 2.0);
        return Math.min(1.0, base / wide);
    }

    public void update(double seconds) {
        double target = active ? level : 1.0;
        double pullTarget = active ? pull : 0.0;
        if (!config.smoothZoom || seconds < 0.0) {
            shown = target;
        } else {
            double blend = 1.0 - Math.exp(-seconds * config.zoomSpeed);
            double log = Math.log(shown) + (Math.log(target) - Math.log(shown)) * blend;
            shown = Math.exp(log);
            if (Math.abs(Math.log(target) - log) < 1.0e-4) {
                shown = target;
            }
        }
        if (!config.pullSmooth || seconds < 0.0) {
            pullShown = pullTarget;
        } else {
            double blend = 1.0 - Math.exp(-seconds * config.pullSpeed);
            pullShown += (pullTarget - pullShown) * blend;
            if (Math.abs(pullTarget - pullShown) < 1.0e-3) {
                pullShown = pullTarget;
            }
        }
    }

    public double applyFov(double fov) {
        if (shown == 1.0 || fov <= 0.0 || fov >= 179.0) {
            return fov;
        }
        double half = Math.tan(Math.toRadians(fov) / 2.0) / shown;
        double result = Math.toDegrees(2.0 * Math.atan(half));
        return Math.max(result, 1.0e-4);
    }

    public double sensitivity() {
        if (shown <= 1.0 || config.sensitivityScaling <= 0) {
            return 1.0;
        }
        return Math.pow(shown, -config.sensitivityScaling / 100.0);
    }

    private static double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }
}
