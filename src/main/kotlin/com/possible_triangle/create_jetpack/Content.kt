package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.CreateJetpackMod.MOD_ID
import com.possible_triangle.create_jetpack.block.JetpackBlock
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.JetpackLogic
import com.possible_triangle.create_jetpack.client.ControlsDisplay
import com.possible_triangle.create_jetpack.client.JetpackArmorLayer
import com.possible_triangle.create_jetpack.compat.CuriosCompat
import com.possible_triangle.create_jetpack.config.Configs
import com.possible_triangle.create_jetpack.item.BronzeJetpack
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ModNetwork
import com.simibubi.create.AllTags.pickaxeOnly
import com.simibubi.create.Create
import com.simibubi.create.content.CreateItemGroup
import com.simibubi.create.content.curiosities.armor.CopperBacktankInstance
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem.CopperBacktankBlockItem
import com.simibubi.create.content.curiosities.armor.CopperBacktankRenderer
import com.simibubi.create.content.curiosities.armor.CopperBacktankTileEntity
import com.simibubi.create.foundation.block.BlockStressDefaults
import com.simibubi.create.foundation.data.AssetLookup
import com.simibubi.create.foundation.data.SharedProperties
import com.tterrag.registrate.Registrate
import com.tterrag.registrate.util.entry.ItemEntry
import com.tterrag.registrate.util.nullness.NonNullFunction
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityToken
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Supplier

object Content {

    private val REGISTRATE = Registrate.create(MOD_ID).creativeModeTab { CreateItemGroup.TAB_TOOLS }

    val PRESSURIZED_AIR_SOURCES = TagKey.create(Registry.ITEM_REGISTRY, Create.asResource("pressurized_air_sources"))

    val JETPACK_BLOCK = REGISTRATE.block<JetpackBlock>("jetpack", ::JetpackBlock)
        .initialProperties { SharedProperties.copperMetal() }
        .blockstate { c, p ->
            p.horizontalBlock(
                c.entry,
                AssetLookup.partialBaseModel(c, p)
            )
        }
        .transform(pickaxeOnly())
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .transform(BlockStressDefaults.setImpact(4.0))
        .loot { lt, block ->
            val builder = LootTable.lootTable()
            val survivesExplosion = ExplosionCondition.survivesExplosion()
            lt.add(
                block, builder.withPool(
                    LootPool.lootPool()
                        .`when`(survivesExplosion)
                        .setRolls(ConstantValue.exactly(1F))
                        .add(
                            LootItem.lootTableItem(JETPACK.get())
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
        .register()

    val JETPACK_TILE = Create.registrate()
        .tileEntity(
            "jetpack",
            ::CopperBacktankTileEntity
        )
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

    val JETPACK: ItemEntry<BronzeJetpack> =
        REGISTRATE.item<BronzeJetpack>("jetpack") { BronzeJetpack(it, JETPACK_PLACEABLE) }
            .model(AssetLookup.customGenericItemModel("_", "item"))
            .tag(PRESSURIZED_AIR_SOURCES)
            .register()

    val JETPACK_CAPABILITY = CapabilityManager.get(object : CapabilityToken<IJetpack>() {})

    val SOUNDS = DeferredRegister.create(Registry.SOUND_EVENT_REGISTRY, MOD_ID)

    val SOUND_WHOOSH = SOUNDS.register("whoosh") { SoundEvent(ResourceLocation(MOD_ID, "whoosh")) }

    private fun attachCapabilities(stack: ItemStack, add: BiConsumer<ResourceLocation, ICapabilityProvider>) {
        val item = stack.item
        if (item is BronzeJetpack) add.accept(ResourceLocation(MOD_ID, "jetpack"), item)
    }

    fun register(modBus: IEventBus) {
        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        SOUNDS.register(modBus)

        Configs.Network.register()

        modBus.addListener { _: FMLCommonSetupEvent ->
            ModNetwork.init()
        }

        modBus.addListener { _: FMLClientSetupEvent ->
            ControlManager.registerKeybinds()
            ControlsDisplay.register()
        }

        modBus.addListener { _: EntityRenderersEvent.AddLayers ->
            val dispatcher = Minecraft.getInstance().entityRenderDispatcher
            JetpackArmorLayer.registerOnAll(dispatcher)
        }

        FORGE_BUS.addListener(ControlManager::onDimensionChange)
        FORGE_BUS.addListener(ControlManager::onLogout)

        FORGE_BUS.addListener(JetpackLogic::tick)
        FORGE_BUS.addListener(Configs::syncConfig)
        FORGE_BUS.addGenericListener(ItemStack::class.java) { event: AttachCapabilitiesEvent<ItemStack> ->
            attachCapabilities(event.`object`, event::addCapability)
        }

        FORGE_BUS.addListener(ControlManager::onTick)
        FORGE_BUS.addListener(ControlManager::onKey)

        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.register()
        }
    }

}