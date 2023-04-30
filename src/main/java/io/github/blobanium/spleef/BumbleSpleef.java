package io.github.blobanium.spleef;

import io.github.blobanium.spleef.ai.Dummy;
import io.github.blobanium.spleef.config.ConfigReader;
import io.github.blobanium.spleef.database.Database;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class BumbleSpleef implements ServerLifecycleEvents.ServerStopping{

    public static Entity pathRequested = null;
    public static void onEnable(){
        //Register Config
        ConfigReader.configRegister(true);

        //Initialize Database if enabled
        if(ConfigReader.database){
            Database.onEnable();
        }

        Dummy.registerAttributes();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            //GiveBee
            dispatcher.register(literal("yourmom").executes(context -> {

                Dummy.createDummyPlayer(context.getSource().getServer());

                return 0;
            }));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            //GiveBee
            dispatcher.register(literal("cometome").executes(context -> {

                pathRequested = context.getSource().getEntity();

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
