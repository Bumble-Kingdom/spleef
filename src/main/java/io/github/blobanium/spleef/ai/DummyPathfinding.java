package io.github.blobanium.spleef.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class DummyPathfinding {

    public static Path getPath(ServerPlayerEntity player, Entity target, int distance){
        MobEntity entity = new ZombieEntity(player.world);
        entity.refreshPositionAndAngles(player.getBlockPos(), player.getYaw(), player.getPitch());
        EntityNavigation nav = entity.getNavigation();
        Path finalpath =nav.findPathTo(target, distance);
        entity.discard();
        return finalpath;
    }
}
