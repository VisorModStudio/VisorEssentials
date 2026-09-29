package org.vmstudio.essentials.core.client.gui;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public final class ContainerSlot {

    private final Slot parent;
    private final int vrPosX;
    private final int vrPosY;

    private Slot vrSlot;

    public ContainerSlot(@NotNull Slot parent, int vrPosX, int vrPosY) {
        this.parent = parent;
        this.vrPosX = vrPosX;
        this.vrPosY = vrPosY;
    }

    public @NotNull Slot parent() {
        return parent;
    }

    public int vrPosX() {
        return vrPosX;
    }

    public int vrPosY() {
        return vrPosY;
    }

    /**
     * A stand-in placed at the VR position, for code that reads the (final since 1.21.2)
     * slot coordinates where no field read can be redirected
     */
    public @NotNull Slot vrSlot() {
        if (vrPosX == parent.x && vrPosY == parent.y) {
            return parent;
        }
        if (vrSlot == null) {
            vrSlot = new Slot(parent.container, parent.getContainerSlot(), vrPosX, vrPosY);
        }
        return vrSlot;
    }
}
