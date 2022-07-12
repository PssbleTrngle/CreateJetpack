package com.possible_triangle.create_jetpack.data

import com.possible_triangle.create_jetpack.data.providers.Blocks
import com.possible_triangle.create_jetpack.data.providers.Items
import com.possible_triangle.create_jetpack.data.providers.Loot
import com.possible_triangle.create_jetpack.data.providers.Recipes
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
object DataGenerators {

    @SubscribeEvent
    fun run(event: GatherDataEvent) {
        if (event.includeServer()) {
            event.generator.addProvider(Recipes(event.generator))
            event.generator.addProvider(Loot(event.generator))
        }
        if (event.includeClient()) {
            event.generator.addProvider(Items(event.generator, event.existingFileHelper))
            event.generator.addProvider(Blocks(event.generator, event.existingFileHelper))
        }
    }

}