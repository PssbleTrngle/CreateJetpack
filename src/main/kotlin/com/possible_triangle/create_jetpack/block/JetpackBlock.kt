package com.possible_triangle.create_jetpack.block

import com.possible_triangle.create_jetpack.Content
import com.simibubi.create.AllBlocks
import com.simibubi.create.content.curiosities.armor.CopperBacktankBlock
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.ListNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class JetpackBlock : CopperBacktankBlock(Properties.from(AllBlocks.COPPER_BACKTANK.get())) {

    override fun getItem(world: IBlockReader, pos: BlockPos, state: BlockState): ItemStack {
        val item = ItemStack(Content.JETPACK)
        val tile = getTileEntityOptional(world, pos)

        val air = tile.map { it.getAirLevel() }.orElse(0) as Int
        item.orCreateTag.putInt("Air", air)

        val enchants = tile.map { it.enchantmentTag }.orElse(ListNBT())
        if (!enchants.isEmpty()) {
            val enchantmentTagList = item.enchantmentTagList
            enchantmentTagList.addAll(enchants)
            item.orCreateTag.put("Enchantments", enchantmentTagList)
        }

        tile.map { it.customName }.ifPresent { item.displayName = it }

        return item
    }

    override fun createTileEntity(state: BlockState, world: IBlockReader): TileEntity? {
        return Content.JETPACK_TILE.create()
    }

}