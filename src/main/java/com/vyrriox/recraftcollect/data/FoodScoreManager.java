package com.vyrriox.recraftcollect.data;

import com.vyrriox.recraftcollect.config.MilestoneConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class FoodScoreManager extends SavedData {

    private static final String DATA_NAME = "recraftcollect_scores";
    public static final long DEFAULT_GOAL = 1_000_000L;

    // ─── Scores ────────────────────────────────────────────────
    private final Map<UUID, Long> playerScores = new HashMap<>();
    private long globalScore = 0;

    // ─── Goal ──────────────────────────────────────────────────
    private long goal = DEFAULT_GOAL;

    // ─── Collection Point ──────────────────────────────────────
    private BlockPos collectionPoint = null;
    private ResourceKey<Level> collectionDimension = null;

    // ─── Boss Bar Zone ─────────────────────────────────────────
    private BlockPos bossBarCenter = null;
    private ResourceKey<Level> bossBarDimension = null;
    private int bossBarRadius = 0;

    // ─── Boss Bar Per-Player Toggle ────────────────────────────
    private final Set<UUID> bossBarHidden = new HashSet<>();

    // ─── Milestones reached (definitions are in MilestoneConfig)
    private final Set<Long> milestonesReached = new HashSet<>();

    // ─── Item Value Overrides (registry name → value) ──────────
    private final Map<String, Integer> itemValueOverrides = new HashMap<>();

    public FoodScoreManager() {
    }

    // ─── Load / Save ───────────────────────────────────────────

    public static FoodScoreManager load(CompoundTag tag) {
        FoodScoreManager manager = new FoodScoreManager();
        manager.globalScore = tag.getLong("GlobalScore");

        if (tag.contains("Goal")) {
            manager.goal = tag.getLong("Goal");
        }

        // Player scores
        ListTag playerList = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            UUID uuid = playerTag.getUUID("UUID");
            long score = playerTag.getLong("Score");
            manager.playerScores.put(uuid, score);
        }

        // Collection point
        if (tag.contains("CollectX")) {
            manager.collectionPoint = new BlockPos(
                    tag.getInt("CollectX"), tag.getInt("CollectY"), tag.getInt("CollectZ"));
            if (tag.contains("CollectDim")) {
                manager.collectionDimension = ResourceKey.create(
                        Registries.DIMENSION, new ResourceLocation(tag.getString("CollectDim")));
            }
        }

        // Boss bar zone
        if (tag.contains("BarCenterX")) {
            manager.bossBarCenter = new BlockPos(
                    tag.getInt("BarCenterX"), tag.getInt("BarCenterY"), tag.getInt("BarCenterZ"));
            manager.bossBarRadius = tag.getInt("BarRadius");
            if (tag.contains("BarDim")) {
                manager.bossBarDimension = ResourceKey.create(
                        Registries.DIMENSION, new ResourceLocation(tag.getString("BarDim")));
            }
        }

        // Boss bar hidden players
        ListTag hiddenList = tag.getList("BossBarHidden", Tag.TAG_COMPOUND);
        for (int i = 0; i < hiddenList.size(); i++) {
            manager.bossBarHidden.add(hiddenList.getCompound(i).getUUID("UUID"));
        }

        // Milestones reached (try new key, fallback to old keys)
        ListTag reachedList = tag.getList("MilestonesReached", Tag.TAG_COMPOUND);
        if (reachedList.isEmpty()) {
            reachedList = tag.getList("Milestones", Tag.TAG_COMPOUND);
        }
        for (int i = 0; i < reachedList.size(); i++) {
            manager.milestonesReached.add(reachedList.getCompound(i).getLong("Value"));
        }

        // Item value overrides
        ListTag overrideList = tag.getList("ItemOverrides", Tag.TAG_COMPOUND);
        for (int i = 0; i < overrideList.size(); i++) {
            CompoundTag ot = overrideList.getCompound(i);
            manager.itemValueOverrides.put(ot.getString("Item"), ot.getInt("Value"));
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("GlobalScore", globalScore);
        tag.putLong("Goal", goal);

        // Player scores
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, Long> entry : playerScores.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());
            playerTag.putLong("Score", entry.getValue());
            playerList.add(playerTag);
        }
        tag.put("Players", playerList);

        // Collection point
        if (collectionPoint != null) {
            tag.putInt("CollectX", collectionPoint.getX());
            tag.putInt("CollectY", collectionPoint.getY());
            tag.putInt("CollectZ", collectionPoint.getZ());
            if (collectionDimension != null) {
                tag.putString("CollectDim", collectionDimension.location().toString());
            }
        }

        // Boss bar zone
        if (bossBarCenter != null) {
            tag.putInt("BarCenterX", bossBarCenter.getX());
            tag.putInt("BarCenterY", bossBarCenter.getY());
            tag.putInt("BarCenterZ", bossBarCenter.getZ());
            tag.putInt("BarRadius", bossBarRadius);
            if (bossBarDimension != null) {
                tag.putString("BarDim", bossBarDimension.location().toString());
            }
        }

        // Boss bar hidden
        ListTag hiddenList = new ListTag();
        for (UUID uuid : bossBarHidden) {
            CompoundTag ht = new CompoundTag();
            ht.putUUID("UUID", uuid);
            hiddenList.add(ht);
        }
        tag.put("BossBarHidden", hiddenList);

        // Milestones reached
        ListTag reachedList = new ListTag();
        for (long milestone : milestonesReached) {
            CompoundTag mt = new CompoundTag();
            mt.putLong("Value", milestone);
            reachedList.add(mt);
        }
        tag.put("MilestonesReached", reachedList);

        // Item value overrides
        ListTag overrideList = new ListTag();
        for (Map.Entry<String, Integer> entry : itemValueOverrides.entrySet()) {
            CompoundTag ot = new CompoundTag();
            ot.putString("Item", entry.getKey());
            ot.putInt("Value", entry.getValue());
            overrideList.add(ot);
        }
        tag.put("ItemOverrides", overrideList);

        return tag;
    }

    // ─── Goal ──────────────────────────────────────────────────

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
        setDirty();
    }

    // ─── Scores ────────────────────────────────────────────────

    public void addScore(UUID playerId, long units) {
        playerScores.merge(playerId, units, Long::sum);
        globalScore += units;
        setDirty();
    }

    public void removeScore(UUID playerId, long units) {
        long current = playerScores.getOrDefault(playerId, 0L);
        long toRemove = Math.min(units, current);
        if (toRemove <= 0) return;
        playerScores.put(playerId, current - toRemove);
        globalScore = Math.max(0, globalScore - toRemove);
        setDirty();
    }

    public long getPlayerScore(UUID playerId) {
        return playerScores.getOrDefault(playerId, 0L);
    }

    public long getGlobalScore() {
        return globalScore;
    }

    public List<Map.Entry<UUID, Long>> getTopScores(int limit) {
        return playerScores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    public void resetAllScores() {
        playerScores.clear();
        globalScore = 0;
        milestonesReached.clear();
        setDirty();
    }

    // ─── Milestones (definitions in MilestoneConfig, tracking here)

    public List<Long> checkAndMarkMilestones() {
        List<Long> newMilestones = new ArrayList<>();
        for (MilestoneConfig.MilestoneEntry entry : MilestoneConfig.getMilestones()) {
            if (globalScore >= entry.threshold && !milestonesReached.contains(entry.threshold)) {
                milestonesReached.add(entry.threshold);
                newMilestones.add(entry.threshold);
            }
        }
        if (!newMilestones.isEmpty()) {
            setDirty();
        }
        return newMilestones;
    }

    public boolean isMilestoneReached(long threshold) {
        return milestonesReached.contains(threshold);
    }

    public void clearMilestoneReached(long threshold) {
        milestonesReached.remove(threshold);
        setDirty();
    }

    // ─── Item Value Overrides ──────────────────────────────────

    public Map<String, Integer> getItemValueOverrides() {
        return Collections.unmodifiableMap(itemValueOverrides);
    }

    public void setItemValue(String itemId, int value) {
        itemValueOverrides.put(itemId, value);
        setDirty();
    }

    public boolean removeItemValue(String itemId) {
        boolean removed = itemValueOverrides.remove(itemId) != null;
        if (removed) setDirty();
        return removed;
    }

    public boolean hasItemOverride(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        return itemValueOverrides.containsKey(itemId);
    }

    // ─── Collection Point ──────────────────────────────────────

    public BlockPos getCollectionPoint() { return collectionPoint; }
    public ResourceKey<Level> getCollectionDimension() { return collectionDimension; }

    public void setCollectionPoint(BlockPos pos, ResourceKey<Level> dimension) {
        this.collectionPoint = pos;
        this.collectionDimension = dimension;
        setDirty();
    }

    public void removeCollectionPoint() {
        this.collectionPoint = null;
        this.collectionDimension = null;
        setDirty();
    }

    // ─── Boss Bar Zone ─────────────────────────────────────────

    public BlockPos getBossBarCenter() { return bossBarCenter; }
    public ResourceKey<Level> getBossBarDimension() { return bossBarDimension; }
    public int getBossBarRadius() { return bossBarRadius; }

    public void setBossBarZone(BlockPos center, ResourceKey<Level> dimension, int radius) {
        this.bossBarCenter = center;
        this.bossBarDimension = dimension;
        this.bossBarRadius = radius;
        setDirty();
    }

    public void removeBossBarZone() {
        this.bossBarCenter = null;
        this.bossBarDimension = null;
        this.bossBarRadius = 0;
        setDirty();
    }

    // ─── Boss Bar Per-Player Toggle ────────────────────────────

    public boolean isBossBarHidden(UUID playerId) {
        return bossBarHidden.contains(playerId);
    }

    public boolean toggleBossBar(UUID playerId) {
        boolean nowHidden;
        if (bossBarHidden.contains(playerId)) {
            bossBarHidden.remove(playerId);
            nowHidden = false;
        } else {
            bossBarHidden.add(playerId);
            nowHidden = true;
        }
        setDirty();
        return nowHidden;
    }

    // ─── Access ────────────────────────────────────────────────

    public static FoodScoreManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                FoodScoreManager::load,
                FoodScoreManager::new,
                DATA_NAME
        );
    }
}
