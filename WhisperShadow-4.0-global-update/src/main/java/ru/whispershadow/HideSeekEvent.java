package ru.whispershadow;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Random;
import java.util.UUID;

/**
 * Two client-only hide-and-seek horror events.
 *
 * HIDE:
 * The player is the one hiding. A watcher is placed on roughly the same
 * vertical level. If the watcher has a clear line of sight to the player,
 * the player loses and the existing RUN punishment starts. If the player
 * stays hidden long enough, the watcher disappears and the player wins.
 *
 * SEEK:
 * The player is the seeker. A watcher is placed at a fair position nearby.
 * The player has 30 seconds to see/find it. If the player gets a clear,
 * close look at the watcher, the event ends without punishment. Otherwise
 * the existing RUN punishment starts when the timer expires.
 *
 * All entities are client-only.
 */
public final class HideSeekEvent {

    private static final Random RANDOM = new Random();

    private static final int HIDE_DURATION = 20 * 15;
    private static final int SEEK_DURATION = 20 * 30;

    private static final double MIN_DISTANCE = 7.0;
    private static final double MAX_DISTANCE = 14.0;

    private static boolean active = false;
    private static boolean hideMode = false;
    private static int ticksLeft = 0;
    private static int graceTicks = 0;

    private static ShadowEntity entity = null;

    private HideSeekEvent() {}

    public static void tick(MinecraftClient client) {
        if (!active) return;

        if (client.player == null || client.world == null) {
            stop(client);
            return;
        }

        if (entity == null || !entity.isAlive()) {
            stop(client);
            return;
        }

        // Keep the watcher on the same general vertical level and make it
        // look toward the player without moving it through blocks.
        entity.lookAt(
                net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES,
                client.player.getEyePos()
        );

        if (graceTicks > 0) {
            graceTicks--;
            return;
        }

        if (hideMode) {
            tickHide(client);
        } else {
            tickSeek(client);
        }
    }

    private static void tickHide(MinecraftClient client) {
        // The watcher "sees" the player only when there is a clear ray from
        // its eyes to the player's eyes. A tree, wall, terrain, etc. blocks it.
        if (hasLineOfSight(entity, client.player)) {
            client.player.sendMessage(
                    Text.translatable("whispershadow.event.hide.caught")
                            .formatted(Formatting.DARK_RED, Formatting.BOLD),
                    false
            );

            stop(client);
            HorrorManager.fireRun(client);
            return;
        }

        ticksLeft--;

        if (ticksLeft <= 0) {
            client.player.sendMessage(
                    Text.translatable("whispershadow.event.hide.win")
                            .formatted(Formatting.GREEN, Formatting.BOLD),
                    false
            );
            stop(client);
        }
    }

    private static void tickSeek(MinecraftClient client) {
        // "Found" means the player has a direct view of the watcher and is
        // actually looking toward it. Merely being nearby is not enough.
        if (isLookingAtEntity(client, entity)) {
            client.player.sendMessage(
                    Text.translatable("whispershadow.event.seek.found")
                            .formatted(Formatting.GRAY, Formatting.BOLD),
                    false
            );
            stop(client);
            return;
        }

        ticksLeft--;

        if (ticksLeft <= 0) {
            client.player.sendMessage(
                    Text.translatable("whispershadow.event.seek.failed")
                            .formatted(Formatting.DARK_RED, Formatting.BOLD),
                    false
            );
            stop(client);
            HorrorManager.fireRun(client);
        }
    }

    public static void startHide(MinecraftClient client) {
        start(client, true);
    }

    public static void startSeek(MinecraftClient client) {
        start(client, false);
    }

    private static void start(MinecraftClient client, boolean hiding) {
        if (client.player == null || client.world == null) return;

        stop(client);

        Vec3d spawn = findFairPosition(client, MIN_DISTANCE, MAX_DISTANCE);
        if (spawn == null) {
            client.player.sendMessage(
                    Text.translatable("whispershadow.event.hide.no_position")
                            .formatted(Formatting.GRAY),
                    false
            );
            return;
        }

        GameProfile profile = client.player.getGameProfile();

        entity = new ShadowEntity(
                client.world,
                profile,
                false
        );

        entity.refreshPositionAndAngles(
                spawn.x,
                spawn.y,
                spawn.z,
                client.player.getYaw() + 180.0f,
                0.0f
        );

        entity.setNoGravity(true);
        entity.noClip = false;
        entity.setInvisible(false);
        entity.setDistorted(false);

        // Give the watcher its own client-only entity id.
        entity.setId(-910000 - RANDOM.nextInt(50000));

        client.world.addEntity(entity);

        active = true;
        hideMode = hiding;
        ticksLeft = hiding ? HIDE_DURATION : SEEK_DURATION;

        // Small preparation period prevents an unfair instant loss as the
        // event begins while the entity is being created.
        graceTicks = 20 * 2;

        client.player.sendMessage(
                Text.translatable(hiding ? "whispershadow.event.hide.start" : "whispershadow.event.seek.start")
                        .formatted(
                                hiding
                                        ? Formatting.DARK_RED
                                        : Formatting.GRAY,
                                Formatting.BOLD
                        ),
                false
        );
    }

