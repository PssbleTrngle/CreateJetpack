package com.possible_triangle.create_jetpack.data

import com.google.gson.GsonBuilder
import net.minecraft.data.DataGenerator
import net.minecraft.data.DirectoryCache
import net.minecraft.data.IDataProvider
import net.minecraft.data.LootTableProvider
import net.minecraft.loot.LootTable
import net.minecraft.loot.LootTableManager
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.io.IOException

abstract class BaseLootTableProvider(private val generator: DataGenerator) : LootTableProvider(generator) {

    private val lootTables: MutableMap<ResourceLocation, LootTable.Builder> = HashMap()
    protected fun addLootTable(name: ResourceLocation, table: LootTable.Builder) {
        lootTables[name] = table
    }

    protected abstract fun addTables()
    override fun act(cache: DirectoryCache) {
        addTables()
        val tables: MutableMap<ResourceLocation, LootTable> = HashMap()
        for ((key, value) in lootTables) {
            tables[key] = value.build()
        }
        writeTables(cache, tables)
    }

    private fun writeTables(cache: DirectoryCache, tables: Map<ResourceLocation, LootTable>) {
        val outputFolder = generator.outputFolder
        tables.forEach { (key: ResourceLocation, lootTable: LootTable?) ->
            val path = outputFolder.resolve("data/" + key.namespace + "/loot_tables/" + key.path + ".json")
            try {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(lootTable), path)
            } catch (e: IOException) {
                LOGGER.error("Couldn't write loot table {}", path, e)
            }
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    }
}