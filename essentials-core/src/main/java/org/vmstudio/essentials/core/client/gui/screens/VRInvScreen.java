package org.vmstudio.essentials.core.client.gui.screens;

import org.vmstudio.essentials.core.client.gui.InventoryEntityPreview;
import org.vmstudio.essentials.core.compatibility.mcversion.EssentialsGuiUtils;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.essentials.core.client.gui.ContainerSlot;
import org.vmstudio.essentials.core.client.gui.RecipeBookButton;
import org.vmstudio.essentials.core.client.gui.overlays.VROverlayContainer;
import org.vmstudio.essentials.core.client.extensions.AbstractContainerScreenExtension;
import org.vmstudio.essentials.core.common.VisorEssentials;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
//? if >=1.21.2 {
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
//?} else {
/*import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Iterator;
*///?}
//? if >=1.20.2 && <1.21.2 {
/*import net.minecraft.world.item.crafting.RecipeHolder;
*///?} elif <1.20.2 {
/*import net.minecraft.world.item.crafting.Recipe;
*///?}
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.api.server.VRServerSettings;

import java.util.List;


public class VRInvScreen extends VRInvEffectInvScreen implements AbstractContainerScreenExtension, RecipeUpdateListener {
    private GuiTexture IMAGE_FULL = new GuiTexture(
            McVersionUtils.newResourceLoc(VisorEssentials.MOD_ID,"textures/gui/inventory.png"),
            0,0,258,156
    );
    private GuiTexture IMAGE_FULL_WITH_OFFHAND = new GuiTexture(
            McVersionUtils.newResourceLoc(VisorEssentials.MOD_ID,"textures/gui/inventory_with_offhand.png"),
            0,0,278,156
    );
    private GuiTexture IMAGE_SIMPLIFIED = new GuiTexture(
            McVersionUtils.newResourceLoc(VisorEssentials.MOD_ID,"textures/gui/inventory_simplified.png"),
            0,0,258,156
    );


    public static final int IMAGE_HEIGHT = 156;
    public static final int RECIPE_BOOK_GAP = 2;
    public static final int CREATIVE_BUTTON_WIDTH = 120;
    public static final int CREATIVE_BUTTON_HEIGHT = 20;
    public static final int CREATIVE_BUTTON_GAP = 4;
    // canvas tall enough to fit the recipe book below the centered inventory
    public static final int MIN_CANVAS_HEIGHT =
            IMAGE_HEIGHT + 2 * (RecipeBookComponent.IMAGE_HEIGHT + RECIPE_BOOK_GAP);

    //? if >=1.21.2 {
    // bound to its menu since 1.21.2, none while the panel shows a container's menu
    @Nullable
    private final CraftingRecipeBookComponent recipeBookComponent;
    //?} else {
    /*private final RecipeBookComponent recipeBookComponent = new VRRecipeBookComponent();
    *///?}
    private boolean recipeBookAvailable;
    // a recipe update arrived while another menu (creative ItemPickerMenu, a chest) was current
    private boolean recipesDirty;

    private Button creativeButton;

    private float xMouse;
    private float yMouse;

    private boolean buttonClicked;

    public VRInvScreen(AbstractContainerMenu menu,
                       Inventory inventory) {
        super(menu,  inventory, Component.literal(""));
        //? if >=1.21.2 {
        this.recipeBookComponent = menu instanceof AbstractCraftingMenu craftingMenu
                ? new CraftingRecipeBookComponent(craftingMenu)
                : null;
        //?}
        this.titleLabelX = 97;
        this.imageWidth = hasOffhandSlot() ? 278 : 258;
        this.imageHeight = IMAGE_HEIGHT;
        visorEssentials$setVRContainer(true);
    }

