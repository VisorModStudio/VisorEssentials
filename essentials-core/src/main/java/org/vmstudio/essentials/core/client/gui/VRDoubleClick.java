package org.vmstudio.essentials.core.client.gui;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
//? if >=1.21.9 {
import net.minecraft.util.Util;
//?}

// 1.21.9 moved double-click detection to the caller (MouseHandler), the VR cursor gets it here
//@TODO TEMPORARY, should be moved to VISOR instead!!
public final class VRDoubleClick {

    //? if >=1.21.9 {
    // MouseHandler's rule
    private static final long THRESHOLD_MS = 250L;

    private long lastClickTime;
    private int lastClickButton = -1;
    private Screen lastClickScreen;
    //?}

    public boolean mouseClicked(@NotNull Screen screen, double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        long now = Util.getMillis();
        boolean doubleClick = lastClickScreen == screen
                && lastClickButton == button
                && now - lastClickTime < THRESHOLD_MS;
        boolean consumed = screen.mouseClicked(McGuiUtils.mouseButtonEvent(mouseX, mouseY, button), doubleClick);
        if (consumed) {
            lastClickTime = now;
            lastClickButton = button;
            lastClickScreen = screen;
        }
        return consumed;
        //?} else {
        /*return McGuiUtils.mouseClicked(screen, mouseX, mouseY, button);
        *///?}
    }
}
