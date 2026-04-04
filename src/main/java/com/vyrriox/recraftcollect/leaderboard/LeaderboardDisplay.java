package com.vyrriox.recraftcollect.leaderboard;

import com.vyrriox.recraftcollect.data.FoodScoreManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

import java.text.NumberFormat;
import java.util.*;

public class LeaderboardDisplay {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.FRANCE);
    private static final String LEADERBOARD_TAG = "recraftcollect_leaderboard";

    // 12 lines: 1 title + 1 blank + 10 player entries
    private static final int TOTAL_LINES = 12;
    private static final double LINE_HEIGHT = 0.28;

    /**
     * Spawns or updates the armor stand leaderboard at the given position.
     */
    public static void createOrUpdate(MinecraftServer server, BlockPos pos, ResourceKey<Level> dimension) {
        ServerLevel level = server.getLevel(dimension);
        if (level == null) return;

        // Remove existing leaderboard
        removeAll(level);

        FoodScoreManager manager = FoodScoreManager.get(server);
        List<Map.Entry<UUID, Long>> top = manager.getTopScores(10);

        double baseX = pos.getX() + 0.5;
        double baseY = pos.getY() + (TOTAL_LINES - 1) * LINE_HEIGHT;
        double baseZ = pos.getZ() + 0.5;

        int lineIndex = 0;

        // Title line
        Component title = Component.literal("== ReCraft Collect ==")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        spawnLine(level, baseX, baseY - lineIndex * LINE_HEIGHT, baseZ, title);
        lineIndex++;

        // Subtitle: global score
        long globalScore = manager.getGlobalScore();
        long goal = manager.getGoal();
        float progress = goal > 0 ? Math.min((float) globalScore / goal * 100f, 100f) : 100f;
        Component subtitle = Component.literal(NF.format(globalScore) + " / " + NF.format(goal) + " (" + String.format("%.1f%%", progress) + ")")
                .withStyle(ChatFormatting.GREEN);
        spawnLine(level, baseX, baseY - lineIndex * LINE_HEIGHT, baseZ, subtitle);
        lineIndex++;

        // Player entries
        if (top.isEmpty()) {
            Component empty = Component.literal("Aucun score").withStyle(ChatFormatting.GRAY);
            spawnLine(level, baseX, baseY - lineIndex * LINE_HEIGHT, baseZ, empty);
        } else {
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<UUID, Long> entry = top.get(i);
                int rank = i + 1;

                String playerName = server.getProfileCache() != null
                        ? server.getProfileCache().get(entry.getKey())
                        .map(p -> p.getName()).orElse("???")
                        : "???";

                ChatFormatting rankColor;
                if (rank == 1) rankColor = ChatFormatting.GOLD;
                else if (rank == 2) rankColor = ChatFormatting.GRAY;
                else if (rank == 3) rankColor = ChatFormatting.RED;
                else rankColor = ChatFormatting.WHITE;

                String scoreStr = NF.format(entry.getValue());
                Component line = Component.literal("#" + rank + " ")
                        .withStyle(rankColor, ChatFormatting.BOLD)
                        .append(Component.literal(playerName).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" - " + scoreStr).withStyle(ChatFormatting.YELLOW));

                spawnLine(level, baseX, baseY - lineIndex * LINE_HEIGHT, baseZ, line);
                lineIndex++;
            }
        }
    }

    private static void spawnLine(ServerLevel level, double x, double y, double z, Component text) {
        ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
        stand.setPos(x, y, z);
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);

        // Set Small + Marker via NBT (setSmall not accessible in Forge)
        CompoundTag nbt = new CompoundTag();
        stand.save(nbt);
        nbt.putBoolean("Small", true);
        nbt.putBoolean("Marker", true);
        stand.load(nbt);

        // Tag for identification
        stand.addTag(LEADERBOARD_TAG);

        level.addFreshEntity(stand);
    }

    /**
     * Removes all leaderboard armor stands in the given level.
     */
    public static void removeAll(ServerLevel level) {
        List<ArmorStand> toRemove = new ArrayList<>();
        for (var entity : level.getAllEntities()) {
            if (entity instanceof ArmorStand stand && stand.getTags().contains(LEADERBOARD_TAG)) {
                toRemove.add(stand);
            }
        }
        for (ArmorStand stand : toRemove) {
            stand.discard();
        }
    }

    /**
     * Removes leaderboard from all dimensions.
     */
    public static void removeFromServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            removeAll(level);
        }
    }
}
