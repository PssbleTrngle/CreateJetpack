package com.possible_triangle.create_jetpack.data.providers

import com.possible_triangle.create_jetpack.Content
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllItems
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeBuilder
import net.minecraft.data.DataGenerator
import net.minecraft.data.IFinishedRecipe
import net.minecraft.data.RecipeProvider
import net.minecraft.item.Items
import net.minecraft.item.crafting.Ingredient
import net.minecraft.tags.ItemTags
import java.util.function.Consumer

class Recipes(gen: DataGenerator) : RecipeProvider(gen) {

    companion object {
        val BRASS_PLATE = Ingredient.fromTag(ItemTags.makeWrapperTag("forge:plates/brass"))
    }

    override fun registerRecipes(consumer: Consumer<IFinishedRecipe>) {

        MechanicalCraftingRecipeBuilder.shapedRecipe(Content.JETPACK)
            .patternLine(" PYP ")
            .patternLine("PXEXP")
            .patternLine("PSXSP")
            .patternLine(" C C ")
            .key('E', Items.ELYTRA)
            .key('S', AllBlocks.SHAFT.get())
            .key('C', AllBlocks.COGWHEEL.get())
            .key('X', AllItems.CHROMATIC_COMPOUND.get())
            .key('Y', AllItems.PRECISION_MECHANISM.get())
            .key('P', BRASS_PLATE)
            .build(consumer)
    }

}