package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import com.possible_triangle.create_jetpack.block.JetpackBlock
import com.possible_triangle.create_jetpack.client.ControlsDisplay
import com.possible_triangle.create_jetpack.config.Configs
import com.possible_triangle.create_jetpack.item.JetpackItem
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.AllTags.AllItemTags
import com.simibubi.create.Create
import com.simibubi.create.api.stress.BlockStressValues
import com.simibubi.create.content.equipment.armor.AllArmorMaterials
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity
import com.simibubi.create.content.equipment.armor.BacktankItem.BacktankBlockItem
import com.simibubi.create.content.equipment.armor.BacktankRenderer
import com.simibubi.create.content.equipment.armor.BacktankUtil
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.foundation.data.TagGen
import com.simibubi.create.foundation.item.ItemDescription
import com.simibubi.create.foundation.item.KineticStats
import com.simibubi.create.foundation.item.TooltipModifier
import com.tterrag.registrate.builders.BlockBuilder
import com.tterrag.registrate.builders.BlockEntityBuilder
import com.tterrag.registrate.builders.ItemBuilder
import com.tterrag.registrate.util.entry.BlockEntry
import com.tterrag.registrate.util.entry.ItemEntry
import com.tterrag.registrate.util.nullness.NonNullFunction
import com.tterrag.registrate.util.nullness.NonNullSupplier
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper
import net.createmod.catnip.lang.FontHelper
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraftforge.fml.config.ModConfig
import java.util.function.Supplier

object Content {

    private val REGISTRATE = CreateRegistrate.create(MOD_ID)
        .setTooltipModifierFactory {
            ItemDescription.Modifier(it, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(it)))
        }

    val COPY_NBT_MECHANICAL_CRAFTING_SERIALIZER = REGISTRATE
        .generic(
            "copy_nbt_mechanical_crafting",
            Registries.RECIPE_SERIALIZER,
            NonNullSupplier { CopyNbtMechanicalCraftingRecipe.Serializer })
        .register()

    val JETPACK_ITEM: ItemEntry<JetpackItem> = REGISTRATE
        .item<JetpackItem>("jetpack") {
            JetpackItem(
                it,
                AllArmorMaterials.COPPER,
                Create.asResource("copper_diving"),
                JETPACK_PLACEABLE
            )
        }
        .properties { it.rarity(Rarity.RARE) }
        .jetpackProperties()
        .register()

    val JETPACK_PLACEABLE: ItemEntry<BacktankBlockItem> = REGISTRATE
        .item<BacktankBlockItem>("jetpack_placeable") {
            BacktankBlockItem(
                JETPACK_BLOCK.get(),
                { JETPACK_ITEM.get() },
                it
            )
        }
        .jetpackPlaceableProperties()
        .register()

    val JETPACK_BLOCK: BlockEntry<JetpackBlock> = REGISTRATE
        .block<JetpackBlock>("jetpack") { JetpackBlock(it) }
        .initialProperties { SharedProperties.copperMetal() }
        .jetpackTransforms { JETPACK_ITEM.get() }
        .register()

    val NETHERITE_JETPACK_ITEM: ItemEntry<JetpackItem> = REGISTRATE
        .item<JetpackItem>("netherite_jetpack") {
            JetpackItem.Layered(
                it,
                ArmorMaterials.NETHERITE,
                Create.asResource("netherite_diving"),
                NETHERITE_JETPACK_PLACEABLE
            )
        }
        .properties { it.rarity(Rarity.EPIC) }
        .properties { it.fireResistant() }
        .jetpackProperties()
        .register()

    val NETHERITE_JETPACK_PLACEABLE: ItemEntry<BacktankBlockItem> = REGISTRATE
        .item<BacktankBlockItem>("netherite_jetpack_placeable") {
            BacktankBlockItem(
                NETHERITE_JETPACK_BLOCK.get(),
                { NETHERITE_JETPACK_ITEM.get() },
                it
            )
        }
        .jetpackPlaceableProperties()
        .register()

    val NETHERITE_JETPACK_BLOCK: BlockEntry<JetpackBlock> = REGISTRATE
        .block<JetpackBlock>("netherite_jetpack") { JetpackBlock(it) }
        .initialProperties { SharedProperties.netheriteMetal() }
        .jetpackTransforms { NETHERITE_JETPACK_ITEM.get() }
        .register()

    fun <T : Block, P> BlockBuilder<T, P>.jetpackTransforms(getItem: () -> Item) = apply {
        blockstate { c, p ->
            val model = p.models().withExistingParent("block/${c.name}", p.modLoc("block/jetpack/block"))
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
                block, builder.withPool(
                    LootPool.lootPool()
                        .`when`(survivesExplosion)
                        .setRolls(ConstantValue.exactly(1F))
                        .add(
                            LootItem.lootTableItem(getItem())
                                .apply(
                                    CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("VanillaTag", "{}", CopyNbtFunction.MergeStrategy.MERGE)
                                )
                                .apply(
                                    CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("Air", "Air")
                                )
                        )
                )
            )
        }
    }

    fun <T : Item, P> ItemBuilder<T, P>.jetpackProperties() = apply {
        model { c, p ->
            p.withExistingParent("item/${c.name}", p.modLoc("block/jetpack/item"))
                .texture("0", "block/${c.name}")
        }
        tag(AllItemTags.PRESSURIZED_AIR_SOURCES.tag)
        transform {
            it.tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!) { mod ->
                mod.accept(ItemStack(it.entry).apply {
                    orCreateTag.putInt("Air", BacktankUtil.maxAirWithoutEnchants())
                })
            }
        }

        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip", "")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.summary", "Allows levitation using pressurized air")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.control1", "Press [JUMP]")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.action1", "Fly upwards")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.control2", "Press [SHIFT]")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.action2", "Fly downwards")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.control3", "Press [G]")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.action3", "Turn engine on/off")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.control4", "Press [H]")
        owner.addRawLang("item.${REGISTRATE.modid}.${name}.tooltip.action4", "Turn hover mode on/off")
    }

    fun <T : Item, P> ItemBuilder<T, P>.jetpackPlaceableProperties() =
        model { context, provider ->
            provider.withExistingParent(context.name, provider.mcLoc("item/barrier"))
        }

    val JETPACK_BLOCK_ENTITY =
        REGISTRATE.blockEntity("jetpack", BlockEntityBuilder.BlockEntityFactory(::BacktankBlockEntity))
            .visual {
                SimpleBlockEntityVisualizer.Factory { ctx, te, f -> SingleAxisRotatingVisual.backtank(ctx, te, f) }
            }
            .validBlocks(JETPACK_BLOCK, NETHERITE_JETPACK_BLOCK)
            .renderer { NonNullFunction { BacktankRenderer(it) } }
            .register()

    fun register() {
        REGISTRATE.register()

        REGISTRATE.addRawLang("key.categories.movement.jetpack", "Create Jetpack")

        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        ServerPlayConnectionEvents.JOIN.register { it, _, _ -> Configs.syncConfig(it.player) }
    }

    fun clientInit() {
        ControlsDisplay.register()

        Configs.Network.registerReceiver()
    }

    fun setupDatagen(generator: FabricDataGenerator) {
        val helper = ExistingFileHelper.withResourcesFromArg()
        REGISTRATE.setupDatagen(generator.createPack(), helper)
    }

}