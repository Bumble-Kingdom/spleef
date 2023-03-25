package io.github.blobanium.spleef;

import io.github.blobanium.spleef.config.ConfigReader;
import io.github.blobanium.spleef.database.Database;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BumbleSpleefEvents {
    public static void onWinEvent(ServerPlayerEntity player, int initPlayers){
        int creditsWon = initPlayers;
        if(player != null) {
            Database.SqlActions.addToUserWithoutCheck(player, creditsWon);
            player.sendMessage(Text.of("Congrats on winning the game! You recieved " + creditsWon + " " + ConfigReader.economy_unit + "!"), false);
        }
    }
}
