package org.vmstudio.essentials.core.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.essentials.core.client.tasks.BowItemTask;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.render.RenderPipelineStageVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.player.VRPose;

import static org.vmstudio.essentials.core.client.AddonEntryClient.MC;


public class BowAimCrosshairRenderer implements VREventListener {

    private static final Identifier ICONS_LOC = McRenderUtils.crosshairTexture();
    private static final float UV_SIZE = McRenderUtils.crosshairUvSize();

    private static final double MAX_AIM_DISTANCE = 64.0;
    private static final float SCALE_REF_DISTANCE = 4.5f;
    private static final float BASE_SCALE = 0.06f;
    private static final float SURFACE_OFFSET = 0.01f;

    private static final float MISS_BRIGHTNESS = 0.5f;
    private static final float READY_BRIGHTNESS = 0.4f;
    private static final float MIN_LIGHT = 0.15f;

    public BowAimCrosshairRenderer(@NotNull VisorAddon owner) {
        VisorAPI.eventBus().registerListener(owner, this);
    }

    @VREventHandler
    public void onRenderPipelineStage(@NotNull RenderPipelineStageVREvent event) {
        if (event.getStage() != RenderPipelineStage.AFTER_WORLD
                || VisorAPI.clientState().stateMode().isNotActive()) {
            return;
        }
        BowItemTask task = BowItemTask.getInstance();
        if (task == null || MC.player == null || MC.level == null) {
            return;
        }
        if (!task.isActive(MC.player) || !task.isNotched()) {
            return;
        }

        var renderPose = VisorAPI.client()
                .getVRLocalPlayer()
                .getPoseData(PlayerPoseType.RENDER);
        VRPose bowHand = renderPose.getHand(task.getBowHolder());

        Vec3 handPos = bowHand.getPositionVec3();
        Vec3 aimDir = bowHand.getDirectionVec3();
        if (aimDir.lengthSqr() < 1.0E-6) {
            return;
        }
        aimDir = aimDir.normalize();

        Vec3 rayStart = handPos.add(aimDir);
        Vec3 rayEnd = handPos.add(aimDir.scale(MAX_AIM_DISTANCE));

        HitResult blockHit = MC.level.clip(new ClipContext(
                rayStart, rayEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                MC.player
        ));
        boolean hitSomething = blockHit.getType() != HitResult.Type.MISS;
        Vec3 hitPos = hitSomething ? blockHit.getLocation() : rayEnd;

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                MC.player, rayStart, hitPos,
                new AABB(rayStart, hitPos).inflate(1.0),
                entity -> !entity.isSpectator() && entity.isPickable(),
                hitPos.distanceToSqr(rayStart)
        );
        if (entityHit != null) {
            hitPos = entityHit.getLocation();
            hitSomething = true;
        }

        VRPose cameraPose = renderPose.getCameraPose(event.getRenderPass());

        Vec3 renderPos = hitPos.subtract(aimDir.scale(SURFACE_OFFSET));

        float distance = (float) hitPos.distanceTo(handPos);
        float scale = BASE_SCALE
                * Mth.sqrt(renderPose.getWorldScale())
                * Math.max(1.0f, distance / SCALE_REF_DISTANCE);

        render(
                event.getPoseStack(),
                cameraPose, renderPos, aimDir, scale,
                getBrightness(task, renderPos, hitSomething)
        );
    }

    private float getBrightness(@NotNull BowItemTask task,
                                @NotNull Vec3 renderPos,
                                boolean hitSomething) {
        float light = MC.level.getMaxLocalRawBrightness(
                BlockPos.containing(renderPos)
        ) / (float) LightEngine.MAX_LEVEL;
        float brightness = Math.max(light, MIN_LIGHT);
        if (!hitSomething) {
            brightness *= MISS_BRIGHTNESS;
        }
        if (!task.isDrawingBow()) {
            brightness *= READY_BRIGHTNESS;
        }
        return brightness;
    }

    private void render(@NotNull PoseStack poseStack,
                        @NotNull VRPose cameraPose,
                        @NotNull Vec3 renderPos,
                        @NotNull Vec3 aimDir,
                        float scale,
                        float brightness) {

        // --- Prepare variables ---

        float horizontalLength = Mth.sqrt(
                (float) (aimDir.x * aimDir.x + aimDir.z * aimDir.z)
        );
        float yaw = (float) Math.toDegrees(Mth.atan2(-aimDir.x, aimDir.z));
        float pitch = (float) Math.toDegrees(Mth.atan2(-aimDir.y, horizontalLength));

        // --- Setup ---
        McGlState.setShaderColor(1f, 1f, 1f, 1f);

        McGlState.enableDepthTest();
        McGlState.depthMask(false);
        McGlState.depthFunc(GL11C.GL_ALWAYS);

        McGlState.enableBlend();
        McGlState.blendFuncSeparate(
                McGlState.Blend.ONE_MINUS_DST_COLOR,
                McGlState.Blend.ONE_MINUS_SRC_COLOR,
                McGlState.Blend.ONE,
                McGlState.Blend.ZERO
        );
        McGlState.disableCull();

        McGlState.setShaderTexture(0, ICONS_LOC);
        McShaders.use(McShaders.Core.POSITION_TEX_COLOR);

        poseStack.pushPose();

        poseStack.setIdentity();
        Matrix4f viewRotation = cameraPose.getRotation().transpose(new Matrix4f());
        poseStack.last().pose().mul(viewRotation);
        poseStack.last().normal().mul(new Matrix3f(viewRotation));
        Vec3 cameraPos = cameraPose.getPositionVec3();
        poseStack.translate(
                renderPos.x - cameraPos.x,
                renderPos.y - cameraPos.y,
                renderPos.z - cameraPos.z
        );

        // --- Render ---
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(scale, scale, scale);
        Matrix4f mat = poseStack.last().pose();

        McVertexBuilder buf = McVertexBuilder.get();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buf.vertex(mat, -1f, 1f, 0f)
                .uv(UV_SIZE, 0f)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat, 1f, 1f, 0f)
                .uv(0f, 0f)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat, 1f, -1f, 0f)
                .uv(0f, UV_SIZE)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.vertex(mat, -1f, -1f, 0f)
                .uv(UV_SIZE, UV_SIZE)
                .color(brightness, brightness, brightness, 1f)
                .endVertex();
        buf.draw();

        // --- Restore ---
        poseStack.popPose();
        McGlState.setShaderColor(1f, 1f, 1f, 1f);
        McGlState.enableCull();
        McGlState.defaultBlendFunc();
        McGlState.disableBlend();
        McGlState.depthMask(true);
        McGlState.depthFunc(GL11C.GL_LEQUAL);
    }
}
