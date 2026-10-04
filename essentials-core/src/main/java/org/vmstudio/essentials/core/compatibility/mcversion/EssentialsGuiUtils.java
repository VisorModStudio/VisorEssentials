package org.vmstudio.essentials.core.compatibility.mcversion;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import java.util.List;
import java.util.Optional;
//? if >=26.1 {
import net.minecraft.world.inventory.ContainerInput;
//?} else {
/*import net.minecraft.world.inventory.ClickType;
*///?}
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
//?} elif >=1.21.6 {
/*import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderPipelines;
*///?} elif >=1.21.2 {
/*import net.minecraft.client.renderer.RenderType;
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
    /*private static final ResourceLocation EFFECT_BACKGROUND_LARGE =
            McVersionUtils.newResourceLoc("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL =
            McVersionUtils.newResourceLoc("container/inventory/effect_background_small");
    *///?}


    // ------- MOB EFFECTS -------

    public static void blitEffectBackground(GuiGraphicsExtractor guiGraphics, int x, int y, boolean large) {
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

    public static void blitEffectIcon(GuiGraphicsExtractor guiGraphics, MobEffectInstance effect, int x, int y, int size) {
        //? if >=26.2 {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                Hud.getMobEffectSprite(effect.getEffect()), x, y, size, size);
        //?} elif >=1.21.6 {
        /*guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                Gui.getMobEffectSprite(effect.getEffect()), x, y, size, size);
        *///?} elif >=1.21.2 {
        /*guiGraphics.blitSprite(RenderType::guiTextured,
                Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect()), x, y, size, size);
        *///?} else {
        /*guiGraphics.blit(x, y, 0, size, size,
                Minecraft.getInstance().getMobEffectTextures().get(effect.getEffect()));
        *///?}
    }


    // ------- LAYERS -------

    // 1.21.6+ batches GUI draws: what follows goes on top of what came before
    public static void nextStratum(GuiGraphicsExtractor guiGraphics) {
        //? if >=1.21.6 {
        guiGraphics.nextStratum();
        //?}
    }


    // ------- CONTAINER CLICKS -------

    public static void pickupClick(MultiPlayerGameMode gameMode, Player player,
                                   int containerId, int slotId, int mouseButton) {
        int button = mouseButton == InputConstants.MOUSE_BUTTON_LEFT ? 0
                : mouseButton == InputConstants.MOUSE_BUTTON_RIGHT ? 1 : mouseButton;
        //? if >=26.1 {
        gameMode.handleContainerInput(containerId, slotId, button, ContainerInput.PICKUP, player);
        //?} else {
        /*gameMode.handleInventoryMouseClick(containerId, slotId, button, ClickType.PICKUP, player);
        *///?}
    }


    // ------- TOOLTIPS -------

    public static void setTooltipForNextFrame(GuiGraphicsExtractor guiGraphics, Font font,
                                              List<Component> lines, int x, int y) {
        //? if >=1.21.6 {
        guiGraphics.setTooltipForNextFrame(font, lines, Optional.empty(), x, y);
        //?} else {
        /*guiGraphics.renderTooltip(font, lines, Optional.empty(), x, y);
        *///?}
    }
}
