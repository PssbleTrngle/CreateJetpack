package com.possible_triangle.create_jetpack;

import com.possible_triangle.create_jetpack.client.ControlsDisplay;
import java.util.function.Supplier;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CJConstants.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClientEntrypoint {

    public NeoForgeClientEntrypoint(ModContainer container, IEventBus modBus) {
        modBus.addListener(ControlsDisplay::register);
        Supplier<IConfigScreenFactory> configScreen = () -> ($, previous) -> new BaseConfigScreen(previous, CJConstants.MOD_ID);
        container.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
    }

}
