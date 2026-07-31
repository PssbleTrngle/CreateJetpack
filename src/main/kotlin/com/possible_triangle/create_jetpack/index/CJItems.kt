package com.possible_triangle.create_jetpack.index

import com.possible_triangle.create_jetpack.CJContent.REGISTRATE
import com.possible_triangle.create_jetpack.world.item.JetpackItem
import com.possible_triangle.flightlib.api.IJetpack
import com.possible_triangle.flightlib.neoforge.api.NeoForgeFlightLib
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.AllDataComponents
import com.simibubi.create.AllTags.AllItemTags
import com.simibubi.create.Create
import com.simibubi.create.content.equipment.armor.AllArmorMaterials
import com.simibubi.create.content.equipment.armor.BacktankItem.BacktankBlockItem
import com.simibubi.create.content.equipment.armor.BacktankUtil
import com.tterrag.registrate.builders.ItemBuilder
import com.tterrag.registrate.util.OneTimeEventReceiver
import com.tterrag.registrate.util.entry.ItemEntry
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

object CJItems {
    private val JETPACK_PLACEABLE: ItemEntry<BacktankBlockItem> =
        REGISTRATE
            .item("jetpack_placeable") {
                BacktankBlockItem(
                    CJBlocks.JETPACK.get(),
                    { CJItems.JETPACK.get() },
                    it,
                )
            }.jetpackPlaceableProperties()
            .register()

    private val NETHERITE_JETPACK_PLACEABLE: ItemEntry<BacktankBlockItem> =
        REGISTRATE
            .item("netherite_jetpack_placeable") {
                BacktankBlockItem(
                    CJBlocks.NETHERITE_JETPACK.get(),
                    { CJItems.NETHERITE_JETPACK.get() },
                    it,
                )
            }.jetpackPlaceableProperties()
            .register()

    val JETPACK: ItemEntry<JetpackItem> =
        REGISTRATE
            .item("jetpack") {
                JetpackItem(
                    it,
                    AllArmorMaterials.COPPER,
                    Create.asResource("copper_diving"),
                    JETPACK_PLACEABLE,
                )
            }.properties { it.rarity(Rarity.RARE) }
            .jetpackProperties()
            .register()

    val NETHERITE_JETPACK: ItemEntry<JetpackItem> =
        REGISTRATE
            .item<JetpackItem>("netherite_jetpack") {
                JetpackItem.Layered(
                    it,
                    ArmorMaterials.NETHERITE,
                    Create.asResource("netherite_diving"),
                    NETHERITE_JETPACK_PLACEABLE,
                )
            }.properties { it.rarity(Rarity.EPIC) }
            .properties { it.fireResistant() }
            .jetpackProperties()
            .register()

    private fun <T : Item, P> ItemBuilder<T, P>.jetpackPlaceableProperties() =
        model { context, provider ->
            provider.withExistingParent(context.name, provider.mcLoc("item/barrier"))
        }

    private fun <T : Item, P> ItemBuilder<T, P>.jetpackProperties() =
        apply {
            model { c, p ->
                p
                    .withExistingParent("item/${c.name}", p.modLoc("block/jetpack/item"))
                    .texture("0", "block/${c.name}")
            }
            tag(ItemTags.CHEST_ARMOR)
            tag(AllItemTags.PRESSURIZED_AIR_SOURCES.tag)
                .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!) { context, mod ->
                    mod.accept(
                        ItemStack(context.get()).apply {
                            set(AllDataComponents.BACKTANK_AIR, BacktankUtil.maxAirWithoutEnchants())
                        },
                    )
                }

            owner.addRawLang("item.${owner.modid}.$name.tooltip", "")
            owner.addRawLang(
                "item.${owner.modid}.$name.tooltip.summary",
                "Allows levitation using pressurized air",
            )
            owner.addRawLang("item.${owner.modid}.$name.tooltip.control1", "Press [JUMP]")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.action1", "Fly upwards")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.control2", "Press [SHIFT]")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.action2", "Fly downwards")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.control3", "Press [G]")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.action3", "Turn engine on/off")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.control4", "Press [H]")
            owner.addRawLang("item.${owner.modid}.$name.tooltip.action4", "Turn hover mode on/off")

            onRegister { item ->
                OneTimeEventReceiver.addModListener(owner, RegisterCapabilitiesEvent::class.java) { event ->
                    event.registerItem(NeoForgeFlightLib.ITEM_CAPABILITY, { stack, _ ->
                        stack.item as IJetpack
                    }, item)
                }
            }
        }

    internal fun register() {
        // Load this class
    }
}
