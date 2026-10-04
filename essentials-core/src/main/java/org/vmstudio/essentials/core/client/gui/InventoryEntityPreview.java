package org.vmstudio.essentials.core.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
//? if >=1.21.2 {
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
//?} else {
/*import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
*///?}

// the player model of the VR inventory panel
public final class InventoryEntityPreview {

    private InventoryEntityPreview() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void renderFollowingMouse(GuiGraphicsExtractor guiGraphics, int left, int top,
                                            float mouseX, float mouseY, LivingEntity entity) {
        //? if >=26.1 {
        InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics,
                left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, entity);
        //?} elif >=1.21.2 {
        /*InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics,
                left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, mouseX, mouseY, entity);
        *///?} else {
        /*renderEntityInInventoryFollowsMouse(guiGraphics, left + 51, top + 75, 30,
                (float) (left + 51) - mouseX, (float) (top + 75 - 50) - mouseY, entity);
        *///?}
    }

    //? if <1.21.2 {
    /*public static void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float)Math.atan(mouseX / 40.0F);
        float g = (float)Math.atan(mouseY / 40.0F);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(g * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf2);
        float h = entity.yBodyRot;
        float i = entity.getYRot();
        float j = entity.getXRot();
        float k = entity.yHeadRotO;
        float l = entity.yHeadRot;
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-g * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        renderEntityInInventory(guiGraphics, x, y, scale, quaternionf, quaternionf2, entity);
        entity.yBodyRot = h;
        entity.setYRot(i);
        entity.setXRot(j);
        entity.yHeadRotO = k;
        entity.yHeadRot = l;
    }

    public static void renderEntityInInventory(GuiGraphics guiGraphics, int x, int y, int scale, Quaternionf pose, @Nullable Quaternionf cameraOrientation, LivingEntity entity) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, (double)50.0F);
        McRenderUtils.mulPose(guiGraphics.pose(), (new Matrix4f()).scaling((float)scale, (float)scale, (float)(-scale)));
        guiGraphics.pose().mulPose(pose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            cameraOrientation.conjugate();
            entityRenderDispatcher.overrideCameraOrientation(cameraOrientation);
        }

        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880));
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }
    *///?}
}
