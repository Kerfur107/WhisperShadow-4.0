package ru.whispershadow;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Additional client-side horror events requested for the project. */
public final class ExtraHorrorEvents {
    private static final Random RANDOM = new Random();
    private static int active = 0;
    private static int duration = 0;
    private static int phase = 0;
    private static String eventName = "";

    private static final List<Entity> temporaryEntities = new ArrayList<>();
    private static final List<BlockPos> temporaryBlocks = new ArrayList<>();
    private static final List<BlockState> temporaryStates = new ArrayList<>();

    private static int mobLookTicks = 0;
    private static boolean mobLookActive = false;
    private static boolean blackFrame = false;
    private static int blackFrameTicks = 0;

    private ExtraHorrorEvents() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clear(client);
            return;
        }

        if (active > 0) {
            active--;

            // The dark "passage" is an illusion: as the player gets close,
            // the fake dark opening snaps back into an ordinary stone wall.
            if (eventName.equals("wall") && !temporaryEntities.isEmpty()) {
                Entity e = temporaryEntities.get(0);
                if (e != null && e.squaredDistanceTo(client.player) < 9.0) {
                    if (e instanceof BlockDisplayEntity display) {
                        display.setBlockState(Blocks.STONE.getDefaultState());
                    }
                }
            }

            // The shadow has no owner: it drifts slightly while remaining on the ground.
            if (eventName.equals("nothing_shadow") && !temporaryEntities.isEmpty()) {
                Entity e = temporaryEntities.get(0);
                if (e != null) {
                    Vec3d p = client.player.getEntityPos().add(client.player.getRotationVec(1.0f).multiply(3.5));
                    double drift = Math.sin((System.currentTimeMillis() % 3000L) / 3000.0 * Math.PI * 2.0) * 0.35;
                    e.setPosition(p.x + drift, p.y - 0.02, p.z - drift * 0.25);
                }
            }

            if (active == 0) clear(client);
        }

        if (mobLookTicks > 0) {
            mobLookTicks--;
            lookNearbyMobs(client);
            if (mobLookTicks == 0) mobLookActive = false;
        }

        if (blackFrameTicks > 0) blackFrameTicks--;
    }

    public static void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        if (blackFrameTicks > 0) {
            context.fill(0, 0, width, height, 0xFF000000);
            return;
        }

        if (active <= 0) return;

        int alpha = Math.min(105, 18 + RANDOM.nextInt(38));
        if (eventName.contains("camera") || eventName.contains("hud")) {
            context.fill(0, 0, width, height, (alpha << 24) | 0x050505);
        }

        switch (eventName) {
            case "wall" -> drawWallGlitch(context, width, height);
            case "vhs_shift" -> drawVhsShift(context, width, height);
            case "nothing_shadow" -> drawShadowHint(context, width, height);
            case "camera_micro", "camera_tilt", "camera_look", "camera_stick",
                 "camera_low", "camera_high", "camera_flip", "camera_bar",
                 "camera_white", "camera_red", "camera_ghost", "camera_double",
                 "camera_scan" -> drawCameraEffect(context, width, height);
            case "hud_coords", "hud_time", "hud_hide", "hud_double", "hud_name",
                 "hud_flip", "hud_symbol", "hud_slot", "hud_item", "hud_health",
                 "hud_hearts" -> drawHudEffect(context, width, height);
        }
    }

    private static void drawWallGlitch(DrawContext c, int w, int h) {
        int x = w / 2 - w / 8 + RANDOM.nextInt(Math.max(1, w / 8));
        int top = h / 4;
        int bottom = h * 3 / 4;
        c.fill(x - 10, top, x + 10, bottom, 0x30000000);
        c.fill(x - 7, top + 8, x + 7, bottom - 8, 0x24000000);
    }

    private static void drawShadowHint(DrawContext c, int w, int h) {
        int cx = w / 2 + (RANDOM.nextInt(41) - 20);
        int cy = h * 3 / 4 + (RANDOM.nextInt(21) - 10);
        c.fill(cx - 70, cy - 5, cx + 70, cy + 5, 0x28000000);
        c.fill(cx - 35, cy - 10, cx + 35, cy + 10, 0x24000000);
    }

    private static void drawVhsShift(DrawContext c, int w, int h) {
        int split = 20 + RANDOM.nextInt(Math.max(1, h - 40));
        int band = 4 + RANDOM.nextInt(12);
        c.fill(0, split, w, Math.min(h, split + band), 0x28000000);
        c.fill(0, Math.max(0, split - 1), w / 2, split + 1, 0x45000000);
    }

    private static void drawCameraEffect(DrawContext c, int w, int h) {
        if (eventName.equals("camera_bar")) {
            int y = RANDOM.nextBoolean() ? 0 : h - 8;
            c.fill(0, y, w, y + 8, 0xA8000000);
        } else if (eventName.equals("camera_white")) {
            c.fill(0, 0, w, h, 0xA8FFFFFF);
        } else if (eventName.equals("camera_red")) {
            c.fill(0, 0, w, h, 0x8CAA0000);
        } else if (eventName.equals("camera_double")) {
            c.fill(0, 0, w / 2, h, 0x18000000);
            c.fill(w / 2, 0, w, h, 0x18000000);
        } else if (eventName.equals("camera_scan")) {
            int y = (int)((System.currentTimeMillis() / 4) % Math.max(1, h));
            c.fill(0, y, w, Math.min(h, y + 2), 0xCCFFFFFF);
        } else if (eventName.equals("camera_flip")) {
            c.fill(0, 0, w, 3, 0xAAFFFFFF);
            c.fill(0, h - 3, w, h, 0xAAFFFFFF);
        } else {
            c.fill(0, 0, w, h, 0x16000000);
        }
    }

    private static void drawHudEffect(DrawContext c, int w, int h) {
        var tr = MinecraftClient.getInstance().textRenderer;
        String text = switch (eventName) {
            case "hud_coords" -> "XYZ: 999999 / -999999 / NaN";
            case "hud_time" -> "TIME: 03:66:66";
            case "hud_hide" -> "";
            case "hud_double" -> "▰▰▰▰▰▰▰▰▰▰▰▰";
            case "hud_name" -> "________";
            case "hud_flip" -> "ɘɔɒdS ɿɘɿɘʜʇo";
            case "hud_symbol" -> "§?";
            case "hud_slot" -> "[  0  ]";
            case "hud_item" -> "ITEM: UNKNOWN";
            case "hud_health" -> "❤  ❤  ❤  ❤  ❤";
            default -> "♡  ♡  ♡  ♡  ♡";
        };
        if (!text.isEmpty()) {
            int x = 8;
            int y = eventName.equals("hud_coords") || eventName.equals("hud_time") ? 8 : h - 32;
            c.drawText(tr, Text.literal(text).formatted(Formatting.GRAY), x, y, 0xFFFFFFFF, true);
        }
        if (eventName.equals("hud_slot")) {
            c.fill(w / 2 - 10, h - 24, w / 2 + 10, h - 4, 0x88AA0000);
        }
    }

    public static void fire(String id, MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        clear(client);
        eventName = id;
        phase = 0;
        active = durationFor(id);

        switch (id) {
            case "wall" -> spawnWallPassage(client);
            case "nothing_shadow" -> spawnNothingShadow(client);
            case "ghost_block" -> spawnGhostBlock(client);
            case "fake_texture" -> spawnFakeTexture(client);
            case "vhs_shift", "camera_micro", "camera_tilt", "camera_look", "camera_stick",
                 "camera_low", "camera_high", "camera_flip", "camera_bar", "camera_white",
                 "camera_red", "camera_ghost", "camera_double", "camera_scan",
                 "hud_coords", "hud_time", "hud_hide", "hud_double", "hud_name", "hud_flip",
                 "hud_symbol", "hud_slot", "hud_item", "hud_health", "hud_hearts" -> {}
            case "mob_stare" -> {
                mobLookActive = true;
                mobLookTicks = 20 * (1 + RANDOM.nextInt(3));
            }
            case "blackout" -> blackFrameTicks = 4 + RANDOM.nextInt(7);
            case "distorted_figure" -> spawnDistortedFigure(client);
            default -> {}
        }

        if (id.startsWith("camera_")) {
            if (id.equals("camera_white") || id.equals("camera_red")) active = 2;
        }
    }

    private static int durationFor(String id) {
        if (id.equals("mob_stare")) return 1;
        if (id.equals("blackout") || id.equals("camera_white") || id.equals("camera_red")) return 3;
        if (id.startsWith("hud_")) return 25;
        return 30 + RANDOM.nextInt(45);
    }

    private static void lookNearbyMobs(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        for (Entity entity : client.world.getEntitiesByClass(
                MobEntity.class,
                client.player.getBoundingBox().expand(24.0),
                mob -> mob.isAlive()
        )) {
            MobEntity mob = (MobEntity) entity;
            mob.getLookControl().lookAt(client.player, 30.0f, 30.0f);
        }
    }

    private static void spawnWallPassage(MinecraftClient client) {
        Vec3d pos = client.player.getEyePos().add(client.player.getRotationVec(1.0f).multiply(5.0));
        BlockDisplayEntity display = new BlockDisplayEntity(EntityTypeHolder.block(), client.world);
        display.setPosition(pos.x, pos.y - 1.0, pos.z);
        display.setBlockState(Blocks.BLACK_CONCRETE.getDefaultState());
        client.world.addEntity(display);
        temporaryEntities.add(display);
    }

    private static void spawnNothingShadow(MinecraftClient client) {
        Vec3d pos = client.player.getEntityPos().add(client.player.getRotationVec(1.0f).multiply(3.5));
        BlockDisplayEntity display = new BlockDisplayEntity(EntityTypeHolder.block(), client.world);
        display.setPosition(pos.x, pos.y - 0.02, pos.z);
        display.setBlockState(Blocks.BLACK_CARPET.getDefaultState());
        display.noClip = true;
        client.world.addEntity(display);
        temporaryEntities.add(display);
    }

    private static void spawnGhostBlock(MinecraftClient client) {
        BlockPos pos = client.player.getBlockPos().offset(client.player.getHorizontalFacing(), 3);
        BlockDisplayEntity display = new BlockDisplayEntity(EntityTypeHolder.block(), client.world);
        display.setPosition(pos.getX(), pos.getY(), pos.getZ());
        display.setBlockState(Blocks.STONE.getDefaultState());
        client.world.addEntity(display);
        temporaryEntities.add(display);
    }

    private static void spawnFakeTexture(MinecraftClient client) {
        BlockPos pos = client.player.getBlockPos().offset(client.player.getHorizontalFacing(), 3);
        BlockDisplayEntity display = new BlockDisplayEntity(EntityTypeHolder.block(), client.world);
        display.setPosition(pos.getX(), pos.getY(), pos.getZ());
        display.setBlockState(Blocks.DIAMOND_BLOCK.getDefaultState());
        client.world.addEntity(display);
        temporaryEntities.add(display);
    }

    private static void spawnDistortedFigure(MinecraftClient client) {
        HorrorManager.spawnFigure(client);
        client.player.sendMessage(
                Text.literal("f̷i̷g̷u̷r̷e̷:  r̴u̴n̴  r̷u̷n̷  r̴u̴n̷")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD), false
        );
        client.player.playSound(ModSounds.GLITCH, 0.35f, 0.45f);
    }

    public static void clear(MinecraftClient client) {
        for (Entity entity : temporaryEntities) {
            if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
        }
        temporaryEntities.clear();
        temporaryBlocks.clear();
        temporaryStates.clear();
        active = 0;
        eventName = "";
        mobLookTicks = 0;
        mobLookActive = false;
    }

    private static final class EntityTypeHolder {
        private static net.minecraft.entity.EntityType<DisplayEntity.BlockDisplayEntity> block() {
            return net.minecraft.entity.EntityType.BLOCK_DISPLAY;
        }
    }
}
