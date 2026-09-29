// #!MC-VERSION:: 1.21.3+
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// the recipe book and the edges are handled by AbstractRecipeBookScreenMixin
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin <T extends AbstractFurnaceMenu>
        extends AbstractContainerScreen<T>
        implements AbstractContainerScreenExtension {

    @Unique
    private Identifier visorEssentials$VrTexture;

    public AbstractFurnaceScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void visorEssentials$onInit(CallbackInfo ci) {
        if(((Object)this) instanceof BlastFurnaceScreen) {
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/blast_furnace.png"
            );
        } else if (((Object)this) instanceof SmokerScreen) {
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/smoker.png"
            );
        } else if(((Object)this) instanceof FurnaceScreen){
            visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
                    VisorEssentials.MOD_ID,
                    "textures/gui/container/furnace.png"
            );
        }
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 86;
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractFurnaceScreen;texture:Lnet/minecraft/resources/Identifier;", ordinal = 0))
    private Identifier visorEssentials$background(Identifier original){
        return visorEssentials$isVRContainer() ? visorEssentials$VrTexture : original;
    }

    // also where the button moves back to when the book is toggled
    @ModifyReturnValue(method = "getRecipeBookButtonPosition", at = @At("RETURN"))
    private ScreenPosition visorEssentials$recipeBookButton(ScreenPosition position){
        return visorEssentials$isVRContainer()
                ? new ScreenPosition(this.leftPos + 20, this.topPos + 35)
                : position;
    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return visorEssentials$VrTexture != null;
    }
}
