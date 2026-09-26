package ru.whispershadow;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** WhisperShadow-localized text with an optional language independent of Minecraft's language. */
public final class WSText {
    public enum Language { GAME, ENGLISH, RUSSIAN }

    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> RU = new HashMap<>();
    private static Language language = Language.GAME;
    private static boolean initialized;

    private WSText() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        load("en_us", EN);
        load("ru_ru", RU);
        loadConfig();
    }

    public static Language getLanguage() {
        init();
        return language;
    }

    public static void setLanguage(Language value) {
        init();
        language = value;
        saveConfig();
    }

    public static MutableText translatable(String key, Object... args) {
        init();
        if (language == Language.GAME) return Text.translatable(key, args);
        String value = (language == Language.RUSSIAN ? RU : EN).get(key);
        if (value == null) return Text.translatable(key, args);
        return Text.literal(format(value, args));
    }

    private static String format(String value, Object[] args) {
        if (args == null || args.length == 0) return value;
        String result = value;
        for (Object arg : args) {
            String replacement = String.valueOf(arg);
            int index = result.indexOf("%s");
            if (index < 0) break;
            result = result.substring(0, index) + replacement + result.substring(index + 2);
        }
        return result;
    }

    private static void load(String locale, Map<String, String> target) {
        try (Reader reader = Files.newBufferedReader(
                FabricLoader.getInstance().getModContainer(WhisperShadowClient.MOD_ID).orElseThrow()
                        .findPath("assets/whispershadow/lang/" + locale + ".json").orElseThrow(),
                StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            json.entrySet().forEach(entry -> target.put(entry.getKey(), entry.getValue().getAsString()));
        } catch (Exception ignored) {
            // Minecraft's normal Text.translatable fallback remains available.
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("whispershadow-language.txt");
    }

    private static void loadConfig() {
        try {
            if (!Files.exists(configPath())) return;
            String value = Files.readString(configPath(), StandardCharsets.UTF_8).trim().toLowerCase();
            language = switch (value) {
                case "english", "en", "en_us" -> Language.ENGLISH;
                case "russian", "ru", "ru_ru" -> Language.RUSSIAN;
                default -> Language.GAME;
            };
        } catch (IOException ignored) {
        }
    }

    private static void saveConfig() {
        try {
            Files.createDirectories(configPath().getParent());
            Files.writeString(configPath(), switch (language) {
                case ENGLISH -> "english";
                case RUSSIAN -> "russian";
                case GAME -> "game";
            }, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
