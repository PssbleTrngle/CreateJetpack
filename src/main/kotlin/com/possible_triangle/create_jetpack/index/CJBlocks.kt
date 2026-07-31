package com.possible_triangle.create_jetpack.index

import com.possible_triangle.create_jetpack.CJContent.REGISTRATE
import com.possible_triangle.create_jetpack.world.block.JetpackBlock
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllDataComponents
import com.simibubi.create.api.stress.BlockStressValues
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.foundation.data.TagGen
import com.tterrag.registrate.builders.BlockBuilder
import com.tterrag.registrate.util.entry.BlockEntry
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import java.util.function.Supplier

object CJBlocks {
    val JETPACK: BlockEntry<JetpackBlock> =
        REGISTRATE
            .block("jetpack") { JetpackBlock(it) }
            .initialProperties { SharedProperties.copperMetal() }
            .jetpackTransforms { CJItems.JETPACK.get() }
            .register()

    val NETHERITE_JETPACK: BlockEntry<JetpackBlock> =
        REGISTRATE
            .block("netherite_jetpack") { JetpackBlock(it) }
            .initialProperties { SharedProperties.netheriteMetal() }
            .jetpackTransforms { CJItems.NETHERITE_JETPACK.get() }
            .register()

    private fun <T : Block, P> BlockBuilder<T, P>.jetpackTransforms(getItem: () -> Item) =
        apply {
            blockstate { c, p ->
                val model =
                    p
                        .models()
                        .withExistingParent("block/${c.name}", p.modLoc("block/jetpack/block"))
                        .texture("0", "block/${c.name}")
                p.horizontalBlock(c.entry, model)
            }
            transform(TagGen.pickaxeOnly())
            onRegister {
                BlockStressValues.IMPACTS.register(it) {
                    BlockStressValues.getImpact(AllBlocks.COPPER_BACKTANK.get())
                }
            }
            addLayer { Supplier { RenderType.cutoutMipped() } }
            loot { lt, block ->
                val builder = LootTable.lootTable()
                val survivesExplosion = ExplosionCondition.survivesExplosion()
                lt.add(
                    block,
                    builder.withPool(
                        LootPool
                            .lootPool()
                            .`when`(survivesExplosion)
                            .setRolls(ConstantValue.exactly(1F))
                            .add(
                                LootItem
                                    .lootTableItem(getItem())
                                    .apply(
                                        CopyComponentsFunction
                                            .copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                            .include(AllDataComponents.BACKTANK_AIR),
                                    ),
                            ),
                    ),
                )
            }
        }

    internal fun register() {
        // Load this class
    }
}
