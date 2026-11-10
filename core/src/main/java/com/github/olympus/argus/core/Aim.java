package com.github.olympus.argus.core;

public final class Aim {
    private static final double TURN = 0.15;

    private double yaw;
    private double pitch;
    private float seenYaw = Float.NaN;
    private float seenPitch = Float.NaN;
    private double pendingYaw;
    private double pendingPitch;

    public void queue(double yawInput, double pitchInput) {
        pendingYaw += yawInput;
        pendingPitch += pitchInput;
    }

    public boolean pending() {
        return Math.abs(pendingYaw) > 1.0e-9 || Math.abs(pendingPitch) > 1.0e-9;
    }

    public double takeYaw(double blend) {
        double part = pendingYaw * blend;
        pendingYaw -= part;
        return part;
    }

    public double takePitch(double blend) {
        double part = pendingPitch * blend;
        pendingPitch -= part;
        return part;
    }

    public void clearPending() {
        pendingYaw = 0.0;
        pendingPitch = 0.0;
    }

    public double steerYaw(float current, double input) {
        if (current != seenYaw) {
            yaw = current;
        }
        yaw += input * TURN;
        return (yaw - current) / TURN;
    }

    public double steerPitch(float current, double input) {
        if (current != seenPitch) {
            pitch = current;
        }
        pitch = Math.max(-90.0, Math.min(90.0, pitch + input * TURN));
        return (pitch - current) / TURN;
    }

    public void settle(float currentYaw, float currentPitch) {
        seenYaw = currentYaw;
        seenPitch = currentPitch;
    }

    public double residualYaw(float current) {
        return current == seenYaw ? yaw - current : 0.0;
    }

    public double residualPitch(float current) {
        return current == seenPitch ? pitch - current : 0.0;
    }

    public void forget() {
        seenYaw = Float.NaN;
        seenPitch = Float.NaN;
        clearPending();
    }
}
