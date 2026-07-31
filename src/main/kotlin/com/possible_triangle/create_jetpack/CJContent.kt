package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.index.CJBlockEntities
import com.possible_triangle.create_jetpack.index.CJBlocks
import com.possible_triangle.create_jetpack.index.CJItems
import com.possible_triangle.create_jetpack.index.CJRecipeTypes
import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.item.ItemDescription
import com.simibubi.create.foundation.item.KineticStats
import com.simibubi.create.foundation.item.TooltipModifier
import net.createmod.catnip.lang.FontHelper
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.bus.api.IEventBus

object CJContent {
    val REGISTRATE: CreateRegistrate =
        CreateRegistrate
            .create(CJConstants.MOD_ID)
            .defaultCreativeTab(null as ResourceKey<CreativeModeTab>?)
            .setTooltipModifierFactory {
                ItemDescription
                    .Modifier(it, FontHelper.Palette.STANDARD_CREATE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create(it)))
            }

    @JvmStatic
    fun register(modBus: IEventBus) {
        REGISTRATE.addRawLang("key.categories.movement.jetpack", "Create Jetpack")

        REGISTRATE.registerEventListeners(modBus)

        CJItems.register()
        CJBlocks.register()
        CJBlockEntities.register()
        CJRecipeTypes.register()
    }
}
