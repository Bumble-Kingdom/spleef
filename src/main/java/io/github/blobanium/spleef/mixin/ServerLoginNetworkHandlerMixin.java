package io.github.blobanium.spleef.mixin;

import io.github.blobanium.spleef.database.Database;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "addToServer")
    private void addToServer(ServerPlayerEntity player, CallbackInfo ci){
        if(Database.connection != null) {
            Database.SqlActions.onPlayerJoin(player);
        }
    }
}
