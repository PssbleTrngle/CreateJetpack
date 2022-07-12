package com.possible_triangle.create_jetpack

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry
import com.possible_triangle.create_jetpack.capability.FakeJetpack
import com.possible_triangle.create_jetpack.capability.IJetpack
import com.possible_triangle.create_jetpack.capability.JetpackStorage
import com.possible_triangle.create_jetpack.client.JetpackArmorLayer
import com.possible_triangle.create_jetpack.client.OverlayRender
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ModNetwork
import com.simibubi.create.content.curiosities.armor.CopperBacktankInstance
import com.simibubi.create.content.curiosities.armor.CopperBacktankRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.entity.LivingRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod.EventBusSubscriber
@Mod(CreateJetpack.MOD_ID)
object CreateJetpack {

    const val MOD_ID: String = "create_jetpack"

    init {
        Content.register()

        MOD_BUS.addListener { _: FMLCommonSetupEvent ->
            CapabilityManager.INSTANCE.register(IJetpack::class.java, JetpackStorage) { FakeJetpack }
            ModNetwork.init()
        }

        MOD_BUS.addListener { _: FMLClientSetupEvent ->
            ControlManager.registerKeybinds()

            InstancedRenderRegistry.getInstance().register(Content.JETPACK_TILE, ::CopperBacktankInstance)
            ClientRegistry.bindTileEntityRenderer(Content.JETPACK_TILE, ::CopperBacktankRenderer)
            RenderTypeLookup.setRenderLayer(Content.JETPACK_BLOCK, RenderType.getCutoutMipped())

            val renderManager = Minecraft.getInstance().renderManager
            JetpackArmorLayer.register(renderManager.playerRenderer)
            renderManager.renderers.forEach {
                val renderer = it.value
                if (renderer is LivingRenderer<*, *>) JetpackArmorLayer.register(renderer)
            }
        }

        FORGE_BUS.addListener(OverlayRender::onRender)

    }

    @SubscribeEvent
    fun attachCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        Content.attachCapabilities(event.`object`, event::addCapability)
    }

}