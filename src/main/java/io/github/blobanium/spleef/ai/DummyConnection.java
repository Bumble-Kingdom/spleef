package io.github.blobanium.spleef.ai;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;

public class DummyConnection extends ClientConnection {
    private ServerPlayerEntity dummy;
    public DummyConnection(NetworkSide side, ServerPlayerEntity dummy) {
        super(side);
        this.dummy = dummy;
    }

    @Override
    public void disableAutoRead() {
        //Leaving this blank as a channel doesn't exist for dummies.
    }

    @Override
    public void handleDisconnection(){
        dummy.getServer().getPlayerManager().remove(dummy);
    }

}
