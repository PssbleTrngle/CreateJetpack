package com.possible_triangle.create_jetpack;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber
@Mod(CreateJetpackMod.MOD_ID)
public class CreateJetpackMod {

    public static final String MOD_ID = "create_jetpack";

    public CreateJetpackMod() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Content.INSTANCE.register(eventBus);
    }

}