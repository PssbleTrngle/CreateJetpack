package com.possible_triangle.create_jetpack

import com.mojang.serialization.MapCodec
import com.simibubi.create.content.equipment.armor.BacktankItem
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe
import net.minecraft.core.HolderLookup
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.ShapedRecipePattern
import kotlin.math.min

class CopyComponentsMechanicalCraftingRecipe(
    group: String,
    category: CraftingBookCategory,
    pattern: ShapedRecipePattern,
    recipeOutput: ItemStack,
    acceptMirrored: Boolean
) :
    MechanicalCraftingRecipe(group, category, pattern, recipeOutput, acceptMirrored) {

    override fun getSerializer(): RecipeSerializer<*> {
        return Content.COPY_NBT_MECHANICAL_CRAFTING_SERIALIZER.get()
    }

    private fun findUpgradable(container: CraftingInput): ItemStack? {
        val width = min(container.width(), width)
        for (x in 0 until width) {
            for (y in 0 until min(container.height(), height)) {
                val stack = container.getItem(x + y * width)
                if (stack.item is BacktankItem) return stack
            }
        }

        return null
    }

    override fun assemble(container: CraftingInput, registries: HolderLookup.Provider): ItemStack {
        val upgradeTarget = findUpgradable(container)
        val upgraded = super.assemble(container, registries)
        upgradeTarget?.let { upgraded.applyComponents(it.components) }
        return upgraded
    }

    object Serializer : MechanicalCraftingRecipe.Serializer() {

        private val CODEC: MapCodec<MechanicalCraftingRecipe> =
            MechanicalCraftingRecipe.Serializer.CODEC.xmap(Serializer::from, Serializer::to)
        private val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, MechanicalCraftingRecipe> =
            MechanicalCraftingRecipe.Serializer.STREAM_CODEC.map(Serializer::from, Serializer::to)

        private fun from(recipe: MechanicalCraftingRecipe): MechanicalCraftingRecipe {
            return CopyComponentsMechanicalCraftingRecipe(
                recipe.group,
                recipe.category(),
                recipe.pattern,
                recipe.getResultItem(null),
                recipe.acceptsMirrored()
            )
        }

        private fun to(recipe: MechanicalCraftingRecipe): MechanicalCraftingRecipe {
            return MechanicalCraftingRecipe(
                recipe.group,
                recipe.category(),
                recipe.pattern,
                recipe.getResultItem(null),
                recipe.acceptsMirrored()
            )
        }

        override fun codec() = CODEC

       // override fun streamCodec() = STREAM_CODEC

    }

}
