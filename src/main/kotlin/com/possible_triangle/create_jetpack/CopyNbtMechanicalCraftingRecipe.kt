package com.possible_triangle.create_jetpack

import com.google.gson.JsonObject
import com.simibubi.create.content.equipment.armor.BacktankItem
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe
import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.ShapedRecipe
import kotlin.math.min

class CopyNbtMechanicalCraftingRecipe(
    idIn: ResourceLocation, group: String, recipeWidth: Int,
    recipeHeight: Int, recipeItems: NonNullList<Ingredient>,
    recipeOutput: ItemStack, acceptMirrored: Boolean,
) : MechanicalCraftingRecipe(
    idIn,
    group, recipeWidth, recipeHeight, recipeItems, recipeOutput, acceptMirrored
) {

    override fun getSerializer(): RecipeSerializer<*> {
        return Content.COPY_NBT_MECHANICAL_CRAFTING_SERIALIZER.get()
    }

    private fun findUpgradable(container: CraftingContainer): ItemStack? {
        val width = min(container.width, width)
        for (x in 0 until width) {
            for (y in 0 until min(container.height, height)) {
                val stack = container.getItem(x + y * width)
                if (stack.item is BacktankItem) return stack
            }
        }

        return null
    }

    override fun assemble(container: CraftingContainer): ItemStack {
        val upgradeTarget = findUpgradable(container)
        val upgraded = super.assemble(container)
        upgradeTarget?.let { upgraded.tag = it.tag }
        return upgraded
    }

    object Serializer : MechanicalCraftingRecipe.Serializer() {

        private fun fromShaped(recipe: ShapedRecipe, acceptMirrored: Boolean): MechanicalCraftingRecipe {
            return CopyNbtMechanicalCraftingRecipe(
                recipe.id,
                recipe.group,
                recipe.width,
                recipe.height,
                recipe.ingredients,
                recipe.resultItem,
                acceptMirrored
            )
        }

        override fun fromJson(recipeId: ResourceLocation, json: JsonObject): ShapedRecipe {
            return fromShaped(super.fromJson(recipeId, json), GsonHelper.getAsBoolean(json, "acceptMirrored", true))
        }

    }

}
