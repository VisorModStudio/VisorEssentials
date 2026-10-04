package org.vmstudio.essentials.core.client.gui.overlays;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.ItemStack;
//? if >=26.3 {
import net.minecraft.util.Prediction;
//?}
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.essentials.core.compatibility.mcversion.EssentialsGuiUtils;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.events.AllowClientFeatureVREvent;
import org.vmstudio.visor.api.client.events.render.HandRenderStateVREvent;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreenInScreen;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;


public class VROverlayDraggedItem extends VROverlayScreen
        implements VREventListener {
    public static final String ID = "dragged_item";

    private Vector3f orientPosOffset = new Vector3f(0,0,-0.6f);
    private Vector3f orientRotationOffset = new Vector3f(0,0,0);

    public VROverlayDraggedItem(@NotNull VisorAddon owner,
                                @NotNull String id) {
        super(owner, id, ComponentPriority.HIGHER, 0.1f);
        setEnabled(true);
        cursorBoundsX = width/2 - 8;
        cursorBoundsY = height/2 - 8;
        cursorBoundsWidth = 16;
        cursorBoundsHeight = 16;
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void onHandRenderState(HandRenderStateVREvent event) {
        if(!isVisible()){
            return;
        }
        var hand = event.getHandType();
        if (hand == VisorAPI.client().getGuiManager().getCursorHandler()
                .getCursorHand()) {
            event.setState(HandRenderState.GUI_HAND);
        }
    }


    @Override
    protected void onRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderFloatingItem(
                guiGraphics,
                minecraft.player.containerMenu.getCarried(),
                width/2 - 8,height/2 - 8,
                null
        );
    }

    @Override
    public boolean updateVisibility() {
        if(!EssentialsClientSettings.getBetterInventory().isEnabled()){
            return false;
        }
        if(!isDraggingItem()) {
            return false;
        }
        var cursorHandler = VisorAPI.client().getGuiManager().getCursorHandler();
        if(supportsDragging(cursorHandler.getFocusedOverlay())){
            return false;
        }

        if(!VisorAPI.client().getVRLocalPlayer().getRawController(
                     cursorHandler.getCursorHand()
                ).isTracking()){
            return false;
        }

        if(isVisible()){
            var cursorResult  = cursorHandler.getCursorResult(
                    cursorHandler.getCursorHand(),
                    VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER),
                    it->it != this,
                    false
            );
            if(supportsDragging(cursorResult.focusedOverlay())){
                return false;
            }

        }
        return true;
    }



    @Override
    public void onUpdatePose(float partialTicks) {
        PoseAnchor anchor =  VisorAPI.client().getGuiManager().getCursorHandler()
                .getCursorHand() == HandType.MAIN
                ? PoseAnchor.MAIN_HAND
                : PoseAnchor.OFFHAND;
        VROverlayHelper.applyPose(
                this,
                anchor,
                anchor,
                1.0f,
                true,
                orientPosOffset,
                orientRotationOffset
        );
    }


    @Override
    protected boolean onMouseClicked(double d, double e, int i) {
        if(minecraft.player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu itemPickerMenu){
            if (i == InputConstants.MOUSE_BUTTON_LEFT) {
                dropCreative(itemPickerMenu.getCarried());
                itemPickerMenu.setCarried(ItemStack.EMPTY);
            }

            if (i == InputConstants.MOUSE_BUTTON_RIGHT) {
                dropCreative(itemPickerMenu.getCarried().split(1));
            }
        }else {
            EssentialsGuiUtils.pickupClick(
                    this.minecraft.gameMode, this.minecraft.player,
                    minecraft.player.containerMenu.containerId, -999, i
            );
        }
        return true;
    }


    private void dropCreative(ItemStack itemStack) {
        //? if >=26.3 {
        this.minecraft.player.drop(itemStack, true, Prediction.PREDICTED);
        //?} else {
        /*this.minecraft.player.drop(itemStack, true);
        *///?}
        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
    }

    private void renderFloatingItem(GuiGraphicsExtractor guiGraphics,
                                    ItemStack itemStack,
                                    int posX, int posY,
                                    String string) {
        McGuiUtils.pushPose(guiGraphics);
        McGuiUtils.renderItem(guiGraphics, itemStack, posX, posY);
        McGuiUtils.renderItemDecorations(
                guiGraphics,
                this.font,
                itemStack,
                posX, posY, string
        );
        McGuiUtils.popPose(guiGraphics);
    }

    private boolean supportsDragging(VROverlay overlay){
        if(overlay != null
                && overlay.getId().equals("game_screen")){
            if(McVersionClientUtils.screen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        if(overlay instanceof VROverlayScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        if(overlay instanceof VROverlayTemplateScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        return false;
    }
    public static boolean isDraggingItem(){
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null
                && mc.player != null
                && mc.player.containerMenu != null
                && mc.player.containerMenu.getCarried() != null
                && !mc.player.containerMenu.getCarried().isEmpty();
    }
}
