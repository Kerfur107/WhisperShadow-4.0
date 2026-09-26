package ru.whispershadow;

import net.minecraft.client.MinecraftClient;

/** Client-only native window title effects. Never sends anything to the server. */
public final class WindowTitleManager {
    private static final String DEFAULT_TITLE = "Minecraft 1.21.11";
    private static String currentTitle = DEFAULT_TITLE;
    private static int horrorTicks = 0;
    private static int phase = 0;

    private WindowTitleManager() {}

    public static void tick(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;
        if (horrorTicks > 0) {
            horrorTicks--;
            if (horrorTicks % 20 == 0) {
                String[] titles = {
                        "I CAN HEAR YOUR VOICE",
                        "DON'T LOOK BEHIND YOU",
                        "WHERE ARE YOU",
                        "I'M STILL HERE"
                };
                phase = (phase + 1) % titles.length;
                set(client, titles[phase]);
            }
        }
    }

    public static void set(MinecraftClient client, String title) {
        if (client == null || client.getWindow() == null) return;
        currentTitle = title == null || title.isBlank() ? DEFAULT_TITLE : title;
        client.getWindow().setTitle(currentTitle);
    }

    public static void horror(MinecraftClient client, int ticks) {
        if (client == null || client.getWindow() == null) return;
        horrorTicks = Math.max(horrorTicks, ticks);
        set(client, "I CAN HEAR YOUR VOICE");
    }

    public static void reset(MinecraftClient client) {
        horrorTicks = 0;
        phase = 0;
        set(client, DEFAULT_TITLE);
    }

    public static String currentTitle() {
        return currentTitle;
    }
}
