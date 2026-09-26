package ru.whispershadow;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.state.property.Properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Client-only scene events: phantom animal circle and door/glass apparitions.
 * These never modify server-side blocks or entities.
 */
public final class PhantomHorrorEvents {
    private static final Random RANDOM = new Random();
    private static final List<Entity> sceneEntities = new ArrayList<>();
    private static final Set<BlockPos> openDoorBlocks = new HashSet<>();

    private static int circleTicks = 0;
    private static int circleBlackoutTicks = 0;
    private static int apparitionTicks = 0;
    private static ShadowEntity apparition = null;
    private static int glassTicks = 0;
    private static ShadowEntity glassWatcher = null;

    private PhantomHorrorEvents() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clear(client);
            return;
        }

        detectDoorOpening(client);

        if (circleTicks > 0) {
            circleTicks--;
            if (circleTicks == 30) {
                for (Entity entity : sceneEntities) {
                    if (entity instanceof MobEntity mob) {
                        mob.getLookControl().lookAt(client.player, 30.0f, 30.0f);
                    }
                }
            }
            if (circleTicks == 0) {
                circleBlackoutTicks = 8;
                removeSceneEntities();
            }
        }

        if (circleBlackoutTicks > 0) {
            circleBlackoutTicks--;
        }

        if (apparitionTicks > 0 && apparition != null) {
            apparitionTicks--;
            apparition.lookAt(
                    net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES,
                    client.player.getEyePos()
            );
            if (apparitionTicks == 0) {
                apparition.remove(Entity.RemovalReason.DISCARDED);
                apparition = null;
            }
        }

        if (glassTicks > 0 && glassWatcher != null) {
            glassTicks--;
            glassWatcher.lookAt(
                    net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES,
                    client.player.getEyePos()
            );

            if (isLookingAt(client, glassWatcher)) {
                glassWatcher.remove(Entity.RemovalReason.DISCARDED);
                glassWatcher = null;
                glassTicks = 0;
                client.player.sendMessage(
                        Text.literal("...").formatted(Formatting.DARK_GRAY), false
                );
            } else if (glassTicks == 0) {
                glassWatcher.remove(Entity.RemovalReason.DISCARDED);
                glassWatcher = null;
            }
        }
    }

    public static void renderOverlay(DrawContext context) {
        if (circleBlackoutTicks > 0) {
            context.fill(
                    0,
                    0,
                    context.getScaledWindowWidth(),
                    context.getScaledWindowHeight(),
                    0xFF000000
            );
        }
    }

    public static void fire(String id, MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        switch (id) {
            case "phantom_circle" -> startPhantomCircle(client);
            case "door_entity" -> spawnDoorEntity(client, client.player.getBlockPos());
            case "glass_watcher" -> spawnGlassWatcher(client);
        }
    }

    private static void startPhantomCircle(MinecraftClient client) {
        clearSceneOnly();

        int count = 6 + RANDOM.nextInt(4);
        double radius = 7.0 + RANDOM.nextDouble() * 2.0;
        double baseY = client.player.getY();

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i / count) + RANDOM.nextDouble() * 0.25;
            Vec3d pos = new Vec3d(
                    client.player.getX() + Math.cos(angle) * radius,
                    baseY,
                    client.player.getZ() + Math.sin(angle) * radius
            );

            AnimalEntity animal = createRandomAnimal(client.world);
            if (animal == null) continue;

            BlockPos feet = BlockPos.ofFloored(pos);
            if (!isSafeAnimalPosition(client.world, feet)) {
                animal.remove(Entity.RemovalReason.DISCARDED);
                continue;
            }

            animal.refreshPositionAndAngles(
                    pos.x,
                    pos.y,
                    pos.z,
                    (float) Math.toDegrees(angle + Math.PI),
                    0.0f
            );
            animal.setNoGravity(true);
            animal.setVelocity(Vec3d.ZERO);
            animal.setId(-930000 - RANDOM.nextInt(50000));
            client.world.addEntity(animal);
            sceneEntities.add(animal);
        }

        circleTicks = 20 * 4;
        client.player.sendMessage(
                Text.literal("...they are waiting...").formatted(Formatting.DARK_GRAY), false
        );
    }

    private static AnimalEntity createRandomAnimal(ClientWorld world) {
        int roll = RANDOM.nextInt(5);
        return switch (roll) {
            case 0 -> EntityType.COW.create(world);
            case 1 -> EntityType.SHEEP.create(world);
            case 2 -> EntityType.PIG.create(world);
            case 3 -> EntityType.CHICKEN.create(world);
            default -> EntityType.HORSE.create(world);
        };
    }

    private static boolean isSafeAnimalPosition(ClientWorld world, BlockPos feet) {
        BlockPos head = feet.up();
        BlockPos floor = feet.down();
        return world.getBlockState(floor).isSolidBlock(world, floor)
                && world.getBlockState(feet).getCollisionShape(world, feet).isEmpty()
                && world.getBlockState(head).getCollisionShape(world, head).isEmpty();
    }

    private static void detectDoorOpening(MinecraftClient client) {
        Set<BlockPos> currentOpen = new HashSet<>();
        BlockPos center = client.player.getBlockPos();

        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = client.world.getBlockState(pos);
                    if (!isWoodenDoorOrTrapdoor(state)) continue;
                    if (!Boolean.TRUE.equals(state.get(Properties.OPEN))) continue;

                    currentOpen.add(pos.toImmutable());
                    if (!openDoorBlocks.contains(pos)) {
                        openDoorBlocks.add(pos.toImmutable());
                        // Rare enough to feel accidental rather than scripted.
                        if (RANDOM.nextInt(100) < 12) {
                            spawnDoorEntity(client, pos);
                        }
                    }
                }
            }
        }

        openDoorBlocks.retainAll(currentOpen);
    }

    private static boolean isWoodenDoorOrTrapdoor(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.OAK_DOOR || block == Blocks.SPRUCE_DOOR
                || block == Blocks.BIRCH_DOOR || block == Blocks.JUNGLE_DOOR
                || block == Blocks.ACACIA_DOOR || block == Blocks.DARK_OAK_DOOR
                || block == Blocks.MANGROVE_DOOR || block == Blocks.CHERRY_DOOR
                || block == Blocks.BAMBOO_DOOR || block == Blocks.CRIMSON_DOOR
                || block == Blocks.WARPED_DOOR || block == Blocks.PALE_OAK_DOOR
                || block == Blocks.OAK_TRAPDOOR || block == Blocks.SPRUCE_TRAPDOOR
                || block == Blocks.BIRCH_TRAPDOOR || block == Blocks.JUNGLE_TRAPDOOR
                || block == Blocks.ACACIA_TRAPDOOR || block == Blocks.DARK_OAK_TRAPDOOR
                || block == Blocks.MANGROVE_TRAPDOOR || block == Blocks.CHERRY_TRAPDOOR
                || block == Blocks.BAMBOO_TRAPDOOR || block == Blocks.CRIMSON_TRAPDOOR
                || block == Blocks.WARPED_TRAPDOOR || block == Blocks.PALE_OAK_TRAPDOOR;
    }

    private static void spawnDoorEntity(MinecraftClient client, BlockPos doorPos) {
        if (apparition != null || client.world == null || client.player == null) return;

        GameProfile profile = client.player.getGameProfile();
        apparition = new ShadowEntity(client.world, profile, false);
        apparition.refreshPositionAndAngles(
                doorPos.getX() + 0.5,
                doorPos.getY(),
                doorPos.getZ() + 0.5,
                client.player.getYaw() + 180.0f,
                0.0f
        );
        apparition.setNoGravity(true);
        apparition.noClip = true;
        apparition.setDistorted(false);
        apparition.setId(-940000 - RANDOM.nextInt(40000));
        client.world.addEntity(apparition);
        apparitionTicks = 20;
    }

    private static void spawnGlassWatcher(MinecraftClient client) {
        if (glassWatcher != null || client.world == null || client.player == null) return;

        BlockPos center = client.player.getBlockPos();
        BlockPos glass = findNearbyGlass(client);
        if (glass == null) {
            client.player.sendMessage(Text.literal("No glass was found nearby.").formatted(Formatting.GRAY), false);
            return;
        }

        Vec3d playerCenter = client.player.getEntityPos();
        Vec3d glassCenter = Vec3d.ofCenter(glass);
        Vec3d away = glassCenter.subtract(playerCenter).normalize();
        Vec3d spawn = glassCenter.add(away.multiply(1.25));

        GameProfile profile = client.player.getGameProfile();
        glassWatcher = new ShadowEntity(client.world, profile, false);
        glassWatcher.refreshPositionAndAngles(
                spawn.x,
                spawn.y - 1.0,
                spawn.z,
                client.player.getYaw() + 180.0f,
                0.0f
        );
        glassWatcher.setNoGravity(true);
        glassWatcher.noClip = true;
        glassWatcher.setId(-950000 - RANDOM.nextInt(40000));
        client.world.addEntity(glassWatcher);
        glassTicks = 20 * 5;
    }

    private static BlockPos findNearbyGlass(MinecraftClient client) {
        BlockPos center = client.player.getBlockPos();
        for (int radius = 3; radius <= 8; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.add(x, y, z);
                        Block block = client.world.getBlockState(pos).getBlock();
                        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE) return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isLookingAt(MinecraftClient client, Entity target) {
        Vec3d eye = client.player.getEyePos();
        Vec3d toTarget = target.getBoundingBox().getCenter().subtract(eye);
        if (toTarget.lengthSquared() > 18.0 * 18.0) return false;
        Vec3d look = client.player.getRotationVec(1.0f).normalize();
        if (look.dotProduct(toTarget.normalize()) < 0.985) return false;

        var hit = client.world.raycast(new net.minecraft.world.RaycastContext(
                eye,
                target.getBoundingBox().getCenter(),
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                client.player
        ));
        return hit.getType() == HitResult.Type.MISS;
    }

    public static void clear(MinecraftClient client) {
        clearSceneOnly();
        if (apparition != null) {
            apparition.remove(Entity.RemovalReason.DISCARDED);
            apparition = null;
        }
        if (glassWatcher != null) {
            glassWatcher.remove(Entity.RemovalReason.DISCARDED);
            glassWatcher = null;
        }
        circleBlackoutTicks = 0;
        apparitionTicks = 0;
        glassTicks = 0;
        openDoorBlocks.clear();
    }

    private static void clearSceneOnly() {
        for (Entity entity : sceneEntities) {
            if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
        }
        sceneEntities.clear();
        circleTicks = 0;
    }

    public static boolean isBusy() {
        return circleTicks > 0 || circleBlackoutTicks > 0 || apparitionTicks > 0 || glassTicks > 0;
    }
}
