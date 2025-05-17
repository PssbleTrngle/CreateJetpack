package com.possible_triangle.create_jetpack;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreateJetpackMod.MOD_ID)
public class CreateJetpackMod {

    public static final String MOD_ID = "create_jetpack";
    public static final Logger LOGGER = LogManager.getLogger();

    public CreateJetpackMod(ModContainer container, IEventBus modBus) {
        Content.INSTANCE.register(container, modBus);
    }

}