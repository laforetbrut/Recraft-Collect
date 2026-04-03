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

public class MilestoneConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("ReCraftCollect");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static final List<MilestoneEntry> milestones = new ArrayList<>();

    // ─── Milestone data structure ──────────────────────────────

    public static class MilestoneEntry {
        public long threshold;
        public String message;
        public List<String> commands;

        public MilestoneEntry() {
            this.commands = new ArrayList<>();
        }

        public MilestoneEntry(long threshold, String message, List<String> commands) {
            this.threshold = threshold;
            this.message = message;
            this.commands = commands != null ? new ArrayList<>(commands) : new ArrayList<>();
        }
    }

    // ─── Init / Load / Save ────────────────────────────────────

    public static void init(Path configDir) {
        configPath = configDir.resolve("recraftcollect-milestones.json");
        load();
    }

    public static void load() {
        milestones.clear();

        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type listType = new TypeToken<List<MilestoneEntry>>() {}.getType();
                List<MilestoneEntry> loaded = GSON.fromJson(reader, listType);
                if (loaded != null) {
                    milestones.addAll(loaded);
                    milestones.sort(Comparator.comparingLong(e -> e.threshold));
                }
                LOGGER.info("Loaded {} milestones from config", milestones.size());
            } catch (Exception e) {
                LOGGER.error("Failed to load milestones config, using defaults", e);
                initDefaults();
                save();
            }
        } else {
            LOGGER.info("No milestones config found, creating defaults");
            initDefaults();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(milestones, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save milestones config", e);
        }
    }

    private static void initDefaults() {
        milestones.clear();
        milestones.add(new MilestoneEntry(10_000L,
                "Les premiers pas sont faits ! 10 000 unites recoltees !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(25_000L,
                "25 000 unites ! La recolte prend forme !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(50_000L,
                "50 000 unites ! Les greniers commencent a se remplir !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(100_000L,
                "100 000 unites ! Un dixieme de l'objectif !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(250_000L,
                "250 000 unites ! Un quart du chemin parcouru !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(500_000L,
                "500 000 unites ! La moitie ! Continuez comme ca !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(750_000L,
                "750 000 unites ! Trois quarts ! La ligne d'arrivee approche !", new ArrayList<>()));
        milestones.add(new MilestoneEntry(1_000_000L,
                "OBJECTIF ATTEINT ! 1 000 000 d'unites ! Felicitations a tous !", new ArrayList<>()));
    }

    // ─── Getters ───────────────────────────────────────────────

    public static List<MilestoneEntry> getMilestones() {
        return Collections.unmodifiableList(milestones);
    }

    public static MilestoneEntry getMilestone(long threshold) {
        return milestones.stream()
                .filter(m -> m.threshold == threshold)
                .findFirst().orElse(null);
    }

    // ─── Milestone management ──────────────────────────────────

    public static void addMilestone(long threshold, String message) {
        MilestoneEntry existing = getMilestone(threshold);
        if (existing != null) {
            existing.message = message;
        } else {
            milestones.add(new MilestoneEntry(threshold, message, new ArrayList<>()));
            milestones.sort(Comparator.comparingLong(e -> e.threshold));
        }
        save();
    }

    public static boolean removeMilestone(long threshold) {
        boolean removed = milestones.removeIf(m -> m.threshold == threshold);
        if (removed) save();
        return removed;
    }

    // ─── Command management ────────────────────────────────────

    public static boolean addCommand(long threshold, String command) {
        MilestoneEntry entry = getMilestone(threshold);
        if (entry == null) return false;
        entry.commands.add(command);
        save();
        return true;
    }

    public static boolean removeCommand(long threshold, int index) {
        MilestoneEntry entry = getMilestone(threshold);
        if (entry == null || index < 0 || index >= entry.commands.size()) return false;
        entry.commands.remove(index);
        save();
        return true;
    }
}
