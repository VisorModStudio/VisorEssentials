package org.vmstudio.essentials.core.client.gui;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;

public final class VRSlotRenderContext {

    private static AbstractContainerScreenExtension rendering;

    private VRSlotRenderContext() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void begin(@Nullable AbstractContainerScreenExtension screen) {
        rendering = screen;
    }

    public static void end() {
        rendering = null;
    }

    public static @NotNull Slot drawnAt(@NotNull Slot slot) {
        AbstractContainerScreenExtension screen = rendering;
        ContainerSlot vrSlot = screen == null ? null : screen.visorEssentials$getVRSlot(slot);
        return vrSlot == null ? slot : vrSlot.vrSlot();
    }
}
