package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import com.possible_triangle.create_jetpack.block.JetpackBlock
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.item.Jetpack
import com.simibubi.create.content.curiosities.armor.CopperBacktankTileEntity
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object Content {

    private val ITEMS = KDeferredRegister(ForgeRegistries.ITEMS, MOD_ID)
    private val BLOCKS = KDeferredRegister(ForgeRegistries.BLOCKS, MOD_ID)
    private val TILES = KDeferredRegister(ForgeRegistries.TILE_ENTITIES, MOD_ID)
    private val EFFECTS = KDeferredRegister(ForgeRegistries.POTIONS, MOD_ID)
    private val FLUIDS = KDeferredRegister(ForgeRegistries.FLUIDS, MOD_ID)
    private val RECIPE_SERIALIZERS = KDeferredRegister(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID)

    val JETPACK by ITEMS.registerObject("jetpack") { Jetpack() }
    val JETPACK_BLOCK by BLOCKS.registerObject("jetpack") { JetpackBlock() }
    val JETPACK_TILE by TILES.registerObject("jetpack") {
        TileEntityType.Builder.create(::createJetpackTile, JETPACK_BLOCK)
            .build(null)
    }

    private fun createJetpackTile(): CopperBacktankTileEntity {
        return CopperBacktankTileEntity(JETPACK_TILE)
    }

    @CapabilityInject(IJetpack::class)
    lateinit var JETPACK_CAPABILITY: Capability<IJetpack>

    fun attachCapabilities(stack: ItemStack, add: (id: ResourceLocation, ICapabilityProvider) -> Unit) {
        val item = stack.item
        if (item is Jetpack) add(ResourceLocation(MOD_ID, "jetpack"), item)
    }

    fun register() {
        listOf(ITEMS, BLOCKS, EFFECTS, FLUIDS, TILES, RECIPE_SERIALIZERS).forEach {
            it.register(MOD_BUS)
        }
    }

}