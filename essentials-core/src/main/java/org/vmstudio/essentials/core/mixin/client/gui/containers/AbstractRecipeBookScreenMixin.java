// #!MC-VERSION:: 1.21.3+
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;

// the crafting table and the furnaces, the recipe book moved here in 1.21.2
@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin<T extends RecipeBookMenu>
        extends AbstractContainerScreen<T>
        implements AbstractContainerScreenExtension {

    @Shadow @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Shadow
    private boolean widthTooNarrow;

    public AbstractRecipeBookScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void visorEssentials$updateEdges(CallbackInfo ci){
        if(recipeBookComponent.isVisible() && !this.widthTooNarrow){
            visorEssentials$setEdgeX(-1);
            visorEssentials$setEdgeY(-1);
            visorEssentials$setEdgeWidth(-1);
            visorEssentials$setEdgeHeight(-1);
        }else {
            visorEssentials$setEdgeX(leftPos);
            visorEssentials$setEdgeY(topPos);
            visorEssentials$setEdgeWidth(imageWidth);
            visorEssentials$setEdgeHeight(imageHeight);
        }
    }
}
