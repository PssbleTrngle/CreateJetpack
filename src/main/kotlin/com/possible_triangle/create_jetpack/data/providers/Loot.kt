package com.possible_triangle.create_jetpack.data.providers

import com.possible_triangle.create_jetpack.Content
import com.possible_triangle.create_jetpack.data.BaseLootTableProvider
import net.minecraft.data.DataGenerator
import net.minecraft.loot.ConstantRange
import net.minecraft.loot.ItemLootEntry
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.conditions.SurvivesExplosion
import net.minecraft.loot.functions.CopyName
import net.minecraft.loot.functions.CopyNbt

class Loot(gen: DataGenerator) : BaseLootTableProvider(gen) {

    override fun addTables() {
        addLootTable(
            Content.JETPACK_BLOCK.lootTable, LootTable.builder()
                .addLootPool(
                    LootPool.builder()
                        .acceptCondition(SurvivesExplosion.builder())
                        .rolls(ConstantRange.of(1))
                        .addEntry(
                            ItemLootEntry.builder(Content.JETPACK)
                                .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                                .acceptFunction(
                                    CopyNbt.func_215881_a(CopyNbt.Source.BLOCK_ENTITY)
                                        .func_216056_a("Air", "Air")
                                )
                        )
                )
        )
    }

}