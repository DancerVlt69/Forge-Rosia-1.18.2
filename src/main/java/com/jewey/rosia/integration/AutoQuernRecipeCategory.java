package com.jewey.rosia.integration;

import com.jewey.rosia.Rosia;
import com.jewey.rosia.common.blocks.ModBlocks;
import com.jewey.rosia.common.items.ModItems;
import com.jewey.rosia.recipe.AutoQuernRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

public class AutoQuernRecipeCategory implements IRecipeCategory<AutoQuernRecipe> {

    public final static ResourceLocation UID = new ResourceLocation(Rosia.MOD_ID, "auto_quern");
    public final static ResourceLocation TEXTURE =
            new ResourceLocation(Rosia.MOD_ID, "textures/gui/auto_quern_jei_2.png");

    private final IDrawable background;
    private final IDrawable icon;

    public AutoQuernRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 97, 26);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.AUTO_QUERN.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends AutoQuernRecipe> getRecipeClass() {
        return AutoQuernRecipe.class;
    }

    @Override
    public Component getTitle() {
        return new TextComponent("Auto-Quern");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull AutoQuernRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 25, 5).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addIngredients(Ingredient.of(ModItems.STEEL_GRINDSTONE.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 5).addItemStack(recipe.getResultItem());
    }
}
