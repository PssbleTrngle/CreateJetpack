package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.CreateJetpack.MOD_ID
import com.possible_triangle.create_jetpack.block.JetpackBlock
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.item.Jetpack
import com.simibubi.create.AllItems
import com.simibubi.create.AllTags.pickaxeOnly
import com.simibubi.create.Create
import com.simibubi.create.content.CreateItemGroup
import com.simibubi.create.content.curiosities.armor.CopperBacktankInstance
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem.CopperBacktankBlockItem
import com.simibubi.create.content.curiosities.armor.CopperBacktankRenderer
import com.simibubi.create.content.curiosities.armor.CopperBacktankTileEntity
import com.simibubi.create.foundation.block.BlockStressDefaults
import com.simibubi.create.foundation.data.AssetLookup
import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.repack.registrate.util.nullness.NonNullFunction
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityToken
import net.minecraftforge.common.capabilities.ICapabilityProvider
import java.util.function.BiFunction
import java.util.function.Supplier


object Content {

    private val REGISTRATE = CreateRegistrate.lazy(MOD_ID).get().creativeModeTab { CreateItemGroup.TAB_TOOLS }

    //private val ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
    //private val BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)
    //private val TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID)
    //private val EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, MOD_ID)
    //private val FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID)
    //private val RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID)

    val JETPACK_BLOCK = REGISTRATE.block<JetpackBlock>("jetpack", ::JetpackBlock)
        .initialProperties { SharedProperties.copperMetal() }
        .blockstate { c, p ->
            p.horizontalBlock(c.entry,
                AssetLookup.partialBaseModel(c, p))
        }
        .transform(pickaxeOnly())
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .transform(BlockStressDefaults.setImpact(4.0))
        .loot { lt, block ->
            val builder = LootTable.lootTable()
            val survivesExplosion = ExplosionCondition.survivesExplosion()
            lt.add(block, builder.withPool(LootPool.lootPool()
                .`when`(survivesExplosion)
                .setRolls(ConstantValue.exactly(1F))
                .add(LootItem.lootTableItem(AllItems.COPPER_BACKTANK.get())
                    .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                    .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                        .copy("Air", "Air"))
                    .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                        .copy("Enchantments", "Enchantments")))))
        }
        .register()

    val JETPACK_TILE = Create.registrate()
        .tileEntity("jetpack",
            ::CopperBacktankTileEntity)
        .instance {
            BiFunction { manager, tile ->
                CopperBacktankInstance(manager, tile)
            }
        }
        .validBlocks(JETPACK_BLOCK)
        .renderer {
            NonNullFunction { context: BlockEntityRendererProvider.Context? ->
                CopperBacktankRenderer(context)
            }
        }
        .register()

    val JETPACK_PLACEABLE = REGISTRATE.item<CopperBacktankBlockItem>("jetpack_placeable") {
        CopperBacktankBlockItem(JETPACK_BLOCK.get(), it)
    }.model { context, provider ->
        provider.withExistingParent(context.name, provider.mcLoc("item/barrier"))
    }.register()

    val JETPACK = REGISTRATE.item<Jetpack>("jetpack") { Jetpack(it, JETPACK_PLACEABLE) }
        .model(AssetLookup.customGenericItemModel("_", "item"))
        .register()

    val JETPACK_CAPABILITY = CapabilityManager.get(object : CapabilityToken<IJetpack>() {})

    fun attachCapabilities(stack: ItemStack, add: (id: ResourceLocation, ICapabilityProvider) -> Unit) {
        val item = stack.item
        if (item is Jetpack) add(ResourceLocation(MOD_ID, "jetpack"), item)
    }

    fun register() {
        // Load this class
       // listOf(ITEMS, BLOCKS, EFFECTS, FLUIDS, TILES, RECIPE_SERIALIZERS).forEach {
       //     it.register(MOD_BUS)
       // }
    }

}