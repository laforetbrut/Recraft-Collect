package com.vyrriox.recraftcollect;

import com.vyrriox.recraftcollect.command.ZombieKillCommand;
import com.vyrriox.recraftcollect.config.MilestoneConfig;
import com.vyrriox.recraftcollect.config.ZombieValueConfig;
import com.vyrriox.recraftcollect.data.ZombieKillResolver;
import com.vyrriox.recraftcollect.data.ZombieScoreManager;
import com.vyrriox.recraftcollect.leaderboard.LeaderboardDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Mod(ReCraftCollect.MODID)
public class ReCraftCollect {
    public static final String MODID = "recraftcollect";
    public static final NumberFormat NF = NumberFormat.getInstance(Locale.FRANCE);
    public static final String OBJECTIVE_NAME = "Purger le monde";

    private static ServerBossEvent bossBar;
    private int tickCounter = 0;

    public ReCraftCollect() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                        (a, b) -> true
                ));

        MilestoneConfig.init(FMLPaths.CONFIGDIR.get());
        ZombieValueConfig.init(FMLPaths.CONFIGDIR.get());

        MinecraftForge.EVENT_BUS.register(this);
    }

    // ─── Boss Bar ──────────────────────────────────────────────

    public static ServerBossEvent getBossBar() {
        return bossBar;
    }

    public static void updateBossBar(MinecraftServer server) {
        if (bossBar == null) return;
        ZombieScoreManager manager = ZombieScoreManager.get(server);
        long globalScore = manager.getGlobalScore();
        long goal = manager.getGoal();
        float progress = goal > 0 ? Math.min((float) globalScore / goal, 1.0f) : 1.0f;

        bossBar.setProgress(progress);
        bossBar.setName(Component.literal(OBJECTIVE_NAME + ": " + NF.format(globalScore) + " / " + NF.format(goal) + " points")
                .withStyle(progress >= 1.0f ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));

        if (progress >= 1.0f) {
            bossBar.setColor(BossEvent.BossBarColor.GREEN);
        } else if (progress >= 0.75f) {
            bossBar.setColor(BossEvent.BossBarColor.YELLOW);
        } else if (progress >= 0.5f) {
            bossBar.setColor(BossEvent.BossBarColor.PURPLE);
        } else {
            bossBar.setColor(BossEvent.BossBarColor.RED);
        }
    }

    private void updateBossBarVisibility(MinecraftServer server) {
        if (bossBar == null) return;
        ZombieScoreManager manager = ZombieScoreManager.get(server);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean shouldShow = shouldShowBossBar(player, manager);
            boolean isShowing = bossBar.getPlayers().contains(player);

            if (shouldShow && !isShowing) {
                bossBar.addPlayer(player);
            } else if (!shouldShow && isShowing) {
                bossBar.removePlayer(player);
            }
        }
    }

    private boolean shouldShowBossBar(ServerPlayer player, ZombieScoreManager manager) {
        if (manager.isBossBarHidden(player.getUUID())) return false;

        BlockPos center = manager.getBossBarCenter();
        int radius = manager.getBossBarRadius();
        if (center == null || radius <= 0) return true;

        ResourceKey<Level> dim = manager.getBossBarDimension();
        if (dim != null && !player.level().dimension().equals(dim)) return false;

        double distSq = player.blockPosition().distSqr(center);
        return distSq <= (double) radius * radius;
    }

    // ─── Leaderboard ───────────────────────────────────────────

    public static void refreshLeaderboard(MinecraftServer server) {
        ZombieScoreManager manager = ZombieScoreManager.get(server);
        BlockPos pos = manager.getLeaderboardPos();
        if (pos != null && manager.getLeaderboardDimension() != null) {
            LeaderboardDisplay.createOrUpdate(server, pos, manager.getLeaderboardDimension());
        }
    }

    // ─── Milestones ────────────────────────────────────────────

    public static void checkMilestones(MinecraftServer server) {
        ZombieScoreManager manager = ZombieScoreManager.get(server);
        List<Long> newMilestones = manager.checkAndMarkMilestones();
        for (long milestone : newMilestones) {
            broadcastMilestone(server, manager, milestone);
        }
    }

    private static void broadcastMilestone(MinecraftServer server, ZombieScoreManager manager, long milestone) {
        MilestoneConfig.MilestoneEntry entry = MilestoneConfig.getMilestone(milestone);
        String message = entry != null ? entry.message : "Palier " + NF.format(milestone) + " points atteint !";

        boolean isFinal = milestone >= manager.getGoal();

        Component component = Component.literal("")
                .append(Component.literal(isFinal ? "★★★ " : "★ ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("[Purger le monde] ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(message).withStyle(isFinal ? ChatFormatting.GREEN : ChatFormatting.AQUA));

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(""));
            p.sendSystemMessage(component);
            p.sendSystemMessage(Component.literal(""));

            if (isFinal) {
                p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 1.0f);
            } else {
                p.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0f, 1.2f);
            }
        }

        if (entry != null && entry.commands != null) {
            for (String cmd : entry.commands) {
                try {
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack().withSuppressedOutput(), cmd);
                } catch (Exception e) {
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "say [Purger le monde] Erreur execution commande palier: " + cmd);
                }
            }
        }
    }

    // ─── Server Lifecycle ──────────────────────────────────────

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        bossBar = new ServerBossEvent(
                Component.literal(OBJECTIVE_NAME).withStyle(ChatFormatting.DARK_RED),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setVisible(true);
        updateBossBar(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (bossBar != null) {
            bossBar.removeAllPlayers();
            bossBar = null;
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter % 20 != 0) return;
        if (event.getServer() != null) {
            updateBossBarVisibility(event.getServer());
        }
    }

    // ─── Player Events ─────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && bossBar != null) {
            ZombieScoreManager manager = ZombieScoreManager.get(serverPlayer.server);
            if (shouldShowBossBar(serverPlayer, manager)) {
                bossBar.addPlayer(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && bossBar != null) {
            bossBar.removePlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ZombieKillCommand.register(event.getDispatcher());
    }

    // ─── Zombie Kill Tracking ──────────────────────────────────

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // Direct kill by a player only - no pets, no environment, no proxies
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        int points = ZombieKillResolver.getPoints(victim);
        if (points <= 0) return;

        ZombieScoreManager manager = ZombieScoreManager.get(killer.server);
        manager.addScore(killer.getUUID(), points);

        sendKillFeedback(killer, victim, points, manager.getPlayerScore(killer.getUUID()));

        updateBossBar(killer.server);
        checkMilestones(killer.server);
        refreshLeaderboard(killer.server);
    }

    private void sendKillFeedback(ServerPlayer player, LivingEntity victim, int points, long totalScore) {
        ChatFormatting color;
        if (points >= 2000) color = ChatFormatting.LIGHT_PURPLE;
        else if (points >= 100) color = ChatFormatting.GOLD;
        else if (points >= 50) color = ChatFormatting.RED;
        else if (points >= 15) color = ChatFormatting.YELLOW;
        else color = ChatFormatting.GREEN;

        player.displayClientMessage(
                Component.literal("+" + NF.format(points) + " pts ").withStyle(color, ChatFormatting.BOLD)
                        .append(Component.literal("(" + NF.format(totalScore) + ")").withStyle(ChatFormatting.GRAY)),
                true);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    victim.getX(), victim.getY() + victim.getBbHeight() / 2.0, victim.getZ(),
                    Math.min(20, 4 + points / 10), 0.3, 0.3, 0.3, 0.02);

            if (points >= 2000) {
                player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.MASTER, 0.6f, 1.2f);
            } else if (points >= 100) {
                player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 0.5f, 1.5f);
            }
        }
    }
}
