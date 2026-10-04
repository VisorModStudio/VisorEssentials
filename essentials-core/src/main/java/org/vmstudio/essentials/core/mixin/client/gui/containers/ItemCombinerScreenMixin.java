package org.vmstudio.essentials.core.mixin.client.gui.containers;

import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
//? if >=26.1 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.injection.At;
//?}

@Mixin(ItemCombinerScreen.class)
public abstract class ItemCombinerScreenMixin implements AbstractContainerScreenExtension {

    //? if >=26.1 {
    @ModifyExpressionValue(method = "extractBackground", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/ItemCombinerScreen;menuResource:Lnet/minecraft/resources/Identifier;"))
    private Identifier visorEssentials$background(Identifier original) {
        Identifier vrTexture = visorEssentials$getVRBackgroundTexture();
        return visorEssentials$isVRContainer() && vrTexture != null ? vrTexture : original;
    }
    //?}
}
