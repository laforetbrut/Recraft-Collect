package com.vyrriox.recraftcollect;

import com.vyrriox.recraftcollect.command.FoodScoreCommand;
import com.vyrriox.recraftcollect.config.MilestoneConfig;
import com.vyrriox.recraftcollect.data.FoodScoreManager;
import com.vyrriox.recraftcollect.data.FoodUnitCalculator;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
import java.util.Map;

@Mod(ReCraftCollect.MODID)
public class ReCraftCollect {
    public static final String MODID = "recraftcollect";
    public static final NumberFormat NF = NumberFormat.getInstance(Locale.FRANCE);

    private static ServerBossEvent bossBar;
    private int tickCounter = 0;

    public ReCraftCollect() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY,
                        (a, b) -> true
                ));

        // Load milestone config from file
        MilestoneConfig.init(FMLPaths.CONFIGDIR.get());

        MinecraftForge.EVENT_BUS.register(this);
    }

    // ─── Boss Bar ──────────────────────────────────────────────

    public static ServerBossEvent getBossBar() {
        return bossBar;
    }

    public static void updateBossBar(MinecraftServer server) {
        if (bossBar == null) return;
        FoodScoreManager manager = FoodScoreManager.get(server);
        long globalScore = manager.getGlobalScore();
        long goal = manager.getGoal();
        float progress = goal > 0 ? Math.min((float) globalScore / goal, 1.0f) : 1.0f;

        bossBar.setProgress(progress);
        bossBar.setName(Component.literal("ReCraft Collect: " + NF.format(globalScore) + " / " + NF.format(goal) + " unites")
                .withStyle(progress >= 1.0f ? ChatFormatting.GREEN : ChatFormatting.GOLD));

        if (progress >= 1.0f) {
            bossBar.setColor(BossEvent.BossBarColor.GREEN);
        } else if (progress >= 0.75f) {
            bossBar.setColor(BossEvent.BossBarColor.YELLOW);
        } else if (progress >= 0.5f) {
            bossBar.setColor(BossEvent.BossBarColor.BLUE);
        } else {
            bossBar.setColor(BossEvent.BossBarColor.WHITE);
        }
    }

    private void updateBossBarVisibility(MinecraftServer server) {
        if (bossBar == null) return;
        FoodScoreManager manager = FoodScoreManager.get(server);

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

    private boolean shouldShowBossBar(ServerPlayer player, FoodScoreManager manager) {
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
        FoodScoreManager manager = FoodScoreManager.get(server);
        BlockPos pos = manager.getLeaderboardPos();
        if (pos != null && manager.getLeaderboardDimension() != null) {
            LeaderboardDisplay.createOrUpdate(server, pos, manager.getLeaderboardDimension());
        }
    }

    // ─── Milestones ────────────────────────────────────────────

    public static void checkMilestones(MinecraftServer server) {
        FoodScoreManager manager = FoodScoreManager.get(server);
        List<Long> newMilestones = manager.checkAndMarkMilestones();
        for (long milestone : newMilestones) {
            broadcastMilestone(server, manager, milestone);
        }
    }

    private static void broadcastMilestone(MinecraftServer server, FoodScoreManager manager, long milestone) {
        MilestoneConfig.MilestoneEntry entry = MilestoneConfig.getMilestone(milestone);
        String message = entry != null ? entry.message : "Palier " + NF.format(milestone) + " unites atteint !";

        boolean isFinal = milestone >= manager.getGoal();

        // Chat announcement
        Component component = Component.literal("")
                .append(Component.literal(isFinal ? "★★★ " : "★ ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("[ReCraft Collect] ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
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

        // Execute milestone commands
        if (entry != null && entry.commands != null) {
            for (String cmd : entry.commands) {
                try {
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack().withSuppressedOutput(), cmd);
                } catch (Exception e) {
                    // Log but don't crash if a command fails
                    server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "say [ReCraft Collect] Erreur execution commande palier: " + cmd);
                }
            }
        }
    }

    // ─── Server Lifecycle ──────────────────────────────────────

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        bossBar = new ServerBossEvent(
                Component.literal("ReCraft Collect").withStyle(ChatFormatting.GOLD),
                BossEvent.BossBarColor.WHITE,
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
            FoodScoreManager manager = FoodScoreManager.get(serverPlayer.server);
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
        FoodScoreCommand.register(event.getDispatcher());
    }

    // ─── Right-Click Collection Point ──────────────────────────

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        FoodScoreManager manager = FoodScoreManager.get(player.server);

        if (manager.getCollectionPoint() == null) return;
        if (!event.getPos().equals(manager.getCollectionPoint())) return;

        ResourceKey<Level> dim = manager.getCollectionDimension();
        if (dim != null && !player.level().dimension().equals(dim)) return;

        event.setCanceled(true);

        Map<String, Integer> overrides = manager.getItemValueOverrides();

        if (player.isShiftKeyDown()) {
            long totalUnits = depositAllFood(player, overrides);
            if (totalUnits > 0) {
                manager.addScore(player.getUUID(), totalUnits);
                updateBossBar(player.server);
                checkMilestones(player.server);
                refreshLeaderboard(player.server);
                sendDepositFeedback(player, totalUnits, manager.getPlayerScore(player.getUUID()), event);
            } else {
                player.displayClientMessage(
                        Component.literal("Aucune nourriture dans votre inventaire !")
                                .withStyle(ChatFormatting.RED), true);
            }
            return;
        }

        ItemStack held = player.getMainHandItem();

        if (held.isEmpty()) {
            showScoreInfo(player, manager);
            return;
        }

        if (!FoodUnitCalculator.isDepositable(held, overrides)) {
            player.displayClientMessage(
                    Component.literal("Cet objet n'est pas de la nourriture !")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        long units = FoodUnitCalculator.getFoodUnits(held, overrides);
        held.setCount(0);
        manager.addScore(player.getUUID(), units);
        updateBossBar(player.server);
        checkMilestones(player.server);
        sendDepositFeedback(player, units, manager.getPlayerScore(player.getUUID()), event);
    }

    private long depositAllFood(ServerPlayer player, Map<String, Integer> overrides) {
        long totalUnits = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && FoodUnitCalculator.isDepositable(stack, overrides)) {
                totalUnits += FoodUnitCalculator.getFoodUnits(stack, overrides);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
        return totalUnits;
    }

    private void sendDepositFeedback(ServerPlayer player, long unitsDeposited, long totalScore,
                                     PlayerInteractEvent.RightClickBlock event) {
        player.displayClientMessage(
                Component.literal("+" + NF.format(unitsDeposited) + " unites ! ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("(Total: " + NF.format(totalScore) + ")")
                                .withStyle(ChatFormatting.GRAY)),
                true);

        player.playNotifySound(SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0f, 1.0f);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    event.getPos().getX() + 0.5,
                    event.getPos().getY() + 1.2,
                    event.getPos().getZ() + 0.5,
                    15, 0.4, 0.4, 0.4, 0.02);
        }
    }

    private void showScoreInfo(ServerPlayer player, FoodScoreManager manager) {
        long playerScore = manager.getPlayerScore(player.getUUID());
        long globalScore = manager.getGlobalScore();
        long goal = manager.getGoal();
        float progress = goal > 0 ? Math.min((float) globalScore / goal * 100f, 100f) : 100f;

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("  === ReCraft Collect ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  Votre score: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(NF.format(playerScore) + " unites").withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("  Score global: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(NF.format(globalScore) + " / " + NF.format(goal) + " unites").withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("  Progression: ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("%.1f%%", progress)).withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal(""));
    }
}
