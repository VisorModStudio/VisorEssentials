package org.vmstudio.essentials.core.mixin.client.gui.containers;

import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Final;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin
        extends AbstractContainerScreen<ChestMenu>
        implements AbstractContainerScreenExtension {

    @Shadow @Final private int containerRows;


    @Unique
    private Identifier visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
            VisorEssentials.MOD_ID,
            "textures/gui/container/generic_54.png"
    );



    public ContainerScreenMixin(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 34 + this.containerRows * 18;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void visorEssentials$updateEdges(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci){
        visorEssentials$setEdgeX(leftPos);
        visorEssentials$setEdgeY(topPos);
        visorEssentials$setEdgeWidth(imageWidth);
        visorEssentials$setEdgeHeight(imageHeight);
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/ContainerScreen;CONTAINER_BACKGROUND:Lnet/minecraft/resources/Identifier;"))
    private Identifier visorEssentials$background(Identifier original){
        return visorEssentials$isVRContainer() ? visorEssentials$VrTexture : original;
    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }
}
