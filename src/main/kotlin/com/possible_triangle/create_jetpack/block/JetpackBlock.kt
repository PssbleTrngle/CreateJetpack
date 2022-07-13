package com.possible_triangle.create_jetpack.block

import com.possible_triangle.create_jetpack.Content
import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock
import com.simibubi.create.content.curiosities.armor.CopperBacktankTileEntity
import net.minecraft.core.BlockPos
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class JetpackBlock(properties: Properties) : CopperBacktankBlock(properties) {

    override fun getCloneItemStack(
        world: BlockGetter,
        pos: BlockPos,
        state: BlockState
    ): ItemStack {
        val item = ItemStack(Content.JETPACK.get())
        val tile = getTileEntityOptional(world, pos)

        val air = tile.map { it.getAirLevel() }.orElse(0) as Int
        item.orCreateTag.putInt("Air", air)

        val enchants = tile.map { it.enchantmentTag }.orElse(ListTag())
        if (!enchants.isEmpty()) {
            val enchantmentTagList = item.enchantmentTags
            enchantmentTagList.addAll(enchants)
            item.orCreateTag.put("Enchantments", enchantmentTagList)
        }

        tile.map { it.customName }.ifPresent { item.hoverName = it }

        return item
    }

    override fun getTileEntityType(): BlockEntityType<out CopperBacktankTileEntity> {
        return Content.JETPACK_TILE.get()
    }

}