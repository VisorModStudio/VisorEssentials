// #!MC-VERSION:: 1.21.3-1.21.10
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenMixin
        extends AbstractContainerScreen<HorseInventoryMenu>
        implements AbstractContainerScreenExtension {

    @Unique
    private ResourceLocation visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
            VisorEssentials.MOD_ID,
            "textures/gui/container/horse.png"
    );

    public HorseInventoryScreenMixin(HorseInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }


    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 88;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void visorEssentials$updateEdges(CallbackInfo ci){
        visorEssentials$setEdgeX(leftPos);
        visorEssentials$setEdgeY(topPos);
        visorEssentials$setEdgeWidth(imageWidth);
        visorEssentials$setEdgeHeight(imageHeight);
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/HorseInventoryScreen;HORSE_INVENTORY_LOCATION:Lnet/minecraft/resources/ResourceLocation;", ordinal = 0))
    private ResourceLocation visorEssentials$background(ResourceLocation original){
        return visorEssentials$isVRContainer() ? visorEssentials$VrTexture : original;
    }


    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }
}
