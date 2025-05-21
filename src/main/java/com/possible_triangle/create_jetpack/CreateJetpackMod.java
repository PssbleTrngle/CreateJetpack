package com.possible_triangle.create_jetpack;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreateJetpackMod.MOD_ID)
public class CreateJetpackMod {

    public static final String MOD_ID = "create_jetpack";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);


    public CreateJetpackMod(ModContainer container, IEventBus modBus, Dist dist) {
        REGISTRATE.registerEventListeners(modBus);
        Content.INSTANCE.register(container, modBus);

        if (dist == Dist.CLIENT) {
            Content.INSTANCE.clientInit(modBus);
        }
    }

}