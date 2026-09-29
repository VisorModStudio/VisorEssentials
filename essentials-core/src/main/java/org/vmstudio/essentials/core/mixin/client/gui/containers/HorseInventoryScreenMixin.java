// #!MC-VERSION:: 1.21.11+
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractMountInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 1.21.11 moved the horse screen into a base class shared with the nautilus, which stays vanilla
@Mixin(AbstractMountInventoryScreen.class)
public abstract class HorseInventoryScreenMixin<T extends AbstractMountInventoryMenu>
        extends AbstractContainerScreen<T>
        implements AbstractContainerScreenExtension {

    @Unique
    private Identifier visorEssentials$VrTexture;

    public HorseInventoryScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void visorEssentials$onInit(CallbackInfo ci) {
        if (((Object) this) instanceof HorseInventoryScreen) {
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/horse.png"
            );
        }
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

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractMountInventoryScreen;getBackgroundTextureLocation()Lnet/minecraft/resources/Identifier;"))
    private Identifier visorEssentials$background(Identifier original){
        return visorEssentials$isVRContainer() ? visorEssentials$VrTexture : original;
    }


    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return visorEssentials$VrTexture != null;
    }
}