    private static boolean hasOffhandSlot() {
        return !VRServerSettings.isTwoHandedVR();
    }
    @Override
    public void visorEssentials$fillVRSlots(
            @NotNull List<ContainerSlot> slots
    ) {
        fullInventory = !VisorAPI.client().getGuiManager()
                .getOverlayManager().getOverlay(VROverlayContainer.ID).isEnabled();

        boolean hasOffhand = hasOffhandSlot();

        int xOffset = hasOffhand ? 20 : 0;

        slots.clear();
        for(Slot slot : menu.slots){
            int posX = 0;
            int posY = 0;
            if((slot.container instanceof CraftingContainer)
                    && fullInventory){
                // 2x2 grid layout
                int index = slot.getContainerSlot();
                int row = index / 2;
                int col = index % 2;
                posX = 181 + xOffset + col * 18;
                posY = 26 + row * 18;
                slots.add(new ContainerSlot(slot, posX,posY));
            }else if(slot.container instanceof Inventory){
                if(slot.getContainerSlot()<=8){
                    //hotbar
                    switch (slot.getContainerSlot()){
                        case 0 -> {
                            posX = 121 + xOffset;
                            posY = 38;
                        }
                        case 1 -> {
                            posX = 121 + xOffset;
                            posY = 11;
                        }
                        case 2 -> {
                            posX = 148 + xOffset;
                            posY = 11;
                        }
                        case 3 -> {
                            posX = 148 + xOffset;
                            posY = 38;
                        }
                        case 4 -> {
                            posX = 148 + xOffset;
                            posY = 65;
                        }
                        case 5 -> {
                            posX = 121 + xOffset;
                            posY = 65;
                        }
                        case 6 -> {
                            posX = 94 + xOffset;
                            posY = 65;
                        }
                        case 7 -> {
                            posX = 94 + xOffset;
                            posY = 38;
                        }
                        case 8 -> {
                            posX = 94 + xOffset;
                            posY = 11;
                        }
                    }

                }else if(slot.getContainerSlot()<=35) {
                    // 9x3 grid layout
                    int index = slot.getContainerSlot() - 9;
                    int row = index / 9;
                    int col = index % 9;
                    posX = 49 + xOffset + col * 18;
                    posY = 96 + row * 18;
                }else{
                    if(!fullInventory) continue;
                    if(slot.getContainerSlot() == 40){
                        if(!hasOffhand) continue;
                        // offhand slot
                        posX = 79;
                        posY = 62;
                    }else {
                        // equipment slots
                        int index = slot.getContainerSlot() - 36;
                        posX = 8;
                        posY = 62 + index * -18;
                    }
                }
                slots.add(new ContainerSlot(slot,posX,posY));
            }
            else if(fullInventory && slot instanceof ResultSlot){
                posX = 237 + xOffset;
                posY = 36;
                slots.add(new ContainerSlot(slot, posX,posY));
            }
        }
    }
    @Override
    protected void init() {
        super.init();
        // [-- Modified: vanilla swaps the whole screen for the creative one,
        // here it is an opt-in button placed above the inventory
        this.creativeButton = null;
        if (fullInventory) {
            this.creativeButton = this.addRenderableWidget(Button.builder(
                            Component.translatable(VisorEssentials.MOD_ID + ".gui.open_creative"),
                            button -> openCreativeInventory())
                    .bounds(
                            this.leftPos + (this.imageWidth - CREATIVE_BUTTON_WIDTH) / 2,
                            this.topPos - CREATIVE_BUTTON_GAP - CREATIVE_BUTTON_HEIGHT,
                            CREATIVE_BUTTON_WIDTH,
                            CREATIVE_BUTTON_HEIGHT
                    )
                    .build());
            this.creativeButton.visible = isCreativeMode();
        }
        // --]
        //? if >=1.21.2 {
        recipeBookAvailable = fullInventory
                && this.recipeBookComponent != null;
        //?} else {
        /*recipeBookAvailable = fullInventory
                && this.menu instanceof RecipeBookMenu;
        *///?}
        if (!recipeBookAvailable) {
            return;
        }

        int bookLeft = (this.width - RecipeBookComponent.IMAGE_WIDTH) / 2;
        int bookTop = this.topPos + this.imageHeight + RECIPE_BOOK_GAP;
        //? if >=1.21.2 {
        this.recipeBookComponent.init(
                2 * (bookLeft + 86) + RecipeBookComponent.IMAGE_WIDTH,
                2 * bookTop + RecipeBookComponent.IMAGE_HEIGHT,
                this.minecraft,
                false
        );
        //?} else {
        /*this.recipeBookComponent.init(
                2 * (bookLeft + 86) + RecipeBookComponent.IMAGE_WIDTH,
                2 * bookTop + RecipeBookComponent.IMAGE_HEIGHT,
                this.minecraft,
                false,
                (RecipeBookMenu) this.menu
        );
        *///?}
        int xOffset = hasOffhandSlot() ? 20 : 0;
        this.addRenderableWidget(RecipeBookButton.create(this.leftPos + 188 + xOffset, this.topPos + 62, (button) -> {
            this.recipeBookComponent.toggleVisibility();
            this.buttonClicked = true;
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    public void containerTick() {
        // [-- Modified: creative-screen switch replaced with a button,
        // its visibility follows the game mode
        if (creativeButton != null) {
            creativeButton.visible = isCreativeMode();
        }
        // --]
        if (recipeBookAvailable) {
            if (recipesDirty && isMenuCurrent()) {
                recipesDirty = false;
                this.recipeBookComponent.recipesUpdated();
            }
            this.recipeBookComponent.tick();
        }
    }


    private boolean isMenuCurrent() {
        return this.minecraft != null
                && this.minecraft.player != null
                && this.minecraft.player.containerMenu == this.menu;
    }

    private boolean isCreativeMode() {
        return this.minecraft != null
                && this.minecraft.gameMode != null
                && this.minecraft.gameMode.getPlayerMode().isCreative();
    }

    private boolean isCreativeButtonVisible() {
        return creativeButton != null && creativeButton.visible;
    }

    private void openCreativeInventory() {
        McVersionClientUtils.setScreen(new CreativeModeInventoryScreen(
                this.minecraft.player,
                this.minecraft.player.connection.enabledFeatures(),
                this.minecraft.options.operatorItemsTab().get()
        ));
    }

    // [-- Modified: no renderBackground inside the overlay,
    // no narrow-screen mode - the book always fits below,
    // recipe book calls guarded by availability
    //? if >=26.1 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // the slot tooltip is part of super since 26.1
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (recipeBookAvailable) {
            EssentialsGuiUtils.nextStratum(guiGraphics);
            this.recipeBookComponent.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.extractTooltip(guiGraphics, mouseX, mouseY, this.hoveredSlot);
        }
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }
    //?} elif >=1.21.2 {
    /*@Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (recipeBookAvailable) {
            EssentialsGuiUtils.nextStratum(guiGraphics);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderTooltip(guiGraphics, mouseX, mouseY, this.hoveredSlot);
        }
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }
    *///?} else {
    /*@Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (recipeBookAvailable) {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, partialTick);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        }
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }
    *///?}
    // --]

    // the ghost recipe is part of the slot pass since 1.21.2, drawn at the VR slots by GhostSlotsMixin
    //? if >=26.1 {
    @Override
    protected void extractSlots(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractSlots(guiGraphics, mouseX, mouseY);
        if (recipeBookAvailable) {
            this.recipeBookComponent.extractGhostRecipe(guiGraphics, false);
        }
    }
    //?} elif >=1.21.11 {
    /*@Override
    protected void renderSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderSlots(guiGraphics, mouseX, mouseY);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, false);
        }
    }
    *///?} elif >=1.21.2 {
    /*@Override
    protected void renderSlots(GuiGraphics guiGraphics) {
        super.renderSlots(guiGraphics);
        if (recipeBookAvailable) {
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, false);
        }
    }
    *///?}


    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*@Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    *///?}
        int i = this.leftPos;
        int j = this.topPos;

        // [-- Modified
        //guiGraphics.blit(INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        if(fullInventory){
            (hasOffhandSlot() ? IMAGE_FULL_WITH_OFFHAND : IMAGE_FULL).blit(guiGraphics, i, j);
        }else {
            IMAGE_SIMPLIFIED.blit(guiGraphics, i + imageWidth - IMAGE_SIMPLIFIED.getWidth(), j);
        }

        if(fullInventory) {
            InventoryEntityPreview.renderFollowingMouse(guiGraphics, i, j, this.xMouse, this.yMouse, this.minecraft.player);
        }
        // --]
    }

    //? if >=1.21.9 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (recipeBookAvailable && this.recipeBookComponent.mouseClicked(event, doubleClick)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(event);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        boolean bl = isOutsidePanel(mouseX, mouseY, guiLeft, guiTop);
        if (!recipeBookAvailable) {
            return bl;
        }
        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight) && bl;
    }
    //?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (recipeBookAvailable && this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean bl = isOutsidePanel(mouseX, mouseY, guiLeft, guiTop);
        if (!recipeBookAvailable) {
            return bl;
        }
        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && bl;
    }
    *///?}

    private boolean isOutsidePanel(double mouseX, double mouseY, int guiLeft, int guiTop) {
        return mouseX < (double)guiLeft || mouseY < (double)guiTop || mouseX >= (double)(guiLeft + this.imageWidth) || mouseY >= (double)(guiTop + this.imageHeight);
    }

    //? if >=26.1 {
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        if (recipeBookAvailable) {
            this.recipeBookComponent.slotClicked(slot);
        }
    }
    //?} else {
    /*protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        if (recipeBookAvailable) {
            this.recipeBookComponent.slotClicked(slot);
        }
    }
    *///?}

    @Override
    public void recipesUpdated() {
        if (!recipeBookAvailable) {
            return;
        }
        // [-- Modified: deferred until this menu is current again, see isMenuCurrent()
        if (!isMenuCurrent()) {
            recipesDirty = true;
            return;
        }
        // --]
        recipesDirty = false;
        this.recipeBookComponent.recipesUpdated();
    }

    //? if >=1.21.2 {
    @Override
    public void fillGhostRecipe(RecipeDisplay recipeDisplay) {
        if (recipeBookAvailable) {
            this.recipeBookComponent.fillGhostRecipe(recipeDisplay);
        }
    }
    //?} else {
    /*@Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
    *///?}



    private boolean isRecipeBookOpen() {
        return recipeBookAvailable && this.recipeBookComponent.isVisible();
    }

    @Override
    public int visorEssentials$getEdgeX() {
        return fullInventory ? leftPos
                : leftPos + imageWidth - IMAGE_SIMPLIFIED.getWidth() + 40;
    }

    @Override
    public int visorEssentials$getEdgeY() {
        // extend the interactable area over the button above
        if (isCreativeButtonVisible()) {
            return topPos - CREATIVE_BUTTON_GAP - CREATIVE_BUTTON_HEIGHT;
        }
        return topPos;
    }

    @Override
    public int visorEssentials$getEdgeWidth() {
        if(hasEffects){
            return fullInventory
                    ? imageWidth + 40 : IMAGE_SIMPLIFIED.getWidth() - 40;
        }else {
            return fullInventory
                    ? imageWidth : IMAGE_SIMPLIFIED.getWidth() - 80;
        }    }

    @Override
    public int visorEssentials$getEdgeHeight() {
        int height = imageHeight;
        // extend the interactable area over the book below
        if (isRecipeBookOpen()) {
            height += RECIPE_BOOK_GAP + RecipeBookComponent.IMAGE_HEIGHT;
        }
        if (isCreativeButtonVisible()) {
            height += CREATIVE_BUTTON_GAP + CREATIVE_BUTTON_HEIGHT;
        }
        return height;
    }


    //? if <1.21.2 {
    /*private class VRRecipeBookComponent extends RecipeBookComponent {
    *///?}

    //? if >=1.20.2 && <1.21.2 {
    /*    @Override
        public void setupGhostRecipe(RecipeHolder<?> recipe, List<Slot> slots) {
            if (!recipeBookAvailable) {
                return;
            }
            ItemStack resultStack = recipe.value().getResultItem(this.minecraft.level.registryAccess());
            this.ghostRecipe.setRecipe(recipe);
            addGhostIngredient(Ingredient.of(resultStack), slots.get(0));
            this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.value().getIngredients().iterator(), 0);
        }
    *///?} elif <1.20.2 {
    /*    @Override
        public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
            if (!recipeBookAvailable) {
                return;
            }
            ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
            this.ghostRecipe.setRecipe(recipe);
            addGhostIngredient(Ingredient.of(resultStack), slots.get(0));
            this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
        }
    *///?}

    //? if >=1.21 && <1.21.2 {
    /*    @Override
        public void addItemToSlot(Ingredient ingredient, int slotIndex, int maxAmount, int gridX, int gridY) {
    *///?} elif <1.21 {
    /*    @Override
        public void addItemToSlot(Iterator<Ingredient> ingredients, int slotIndex, int maxAmount, int gridX, int gridY) {
            Ingredient ingredient = ingredients.next();
    *///?}
    //? if <1.21.2 {
    /*        if (!ingredient.isEmpty()) {
                addGhostIngredient(ingredient, this.menu.slots.get(slotIndex));
            }
        }

        private void addGhostIngredient(Ingredient ingredient, Slot slot) {
            for (ContainerSlot vrSlot : visorEssentials$getVRSlots()) {
                if (vrSlot.parent() == slot) {
                    this.ghostRecipe.addIngredient(ingredient, vrSlot.vrPosX(), vrSlot.vrPosY());
                    return;
                }
            }
            this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
        }
    }
    *///?}
}
