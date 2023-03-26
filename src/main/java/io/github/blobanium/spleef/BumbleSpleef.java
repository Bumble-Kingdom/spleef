package io.github.blobanium.spleef;

import io.github.blobanium.spleef.config.ConfigReader;
import io.github.blobanium.spleef.database.Database;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class BumbleSpleef implements ServerLifecycleEvents.ServerStopping{
    public static void onEnable(){
        //Register Config
        ConfigReader.configRegister(true);

        //Initialize Database if enabled
        if(ConfigReader.database){
            Database.onEnable();
        }

        System.out.println("Spleef Extension initialized!");
    }


    @Override
    public void onServerStopping(MinecraftServer server) {
        if(Database.isactive){
            Database.onDisable();
        }
    }
}
