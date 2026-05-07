package com.vyrriox.recraftcollect.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Stores entity-id -> points mapping for zombie kills.
 * Supports wildcards "modid:*" applied when no exact match is found.
 * Persisted to config/recraftcollect-zombievalues.json.
 */
public class ZombieValueConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("ReCraftCollect");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static final Map<String, Integer> values = new LinkedHashMap<>();

    public static void init(Path configDir) {
        configPath = configDir.resolve("recraftcollect-zombievalues.json");
        load();
    }

    public static void load() {
        values.clear();
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type mapType = new TypeToken<LinkedHashMap<String, Integer>>() {}.getType();
                Map<String, Integer> loaded = GSON.fromJson(reader, mapType);
                if (loaded != null) values.putAll(loaded);
                LOGGER.info("Loaded {} zombie value entries from config", values.size());
            } catch (Exception e) {
                LOGGER.error("Failed to load zombie values config, using defaults", e);
                initDefaults();
                save();
            }
        } else {
            LOGGER.info("No zombie values config found, creating defaults");
            initDefaults();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(values, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save zombie values config", e);
        }
    }

    /**
     * Returns the point value for the given entity id.
     * Resolution order: exact match -> "modid:*" wildcard -> 0 (not counted).
     */
    public static int getValue(String entityId) {
        if (entityId == null || entityId.isEmpty()) return 0;
        Integer exact = values.get(entityId);
        if (exact != null) return exact;

        int colon = entityId.indexOf(':');
        if (colon > 0) {
            String wildcard = entityId.substring(0, colon) + ":*";
            Integer wild = values.get(wildcard);
            if (wild != null) return wild;
        }
        return 0;
    }

    public static boolean isCounted(String entityId) {
        return getValue(entityId) > 0;
    }

    public static Map<String, Integer> getAll() {
        return Collections.unmodifiableMap(values);
    }

    public static void setValue(String entityId, int points) {
        values.put(entityId, points);
        save();
    }

    public static boolean removeValue(String entityId) {
        boolean removed = values.remove(entityId) != null;
        if (removed) save();
        return removed;
    }

    // ─── Defaults ──────────────────────────────────────────────
    // NOTE: Entity IDs for third-party mods are best-effort guesses
    // based on conventional Forge naming. Adjust in-game with /zk setvalue
    // if any are wrong. Use "modid:*" wildcard to catch all entities of a mod.

    private static void initDefaults() {
        values.clear();

        // ─── NORMAL ZOMBIES (1 point) ──────────────────────────
        // Vanilla
        put("minecraft:zombie", 1);
        put("minecraft:drowned", 1);
        put("minecraft:husk", 1);
        put("minecraft:zombie_villager", 1);

        // Spawn Eggs mod (catch-all for all its zombies)
        put("spawn_eggs:*", 1);

        // Zombie Extreme - basic infected
        put("zombieextreme:infected", 1);
        put("zombieextreme:devastated", 1);
        put("zombieextreme:parasite", 1);

        // Undead Revamp 2 - basic
        put("undead_revamp2:the_bidy", 1);
        put("undead_revamp2:the_immortal", 1);

        // Apocalypse Now (catch-all)
        put("apocalypsenow:*", 1);
        put("apocalypse_now:*", 1);

        // ─── SUPERIOR 1 (15 points) ────────────────────────────
        put("zombieextreme:runner", 15);
        put("zombieextreme:infected_police", 15);
        put("undead_revamp2:the_bomber", 15);
        put("undead_revamp2:the_rod", 15);
        put("undead_revamp2:the_horrors", 15);
        put("undead_revamp2:the_wolf", 15);
        put("undead_revamp2:the_bruin", 15);
        put("undead_revamp2:the_wheezer", 15);
        put("undead_revamp2:sucker", 15);
        put("undead_revamp2:the_skeepper", 15);

        // ─── SUPERIOR 2 (50 points) ────────────────────────────
        put("zombieextreme:infected_hazmat", 50);
        put("zombieextreme:infected_military", 50);
        put("zombieextreme:boomer", 50);
        put("zombieextreme:chainsaw", 50);
        put("zombieextreme:explosive_infected", 50);
        put("zombieextreme:clicker", 50);
        put("zombieextreme:inflated", 50);
        put("undead_revamp2:dead_clogger", 50);
        put("undead_revamp2:the_spitter", 50);
        put("undead_revamp2:the_royal", 50);
        put("undead_revamp2:the_dungeon", 50);
        put("undead_revamp2:the_swarmer", 50);
        put("undead_revamp2:the_moonflower", 50);
        put("undead_revamp2:the_hunter", 50);
        put("undead_revamp2:the_rabidus", 50);

        // ─── SUPERIOR 3 (100 points) ───────────────────────────
        put("zombieextreme:infected_juggernaut", 100);
        put("zombieextreme:ram", 100);
        put("zombieextreme:night_hunter", 100);
        put("zombieextreme:faceless", 100);
        put("zombieextreme:goon", 100);
        put("zombieextreme:revived", 100);
        put("zombieextreme:divided", 100);
        put("undead_revamp2:the_pregnant", 100);
        put("undead_revamp2:the_glitter", 100);
        put("undead_revamp2:the_bigger_suck", 100);
        put("undead_revamp2:the_lechery", 100);
        put("undead_revamp2:the_posessive", 100);

        // ─── BOSS (2000 points) ────────────────────────────────
        put("undead_revamp2:the_heavy", 2000);
        put("zombieextreme:demolisher", 2000);
        put("zombieextreme:rat_king", 2000);
        put("zombieextreme:zero_patient", 2000);
        put("undead_revamp2:the_clogger", 2000);
        put("undead_revamp2:the_lurker", 2000);
    }

    private static void put(String id, int value) {
        values.put(id, value);
    }
}
