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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Persistent storage for zombie purge scores, goal, milestones reached,
 * boss bar zone, and leaderboard position. Survives world reloads.
 *
 * Save name kept as recraftcollect_scores for v1.x save compatibility.
 */
public class ZombieScoreManager extends SavedData {

    private static final String DATA_NAME = "recraftcollect_scores";
    public static final long DEFAULT_GOAL = 1_000_000L;

    // ─── Scores ────────────────────────────────────────────────
    private final Map<UUID, Long> playerScores = new HashMap<>();
    private long globalScore = 0;

    // ─── Goal ──────────────────────────────────────────────────
    private long goal = DEFAULT_GOAL;

    // ─── Boss Bar Zone ─────────────────────────────────────────
    private BlockPos bossBarCenter = null;
    private ResourceKey<Level> bossBarDimension = null;
    private int bossBarRadius = 0;

    // ─── Boss Bar Per-Player Toggle ────────────────────────────
    private final Set<UUID> bossBarHidden = new HashSet<>();

    // ─── Milestones reached (definitions are in MilestoneConfig)
    private final Set<Long> milestonesReached = new HashSet<>();

    // ─── Leaderboard Display Position ──────────────────────────
    private BlockPos leaderboardPos = null;
    private ResourceKey<Level> leaderboardDimension = null;

    public ZombieScoreManager() {
    }

    // ─── Load / Save ───────────────────────────────────────────

    public static ZombieScoreManager load(CompoundTag tag) {
        ZombieScoreManager manager = new ZombieScoreManager();
        manager.globalScore = tag.getLong("GlobalScore");

        if (tag.contains("Goal")) {
            manager.goal = tag.getLong("Goal");
        }

        ListTag playerList = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            UUID uuid = playerTag.getUUID("UUID");
            long score = playerTag.getLong("Score");
            manager.playerScores.put(uuid, score);
        }

        if (tag.contains("BarCenterX")) {
            manager.bossBarCenter = new BlockPos(
                    tag.getInt("BarCenterX"), tag.getInt("BarCenterY"), tag.getInt("BarCenterZ"));
            manager.bossBarRadius = tag.getInt("BarRadius");
            if (tag.contains("BarDim")) {
                manager.bossBarDimension = ResourceKey.create(
                        Registries.DIMENSION, new ResourceLocation(tag.getString("BarDim")));
            }
        }

        ListTag hiddenList = tag.getList("BossBarHidden", Tag.TAG_COMPOUND);
        for (int i = 0; i < hiddenList.size(); i++) {
            manager.bossBarHidden.add(hiddenList.getCompound(i).getUUID("UUID"));
        }

        ListTag reachedList = tag.getList("MilestonesReached", Tag.TAG_COMPOUND);
        if (reachedList.isEmpty()) {
            reachedList = tag.getList("Milestones", Tag.TAG_COMPOUND);
        }
        for (int i = 0; i < reachedList.size(); i++) {
            manager.milestonesReached.add(reachedList.getCompound(i).getLong("Value"));
        }

        if (tag.contains("LeaderX")) {
            manager.leaderboardPos = new BlockPos(
                    tag.getInt("LeaderX"), tag.getInt("LeaderY"), tag.getInt("LeaderZ"));
            if (tag.contains("LeaderDim")) {
                manager.leaderboardDimension = ResourceKey.create(
                        Registries.DIMENSION, new ResourceLocation(tag.getString("LeaderDim")));
            }
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("GlobalScore", globalScore);
        tag.putLong("Goal", goal);

        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, Long> entry : playerScores.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());
            playerTag.putLong("Score", entry.getValue());
            playerList.add(playerTag);
        }
        tag.put("Players", playerList);

        if (bossBarCenter != null) {
            tag.putInt("BarCenterX", bossBarCenter.getX());
            tag.putInt("BarCenterY", bossBarCenter.getY());
            tag.putInt("BarCenterZ", bossBarCenter.getZ());
            tag.putInt("BarRadius", bossBarRadius);
            if (bossBarDimension != null) {
                tag.putString("BarDim", bossBarDimension.location().toString());
            }
        }

        ListTag hiddenList = new ListTag();
        for (UUID uuid : bossBarHidden) {
            CompoundTag ht = new CompoundTag();
            ht.putUUID("UUID", uuid);
            hiddenList.add(ht);
        }
        tag.put("BossBarHidden", hiddenList);

        ListTag reachedList = new ListTag();
        for (long milestone : milestonesReached) {
            CompoundTag mt = new CompoundTag();
            mt.putLong("Value", milestone);
            reachedList.add(mt);
        }
        tag.put("MilestonesReached", reachedList);

        if (leaderboardPos != null) {
            tag.putInt("LeaderX", leaderboardPos.getX());
            tag.putInt("LeaderY", leaderboardPos.getY());
            tag.putInt("LeaderZ", leaderboardPos.getZ());
            if (leaderboardDimension != null) {
                tag.putString("LeaderDim", leaderboardDimension.location().toString());
            }
        }

        return tag;
    }

    // ─── Goal ──────────────────────────────────────────────────

    public long getGoal() { return goal; }

    public void setGoal(long goal) {
        this.goal = goal;
        setDirty();
    }

    // ─── Scores ────────────────────────────────────────────────

    public void addScore(UUID playerId, long points) {
        playerScores.merge(playerId, points, Long::sum);
        globalScore += points;
        setDirty();
    }

    public void removeScore(UUID playerId, long points) {
        long current = playerScores.getOrDefault(playerId, 0L);
        long toRemove = Math.min(points, current);
        if (toRemove <= 0) return;
        playerScores.put(playerId, current - toRemove);
        globalScore = Math.max(0, globalScore - toRemove);
        setDirty();
    }

    public long getPlayerScore(UUID playerId) {
        return playerScores.getOrDefault(playerId, 0L);
    }

    public long getGlobalScore() { return globalScore; }

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

    // ─── Milestones ────────────────────────────────────────────

    public List<Long> checkAndMarkMilestones() {
        List<Long> newMilestones = new ArrayList<>();
        for (MilestoneConfig.MilestoneEntry entry : MilestoneConfig.getMilestones()) {
            if (globalScore >= entry.threshold && !milestonesReached.contains(entry.threshold)) {
                milestonesReached.add(entry.threshold);
                newMilestones.add(entry.threshold);
            }
        }
        if (!newMilestones.isEmpty()) setDirty();
        return newMilestones;
    }

    public boolean isMilestoneReached(long threshold) {
        return milestonesReached.contains(threshold);
    }

    public void clearMilestoneReached(long threshold) {
        milestonesReached.remove(threshold);
        setDirty();
    }

    public void resetAllMilestonesReached() {
        milestonesReached.clear();
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

    // ─── Leaderboard Display ─────────────────────────────────

    public BlockPos getLeaderboardPos() { return leaderboardPos; }
    public ResourceKey<Level> getLeaderboardDimension() { return leaderboardDimension; }

    public void setLeaderboardPos(BlockPos pos, ResourceKey<Level> dimension) {
        this.leaderboardPos = pos;
        this.leaderboardDimension = dimension;
        setDirty();
    }

    public void removeLeaderboardPos() {
        this.leaderboardPos = null;
        this.leaderboardDimension = null;
        setDirty();
    }

    // ─── Access ────────────────────────────────────────────────

    public static ZombieScoreManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                ZombieScoreManager::load,
                ZombieScoreManager::new,
                DATA_NAME
        );
    }
}
