package io.github.blobanium.spleef.ai;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class DummyPathfinding {

    public static Path getPath(ServerPlayerEntity player, Entity target, int distance){
        MobEntity entity = EntityType.ZOMBIE.create(player.getWorld(), null, null, player.getBlockPos(), SpawnReason.EVENT, true , false);
        entity.setSilent(true);
        entity.refreshPositionAndAngles(player.getBlockPos(), player.getYaw(), player.getPitch());
        EntityNavigation nav = entity.getNavigation();
        Path finalpath = nav.findPathTo(target.getX(), target.getY(), target.getZ(), distance);
        if (finalpath == null){
            throw new NullPointerException();
        }
        entity.discard();
        return finalpath;
    }
}
