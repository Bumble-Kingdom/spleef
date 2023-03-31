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
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class Dummy {
        public static final EntityType<DummyPlayerEntity> DUMMY_PLAYER_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                new Identifier("examplemod", "dummy_player"),
                FabricEntityTypeBuilder.<DummyPlayerEntity>create()
                        .trackable(64, 10, true)
                        .build()
        );

        public static void registerAttributes() {
            FabricDefaultAttributeRegistry.register(DUMMY_PLAYER_TYPE, DummyPlayerEntity.createMobAttributes());
        }

        public static ServerPlayerEntity createDummyPlayer(MinecraftServer server) {
            ServerPlayerEntity player = new DummyPlayerEntity(server, server.getOverworld(), new BlockPos(0, 70, 0));
            server.getPlayerManager().onPlayerConnect(new ClientConnection(NetworkSide.SERVERBOUND), player);
            return player;
        }

        public static class DummyPlayerEntity extends ServerPlayerEntity {
            private int sprintTimer = 0;
            public DummyPlayerEntity(MinecraftServer server, ServerWorld world, BlockPos pos) {
                super(server, world, new GameProfile(UUID.randomUUID(), "DummyPlayer"));
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

                // update movement
                Vec3d movement = this.getVelocity();
                this.setVelocity(movement.multiply(1.0D, 0.6D, 1.0D)); // simulate jumping behavior
                this.move(MovementType.SELF, this.getVelocity());

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
                        this.jump();
                    } else {
                        this.moveForward();
                    }
                } else {
                    // air behavior
                    // TODO: add custom air behavior
                }

                // Apply gravity
                double gravity = 0.08;
                this.setVelocity(this.getVelocity().getX(), this.getVelocity().getY() - gravity, this.getVelocity().getZ());

                // Move the entity
                double x = this.getX() + this.getVelocity().getX();
                double y = this.getY() + this.getVelocity().getY();
                double z = this.getZ() + this.getVelocity().getZ();
                this.setPosition(x, y, z);

                // Limit the maximum velocity
                double maxVelocity = 0.5;
                double currentVelocity = this.getVelocity().length();
                if (currentVelocity > maxVelocity) {
                    double factor = maxVelocity / currentVelocity;
                    this.setVelocity(this.getVelocity().getX() * factor, this.getVelocity().getY() * factor, this.getVelocity().getZ() * factor);
                }

                // get the entity's bounding box
                Box entityBox = this.getBoundingBox();

                // get the world object
                World world = this.getEntityWorld();

                // get a list of all the blocks that intersect with the entity's bounding box
                Iterable<VoxelShape> collisionShapes = world.getBlockCollisions(this, entityBox);

                // check if any of the collision shapes are non-empty
                for (VoxelShape shape : collisionShapes) {
                    if (!shape.isEmpty()) {
                        // there is a collision!

                        // calculate the collision vector (the direction the entity needs to move to escape the collision)
                        Vec3d position = this.getPos().negate();
                        Optional<Vec3d> collisionVector = shape.offset(position.x, position.y, position.z).getClosestPointTo(Vec3d.ZERO);

                        // adjust the entity's position to move it out of the collision
                        double dx = Math.abs(collisionVector.get().x);
                        double dy = Math.abs(collisionVector.get().y);
                        double dz = Math.abs(collisionVector.get().z);
                        if (dx < dy && dx < dz) {
                            this.setVelocity(this.getVelocity().multiply(-1, 1, 1));
                        } else if (dy < dz) {
                            this.setVelocity(this.getVelocity().multiply(1, -1, 1));
                        } else {
                            this.setVelocity(this.getVelocity().multiply(1, 1, -1));
                        }
                    }
                }

                // update the entity's position based on its velocity
                this.move(MovementType.SELF, this.getVelocity());

                // Apply gravity to the entity
                this.move(MovementType.SELF, this.getVelocity().add(0.0, -0.08, 0.0));

            }

            private void moveForward() {
                if (this.random.nextFloat() < 0.5F && this.sprintTimer <= 0) {
                    this.setSprinting(true);
                } else {
                    this.setSprinting(false);
                }

                if (this.random.nextFloat() < 0.5F) {
                    this.setMovementSpeed(0.25F);
                } else {
                    this.setMovementSpeed(0.5F);
                }

                this.move(MovementType.SELF, this.getRotationVector().multiply(this.getMovementSpeed(), 0.0D, this.getMovementSpeed()));
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
