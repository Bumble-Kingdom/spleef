package io.github.blobanium.spleef.ai;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class Dummy {
        public static final EntityType<DummyPlayerEntity> DUMMY_PLAYER_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                new Identifier("examplemod", "dummy_player"),
                FabricEntityTypeBuilder.Living.<DummyPlayerEntity>create()
                        .trackable(64, 10, true)
                        .build()
        );

        public static void registerAttributes() {
            FabricDefaultAttributeRegistry.register(DUMMY_PLAYER_TYPE, DummyPlayerEntity.createMobAttributes());
        }

        public static ServerPlayerEntity createDummyPlayer(MinecraftServer server) {
            ServerPlayerEntity player = new DummyPlayerEntity(server, server.getOverworld(), new BlockPos(0, 70, 0));
            return player;
        }

        public static class DummyPlayerEntity extends ServerPlayerEntity {
            private int sprintTimer = 0;
            private DummyConnection cconnection;

            public DummyPlayerEntity(MinecraftServer server, ServerWorld world, BlockPos pos) {
                super(server, world, new GameProfile(UUID.randomUUID(), "DummyPlayer"));
                this.cconnection = new DummyConnection(NetworkSide.SERVERBOUND);
                server.getPlayerManager().onPlayerConnect(cconnection,this);
                this.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            }

            public static DefaultAttributeContainer.Builder createMobAttributes() {
                return PlayerEntity.createPlayerAttributes()
                        .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
            }


            @Override
            public void tick() {
                super.tick();

                // update sprinting state
                this.setSprinting(this.shouldSprint());

                // update look direction
                if (this.world.getTime() % 10 == 0) {
                    if(this.getClosestVisiblePlayer() != null) {
                        this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.getClosestVisiblePlayer().getEyePos());
                    }
                }

                // update sprint timer
                if (this.isSprinting()) {
                    this.sprintTimer = Math.min(this.sprintTimer + 1, 8);
                } else {
                    this.sprintTimer = Math.max(this.sprintTimer - 1, 0);
                }

                // perform actions based on current state
                if (this.isSwimming()) {
                    // swim behavior
                    // TODO: add custom swim behavior
                } else if (this.isOnGround()) {
                    // ground behavior
                    if (this.shouldJump()) {
                        //this.jump();
                    } else {
                        //this.moveForward();
                    }
                } else {
                    // air behavior
                    // TODO: add custom air behavior
                }

                if(this.getClosestVisiblePlayer() != null && this.distanceTo(getClosestVisiblePlayer())<32 && this.distanceTo(getClosestVisiblePlayer())>2) {
                    this.travel(new Vec3d(0, 0, 1));
                } else{
                    this.travel(new Vec3d(0, 0, 0));
                }
            }

            public void jump() {
                this.setVelocity(this.getVelocity().add(0.0D, 0.42D, 0.0D));
            }

            private boolean shouldJump() {
                return this.random.nextFloat() < 0.1F;
            }

            private boolean shouldSprint() {
                return this.random.nextFloat() < 0.5F;
            }

            private PlayerEntity getClosestVisiblePlayer() {
                double distance = 32.0D;
                PlayerEntity closestPlayer = null;

                for (PlayerEntity player : this.world.getPlayers()) {
                    if (player == this || player.isSpectator()) {
                        continue;
                    }

                    double d = this.squaredDistanceTo(player);
                    if (d <= distance && this.canSee(player)) {
                        distance = d;
                        closestPlayer = player;
                    }
                }

                return closestPlayer;
            }

            public boolean canSee(Entity entity) {
                Vec3d vec3d = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
                Vec3d vec3d1 = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
                Box searchBox = this.getBoundingBox().stretch(entity.getBoundingBox().getCenter().subtract(this.getBoundingBox().getCenter())).expand(1.0D); // create a box that extends from the `this` to the `entity`
                EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(this.world, this, vec3d, vec3d1, searchBox, entity1 -> entity1 == entity); // perform the raycast, checking for collisions with only the `entity`
                return entityHitResult == null || entityHitResult.getEntity() == entity; // check if the target entity was hit by the raycast or not
            }

        }
}
