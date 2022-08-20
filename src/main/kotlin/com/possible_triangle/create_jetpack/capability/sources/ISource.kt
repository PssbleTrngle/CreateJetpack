package com.possible_triangle.create_jetpack.capability.sources

import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.common.capabilities.ICapabilityProvider

interface ISource {

    fun interface Provider {
        fun get(entity: LivingEntity): List<Pair<ICapabilityProvider, ISource>>
    }

    companion object {

        private val PROVIDERS = arrayListOf<Provider>()

        fun addSource(provider: Provider) {
            PROVIDERS.add(provider)
        }

        fun get(entity: LivingEntity): List<Pair<ICapabilityProvider, ISource>> {
            return PROVIDERS.flatMap { it.get(entity) }
        }

    }
}