package com.possible_triangle.create_jetpack.capability

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability

object JetpackStorage : Capability.IStorage<IJetpack> {

    override fun writeNBT(capability: Capability<IJetpack>, instance: IJetpack, side: Direction?): INBT? {
       return CompoundNBT()
    }

    override fun readNBT(capability: Capability<IJetpack>, instance: IJetpack, side: Direction?, nbt: INBT) {
        if(nbt !is CompoundNBT) throw IllegalArgumentException()
    }

}