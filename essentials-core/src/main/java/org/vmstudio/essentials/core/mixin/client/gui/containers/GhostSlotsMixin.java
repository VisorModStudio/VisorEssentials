// #!MC-VERSION:: 1.21.3+
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.essentials.core.client.gui.VRSlotRenderContext;

import java.util.function.BiConsumer;

// the ghost recipe lambda has no portable name across loaders: the slots handed to it are swapped
@Mixin(GhostSlots.class)
public abstract class GhostSlotsMixin {

    //? if >=26.1 {
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectMap;forEach(Ljava/util/function/BiConsumer;)V", remap = false)
    )
    //?} else {
    /*@WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectMap;forEach(Ljava/util/function/BiConsumer;)V", remap = false)
    )
    *///?}
    private void visorEssentials$ghostOnVRSlots(Reference2ObjectMap<Slot, Object> ingredients,
                                                BiConsumer<Slot, Object> action,
                                                Operation<Void> original) {
        BiConsumer<Slot, Object> onVRSlots = (slot, ghost) -> action.accept(VRSlotRenderContext.drawnAt(slot), ghost);
        original.call(ingredients, onVRSlots);
    }
}
