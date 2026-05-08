package com.vyrriox.recraftcollect.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vyrriox.recraftcollect.ReCraftCollect;
import com.vyrriox.recraftcollect.config.MilestoneConfig;
import com.vyrriox.recraftcollect.config.ZombieValueConfig;
import com.vyrriox.recraftcollect.data.ZombieScoreManager;
import com.vyrriox.recraftcollect.leaderboard.LeaderboardDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ZombieKillCommand {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.FRANCE);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zk")
                .executes(ctx -> showHelp(ctx.getSource()))

                // ─── Player commands ───────────────────────────
                .then(Commands.literal("score")
                        .executes(ctx -> showScore(ctx.getSource())))

                .then(Commands.literal("top")
                        .executes(ctx -> showTop(ctx.getSource())))

                .then(Commands.literal("bossbar")
                        .executes(ctx -> toggleBossBar(ctx.getSource())))

                // ─── Admin: Points ─────────────────────────────
                .then(Commands.literal("give")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(ctx -> givePoints(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                LongArgumentType.getLong(ctx, "amount"))))))

                .then(Commands.literal("take")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(ctx -> takePoints(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                LongArgumentType.getLong(ctx, "amount"))))))

                // ─── Admin: Goal ───────────────────────────────
                .then(Commands.literal("setgoal")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(ctx -> setGoal(ctx.getSource(),
                                        LongArgumentType.getLong(ctx, "amount")))))

                // ─── Admin: Milestones ─────────────────────────
                .then(Commands.literal("milestone")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("threshold", LongArgumentType.longArg(1))
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(ctx -> addMilestone(ctx.getSource(),
                                                        LongArgumentType.getLong(ctx, "threshold"),
                                                        StringArgumentType.getString(ctx, "message"))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("threshold", LongArgumentType.longArg(1))
                                        .executes(ctx -> removeMilestone(ctx.getSource(),
                                                LongArgumentType.getLong(ctx, "threshold")))))
                        .then(Commands.literal("addcmd")
                                .then(Commands.argument("threshold", LongArgumentType.longArg(1))
                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                .executes(ctx -> addMilestoneCmd(ctx.getSource(),
                                                        LongArgumentType.getLong(ctx, "threshold"),
                                                        StringArgumentType.getString(ctx, "command"))))))
                        .then(Commands.literal("removecmd")
                                .then(Commands.argument("threshold", LongArgumentType.longArg(1))
                                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                .executes(ctx -> removeMilestoneCmd(ctx.getSource(),
                                                        LongArgumentType.getLong(ctx, "threshold"),
                                                        IntegerArgumentType.getInteger(ctx, "index"))))))
                        .then(Commands.literal("list")
                                .executes(ctx -> listMilestones(ctx.getSource())))
                        .then(Commands.literal("reload")
                                .executes(ctx -> reloadMilestones(ctx.getSource())))
                        .then(Commands.literal("resetreached")
                                .executes(ctx -> resetMilestonesReached(ctx.getSource())))
                        .then(Commands.literal("resetdefaults")
                                .requires(src -> src.hasPermission(3))
                                .then(Commands.literal("confirm")
                                        .executes(ctx -> resetMilestoneDefaults(ctx.getSource())))))

                // ─── Admin: Leaderboard ────────────────────────
                .then(Commands.literal("leaderboard")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("set")
                                .executes(ctx -> setLeaderboard(ctx.getSource())))
                        .then(Commands.literal("remove")
                                .executes(ctx -> removeLeaderboard(ctx.getSource())))
                        .then(Commands.literal("refresh")
                                .executes(ctx -> refreshLeaderboard(ctx.getSource())))
                        .then(Commands.literal("show")
                                .executes(ctx -> showTop(ctx.getSource()))))

                // ─── Admin: Zombie Values ──────────────────────
                .then(Commands.literal("setvalue")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("entity_id", StringArgumentType.string())
                                .then(Commands.argument("points", IntegerArgumentType.integer(0, 100000))
                                        .executes(ctx -> setZombieValue(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "entity_id"),
                                                IntegerArgumentType.getInteger(ctx, "points"))))))

                // Wildcard helper: /zk setmodvalue <modid> <points> applies "modid:*"
                // Avoids Brigadier rejecting '*' in unquoted single-word args.
                .then(Commands.literal("setmodvalue")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("modid", StringArgumentType.word())
                                .then(Commands.argument("points", IntegerArgumentType.integer(0, 100000))
                                        .executes(ctx -> setModWildcardValue(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "modid"),
                                                IntegerArgumentType.getInteger(ctx, "points"))))))

                .then(Commands.literal("removevalue")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("entity_id", StringArgumentType.string())
                                .executes(ctx -> removeZombieValue(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "entity_id")))))

                .then(Commands.literal("listvalues")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> listZombieValues(ctx.getSource())))

                .then(Commands.literal("reloadvalues")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> reloadZombieValues(ctx.getSource())))

                .then(Commands.literal("resetvalues")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> resetZombieValuesToDefaults(ctx.getSource()))))

                // ─── Admin: Boss Bar Zone ──────────────────────
                .then(Commands.literal("setcenter")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 10000))
                                .executes(ctx -> setCenter(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "radius")))))

                .then(Commands.literal("removecenter")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> removeCenter(ctx.getSource())))

                // ─── Admin: Reset / Info ───────────────────────
                .then(Commands.literal("reset")
                        .requires(src -> src.hasPermission(3))
                        .then(Commands.literal("confirm")
                                .executes(ctx -> resetScores(ctx.getSource()))))

                .then(Commands.literal("info")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> showInfo(ctx.getSource())))
        );
    }

    // ═══════════════════════════════════════════════════════════
    //  PLAYER COMMANDS
    // ═══════════════════════════════════════════════════════════

    private static int showHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== Purger le monde ===").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("/zk score").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Voir votre score").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("/zk top").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Classement des chasseurs").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("/zk bossbar").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Afficher/masquer la barre").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Tuez des zombies pour gagner des points !").withStyle(ChatFormatting.DARK_GRAY), false);
        source.sendSuccess(() -> Component.literal("1 / 15 / 50 / 100 / 1000 selon la dangerosite").withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int showScore(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ZombieScoreManager manager = ZombieScoreManager.get(player.server);
            long playerScore = manager.getPlayerScore(player.getUUID());
            long globalScore = manager.getGlobalScore();
            long goal = manager.getGoal();
            float progress = goal > 0 ? Math.min((float) globalScore / goal * 100f, 100f) : 100f;

            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("  === Purger le monde ===").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
            source.sendSuccess(() -> Component.literal("  Votre score: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(NF.format(playerScore) + " pts").withStyle(ChatFormatting.WHITE)), false);
            source.sendSuccess(() -> Component.literal("  Score global: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(NF.format(globalScore) + " / " + NF.format(goal)).withStyle(ChatFormatting.WHITE)), false);
            source.sendSuccess(() -> Component.literal("  Progression: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.format("%.1f%%", progress)).withStyle(ChatFormatting.WHITE)), false);
            source.sendSuccess(() -> Component.literal(""), false);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int showTop(CommandSourceStack source) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        List<Map.Entry<UUID, Long>> top = manager.getTopScores(10);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("  === Classement Purger le monde ===").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);

        if (top.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Aucun kill pour le moment.").withStyle(ChatFormatting.GRAY), false);
        } else {
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<UUID, Long> entry = top.get(i);
                int rank = i + 1;
                String playerName = source.getServer().getProfileCache() != null
                        ? source.getServer().getProfileCache().get(entry.getKey())
                        .map(p -> p.getName()).orElse(entry.getKey().toString().substring(0, 8))
                        : entry.getKey().toString().substring(0, 8);

                ChatFormatting rankColor;
                if (rank == 1) rankColor = ChatFormatting.GOLD;
                else if (rank == 2) rankColor = ChatFormatting.GRAY;
                else if (rank == 3) rankColor = ChatFormatting.RED;
                else rankColor = ChatFormatting.DARK_GRAY;

                String scoreStr = NF.format(entry.getValue());
                source.sendSuccess(() -> Component.literal("  " + rank + ". ").withStyle(rankColor, ChatFormatting.BOLD)
                        .append(Component.literal(playerName).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" - " + scoreStr + " pts").withStyle(ChatFormatting.YELLOW)), false);
            }
        }

        long globalScore = manager.getGlobalScore();
        long goal = manager.getGoal();
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("  Total: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(NF.format(globalScore) + " / " + NF.format(goal)).withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal(""), false);

        return 1;
    }

    private static int toggleBossBar(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ZombieScoreManager manager = ZombieScoreManager.get(player.server);
            boolean nowHidden = manager.toggleBossBar(player.getUUID());

            if (ReCraftCollect.getBossBar() != null) {
                if (nowHidden) ReCraftCollect.getBossBar().removePlayer(player);
                else ReCraftCollect.getBossBar().addPlayer(player);
            }

            source.sendSuccess(() -> Component.literal(nowHidden
                            ? "Barre de progression masquee." : "Barre de progression affichee.")
                    .withStyle(nowHidden ? ChatFormatting.YELLOW : ChatFormatting.GREEN), false);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: GIVE / TAKE
    // ═══════════════════════════════════════════════════════════

    private static int givePoints(CommandSourceStack source, ServerPlayer target, long amount) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        manager.addScore(target.getUUID(), amount);
        ReCraftCollect.updateBossBar(source.getServer());
        ReCraftCollect.checkMilestones(source.getServer());
        ReCraftCollect.refreshLeaderboard(source.getServer());

        String targetName = target.getGameProfile().getName();
        source.sendSuccess(() -> Component.literal("+" + NF.format(amount) + " pts donnes a " + targetName)
                .withStyle(ChatFormatting.GREEN), true);
        target.sendSystemMessage(Component.literal("[Purger le monde] ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .append(Component.literal("Un administrateur vous a attribue " + NF.format(amount) + " pts !")
                        .withStyle(ChatFormatting.GREEN)));
        return 1;
    }

    private static int takePoints(CommandSourceStack source, ServerPlayer target, long amount) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        long current = manager.getPlayerScore(target.getUUID());
        long actualRemoved = Math.min(amount, current);

        if (actualRemoved <= 0) {
            source.sendFailure(Component.literal("Ce joueur n'a aucun point a retirer."));
            return 0;
        }

        manager.removeScore(target.getUUID(), actualRemoved);
        ReCraftCollect.updateBossBar(source.getServer());
        ReCraftCollect.refreshLeaderboard(source.getServer());

        String targetName = target.getGameProfile().getName();
        source.sendSuccess(() -> Component.literal("-" + NF.format(actualRemoved) + " pts retires a " + targetName)
                .withStyle(ChatFormatting.RED), true);
        target.sendSystemMessage(Component.literal("[Purger le monde] ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .append(Component.literal("Un administrateur vous a retire " + NF.format(actualRemoved) + " pts.")
                        .withStyle(ChatFormatting.RED)));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: GOAL
    // ═══════════════════════════════════════════════════════════

    private static int setGoal(CommandSourceStack source, long amount) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        manager.setGoal(amount);
        ReCraftCollect.updateBossBar(source.getServer());
        ReCraftCollect.refreshLeaderboard(source.getServer());
        source.sendSuccess(() -> Component.literal("Objectif modifie: " + NF.format(amount) + " pts")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: MILESTONES
    // ═══════════════════════════════════════════════════════════

    private static int addMilestone(CommandSourceStack source, long threshold, String message) {
        MilestoneConfig.addMilestone(threshold, message);
        source.sendSuccess(() -> Component.literal("Palier " + NF.format(threshold) + " defini: \"" + message + "\"")
                .withStyle(ChatFormatting.GREEN), true);
        source.sendSuccess(() -> Component.literal("Sauvegarde dans config/recraftcollect-milestones.json")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int removeMilestone(CommandSourceStack source, long threshold) {
        if (MilestoneConfig.removeMilestone(threshold)) {
            ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
            manager.clearMilestoneReached(threshold);
            source.sendSuccess(() -> Component.literal("Palier " + NF.format(threshold) + " supprime.")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else {
            source.sendFailure(Component.literal("Aucun palier a " + NF.format(threshold) + "."));
        }
        return 1;
    }

    private static int addMilestoneCmd(CommandSourceStack source, long threshold, String command) {
        if (MilestoneConfig.getMilestone(threshold) == null) {
            source.sendFailure(Component.literal("Aucun palier a " + NF.format(threshold) + ". Creez-le d'abord avec /zk milestone add."));
            return 0;
        }
        MilestoneConfig.addCommand(threshold, command);
        MilestoneConfig.MilestoneEntry entry = MilestoneConfig.getMilestone(threshold);
        int index = entry.commands.size() - 1;

        source.sendSuccess(() -> Component.literal("Commande ajoutee au palier " + NF.format(threshold) + " [#" + index + "]: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(command).withStyle(ChatFormatting.WHITE)), true);
        return 1;
    }

    private static int removeMilestoneCmd(CommandSourceStack source, long threshold, int index) {
        MilestoneConfig.MilestoneEntry entry = MilestoneConfig.getMilestone(threshold);
        if (entry == null) {
            source.sendFailure(Component.literal("Aucun palier a " + NF.format(threshold) + "."));
            return 0;
        }
        if (index < 0 || index >= entry.commands.size()) {
            source.sendFailure(Component.literal("Index invalide. Ce palier a " + entry.commands.size() + " commande(s) (indices 0-" + (entry.commands.size() - 1) + ")."));
            return 0;
        }

        String removed = entry.commands.get(index);
        MilestoneConfig.removeCommand(threshold, index);
        source.sendSuccess(() -> Component.literal("Commande #" + index + " supprimee du palier " + NF.format(threshold) + ": ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(removed).withStyle(ChatFormatting.GRAY)), true);
        return 1;
    }

    private static int listMilestones(CommandSourceStack source) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        List<MilestoneConfig.MilestoneEntry> entries = MilestoneConfig.getMilestones();

        source.sendSuccess(() -> Component.literal("=== Paliers Purger le monde ===").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("Fichier: config/recraftcollect-milestones.json").withStyle(ChatFormatting.DARK_GRAY), false);
        source.sendSuccess(() -> Component.literal(""), false);

        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Aucun palier defini.").withStyle(ChatFormatting.GRAY), false);
        } else {
            for (MilestoneConfig.MilestoneEntry entry : entries) {
                boolean reached = manager.isMilestoneReached(entry.threshold);
                String label = NF.format(entry.threshold);

                source.sendSuccess(() -> Component.literal("  " + (reached ? "[X] " : "[ ] ") + label)
                        .withStyle(reached ? ChatFormatting.GREEN : ChatFormatting.WHITE)
                        .append(Component.literal(" - " + entry.message).withStyle(ChatFormatting.GRAY)), false);

                if (!entry.commands.isEmpty()) {
                    for (int i = 0; i < entry.commands.size(); i++) {
                        int idx = i;
                        source.sendSuccess(() -> Component.literal("      #" + idx + " > ")
                                .withStyle(ChatFormatting.DARK_GRAY)
                                .append(Component.literal(entry.commands.get(idx))
                                        .withStyle(ChatFormatting.AQUA)), false);
                    }
                }
            }
        }
        return 1;
    }

    private static int reloadMilestones(CommandSourceStack source) {
        MilestoneConfig.load();
        int count = MilestoneConfig.getMilestones().size();
        source.sendSuccess(() -> Component.literal("Config paliers rechargee: " + count + " palier(s).")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int resetMilestonesReached(CommandSourceStack source) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        manager.resetAllMilestonesReached();
        source.sendSuccess(() -> Component.literal("Statut des paliers atteints reinitialise. Ils pourront se redeclencher.")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: LEADERBOARD (armor stands)
    // ═══════════════════════════════════════════════════════════

    private static int setLeaderboard(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.literal("Regardez un bloc pour placer le leaderboard !"));
                return 0;
            }

            BlockPos pos = ((BlockHitResult) hit).getBlockPos().above();
            ZombieScoreManager manager = ZombieScoreManager.get(player.server);
            manager.setLeaderboardPos(pos, player.level().dimension());

            LeaderboardDisplay.createOrUpdate(player.server, pos, player.level().dimension());

            source.sendSuccess(() -> Component.literal("Leaderboard place en [" +
                            pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                    .withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int removeLeaderboard(CommandSourceStack source) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        LeaderboardDisplay.removeFromServer(source.getServer());
        manager.removeLeaderboardPos();
        source.sendSuccess(() -> Component.literal("Leaderboard supprime.").withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    private static int refreshLeaderboard(CommandSourceStack source) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());
        BlockPos pos = manager.getLeaderboardPos();
        if (pos == null) {
            source.sendFailure(Component.literal("Aucun leaderboard defini. Utilisez /zk leaderboard set."));
            return 0;
        }
        LeaderboardDisplay.createOrUpdate(source.getServer(), pos, manager.getLeaderboardDimension());
        source.sendSuccess(() -> Component.literal("Leaderboard actualise.").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: ZOMBIE VALUES
    // ═══════════════════════════════════════════════════════════

    private static int setZombieValue(CommandSourceStack source, String entityId, int points) {
        ZombieValueConfig.setValue(entityId, points);
        source.sendSuccess(() -> Component.literal("Valeur de " + entityId + " = " + points + " pts")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int removeZombieValue(CommandSourceStack source, String entityId) {
        if (ZombieValueConfig.removeValue(entityId)) {
            source.sendSuccess(() -> Component.literal("Entree " + entityId + " supprimee.")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else {
            source.sendFailure(Component.literal("Aucune entree pour " + entityId + "."));
        }
        return 1;
    }

    private static int listZombieValues(CommandSourceStack source) {
        Map<String, Integer> all = ZombieValueConfig.getAll();

        source.sendSuccess(() -> Component.literal("=== Valeurs des zombies (" + all.size() + ") ===").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("Fichier: config/recraftcollect-zombievalues.json").withStyle(ChatFormatting.DARK_GRAY), false);

        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Aucune valeur definie.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        // Group by point tier for readability
        all.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(entry -> {
                    ChatFormatting color;
                    int v = entry.getValue();
                    if (v >= 1000) color = ChatFormatting.LIGHT_PURPLE;
                    else if (v >= 100) color = ChatFormatting.GOLD;
                    else if (v >= 50) color = ChatFormatting.RED;
                    else if (v >= 15) color = ChatFormatting.YELLOW;
                    else color = ChatFormatting.GREEN;

                    source.sendSuccess(() -> Component.literal("  " + entry.getKey()).withStyle(ChatFormatting.WHITE)
                            .append(Component.literal(" = " + v + " pts").withStyle(color)), false);
                });
        return 1;
    }

    private static int reloadZombieValues(CommandSourceStack source) {
        ZombieValueConfig.load();
        int count = ZombieValueConfig.getAll().size();
        source.sendSuccess(() -> Component.literal("Config valeurs rechargee: " + count + " entree(s).")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int setModWildcardValue(CommandSourceStack source, String modid, int points) {
        String key = modid + ":*";
        ZombieValueConfig.setValue(key, points);
        source.sendSuccess(() -> Component.literal("Wildcard " + key + " = " + points + " pts (toutes les entites du mod " + modid + ")")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int resetZombieValuesToDefaults(CommandSourceStack source) {
        ZombieValueConfig.resetToDefaults();
        int count = ZombieValueConfig.getAll().size();
        source.sendSuccess(() -> Component.literal("Valeurs zombies reinitialisees aux defauts v2.0.0: " + count + " entree(s).")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), true);
        source.sendSuccess(() -> Component.literal("Modifications utilisateur ecrasees. Rechargez les mods cibles si necessaire.")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int resetMilestoneDefaults(CommandSourceStack source) {
        MilestoneConfig.resetToDefaults();
        int count = MilestoneConfig.getMilestones().size();
        source.sendSuccess(() -> Component.literal("Paliers reinitialises aux defauts v2.0.0: " + count + " palier(s).")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), true);
        source.sendSuccess(() -> Component.literal("Les messages des paliers utilisent maintenant le theme \"Purger le monde\".")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: BOSS BAR ZONE
    // ═══════════════════════════════════════════════════════════

    private static int setCenter(CommandSourceStack source, int radius) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.literal("Regardez un bloc pour definir le centre de la boss bar !"));
                return 0;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            ZombieScoreManager.get(player.server).setBossBarZone(pos, player.level().dimension(), radius);
            source.sendSuccess(() -> Component.literal("Zone boss bar: [" +
                            pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] rayon " + radius)
                    .withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int removeCenter(CommandSourceStack source) {
        ZombieScoreManager.get(source.getServer()).removeBossBarZone();
        source.sendSuccess(() -> Component.literal("Zone boss bar supprimee (visible partout).").withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: RESET / INFO
    // ═══════════════════════════════════════════════════════════

    private static int resetScores(CommandSourceStack source) {
        ZombieScoreManager.get(source.getServer()).resetAllScores();
        ReCraftCollect.updateBossBar(source.getServer());
        ReCraftCollect.refreshLeaderboard(source.getServer());
        source.sendSuccess(() -> Component.literal("Tous les scores ont ete reinitialises !")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("[Purger le monde] ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(Component.literal("Les scores ont ete reinitialises par un administrateur.")
                            .withStyle(ChatFormatting.RED)));
        }
        return 1;
    }

    private static int showInfo(CommandSourceStack source) {
        ZombieScoreManager manager = ZombieScoreManager.get(source.getServer());

        source.sendSuccess(() -> Component.literal("=== Info Purger le monde ===").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);

        long goal = manager.getGoal();
        source.sendSuccess(() -> Component.literal("Objectif: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(NF.format(goal) + " pts").withStyle(ChatFormatting.WHITE)), false);

        BlockPos center = manager.getBossBarCenter();
        if (center == null || manager.getBossBarRadius() <= 0) {
            source.sendSuccess(() -> Component.literal("Zone boss bar: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("partout").withStyle(ChatFormatting.WHITE)), false);
        } else {
            int radius = manager.getBossBarRadius();
            source.sendSuccess(() -> Component.literal("Zone boss bar: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("[" + center.getX() + ", " + center.getY() + ", " + center.getZ() + "] rayon " + radius)
                            .withStyle(ChatFormatting.GREEN)), false);
        }

        long globalScore = manager.getGlobalScore();
        float progress = goal > 0 ? Math.min((float) globalScore / goal * 100f, 100f) : 100f;
        source.sendSuccess(() -> Component.literal("Score global: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(NF.format(globalScore) + " / " + NF.format(goal) + " (" + String.format("%.1f%%", progress) + ")")
                        .withStyle(ChatFormatting.WHITE)), false);

        source.sendSuccess(() -> Component.literal("Joueurs: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(String.valueOf(manager.getTopScores(Integer.MAX_VALUE).size())).withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("Valeurs zombies: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(ZombieValueConfig.getAll().size() + " entrees").withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("Paliers: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(MilestoneConfig.getMilestones().size() + " (config/recraftcollect-milestones.json)")
                        .withStyle(ChatFormatting.WHITE)), false);

        BlockPos lbPos = manager.getLeaderboardPos();
        if (lbPos == null) {
            source.sendSuccess(() -> Component.literal("Leaderboard: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("non defini").withStyle(ChatFormatting.RED)), false);
        } else {
            source.sendSuccess(() -> Component.literal("Leaderboard: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("[" + lbPos.getX() + ", " + lbPos.getY() + ", " + lbPos.getZ() + "]")
                            .withStyle(ChatFormatting.GREEN)), false);
        }

        return 1;
    }
}
