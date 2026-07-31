package com.possible_triangle.create_jetpack.index

import com.possible_triangle.create_jetpack.CJContent.REGISTRATE
import com.possible_triangle.create_jetpack.world.recipe.CopyComponentsMechanicalCraftingRecipe
import net.minecraft.core.registries.Registries

object CJRecipeTypes {
    val COPY_NBT_MECHANICAL_CRAFTING_SERIALIZER =
        REGISTRATE
            .generic(
                "copy_components_mechanical_crafting",
                Registries.RECIPE_SERIALIZER,
            ) { CopyComponentsMechanicalCraftingRecipe.Serializer }
            .register()

    internal fun register() {
        // Load this class
    }
}
