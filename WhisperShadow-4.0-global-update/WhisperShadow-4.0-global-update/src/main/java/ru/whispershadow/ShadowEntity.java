package ru.whispershadow;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.math.Vec3d;

public final class ShadowEntity extends OtherClientPlayerEntity {

    private final boolean delayedMotion;
    private Vec3d lastPlayerPos;

    // Прогресс погони: 0.0 -> начало, 1.0 -> конец.
    private double chaseProgress = 0.0;
    private boolean distorted = false;
    private final double distortionSeed = Math.random() * Math.PI * 2.0;

    public ShadowEntity(
            ClientWorld world,
            GameProfile profile,
            boolean delayedMotion
    ) {
        super(world, profile);

        this.delayedMotion = delayedMotion;

        this.noClip = true;

        this.setNoGravity(true);

        this.lastPlayerPos =
                getEntityPos();
    }

    /*
     * PlayerEntityRenderer uses these flags to decide whether the
     * second skin layer (hat, jacket, sleeves and pants legs) is
     * rendered. OtherClientPlayerEntity does not inherit the local
     * player's customization state, so fake player-like entities
     * otherwise render only the base skin.
     *
     * These horror entities are meant to look like real players, so
     * keep all vanilla outer-skin parts enabled.
     */
    @Override
    public boolean isModelPartVisible(PlayerModelPart part) {
        return true;
    }

    public void setDistorted(boolean distorted) {
        this.distorted = distorted;
    }

    public void horrorTick(
            ClientPlayerEntity player
    ) {

        Vec3d target =
                player.getEyePos();

        if (delayedMotion) {

            Vec3d currentPlayer =
                    player.getEntityPos();

            Vec3d old =
                    lastPlayerPos;

            lastPlayerPos =
                    currentPlayer;

            double dx =
                    old.x - getX();

            double dz =
                    old.z - getZ();

            if (Math.abs(dx) +
                    Math.abs(dz) > 0.02) {

                setPos(
                        getX() + dx * 0.12,
                        getY() +
                                (old.y - getY()) * 0.08,
                        getZ() + dz * 0.12
                );
            }
        }

        lookAt(
                EntityAnchorArgumentType.EntityAnchor.EYES,
                target
        );

        if (distorted) {
            double t = System.currentTimeMillis() * 0.009 + distortionSeed;
            float yawBreak = (float)(Math.sin(t * 1.7) * 11.0 + Math.sin(t * 3.1) * 4.0);
            float headBreak = (float)(Math.cos(t * 2.3) * 17.0);
            float pitchBreak = (float)(Math.sin(t * 1.3) * 8.0 + Math.cos(t * 3.7) * 3.0);
            setYaw(getYaw() + yawBreak);
            setHeadYaw(getHeadYaw() + headBreak);
            setPitch(Math.max(-89.0f, Math.min(89.0f, getPitch() + pitchBreak)));
        }
    }

    public void chasePlayer(
            ClientPlayerEntity player
    ) {

        Vec3d target =
                player.getEntityPos();

        double dx =
                target.x - getX();

        double dz =
                target.z - getZ();

        double distance =
                Math.sqrt(
                        dx * dx +
                                dz * dz
                );

        if (distance > 0.01) {

            /*
             * РАЗГОН ОТ ВРЕМЕНИ ПОГОНЯ
             *
             * В начале:
             * 0.14
             *
             * В конце:
             * примерно 0.30
             */
            double timeSpeed =
                    0.14 +
                            chaseProgress * 0.16;

            /*
             * РАЗГОН ОТ РАССТОЯНИЯ
             *
             * Далеко -> быстрее
             * Близко -> немного медленнее
             */
            double distanceBonus;

            if (distance > 20.0) {

                distanceBonus = 0.10;

            } else if (distance > 12.0) {

                distanceBonus = 0.07;

            } else if (distance > 7.0) {

                distanceBonus = 0.04;

            } else if (distance > 3.0) {

                distanceBonus = 0.01;

            } else {

                distanceBonus = -0.02;
            }

            double speed =
                    timeSpeed +
                            distanceBonus;

            /*
             * Безопасные пределы скорости.
             */
            speed =
                    Math.max(
                            0.10,
                            Math.min(
                                    0.34,
                                    speed
                            )
                    );

            double velocityX =
                    (dx / distance) * speed;

            double velocityY =
                    (target.y - getY()) * 0.08;

            double velocityZ =
                    (dz / distance) * speed;

            setVelocity(
                    velocityX,
                    velocityY,
                    velocityZ
            );

            /*
             * Ghost chase movement: bypass collision resolution entirely.
             * This prevents the figure from getting stuck in walls, doors,
             * corners or other block collision shapes while pursuing the player.
             */
            setPosition(
                    getX() + velocityX,
                    getY() + velocityY,
                    getZ() + velocityZ
            );

            setVelocity(
                    Vec3d.ZERO
            );
        }

        /*
         * Медленно увеличиваем скорость
         * с каждым игровым тиком.
         */
        chaseProgress =
                Math.min(
                        1.0,
                        chaseProgress + 0.001
                );

        lookAt(
                EntityAnchorArgumentType.EntityAnchor.EYES,
                player.getEyePos()
        );

        if (distorted) {
            double t = System.currentTimeMillis() * 0.009 + distortionSeed;
            float yawBreak = (float)(Math.sin(t * 1.7) * 11.0 + Math.sin(t * 3.1) * 4.0);
            float headBreak = (float)(Math.cos(t * 2.3) * 17.0);
            float pitchBreak = (float)(Math.sin(t * 1.3) * 8.0 + Math.cos(t * 3.7) * 3.0);
            setYaw(getYaw() + yawBreak);
            setHeadYaw(getHeadYaw() + headBreak);
            setPitch(Math.max(-89.0f, Math.min(89.0f, getPitch() + pitchBreak)));
        }
    }
}
