// #!MC-VERSION:: 1.21.8+
package org.vmstudio.essentials.core.mixin.client.gui.containers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.client.gui.VRSlotRenderContext;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreenInScreen;
import org.vmstudio.essentials.core.client.gui.ContainerSlot;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static org.vmstudio.essentials.core.client.AddonEntryClient.MC;


@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin <T extends AbstractContainerMenu>
        extends Screen
        implements MenuAccess<T>, AbstractContainerScreenExtension {
    @Shadow @Final protected T menu;

    @Unique
    private List<ContainerSlot> visorEssentials$vrSlots;
    @Unique
    private LinkedHashMap<Slot, ContainerSlot> visorEssentials$vrSlotsMap;
    @Unique
    private boolean visorEssentials$isVrContainer;

    @Unique
    private int visorEssentials$edgeX = -1;
    @Unique
    private int visorEssentials$edgeY = -1;
    @Unique
    private int visorEssentials$edgeWidth = -1;
    @Unique
    private int visorEssentials$edgeHeight = -1;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @ModifyExpressionValue(
            method = "renderCarriedItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
    )
    private boolean visorEssentials$noDraggingItem(boolean isEmpty) {
        return isEmpty || visorEssentials$isCarriedByOverlay();
    }

    @Unique
    private boolean visorEssentials$isCarriedByOverlay() {
        if(!EssentialsClientSettings.getBetterInventory().isEnabled()){
            return false;
        }
        if(VisorAPI.clientState().stateMode().isNotActive()){
            return false;
        }
        var focused = VisorAPI.client().getGuiManager().getCursorHandler()
                .getFocusedOverlay();
        if(focused != null
                && focused.getId().equals("game_screen")){
            if(MC.screen == this){
                return false;
            }
        }
        if(focused instanceof VROverlayScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() == this){
                return false;
            }
        }
        if(focused instanceof VROverlayTemplateScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() == this){
                return false;
            }
        }
        return true;
    }

    @Inject(method = "<init>", at  = @At("TAIL"))
    public void visorEssentials$onInit(CallbackInfo ci){
        visorEssentials$vrSlots = new ArrayList<>();
        visorEssentials$vrSlotsMap = new LinkedHashMap<>();

        visorEssentials$fillVRSlots(
                visorEssentials$vrSlots
        );
        for(var entry : visorEssentials$vrSlots){
            visorEssentials$vrSlotsMap.put(
                    entry.parent(), entry
            );
        }
    }

    @Unique
    private ContainerSlot visorEssentials$vrSlot(Slot slot){
        if(!visorEssentials$hasVRSlots()){
            return null;
        }
        return visorEssentials$vrSlotsMap.get(slot);
    }
    @Unique
    private boolean visorEssentials$hasVRSlots(){
        return visorEssentials$isVrContainer
                && visorEssentials$vrSlots != null
                && visorEssentials$vrSlotsMap != null;
    }
    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci){
        if(visorEssentials$isVrContainer) {
            visorEssentials$preInit();
        }
    }

    // on both paths: render() and AbstractRecipeBookScreen's own render(), which skips it
    @Inject(method = "renderContents", at = @At("HEAD"))
    private void visorEssentials$beginSlotRender(CallbackInfo ci){
        VRSlotRenderContext.begin(this);
    }

    @Inject(method = "renderContents", at = @At("RETURN"))
    private void visorEssentials$endSlotRender(CallbackInfo ci){
        VRSlotRenderContext.end();
    }

    @ModifyExpressionValue(
            method = "renderLabels",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;playerInventoryTitle:Lnet/minecraft/network/chat/Component;")
    )
    private Component visorEssentials$noInventoryTitle(Component title){
        return visorEssentials$isVrContainer ? Component.empty() : title;
    }

    @ModifyExpressionValue(
            method = {"renderSlots", "getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;", "mouseReleased"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;iterator()Ljava/util/Iterator;")
    )
    private Iterator<Slot> visorEssentials$vrSlotsOnly(Iterator<Slot> slots) {
        if(visorEssentials$hasVRSlots()){
            return visorEssentials$vrSlotsMap.keySet().iterator();
        }
        return slots;
    }

    // mouseReleased: where a snapped-back item flies to
    @WrapOperation(
            method = {"renderSlot", "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", "renderSlotHighlightBack", "renderSlotHighlightFront", "mouseReleased"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;x:I")
    )
    private int visorEssentials$vrSlotX(Slot slot, Operation<Integer> original){
        var vrSlot = visorEssentials$vrSlot(slot);
        return vrSlot != null ? vrSlot.vrPosX() : original.call(slot);
    }

    @WrapOperation(
            method = {"renderSlot", "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", "renderSlotHighlightBack", "renderSlotHighlightFront", "mouseReleased"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I")
    )
    private int visorEssentials$vrSlotY(Slot slot, Operation<Integer> original){
        var vrSlot = visorEssentials$vrSlot(slot);
        return vrSlot != null ? vrSlot.vrPosY() : original.call(slot);
    }


    @Override
    public void visorEssentials$fillVRSlots(
            @NotNull List<ContainerSlot> slots
    ) {
        slots.clear();
        for(Slot slot : menu.slots){
            if(!(slot.container instanceof Inventory)){
                slots.add(
                        new ContainerSlot(
                                slot,
                                slot.x, slot.y
                        )
                );
            }
        }
    }

    @Override
    public void visorEssentials$setVRContainer(boolean flag) {
        visorEssentials$isVrContainer = flag;
    }
    @Override
    public boolean visorEssentials$isVRContainer() {
        return visorEssentials$isVrContainer;
    }

    @Override
    public @NotNull List<ContainerSlot> visorEssentials$getVRSlots() {
        return visorEssentials$vrSlots == null ? List.of() : visorEssentials$vrSlots;
    }

    @Override
    public ContainerSlot visorEssentials$getVRSlot(@NotNull Slot slot) {
        return visorEssentials$vrSlot(slot);
    }


    @Override
    public void visorEssentials$setEdgeX(int value) {
        visorEssentials$edgeX = value;
    }
    @Override
    public void visorEssentials$setEdgeY(int value) {
        visorEssentials$edgeY = value;
    }

    @Override
    public void visorEssentials$setEdgeWidth(int value) {
        visorEssentials$edgeWidth = value;
    }
    @Override
    public void visorEssentials$setEdgeHeight(int value) {
        visorEssentials$edgeHeight = value;
    }

    @Override
    public int visorEssentials$getEdgeX() {
        return visorEssentials$edgeX;
    }

    @Override
    public int visorEssentials$getEdgeY() {
        return visorEssentials$edgeY;
    }

    @Override
    public int visorEssentials$getEdgeWidth() {
        return visorEssentials$edgeWidth;
    }

    @Override
    public int visorEssentials$getEdgeHeight() {
        return visorEssentials$edgeHeight;
    }
}
