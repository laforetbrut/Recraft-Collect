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

    public static void initDefaults() {
        values.clear();

        // ─── NORMAL ZOMBIES (1 point) ──────────────────────────
        // Vanilla
        put("minecraft:zombie", 1);
        put("minecraft:drowned", 1);
        put("minecraft:husk", 1);
        put("minecraft:zombie_villager", 1);

        // Spawn Eggs mod (catch-all)
        put("spawn_eggs:*", 1);

        // Zombie Extreme - basic infected
        put("zombie_extreme:infected", 1);
        put("zombie_extreme:devestated", 1);
        put("zombie_extreme:parasite", 1);

        // Undead Revamp 2 - basic
        put("undead_revamp2:thbidy", 1);
        put("undead_revamp2:theimmortal", 1);

        // Apocalypse Now (catch-all for all entities of the mod)
        put("apocalypse_now:*", 1);

        // ─── SUPERIOR 1 (15 points) ────────────────────────────
        put("zombie_extreme:runner", 15);
        put("zombie_extreme:infected_police", 15);
        put("undead_revamp2:thebomber", 15);
        put("undead_revamp2:therod", 15);
        put("undead_revamp2:thehorrors", 15);
        put("undead_revamp2:thewolf", 15);
        put("undead_revamp2:thebruin", 15);
        put("undead_revamp2:thewheezer", 15);
        put("undead_revamp2:sucker", 15);
        put("undead_revamp2:theskeepper", 15);

        // ─── SUPERIOR 2 (50 points) ────────────────────────────
        put("zombie_extreme:infected_hazmat", 50);
        put("zombie_extreme:infected_military", 50);
        put("zombie_extreme:boomer", 50);
        put("zombie_extreme:chainsaw", 50);
        put("zombie_extreme:explosive_infected", 50);
        put("zombie_extreme:clicker", 50);
        put("zombie_extreme:inflated", 50);
        put("undead_revamp2:deadclogger", 50);
        put("undead_revamp2:thespitter", 50);
        put("undead_revamp2:theroyal", 50);
        put("undead_revamp2:thedungeon", 50);
        put("undead_revamp2:theswarmer", 50);
        put("undead_revamp2:the_moonflower", 50);
        put("undead_revamp2:thehunter", 50);
        put("undead_revamp2:therabidus", 50);

        // ─── SUPERIOR 3 (100 points) ───────────────────────────
        put("zombie_extreme:infected_juggernaut", 100);
        put("zombie_extreme:ram", 100);
        put("zombie_extreme:night_hunter", 100);
        put("zombie_extreme:faceless", 100);
        put("zombie_extreme:goon", 100);
        put("zombie_extreme:revived", 100);
        put("zombie_extreme:divided", 100);
        put("undead_revamp2:thepregnant", 100);
        put("undead_revamp2:thegliter", 100);
        put("undead_revamp2:bigsucker", 100);
        put("undead_revamp2:lechery", 100);
        put("undead_revamp2:theposessive", 100);
        put("undead_revamp2:clogger", 100);

        // ─── BOSS (1000 points) ────────────────────────────────
        put("undead_revamp2:theheavy", 1000);
        put("zombie_extreme:demolisher", 1000);
        put("zombie_extreme:rat_king", 1000);
        put("zombie_extreme:zero_patient", 1000);
        put("undead_revamp2:thelurker", 1000);
    }

    /**
     * Force-regenerates the defaults, overwriting any user-modified values
     * and writing them to disk.
     */
    public static void resetToDefaults() {
        initDefaults();
        save();
    }

    private static void put(String id, int value) {
        values.put(id, value);
    }
}
