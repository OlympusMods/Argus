package com.github.olympus.argus.client;

import java.util.List;
import net.minecraft.client.KeyMapping;

public final class KeyBindings {
    public final KeyMapping zoom;
    public final KeyMapping fine;
    public final KeyMapping reset;
    public final KeyMapping freelook;
    public final KeyMapping recenter;
    public final KeyMapping menu;

    public KeyBindings(KeyMapping zoom, KeyMapping fine, KeyMapping reset, KeyMapping freelook, KeyMapping recenter,
            KeyMapping menu) {
        this.zoom = zoom;
        this.fine = fine;
        this.reset = reset;
        this.freelook = freelook;
        this.recenter = recenter;
        this.menu = menu;
    }

    public List<KeyMapping> all() {
        return List.of(zoom, fine, reset, freelook, recenter, menu);
    }
}
