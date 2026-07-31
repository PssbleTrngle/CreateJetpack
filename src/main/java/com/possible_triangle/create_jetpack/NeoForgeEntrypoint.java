package com.possible_triangle.create_jetpack;

import com.possible_triangle.create_jetpack.config.Configs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CJConstants.MOD_ID)
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint(ModContainer container, IEventBus modBus) {
        Configs.register(container, modBus);
        CJContent.register(modBus);
    }

}
