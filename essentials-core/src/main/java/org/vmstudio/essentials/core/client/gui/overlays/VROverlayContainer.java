package org.vmstudio.essentials.core.client.gui.overlays;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import lombok.Setter;
import org.vmstudio.essentials.core.client.EssentialsClientSettings;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.client.gui.VRDoubleClick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vmstudio.essentials.core.common.VisorEssentials;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;

public class VROverlayContainer extends VROverlayScreenInScreen<AbstractContainerScreen<?>> {

    public static final String ID = "container";

    private final VRDoubleClick clicks = new VRDoubleClick();


    @Getter
    private Vector3f sourcePos;
    @Getter
    private Vector3f sourceBlockPos;
    @Getter
    private Entity sourceEntity;

    //For convenience
    private final float extraXForBlock = 0.5f;
    private final float extraYForBlock = 0;
    private final float extraZForBlock = 0.5f;


    @Getter
    @Setter
    private long lastManualClose;


    private boolean orientInitialized;


    public VROverlayContainer(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id, ComponentPriority.LOW, 1.0f,null);
    }

    @Override
    protected void init() {

    }


    @Override
    protected boolean updateVisibility() {
        return true;
    }


    @Override
    protected void onTick() {
        if (!EssentialsClientSettings.getBetterInventory().isEnabled()) {
            setEnabled(false);
            return;
        }
        if (McVersionClientUtils.screen() != null || minecraft.player == null) {
            setEnabled(false);
            return;
        }
        if (minecraft.player.containerMenu
                == minecraft.player.inventoryMenu) {
            setEnabled(false);
            return;
        }
        if (!isOverlayNearPlayer()) {
            setEnabled(false);
            return;
        }

        var vrContainer = (AbstractContainerScreenExtension)screen;

        cursorBoundsX = vrContainer.visorEssentials$getEdgeX();
        cursorBoundsY = vrContainer.visorEssentials$getEdgeY();
        cursorBoundsWidth = vrContainer.visorEssentials$getEdgeWidth();
        cursorBoundsHeight = vrContainer.visorEssentials$getEdgeHeight();

        super.onTick();
    }


    @Override
    public void onUpdatePose(float partialTick) {
        Vector3f newPosition;
        Matrix4f newRotation;

        boolean aimedAtEntity;
        boolean aimedAtBlock;

        if (orientInitialized) {
            aimedAtEntity = sourceEntity != null;
            aimedAtBlock = !aimedAtEntity;
        } else {
            aimedAtEntity = minecraft.hitResult instanceof EntityHitResult
                    && (((EntityHitResult) minecraft.hitResult).getEntity() instanceof ContainerEntity
                    || ((EntityHitResult) minecraft.hitResult).getEntity() instanceof InventoryCarrier
                    || ((EntityHitResult) minecraft.hitResult).getEntity() instanceof HasCustomInventoryScreen);

            aimedAtBlock = !aimedAtEntity
                    && minecraft.hitResult != null
                    && (minecraft.hitResult.getType() == HitResult.Type.BLOCK);

        }

        var facingElement = VisorAPI.client().getVRLocalPlayer()
                .getPoseData(PlayerPoseType.RENDER)
                .getHmd();

        if ((aimedAtBlock || aimedAtEntity || sourcePos != null)) {
            if (sourcePos == null) {
                if (aimedAtEntity) {
                    EntityHitResult entityHitResult = (EntityHitResult) minecraft.hitResult;
                    sourceEntity = entityHitResult.getEntity();
                    Vector3f pos = sourceEntity
                            .getPosition(partialTick).toVector3f();
                    sourcePos = new Vector3f(
                            pos.x,
                            pos.y,
                            pos.z
                    );
                } else {
                    BlockHitResult blockHitResult = (BlockHitResult) minecraft.hitResult;
                    sourcePos = new Vector3f(
                            blockHitResult.getBlockPos().getX() + extraXForBlock,
                            blockHitResult.getBlockPos().getY() + extraYForBlock,
                            blockHitResult.getBlockPos().getZ() + extraZForBlock
                    );
                    sourceBlockPos = new Vector3f(
                            blockHitResult.getBlockPos().getX(),
                            blockHitResult.getBlockPos().getY(),
                            blockHitResult.getBlockPos().getZ()
                    );
                }
            } else if (sourceEntity != null && sourceEntity.isAlive()) {
                Vector3f pos = sourceEntity
                        .getPosition(partialTick).toVector3f();
                sourcePos = new Vector3f(
                        pos.x,
                        pos.y,
                        pos.z
                );
            }

            float yPos = findYPos(
                    sourcePos.y,
                    (float) (aimedAtEntity ?
                            sourceEntity.getEyePosition(partialTick).y
                            : sourcePos.y + 1.1),
                    VisorAPI.client().getVRLocalPlayer()
                            .getPoseData(PlayerPoseType.RENDER)
                            .convertPositionFrom(
                                    PlayerPoseType.ROOM,
                                    facingElement.getPosition()
                            ).y
            );
            newPosition = new Vector3f(sourcePos.x, yPos, sourcePos.z);

        } else {
            Vector3f vec3 = new Vector3f(0.0f, 0.0f, -2.0f);


            var hmdPos = facingElement.getPosition();
            var vec32 = facingElement.getCustomVector(vec3);
            newPosition = (
                    new Vector3f(
                            vec32.x / 2.0f + hmdPos.x(),
                            vec32.y / 2.0f + hmdPos.y(),
                            vec32.z / 2.0f + hmdPos.z()
                    )
            );
        }

        // orient screen
        var hmdPos = facingElement.getPosition();
        Vector3f look = new Vector3f();

        look.x = newPosition.x - hmdPos.x();
        look.y = newPosition.y - hmdPos.y();
        look.z = newPosition.z - hmdPos.z();
        float pitch = (float) Math.asin((look.y() / look.length()));
        float yaw = (float) (Math.PI + Mth.atan2(look.x(), look.z()));
        Matrix4f rotation = new Matrix4f().rotationY(yaw);
        Matrix4f tilt = new Matrix4f().rotationX(pitch);
        newRotation = rotation.mul(tilt);
        getPose().update(
                newPosition, newRotation,
                getPose().getScale()
        );
        orientInitialized = true;
    }


    @Override
    public void onEnable() {
        minecraft.player.containerMenu = screen.getMenu();

    }

    @Override
    public void onDisable() {
        if (screen != null) {
            screen.onClose();
            screen.removed();
            screen = null;
            sourcePos = null;
            sourceBlockPos = null;
            sourceEntity = null;
            orientInitialized = false;
        }
    }



    private boolean isOverlayNearPlayer() {
        if (sourcePos == null) return true;

        var localPlayer = VisorAPI.client().getVRLocalPlayer();

        var roomPos = localPlayer
                .getPoseData(PlayerPoseType.ROOM)
                .convertPositionFrom(PlayerPoseType.TICK, sourcePos);

        var hmdPos = localPlayer
                .getPoseData(PlayerPoseType.ROOM)
                .getHmd()
                .getPosition();
        double distance = roomPos.sub(hmdPos).length();
        return distance < 4f;
    }



    private float findYPos(float lowerBound,
                           float upperBound,
                           float hmdPos
    ) {
        return Math.max(
                lowerBound,
                Math.min(hmdPos, upperBound)
        ) + (0.25F * getPose().getScale());
    }



    public static boolean openInstead(Screen screen) {
        if(!EssentialsClientSettings.getBetterInventory().isEnabled()) return false;
        if(VisorAPI.clientState().stateMode().isNotActive()) return false;

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if(hitResult == null
                || hitResult.getType() == HitResult.Type.MISS){
            return false;
        }


        if (McVersionClientUtils.screen() != null) {
            return false;
        }

        if (!(screen instanceof InventoryScreen)
                && !(screen instanceof CreativeModeInventoryScreen)
                && (screen instanceof AbstractContainerScreen<?> containerScreen)) {
            boolean supportsVR = ((AbstractContainerScreenExtension)containerScreen)
                    .visorEssentials$supportsVRContainer();
            if(!supportsVR){
                return false;
            }

            var overlayContainer = VisorAPI.client().getGuiManager()
                    .getOverlayManager()
                    .getOverlay(
                            ID,
                            VROverlayContainer.class
                    );

            overlayContainer.openMenu(
                    containerScreen
            );
            return true;
        }
        return false;
    }

    public void openMenu(AbstractContainerScreen<?> newScreen) {

        ((AbstractContainerScreenExtension)newScreen)
                .visorEssentials$setVRContainer(true);

        screen = newScreen;
        if (isEnabled()) {
            sourcePos = null;
            sourceBlockPos = null;
            sourceEntity = null;
            orientInitialized = false;
            onEnable();
        }else {
            setEnabled(true);
        }
        McGuiUtils.initScreen(screen, width, height);
        updatePose(1);
    }

    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int buttonType) {
        if (screen == null
                || buttonType == InputConstants.MOUSE_BUTTON_LEFT && (isCursorOnResizeHandle(getRawMouseX(), getRawMouseY())
                || isCursorOnDragHandle(getRawMouseX(), getRawMouseY()))) {
            return super.onMouseClicked(mouseX, mouseY, buttonType);
        }
        return clicks.mouseClicked(screen, mouseX, mouseY, buttonType);
    }

    public boolean isAttachedTo(BlockPos blockPos){
        if(sourceBlockPos == null) return false;

        return sourceBlockPos.x == blockPos.getX()
                && sourceBlockPos.y == blockPos.getY()
                && sourceBlockPos.z == blockPos.getZ();
    }
}
