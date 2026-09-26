package ru.whispershadow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public final class WhisperShadowClient implements ClientModInitializer {

    public static final String MOD_ID = "whispershadow";

    @Override
    public void onInitializeClient() {

        ModSounds.init();

        HorrorManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {

                    HorrorManager.tick(client);
                    ExtraHorrorEvents.tick(client);
                    WindowTitleManager.tick(client);

                    WatcherEvent.tick(client);
                }
        );

        ClientSendMessageEvents.CHAT.register(
                HorrorManager::handlePlayerChat
        );

        HudRenderCallback.EVENT.register(
                (context, tickCounter) -> {
                    HorrorManager.renderOverlay(context, tickCounter);
                    ExtraHorrorEvents.renderOverlay(context, tickCounter);
                }
        );

        // ========================================
        // TEST COMMANDS
        // ========================================
        //
        // Every director/event type has a quick client-side test command:
        //
        // /ws message
        // /ws glitch
        // /ws whisper
        // /ws figure
        // /ws decoy
        // /ws vhs
        // /ws tab
        // /ws eyes
        // /ws footsteps
        // /ws torches
        // /ws doppel
        // /ws peripheral
        // /ws distort
        // /ws run
        // /ws broken
        // /ws dontmove
        // /ws findus
        // /ws sign
        // /ws silence
        // /ws watcher
        // Plus the additional wall/VHS/camera/HUD/mob-test commands documented in README.
        //
        // These are local client commands and do not need to be sent
        // to a server as chat commands.

        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> {

                    dispatcher.register(
                            ClientCommandManager.literal("ws")

                                    // LOCAL_MESSAGE
                                    .then(
                                            ClientCommandManager.literal("message")
                                                    .executes(context -> {

                                                        HorrorManager.fireDirectorMessage(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // GLITCH
                                    .then(
                                            ClientCommandManager.literal("glitch")
                                                    .executes(context -> {

                                                        HorrorManager.fireDirectorGlitch();

                                                        return 1;
                                                    })
                                    )

                                    // WHISPER
                                    .then(
                                            ClientCommandManager.literal("whisper")
                                                    .executes(context -> {

                                                        HorrorManager.fireDirectorWhisper(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // FIGURE
                                    .then(
                                            ClientCommandManager.literal("figure")
                                                    .executes(context -> {

                                                        HorrorManager.spawnFigure(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // DECOY
                                    .then(
                                            ClientCommandManager.literal("decoy")
                                                    .executes(context -> {

                                                        HorrorManager.fireDecoy(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // VHS
                                    .then(
                                            ClientCommandManager.literal("vhs")
                                                    .executes(context -> {

                                                        HorrorManager.fireDirectorVhs();

                                                        return 1;
                                                    })
                                    )

                                    // TAB MESSAGE
                                    .then(
                                            ClientCommandManager.literal("tab")
                                                    .executes(context -> {

                                                        HorrorManager.fireTabIllusion(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // EYES
                                    .then(
                                            ClientCommandManager.literal("eyes")
                                                    .executes(context -> {

                                                        HorrorManager.fireEyes();

                                                        return 1;
                                                    })
                                    )

                                    // FOOTSTEPS
                                    .then(
                                            ClientCommandManager.literal("footsteps")
                                                    .executes(context -> {

                                                        HorrorManager.fireFootsteps(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // TORCHES
                                    .then(
                                            ClientCommandManager.literal("torches")
                                                    .executes(context -> {

                                                        HorrorManager.fireTorchIllusion(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // DOPPELGANGER
                                    .then(
                                            ClientCommandManager.literal("doppel")
                                                    .executes(context -> {

                                                        HorrorManager.fireDoppelganger(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // PERIPHERAL
                                    .then(
                                            ClientCommandManager.literal("peripheral")
                                                    .executes(context -> {

                                                        HorrorManager.firePeripheral();

                                                        return 1;
                                                    })
                                    )

                                    // WORLD DISTORTION
                                    .then(
                                            ClientCommandManager.literal("distort")
                                                    .executes(context -> {

                                                        HorrorManager.fireWorldDistortion();

                                                        return 1;
                                                    })
                                    )

                                    // RUN / CHASE
                                    .then(
                                            ClientCommandManager.literal("run")
                                                    .executes(context -> {

                                                        HorrorManager.fireRun(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // BROKEN SCRIPT
                                    .then(
                                            ClientCommandManager.literal("broken")
                                                    .executes(context -> {

                                                        HorrorManager.fireBrokenScript(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // DON'T MOVE
                                    .then(
                                            ClientCommandManager.literal("dontmove")
                                                    .executes(context -> {

                                                        HorrorManager.fireDontMove(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // FIND US
                                    .then(
                                            ClientCommandManager.literal("findus")
                                                    .executes(context -> {

                                                        FindUsEvent.fire(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // SIGN
                                    .then(
                                            ClientCommandManager.literal("sign")
                                                    .executes(context -> {

                                                        SignIllusionEvent.start(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // SILENCE
                                    //
                                    // EventDirector's SILENCE is intentionally
                                    // an empty transition. This command exists
                                    // so every EventType still has a test entry.
                                    .then(
                                            ClientCommandManager.literal("silence")
                                                    .executes(context -> 1)
                                    )

                                    // WATCHER
                                    .then(
                                            ClientCommandManager.literal("watcher")
                                                    .executes(context -> {

                                                        WatcherEvent.fire(
                                                                context.getSource().getClient()
                                                        );

                                                        return 1;
                                                    })
                                    )
                                    .then(
                                            ClientCommandManager.literal("distortedfigure")
                                                    .executes(context -> { ExtraHorrorEvents.fire("distorted_figure", context.getSource().getClient()); return 1; })
                                    )
                                    .then(ClientCommandManager.literal("wall").executes(context -> { ExtraHorrorEvents.fire("wall", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("vhs_shift").executes(context -> { ExtraHorrorEvents.fire("vhs_shift", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("blackout").executes(context -> { ExtraHorrorEvents.fire("blackout", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("nothing_shadow").executes(context -> { ExtraHorrorEvents.fire("nothing_shadow", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("ghost_block").executes(context -> { ExtraHorrorEvents.fire("ghost_block", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("fake_texture").executes(context -> { ExtraHorrorEvents.fire("fake_texture", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("mob_stare").executes(context -> { ExtraHorrorEvents.fire("mob_stare", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_micro").executes(context -> { ExtraHorrorEvents.fire("camera_micro", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_tilt").executes(context -> { ExtraHorrorEvents.fire("camera_tilt", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_look").executes(context -> { ExtraHorrorEvents.fire("camera_look", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_stick").executes(context -> { ExtraHorrorEvents.fire("camera_stick", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_zoom").executes(context -> { ExtraHorrorEvents.fire("camera_micro", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_far").executes(context -> { ExtraHorrorEvents.fire("camera_high", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_low").executes(context -> { ExtraHorrorEvents.fire("camera_low", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_high").executes(context -> { ExtraHorrorEvents.fire("camera_high", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_flip").executes(context -> { ExtraHorrorEvents.fire("camera_flip", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_bar").executes(context -> { ExtraHorrorEvents.fire("camera_bar", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_white").executes(context -> { ExtraHorrorEvents.fire("camera_white", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_red").executes(context -> { ExtraHorrorEvents.fire("camera_red", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_ghost").executes(context -> { ExtraHorrorEvents.fire("camera_ghost", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_double").executes(context -> { ExtraHorrorEvents.fire("camera_double", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("cam_scan").executes(context -> { ExtraHorrorEvents.fire("camera_scan", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_coords").executes(context -> { ExtraHorrorEvents.fire("hud_coords", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_time").executes(context -> { ExtraHorrorEvents.fire("hud_time", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_hide").executes(context -> { ExtraHorrorEvents.fire("hud_hide", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_double").executes(context -> { ExtraHorrorEvents.fire("hud_double", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_name").executes(context -> { ExtraHorrorEvents.fire("hud_name", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_flip").executes(context -> { ExtraHorrorEvents.fire("hud_flip", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_symbol").executes(context -> { ExtraHorrorEvents.fire("hud_symbol", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_slot").executes(context -> { ExtraHorrorEvents.fire("hud_slot", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_item").executes(context -> { ExtraHorrorEvents.fire("hud_item", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_health").executes(context -> { ExtraHorrorEvents.fire("hud_health", context.getSource().getClient()); return 1; }))
                                    .then(ClientCommandManager.literal("hud_hearts").executes(context -> { ExtraHorrorEvents.fire("hud_hearts", context.getSource().getClient()); return 1; }))
                                    .then(
                                            ClientCommandManager.literal("title")
                                                    .executes(context -> {
                                                        WindowTitleManager.set(context.getSource().getClient(), "I CAN HEAR YOUR VOICE");
                                                        return 1;
                                                    })
                                                    .then(argument("text", StringArgumentType.greedyString())
                                                            .executes(context -> {
                                                                String title = StringArgumentType.getString(context, "text");
                                                                WindowTitleManager.set(context.getSource().getClient(), title);
                                                                return 1;
                                                            })
                                                    )
                                    )
                                    .then(
                                            ClientCommandManager.literal("title_reset")
                                                    .executes(context -> {
                                                        WindowTitleManager.reset(context.getSource().getClient());
                                                        return 1;
                                                    })
                                    )
                    );
                }
        );
    }
}
