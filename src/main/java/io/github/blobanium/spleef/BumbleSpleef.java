package io.github.blobanium.spleef;

import io.github.blobanium.spleef.config.ConfigReader;
import io.github.blobanium.spleef.database.Database;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import static net.minecraft.server.command.CommandManager.literal;

public class BumbleSpleef implements ServerLifecycleEvents.ServerStopping{
    public static void onEnable(){
        //Register Config
        ConfigReader.configRegister(true);

        //Initialize Database if enabled
        if(ConfigReader.database){
            Database.onEnable();
        }


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            //GiveBee
            dispatcher.register(literal("yourmom").executes(context -> {


                return 0;
            }));
        });

        System.out.println("Spleef Extension initialized!");
    }


    @Override
    public void onServerStopping(MinecraftServer server) {
        if(Database.isactive){
            Database.onDisable();
        }
    }
}
