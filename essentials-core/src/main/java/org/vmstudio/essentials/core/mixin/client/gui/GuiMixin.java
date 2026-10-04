package org.vmstudio.essentials.core.mixin.client.gui;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
//? if >=26.2 {
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
//?}

@Mixin(Gui.class)
public abstract class GuiMixin {

    //? if >=26.2 {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void visorEssentials$useVRContainerScreen(Screen screen, CallbackInfo ci) {
        if (VROverlayContainer.openInstead(screen)) {
            ci.cancel();
        }
    }
    //?}
}
