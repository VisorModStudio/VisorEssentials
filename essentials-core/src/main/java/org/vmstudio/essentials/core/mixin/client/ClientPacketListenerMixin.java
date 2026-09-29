// #!MC-VERSION:: 1.21.3+
package org.vmstudio.essentials.core.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayInventory;
import org.vmstudio.essentials.core.client.gui.screens.VRInvScreen;
import org.vmstudio.essentials.core.server.EssentialsServerSettings;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void visorEssentials$onJoinedServer(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isLocalServer()) {
            EssentialsServerSettings.joinedDedicatedServer();
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void visorEssentials$onDisconnected(CallbackInfo ci) {
        EssentialsServerSettings.resetToDefaults();
    }


    // every recipe book packet (add, remove, settings) ends here since 1.21.2
    @Inject(method = "refreshRecipeBook", at = @At("TAIL"))
    private void visorEssentials$forwardRecipesUpdated(ClientRecipeBook recipeBook, CallbackInfo ci) {
        if (!EssentialsClientSettings.getBetterInventory().isEnabled()) {
            return;
        }
        if (VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }
        var overlayManager = VisorAPI.client().getGuiManager().getOverlayManager();
        visorEssentials$notifyScreen(overlayManager.getOverlay(VROverlayInventory.ID, VROverlayInventory.class));
        visorEssentials$notifyScreen(overlayManager.getOverlay(VROverlayContainer.ID, VROverlayContainer.class));
    }

    @Unique
    private static void visorEssentials$notifyScreen(VROverlayScreenInScreen<?> overlay) {
        if (overlay == null
                || !(overlay.getScreen() instanceof RecipeUpdateListener listener)) {
            return;
        }
        if (!(listener instanceof VRInvScreen)) {
            var player = Minecraft.getInstance().player;
            if (player == null
                    || !(listener instanceof MenuAccess<?> menuAccess)
                    || menuAccess.getMenu() != player.containerMenu) {
                return;
            }
        }
        listener.recipesUpdated();
    }

    /**
     * Same forwarding for the ghost recipe "hint" packet,
     * sent by the server when a clicked recipe can't be fully placed.
     */
    @Inject(method = "handlePlaceRecipe", at = @At("TAIL"))
    private void visorEssentials$forwardGhostRecipe(ClientboundPlaceGhostRecipePacket packet, CallbackInfo ci) {
        if (!EssentialsClientSettings.getBetterInventory().isEnabled()) {
            return;
        }
        if (VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        AbstractContainerMenu containerMenu = minecraft.player.containerMenu;
        if (containerMenu.containerId != packet.containerId()) {
            return;
        }
        var overlayManager = VisorAPI.client().getGuiManager().getOverlayManager();
        visorEssentials$fillGhostRecipe(
                overlayManager.getOverlay(VROverlayInventory.ID, VROverlayInventory.class),
                containerMenu, packet.recipeDisplay()
        );
        visorEssentials$fillGhostRecipe(
                overlayManager.getOverlay(VROverlayContainer.ID, VROverlayContainer.class),
                containerMenu, packet.recipeDisplay()
        );
    }

    @Unique
    private static void visorEssentials$fillGhostRecipe(VROverlayScreenInScreen<?> overlay,
                                                        AbstractContainerMenu menu,
                                                        RecipeDisplay recipeDisplay) {
        if (overlay == null) {
            return;
        }
        var screen = overlay.getScreen();
        if (!(screen instanceof RecipeUpdateListener listener)) {
            return;
        }
        // only the screen actually hosting this menu shows the hint
        if (!(screen instanceof MenuAccess<?> menuAccess)
                || menuAccess.getMenu() != menu) {
            return;
        }
        listener.fillGhostRecipe(recipeDisplay);
    }
}
