package org.vmstudio.essentials.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.tasks.BowItemTask;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Final
    public Options options;

    @Shadow
    public HitResult hitResult;

    @Shadow
    public LocalPlayer player;

    //? if <26.2 {
    /*// replaces vanilla container screen with overlay, the hook is in GuiMixin since 26.2
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void visorEssentials$UseVRContainerScreen(Screen screen, CallbackInfo info) {
        if (VROverlayContainer.openInstead(screen)) {
            info.cancel();
        }
    }
    *///?}


    /**
     * Disable mouse bindings when focused at overlay.
     * &
     * Closes container overlay
     * if player has it active
     * and hitResult is an entity or block
     * which is the container holder
     *
     * @param ci s
     */
    //@TODO move logic to VR input
    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true)
    private void visorEssentials$mouseAndOverlays(CallbackInfo ci) {
        if(VisorAPI.clientState().stateMode().isNotActive()) return;

        if (VisorAPI.client().getGuiManager().getCursorHandler().getFocusedOverlay() != null) {
            ci.cancel();
        }
        var container = VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getOverlay("container", VROverlayContainer.class);


        if (System.currentTimeMillis() < container.getLastManualClose() + 200) {
            // consume all key usages
            // if too small-time left after overlay close
            // it fixes accidental opening of same menu
            while (this.options.keyUse.consumeClick()) {
            }
            ci.cancel();
            return;
        }
        if (container.isEnabled()) {
            boolean isBlock = hitResult != null
                    && (hitResult.getType() == HitResult.Type.BLOCK);
            boolean isEntity = hitResult instanceof EntityHitResult
                    && (((EntityHitResult) hitResult).getEntity() instanceof ContainerEntity
                    || ((EntityHitResult) hitResult).getEntity() instanceof InventoryCarrier
                    || ((EntityHitResult) hitResult).getEntity() instanceof HasCustomInventoryScreen);

            if (isEntity) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();
                if (container.getSourceEntity() == entity) {
                    boolean b = false;
                    // consume all key usages
                    // if too small-time left after overlay close
                    // it fixes accidental opening of same menu
                    while (this.options.keyUse.consumeClick()) {
                        b = true;
                    }
                    if (b) {
                        //close container attached to an entity
                        container.setLastManualClose(System.currentTimeMillis());
                        container.setEnabled(false);
                        ci.cancel();
                    }
                }
            } else if (isBlock) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos blockPos = blockHitResult.getBlockPos();
                if (container.isAttachedTo(blockPos)) {
                    boolean b = false;
                    //consume all key usages
                    // if too small-time left after overlay close
                    //if fixes accidental opening of same menu
                    while (this.options.keyUse.consumeClick()) {
                        b = true;
                    }
                    if (b) {
                        //close container attached to block
                        container.setLastManualClose(System.currentTimeMillis());
                        container.setEnabled(false);
                        ci.cancel();
                    }
                }
            }

        }
    }


    @WrapOperation(method = "handleKeybinds",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;isDown()Z",
                    ordinal = 2))
    private boolean visorEssentials$keepBowUse(KeyMapping instance, Operation<Boolean> original) {
        if (VisorAPI.clientState().stateMode().isNotActive()) {
            return original.call(instance);
        }
        BowItemTask bow = BowItemTask.getInstance();
        if (bow != null
                && bow.isActive(this.player)
                && bow.isNotched()) {
            return true;
        }
        return original.call(instance);
    }

}