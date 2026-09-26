package ru.whispershadow;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public final class SignIllusionEvent {

    private static final Random RANDOM = new Random();

    private static boolean active = false;

    private static int ticks = 0;
    private static int maxTicks = 0;

    private static int entityId = -1;
    private static int textEntityId = -1;

    private static DisplayEntity.BlockDisplayEntity signEntity;
    private static TextDisplayEntity textEntity;

    private static String line1 = "";
    private static String line2 = "";
    private static String line3 = "";

    private static float fade = 0.0f;

    private SignIllusionEvent() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void start(MinecraftClient client) {
        if (client == null ||
                client.player == null ||
                client.world == null) {
            return;
        }

        if (active) {
            return;
        }

        active = true;

        maxTicks = 120 + RANDOM.nextInt(100);
        ticks = maxTicks;

        chooseMessage();

        fade = 0.0f;

        spawnSign(client);

        client.player.playSound(
                ModSounds.STATIC,
                0.10f,
                0.45f + RANDOM.nextFloat() * 0.15f
        );

        InsanityManager.add(
                0.5f + RANDOM.nextFloat() * 1.5f
        );
    }

    public static void startMessage(
            MinecraftClient client,
            String first,
            String second,
            String third
    ) {
        if (client == null ||
                client.player == null ||
                client.world == null) {
            return;
        }

        if (active) {
            return;
        }

        active = true;

        maxTicks = 120;
        ticks = maxTicks;

        line1 = first == null ? "" : first;
        line2 = second == null ? "" : second;
        line3 = third == null ? "" : third;

        fade = 0.0f;

        spawnSign(client);

        client.player.playSound(
                ModSounds.STATIC,
                0.10f,
                0.50f
        );

        InsanityManager.add(1.0f);
    }

    private static void chooseMessage() {

        int level = InsanityManager.getLevelNumber();

        if (level <= 1) {

            switch (RANDOM.nextInt(6)) {

                case 0 -> {
                    line1 = "whispershadow.sign.hello";
                    line2 = "";
                    line3 = "";
                }

                case 1 -> {
                    line1 = "whispershadow.sign.are_you";
                    line2 = "whispershadow.sign.lost";
                    line3 = "";
                }

                case 2 -> {
                    line1 = "whispershadow.sign.keep";
                    line2 = "whispershadow.sign.walking";
                    line3 = "";
                }

                case 3 -> {
                    line1 = "whispershadow.sign.dont";
                    line2 = "whispershadow.sign.worry";
                    line3 = "";
                }

                case 4 -> {
                    line1 = "whispershadow.sign.look";
                    line2 = "whispershadow.sign.around";
                    line3 = "";
                }

                default -> {
                    line1 = "whispershadow.sign.welcome";
                    line2 = "";
                    line3 = "";
                }
            }

            return;
        }

        if (level == 2) {

            switch (RANDOM.nextInt(8)) {

                case 0 -> {
                    line1 = "whispershadow.sign.dont";
                    line2 = "whispershadow.sign.turn_plain";
                    line3 = "whispershadow.sign.around";
                }

                case 1 -> {
                    line1 = "whispershadow.sign.we";
                    line2 = "whispershadow.sign.saw";
                    line3 = "whispershadow.sign.you";
                }

                case 2 -> {
                    line1 = "whispershadow.sign.keep";
                    line2 = "whispershadow.sign.moving_plain";
                    line3 = "";
                }

                case 3 -> {
                    line1 = "whispershadow.sign.you_were";
                    line2 = "whispershadow.sign.here";
                    line3 = "whispershadow.sign.before";
                }

                case 4 -> {
                    line1 = "whispershadow.sign.can_you";
                    line2 = "whispershadow.sign.hear";
                    line3 = "whispershadow.sign.us";
                }

                case 5 -> {
                    line1 = "whispershadow.sign.dont";
                    line2 = "whispershadow.sign.look_plain";
                    line3 = "whispershadow.sign.back_plain";
                }

                case 6 -> {
                    line1 = "whispershadow.sign.we_are";
                    line2 = "whispershadow.sign.closer";
                    line3 = "";
                }

                default -> {
                    line1 = "whispershadow.sign.welcome";
                    line2 = "whispershadow.sign.back";
                    line3 = "";
                }
            }

            return;
        }

        switch (RANDOM.nextInt(10)) {

            case 0 -> {
                line1 = "whispershadow.sign.we_found";
                line2 = "whispershadow.sign.you_plain";
                line3 = "";
            }

            case 1 -> {
                line1 = "whispershadow.sign.dont";
                line2 = "whispershadow.sign.move_plain";
                line3 = "";
            }

            case 2 -> {
                line1 = "whispershadow.sign.we_are";
                line2 = "whispershadow.sign.behind";
                line3 = "whispershadow.sign.you";
            }

            case 3 -> {
                line1 = "whispershadow.sign.this_is";
                line2 = "whispershadow.sign.not";
                line3 = "whispershadow.sign.your_world";
            }

            case 4 -> {
                line1 = "whispershadow.sign.you";
                line2 = "whispershadow.sign.shouldnt";
                line3 = "whispershadow.sign.be_here";
            }

            case 5 -> {
                line1 = "whispershadow.sign.we_know";
                line2 = "whispershadow.sign.you_can_plain";
                line3 = "whispershadow.sign.see_this";
            }

            case 6 -> {
                line1 = "whispershadow.sign.too";
                line2 = "whispershadow.sign.late";
                line3 = "";
            }

            case 7 -> {
                line1 = "whispershadow.sign.turn";
                line2 = "whispershadow.sign.around";
                line3 = "whispershadow.sign.now";
            }

            case 8 -> {
                line1 = "whispershadow.sign.run";
                line2 = "";
                line3 = "";
            }

            default -> {
                line1 = "whispershadow.sign.find";
                line2 = "whispershadow.sign.us_plain";
                line3 = "";
            }
        }
    }

    private static void spawnSign(MinecraftClient client) {

        if (client.world == null || client.player == null) {
            return;
        }

        ClientWorld world = client.world;

        // Put the sign in the world directly in front of the player.
        Vec3d look = client.player.getRotationVec(1.0f);
        Vec3d position = client.player.getEyePos().add(look.multiply(4.5));
        position = position.add(0.0, -1.15, 0.0);

        signEntity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
        signEntity.setPosition(position.x, position.y, position.z);
        signEntity.setBlockState(Blocks.OAK_SIGN.getDefaultState());

        // The front of the sign faces the player.
        float yaw = client.player.getYaw() + 180.0f;
        signEntity.setYaw(yaw);
        signEntity.setBodyYaw(yaw);
        world.addEntity(signEntity);

        // Text is a fixed part of the sign assembly, not a screen/HUD message.
        // Build the face direction from the actual sign -> player vector instead
        // of guessing it from yaw. This keeps the text on the same face even
        // when the player is looking diagonally at the sign.
        textEntity = new TextDisplayEntity(EntityType.TEXT_DISPLAY, world);

        // The sign already has a deterministic facing. Use that exact facing
        // for the text instead of recalculating a direction from the player's
        // current position. The old code also pushed the text ~0.5 blocks away
        // from the sign, which made it visibly drift to the side.
        Vec3d face = new Vec3d(
                -Math.sin(Math.toRadians(yaw)),
                0.0,
                Math.cos(Math.toRadians(yaw))
        );

        // A sign is thin: keep the text just a few centimeters in front of it.
        Vec3d textPosition = position
                .add(0.0, 0.43, 0.0)
                .add(face.multiply(0.065));

        textEntity.setPosition(textPosition.x, textPosition.y, textPosition.z);
        textEntity.setText(buildText().formatted(Formatting.BOLD));
        textEntity.setBillboardMode(DisplayEntity.BillboardMode.FIXED);
        textEntity.setYaw(yaw);
        textEntity.setPitch(0.0f);
        textEntity.setScale(0.38f, 0.38f, 0.38f);
        world.addEntity(textEntity);

        entityId = signEntity.getId();
        textEntityId = textEntity.getId();
    }

    private static Text buildText() {
        Text text = Text.empty();
        boolean hasLine = false;

        if (!line1.isEmpty()) {
            text = text.copy().append(translateLine(line1));
            hasLine = true;
        }

        if (!line2.isEmpty()) {
            if (hasLine) text = text.copy().append("\n");
            text = text.copy().append(translateLine(line2));
            hasLine = true;
        }

        if (!line3.isEmpty()) {
            if (hasLine) text = text.copy().append("\n");
            text = text.copy().append(translateLine(line3));
        }

        return text;
    }

    private static Text translateLine(String value) {
        if (value.startsWith("whispershadow.")) {
            return Text.translatable(value);
        }
        return Text.literal(value);
    }

    public static void tick(MinecraftClient client) {

        if (!active) {
            return;
        }

        if (client == null ||
                client.player == null ||
                client.world == null) {

            stop();
            return;
        }

        ticks--;

        /*
         * Плавное появление.
         */
        if (ticks > maxTicks - 15) {

            fade += 1.0f / 15.0f;

            if (fade > 1.0f) {
                fade = 1.0f;
            }
        }

        /*
         * Плавное исчезновение.
         */
        if (ticks < 15) {

            fade -= 1.0f / 15.0f;

            if (fade < 0.0f) {
                fade = 0.0f;
            }
        }

        /*
         * Проверяем деревянную часть.
         */
        if (signEntity != null &&
                signEntity.isRemoved()) {

            stop();
            return;
        }

        /*
         * Проверяем текст.
         */
        if (textEntity != null &&
                textEntity.isRemoved()) {

            stop();
            return;
        }

        if (ticks <= 0) {
            stop();
        }
    }

    /*
     * Табличка теперь полностью находится
     * в игровом мире.
     *
     * HUD здесь больше ничего не рисует.
     */
    public static void renderOverlay(
            DrawContext context,
            RenderTickCounter tickCounter
    ) {
    }

    public static void stop() {

        if (signEntity != null) {

            signEntity.remove(
                    Entity.RemovalReason.DISCARDED
            );

            signEntity = null;
        }

        if (textEntity != null) {

            textEntity.remove(
                    Entity.RemovalReason.DISCARDED
            );

            textEntity = null;
        }

        active = false;

        ticks = 0;
        maxTicks = 0;

        entityId = -1;
        textEntityId = -1;

        fade = 0.0f;

        line1 = "";
        line2 = "";
        line3 = "";
    }
}