    /**
     * Searches around the player for a position that:
     * - is on approximately the player's current Y level;
     * - has a solid floor;
     * - has two blocks of clear standing space;
     * - is not inside a collision shape;
     * - is not too close or too far;
     * - is not directly above/below the player.
     */
    private static Vec3d findFairPosition(
            MinecraftClient client,
            double minDistance,
            double maxDistance
    ) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;

        if (world == null || player == null) return null;

        int playerY = player.getBlockPos().getY();

        for (int attempt = 0; attempt < 80; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0;
            double distance =
                    minDistance +
                            RANDOM.nextDouble() *
                                    (maxDistance - minDistance);

            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;

            BlockPos feet = BlockPos.ofFloored(x, playerY, z);
            BlockPos head = feet.up();
            BlockPos floor = feet.down();

            // Keep the event on the same vertical level. This avoids spawning
            // above the player on a mountain or below them in a cave.
            if (Math.abs(feet.getY() - playerY) > 1) continue;

            if (!world.getBlockState(floor).isSolidBlock(world, floor)) {
                continue;
            }

            if (!world.getBlockState(feet).getCollisionShape(world, feet).isEmpty()) {
                continue;
            }

            if (!world.getBlockState(head).getCollisionShape(world, head).isEmpty()) {
                continue;
            }

            Vec3d candidate = new Vec3d(
                    x,
                    feet.getY(),
                    z
            );

            // Do not spawn in the player's immediate collision space.
            if (candidate.distanceTo(player.getEntityPos()) < minDistance - 0.5) {
                continue;
            }

            return candidate;
        }

        return null;
    }

    private static boolean hasLineOfSight(
            Entity watcher,
            ClientPlayerEntity player
    ) {
        if (watcher == null || player == null) return false;

        Vec3d from = watcher.getEyePos();
        Vec3d to = player.getEyePos();

        var result = MinecraftClient.getInstance().world.raycast(
                new RaycastContext(
                        from,
                        to,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        watcher
                )
        );

        return result.getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }

    private static boolean isLookingAtEntity(
            MinecraftClient client,
            Entity target
    ) {
        if (client.player == null || target == null) return false;

        Vec3d eye = client.player.getEyePos();
        Vec3d targetPoint = target.getBoundingBox().getCenter();
        Vec3d direction = targetPoint.subtract(eye);

        double distance = direction.length();
        if (distance < 0.001 || distance > 24.0) return false;

        Vec3d look = client.player.getRotationVec(1.0f).normalize();
        Vec3d toTarget = direction.normalize();

        // Roughly an 8 degree cone.
        if (look.dotProduct(toTarget) < 0.990) return false;

        // It must also actually be visible, not merely aligned through a wall.
        var result = client.world.raycast(
                new RaycastContext(
                        eye,
                        targetPoint,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        client.player
                )
        );

        return result.getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }

    public static void renderOverlay(MinecraftClient client, DrawContext context) {
        if (!active || client.player == null || client.textRenderer == null) return;

        int width = context.getScaledWindowWidth();
        int centerX = width / 2;
        int titleY = 18;
        int timerY = 38;

        Text title = Text.translatable(hideMode ? "whispershadow.hud.hide.title" : "whispershadow.hud.seek.title")
                .formatted(Formatting.BOLD, hideMode ? Formatting.DARK_RED : Formatting.GRAY);

        int seconds = Math.max(0, (ticksLeft + 19) / 20);
        Text timer = Text.literal(String.format("%02d", seconds))
                .formatted(Formatting.BOLD);

        context.drawCenteredTextWithShadow(client.textRenderer, title, centerX, titleY, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, timer, centerX, timerY, 0xFFFFFFFF);

        Text objective = Text.translatable(hideMode
                ? "whispershadow.hud.hide.objective"
                : "whispershadow.hud.seek.objective");
        context.drawCenteredTextWithShadow(client.textRenderer, objective, centerX, timerY + 13, 0xFFAAAAAA);
    }

    public static boolean isActive() {
        return active;
    }

    public static void stop(MinecraftClient client) {
        if (entity != null) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            entity = null;
        }

        active = false;
        hideMode = false;
        ticksLeft = 0;
        graceTicks = 0;
    }
}
