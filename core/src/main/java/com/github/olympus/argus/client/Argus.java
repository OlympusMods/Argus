package com.github.olympus.argus.client;

import com.github.olympus.argus.client.ui.ArgusOverlay;
import com.github.olympus.argus.core.Aim;
import com.github.olympus.argus.core.ArgusConfig;
import com.github.olympus.argus.core.FreelookModel;
import com.github.olympus.argus.core.ZoomModel;
import com.github.olympus.proteus.client.ui.Overlays;
import java.nio.file.Path;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public final class Argus {
    private static final double TURN = 0.15;
    private static final double HEAD_LIMIT = 75.0;

    private static ArgusConfig config;
    private static ZoomModel zoom;
    private static FreelookModel freelook;
    private static final Aim AIM = new Aim();
    private static boolean overlayInstalled;
    private static boolean turning;
    private static boolean forcedThirdPerson;
    private static CameraType savedCamera;
    private static long lastFrame;

    private Argus() {
    }

    public static synchronized ArgusConfig config() {
        if (config == null) {
            config = ArgusConfig.load(file());
            zoom = new ZoomModel(config);
            freelook = new FreelookModel(config);
        }
        return config;
    }

    public static ZoomModel zoom() {
        config();
        return zoom;
    }

    public static FreelookModel freelook() {
        config();
        return freelook;
    }

    public static void save() {
        config().sanitize().save(file());
    }

    private static Path file() {
        return ArgusClient.configDirectory().resolve("argus.json");
    }

    public static void tick(Minecraft minecraft) {
        ArgusConfig settings = config();
        if (!overlayInstalled) {
            overlayInstalled = true;
            Overlays.add(new ArgusOverlay());
        }
        KeyBindings keys = ArgusClient.keys();
        LocalPlayer player = minecraft.player;
        boolean playing = player != null && Compat.screen(minecraft) == null;

        while (keys.menu.consumeClick()) {
            if (playing) {
                Compat.show(minecraft, ArgusMenu.create(null));
                playing = false;
            }
        }

        boolean zoomWanted = zoom.active();
        if (settings.zoomToggle) {
            while (keys.zoom.consumeClick()) {
                zoomWanted = !zoomWanted;
            }
        } else {
            drain(keys.zoom);
            zoomWanted = keys.zoom.isDown();
        }
        if (!playing) {
            zoomWanted = false;
        }
        if (zoomWanted && !zoom.active()) {
            zoom.start();
        } else if (!zoomWanted && zoom.active() && zoom.stop()) {
            save();
        }

        boolean resetWanted = false;
        while (keys.reset.consumeClick()) {
            resetWanted = true;
        }
        if (zoom.active() && settings.middleClickReset && minecraft.mouseHandler.isMiddlePressed()) {
            resetWanted = true;
            drain(minecraft.options.keyPickItem);
        }
        if (resetWanted && zoom.active()) {
            zoom.reset();
        }

        boolean lookWanted = freelook.held();
        if (settings.freelookToggle) {
            while (keys.freelook.consumeClick()) {
                lookWanted = !lookWanted;
            }
        } else {
            drain(keys.freelook);
            lookWanted = keys.freelook.isDown();
        }
        if (!playing) {
            lookWanted = false;
        }
        if (player == null) {
            freelook.recenter();
            AIM.forget();
        } else if (lookWanted && !freelook.held()) {
            freelook.start(player.getYRot(), player.getXRot());
        } else if (!lookWanted && freelook.held()) {
            freelook.stop();
        }
        while (keys.recenter.consumeClick()) {
            freelook.recenter();
        }

        forceCamera(minecraft, settings);
    }

    private static void drain(KeyMapping key) {
        while (key.consumeClick()) {
            continue;
        }
    }

    private static void forceCamera(Minecraft minecraft, ArgusConfig settings) {
        boolean wanted = minecraft.player != null && freelook.detached()
                && (settings.autoThirdPerson || (zoom.active() && zoom.pull() > 0.0) || (forcedThirdPerson && zoom.pulled()));
        if (wanted && !forcedThirdPerson) {
            if (minecraft.options.getCameraType().isFirstPerson()) {
                savedCamera = minecraft.options.getCameraType();
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                forcedThirdPerson = true;
            }
        } else if (!wanted && forcedThirdPerson) {
            if (minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK && savedCamera != null) {
                minecraft.options.setCameraType(savedCamera);
            }
            forcedThirdPerson = false;
            savedCamera = null;
        }
    }

    public static void frame() {
        config();
        long now = System.nanoTime();
        double seconds = lastFrame == 0L ? 0.0 : Math.min(0.1, (now - lastFrame) / 1.0e9);
        if (lastFrame != 0L && seconds < 1.0e-4) {
            return;
        }
        lastFrame = now;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        zoom.update(seconds);
        if (player == null) {
            return;
        }
        freelook.update(seconds, player.getYRot(), player.getXRot());
        if (AIM.pending()) {
            if (zoom.shown() > 1.0 && config.cinematic && !freelook.held()) {
                double rate = 2.0 + (100 - config.cinematicStrength) / 95.0 * 38.0;
                double blend = 1.0 - Math.exp(-seconds * rate);
                steer(player, AIM.takeYaw(blend), AIM.takePitch(blend));
            } else {
                AIM.clearPending();
            }
        }
        if (freelook.detached() && config.headFollows && !minecraft.options.getCameraType().isFirstPerson()) {
            double offset = FreelookModel.wrap(freelook.yaw() - player.yBodyRot);
            float head = (float) (player.yBodyRot + Math.max(-HEAD_LIMIT, Math.min(HEAD_LIMIT, offset)));
            player.yHeadRot = head;
            player.yHeadRotO = head;
        }
    }

    public static boolean onScroll(double vertical) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || Compat.screen(minecraft) != null) {
            return false;
        }
        config();
        if (!zoom.active()) {
            return false;
        }
        if (vertical != 0.0) {
            double amount = Math.abs(vertical) < 1.0 ? Math.signum(vertical) : Math.max(-3.0, Math.min(3.0, vertical));
            boolean canPull = !minecraft.options.getCameraType().isFirstPerson() || freelook.detached();
            zoom.scroll(amount, ArgusClient.keys().fine.isDown(), canPull, minecraft.options.fov().get());
        }
        return true;
    }

    public static boolean onTurn(Entity entity, double yaw, double pitch) {
        if (turning) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (entity != minecraft.player) {
            return false;
        }
        frame();
        if (freelook.held()) {
            freelook.turn(yaw * TURN, pitch * TURN);
            return true;
        }
        if (zoom.shown() <= 1.0) {
            AIM.forget();
            return false;
        }
        double scale = zoom.sensitivity();
        if (config.cinematic) {
            AIM.queue(yaw * scale, pitch * scale);
            return true;
        }
        steer(entity, yaw * scale, pitch * scale);
        return true;
    }

    private static void steer(Entity entity, double yaw, double pitch) {
        double steeredYaw = AIM.steerYaw(entity.getYRot(), yaw);
        double steeredPitch = AIM.steerPitch(entity.getXRot(), pitch);
        turning = true;
        try {
            entity.turn(steeredYaw, steeredPitch);
        } finally {
            turning = false;
        }
        AIM.settle(entity.getYRot(), entity.getXRot());
    }

    public static double fov(double fov) {
        frame();
        return zoom.applyFov(fov);
    }

    public static boolean cameraDetached() {
        frame();
        return freelook.detached();
    }

    public static float cameraYaw() {
        return (float) freelook.yaw();
    }

    public static float cameraPitch() {
        return (float) freelook.pitch();
    }

    public static double cameraDistance(double distance) {
        config();
        double pull = zoom.pullShown();
        return pull > 0.0 ? distance + pull : distance;
    }

    public static double residualYaw() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || config == null || zoom.shown() < 4.0 || freelook.detached()) {
            return 0.0;
        }
        return AIM.residualYaw(minecraft.player.getYRot());
    }

    public static double residualPitch() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || config == null || zoom.shown() < 4.0 || freelook.detached()) {
            return 0.0;
        }
        return AIM.residualPitch(minecraft.player.getXRot());
    }

    public static boolean hideHand() {
        if (config == null) {
            return false;
        }
        if (config.zoomHideHand && zoom.active()) {
            return true;
        }
        return config.freelookHideHand && freelook.detached();
    }

    public static boolean hideHud() {
        return config != null && config.hideHud && zoom.active();
    }

    public static boolean hideCrosshair() {
        return config != null && config.hideCrosshair && zoom.shown() > 1.0;
    }

    public static boolean steadyView() {
        return config != null && zoom.shown() > 1.5;
    }
}
