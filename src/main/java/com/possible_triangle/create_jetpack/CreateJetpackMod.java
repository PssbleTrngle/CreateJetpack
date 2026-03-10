package com.possible_triangle.create_jetpack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateJetpackMod implements ModInitializer, ClientModInitializer, DataGeneratorEntrypoint {

    public static final String MOD_ID = "create_jetpack";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        Content.INSTANCE.register();
    }

    @Override
    public void onInitializeClient() {
        Content.INSTANCE.clientInit();
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        Content.INSTANCE.setupDatagen(generator);
    }

}