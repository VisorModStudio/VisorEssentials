// #!MC-VERSION:: 1.21.3+
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin
        extends AbstractContainerScreen<CraftingMenu>
        implements AbstractContainerScreenExtension {

    @Unique
    private Identifier visorEssentials$VrTexture = McVersionUtils.newResourceLoc(
            VisorEssentials.MOD_ID,
            "textures/gui/container/crafting_table.png"
            );

    public CraftingScreenMixin(CraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void visorEssentials$preInit() {
        imageWidth = 176;
        imageHeight = 86;
    }

    @ModifyExpressionValue(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CraftingScreen;CRAFTING_TABLE_LOCATION:Lnet/minecraft/resources/Identifier;"))
    private Identifier visorEssentials$background(Identifier original){
        return visorEssentials$isVRContainer() ? visorEssentials$VrTexture : original;
    }

    // also where the button moves back to when the book is toggled
    @ModifyReturnValue(method = "getRecipeBookButtonPosition", at = @At("RETURN"))
    private ScreenPosition visorEssentials$recipeBookButton(ScreenPosition position){
        return visorEssentials$isVRContainer()
                ? new ScreenPosition(this.leftPos + 5, this.topPos + 35)
                : position;
    }

    @Override
    public boolean visorEssentials$supportsVRContainer() {
        return true;
    }

}
