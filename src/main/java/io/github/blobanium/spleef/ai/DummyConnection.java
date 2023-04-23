package io.github.blobanium.spleef.ai;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.text.Text;

public class DummyConnection extends ClientConnection {
    public DummyConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public void disableAutoRead() {
        //Leaving this blank as a channel doesn't exist for dummies.
    }
}
