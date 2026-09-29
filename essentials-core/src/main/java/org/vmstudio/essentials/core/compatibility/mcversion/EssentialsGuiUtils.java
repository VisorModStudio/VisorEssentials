package org.vmstudio.essentials.core.compatibility.mcversion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.List;
import java.util.Optional;
//? if >=1.21.6 {
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderPipelines;
//?} elif >=1.21.2 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
*///?} elif <1.20.2 {
/*import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
*///?}

// GUI adapters Visor's McGuiUtils has no counterpart for
public class EssentialsGuiUtils {
    private EssentialsGuiUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    //? if >=1.21.11 {
    // one nine-sliced sprite for both sizes
    private static final Identifier EFFECT_BACKGROUND_LARGE =
            McVersionUtils.newResourceLoc("container/inventory/effect_background");
    private static final Identifier EFFECT_BACKGROUND_SMALL = EFFECT_BACKGROUND_LARGE;
    //?} elif >=1.20.2 {
    /*private static final Identifier EFFECT_BACKGROUND_LARGE =
            McVersionUtils.newResourceLoc("container/inventory/effect_background_large");
    private static final Identifier EFFECT_BACKGROUND_SMALL =
            McVersionUtils.newResourceLoc("container/inventory/effect_background_small");
    *///?}


    // ------- MOB EFFECTS -------

    public static void blitEffectBackground(GuiGraphics guiGraphics, int x, int y, boolean large) {
        int width = large ? 120 : 32;
        //? if >=1.21.6 {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                large ? EFFECT_BACKGROUND_LARGE : EFFECT_BACKGROUND_SMALL, x, y, width, 32);
        //?} elif >=1.21.2 {
        /*guiGraphics.blitSprite(RenderType::guiTextured,
                large ? EFFECT_BACKGROUND_LARGE : EFFECT_BACKGROUND_SMALL, x, y, width, 32);
        *///?} elif >=1.20.2 {
        /*guiGraphics.blitSprite(large ? EFFECT_BACKGROUND_LARGE : EFFECT_BACKGROUND_SMALL, x, y, width, 32);
        *///?} else {
        /*guiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, x, y, 0, large ? 166 : 198, width, 32);
        *///?}
    }

    public static void blitEffectIcon(GuiGraphics guiGraphics, MobEffectInstance effect, int x, int y, int size) {
        //? if >=1.21.6 {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                Gui.getMobEffectSprite(effect.getEffect()), x, y, size, size);
        //?} elif >=1.21.2 {
        /*guiGraphics.blitSprite(RenderType::guiTextured,
                Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect()), x, y, size, size);
        *///?} else {
        /*guiGraphics.blit(x, y, 0, size, size,
                Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect()));
        *///?}
    }


    // ------- LAYERS -------

    // 1.21.6+ batches GUI draws: what follows goes on top of what came before
    public static void nextStratum(GuiGraphics guiGraphics) {
        //? if >=1.21.6 {
        guiGraphics.nextStratum();
        //?}
    }


    // ------- TOOLTIPS -------

    public static void setTooltipForNextFrame(GuiGraphics guiGraphics, Font font,
                                              List<Component> lines, int x, int y) {
        //? if >=1.21.6 {
        guiGraphics.setTooltipForNextFrame(font, lines, Optional.empty(), x, y);
        //?} else {
        /*guiGraphics.renderTooltip(font, lines, Optional.empty(), x, y);
        *///?}
    }
}
