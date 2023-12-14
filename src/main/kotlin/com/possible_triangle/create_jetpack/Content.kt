package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import com.possible_triangle.create_jetpack.block.JetpackBlock
import com.possible_triangle.create_jetpack.client.ControlsDisplay
import com.possible_triangle.create_jetpack.config.Configs
import com.possible_triangle.create_jetpack.item.JetpackItem
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.AllTags.AllItemTags
import com.simibubi.create.Create
import com.simibubi.create.content.equipment.armor.*
import com.simibubi.create.content.equipment.armor.BacktankItem.BacktankBlockItem
import com.simibubi.create.content.kinetics.BlockStressDefaults
import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.foundation.data.TagGen
import com.simibubi.create.foundation.item.ItemDescription
import com.simibubi.create.foundation.item.KineticStats
import com.simibubi.create.foundation.item.TooltipHelper
import com.simibubi.create.foundation.item.TooltipModifier
import com.tterrag.registrate.builders.BlockBuilder
import com.tterrag.registrate.builders.BlockEntityBuilder
import com.tterrag.registrate.builders.ItemBuilder
import com.tterrag.registrate.util.entry.BlockEntry
import com.tterrag.registrate.util.entry.ItemEntry
import com.tterrag.registrate.util.nullness.NonNullFunction
import com.tterrag.registrate.util.nullness.NonNullSupplier
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Supplier

object Content {

    private val REGISTRATE = CreateRegistrate.create(MOD_ID)
        .creativeModeTab { AllCreativeModeTabs.BASE_CREATIVE_TAB }
        .setTooltipModifierFactory {
            ItemDescription.Modifier(it, TooltipHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(it)))
        }

    val COPY_NBT_MECHANICAL_CRAFTING_SERIALIZER = REGISTRATE
        .simple(
            "copy_nbt_mechanical_crafting",
            Registry.RECIPE_SERIALIZER_REGISTRY,
            NonNullSupplier { CopyNbtMechanicalCraftingRecipe.Serializer })

    val JETPACK_ITEM: ItemEntry<JetpackItem> = REGISTRATE
        .item<JetpackItem>("jetpack") { JetpackItem(it, AllArmorMaterials.COPPER, Create.asResource("copper_diving"), JETPACK_PLACEABLE) }
        .properties { it.rarity(Rarity.RARE) }
        .jetpackProperties()
        .register()

    val JETPACK_PLACEABLE: ItemEntry<BacktankBlockItem> = REGISTRATE
        .item<BacktankBlockItem>("jetpack_placeable") { BacktankBlockItem(JETPACK_BLOCK.get(), { JETPACK_ITEM.get() }, it) }
        .jetpackPlaceableProperties()
        .register()

    val JETPACK_BLOCK: BlockEntry<JetpackBlock> = REGISTRATE
        .block<JetpackBlock>("jetpack") { JetpackBlock({ JETPACK_ITEM.get() }, it) }
        .initialProperties { SharedProperties.copperMetal() }
        .jetpackTransforms { JETPACK_ITEM.get() }
        .register()

    val NETHERITE_JETPACK_ITEM: ItemEntry<JetpackItem> = REGISTRATE
        .item<JetpackItem>("netherite_jetpack") { JetpackItem.Layered(it, ArmorMaterials.NETHERITE, Create.asResource("netherite_diving"), NETHERITE_JETPACK_PLACEABLE) }
        .properties { it.rarity(Rarity.EPIC) }
        .properties { it.fireResistant() }
        .jetpackProperties()
        .register()

    val NETHERITE_JETPACK_PLACEABLE: ItemEntry<BacktankBlockItem> = REGISTRATE
        .item<BacktankBlockItem>("netherite_jetpack_placeable") { BacktankBlockItem(NETHERITE_JETPACK_BLOCK.get(), { NETHERITE_JETPACK_ITEM.get() }, it) }
        .jetpackPlaceableProperties()
        .register()

    val NETHERITE_JETPACK_BLOCK: BlockEntry<JetpackBlock> = REGISTRATE
        .block<JetpackBlock>("netherite_jetpack") { JetpackBlock({ NETHERITE_JETPACK_ITEM.get() }, it) }
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
        addLayer { Supplier { RenderType.cutoutMipped() } }
        transform(BlockStressDefaults.setImpact(4.0))
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
                                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                                .apply(
                                    CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("Air", "Air")
                                )
                                .apply(
                                    CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("Enchantments", "Enchantments")
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
            .instance { BiFunction { manager, tile -> BacktankInstance(manager, tile) } }
            .validBlocks(JETPACK_BLOCK, NETHERITE_JETPACK_BLOCK)
            .renderer {
                NonNullFunction { context: BlockEntityRendererProvider.Context? ->
                    BacktankRenderer(context)
                }
            }
            .register()

    private fun attachCapabilities(stack: ItemStack, add: BiConsumer<ResourceLocation, ICapabilityProvider>) {
        val item = stack.item
        if (item is JetpackItem) add.accept(ResourceLocation(MOD_ID, "jetpack"), item)
    }

    fun register(modBus: IEventBus) {
        REGISTRATE.registerEventListeners(modBus)

        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        Configs.Network.register()

        modBus.addListener(ControlsDisplay::register)

        FORGE_BUS.addListener(Configs::syncConfig)
        FORGE_BUS.addGenericListener(ItemStack::class.java) { event: AttachCapabilitiesEvent<ItemStack> ->
            attachCapabilities(event.`object`, event::addCapability)
        }
    }

}