package com.possible_triangle.create_jetpack

import com.possible_triangle.create_jetpack.client.JetpackArmorLayer
import com.possible_triangle.create_jetpack.client.OverlayRender
import com.possible_triangle.create_jetpack.network.ControlManager
import com.possible_triangle.create_jetpack.network.ModNetwork
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
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
            // TODO check if neccessary
            //CapabilityManager.INSTANCE.(IJetpack::class.java, JetpackStorage) { FakeJetpack }
            ModNetwork.init()
        }

        MOD_BUS.addListener { _: FMLClientSetupEvent ->
            ControlManager.registerKeybinds()

            //InstancedRenderRegistry().register(Content.JETPACK_TILE, ::CopperBacktankInstance)
            //ClientRegistry.bindTileEntityRenderer(Content.JETPACK_TILE, ::CopperBacktankRenderer)
            //RenderTypeLookup.setRenderLayer(Content.JETPACK_BLOCK, RenderType.getCutoutMipped())
        }

        MOD_BUS.addListener { _: EntityRenderersEvent.AddLayers ->
            val dispatcher = Minecraft.getInstance().entityRenderDispatcher
            JetpackArmorLayer.registerOnAll(dispatcher)
        }

        FORGE_BUS.addListener(OverlayRender::onRender)

    }

    @SubscribeEvent
    fun attachCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        Content.attachCapabilities(event.`object`, event::addCapability)
    }

}