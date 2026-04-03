package com.vyrriox.recraftcollect.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vyrriox.recraftcollect.ReCraftCollect;
import com.vyrriox.recraftcollect.config.MilestoneConfig;
import com.vyrriox.recraftcollect.data.FoodScoreManager;
import com.vyrriox.recraftcollect.data.FoodUnitCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FoodScoreCommand {

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.FRANCE);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fc")
                .executes(ctx -> showHelp(ctx.getSource()))

                // ─── Player commands ───────────────────────────
                .then(Commands.literal("score")
                        .executes(ctx -> showScore(ctx.getSource())))

                .then(Commands.literal("top")
                        .executes(ctx -> showTop(ctx.getSource())))

                .then(Commands.literal("deposit")
                        .executes(ctx -> depositHeld(ctx.getSource())))

                .then(Commands.literal("depositall")
                        .executes(ctx -> depositAll(ctx.getSource())))

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
                                .executes(ctx -> reloadMilestones(ctx.getSource()))))

                // ─── Admin: Item Values ────────────────────────
                .then(Commands.literal("setvalue")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 10000))
                                .executes(ctx -> setItemValue(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "value")))))

                .then(Commands.literal("removevalue")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> removeItemValue(ctx.getSource())))

                .then(Commands.literal("listvalues")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> listItemValues(ctx.getSource())))

                // ─── Admin: Collection Point ───────────────────
                .then(Commands.literal("setpoint")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> setPoint(ctx.getSource())))

                .then(Commands.literal("removepoint")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> removePoint(ctx.getSource())))

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
        source.sendSuccess(() -> Component.literal("=== ReCraft Collect ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("/fc score").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Voir votre score").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("/fc top").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Classement des joueurs").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("/fc deposit").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Deposer la nourriture en main").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("/fc depositall").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Deposer toute la nourriture").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal("/fc bossbar").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - Afficher/masquer la barre").withStyle(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Clic droit sur le collecteur pour deposer !").withStyle(ChatFormatting.DARK_GRAY), false);
        source.sendSuccess(() -> Component.literal("Sneak + clic droit = deposer tout l'inventaire").withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int showScore(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            FoodScoreManager manager = FoodScoreManager.get(player.server);
            long playerScore = manager.getPlayerScore(player.getUUID());
            long globalScore = manager.getGlobalScore();
            long goal = manager.getGoal();
            float progress = goal > 0 ? Math.min((float) globalScore / goal * 100f, 100f) : 100f;

            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("  === ReCraft Collect ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            source.sendSuccess(() -> Component.literal("  Votre score: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(NF.format(playerScore) + " unites").withStyle(ChatFormatting.WHITE)), false);
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
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());
        List<Map.Entry<UUID, Long>> top = manager.getTopScores(10);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("  === Classement ReCraft Collect ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        if (top.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Aucun score pour le moment.").withStyle(ChatFormatting.GRAY), false);
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
                        .append(Component.literal(" - " + scoreStr + " unites").withStyle(ChatFormatting.YELLOW)), false);
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

    private static int depositHeld(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            FoodScoreManager manager = FoodScoreManager.get(player.server);
            Map<String, Integer> overrides = manager.getItemValueOverrides();
            ItemStack held = player.getMainHandItem();

            if (!FoodUnitCalculator.isDepositable(held, overrides)) {
                source.sendFailure(Component.literal("Vous devez tenir de la nourriture en main !"));
                return 0;
            }

            long units = FoodUnitCalculator.getFoodUnits(held, overrides);
            held.setCount(0);
            manager.addScore(player.getUUID(), units);
            ReCraftCollect.updateBossBar(player.server);
            ReCraftCollect.checkMilestones(player.server);

            source.sendSuccess(() -> Component.literal("+" + NF.format(units) + " unites deposees !")
                    .withStyle(ChatFormatting.GREEN), false);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int depositAll(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            FoodScoreManager manager = FoodScoreManager.get(player.server);
            Map<String, Integer> overrides = manager.getItemValueOverrides();
            long totalUnits = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && FoodUnitCalculator.isDepositable(stack, overrides)) {
                    totalUnits += FoodUnitCalculator.getFoodUnits(stack, overrides);
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }

            if (totalUnits == 0) {
                source.sendFailure(Component.literal("Aucune nourriture dans votre inventaire !"));
                return 0;
            }

            manager.addScore(player.getUUID(), totalUnits);
            ReCraftCollect.updateBossBar(player.server);
            ReCraftCollect.checkMilestones(player.server);

            long finalUnits = totalUnits;
            source.sendSuccess(() -> Component.literal("+" + NF.format(finalUnits) + " unites deposees !")
                    .withStyle(ChatFormatting.GREEN), false);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int toggleBossBar(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            FoodScoreManager manager = FoodScoreManager.get(player.server);
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
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());
        manager.addScore(target.getUUID(), amount);
        ReCraftCollect.updateBossBar(source.getServer());
        ReCraftCollect.checkMilestones(source.getServer());

        String targetName = target.getGameProfile().getName();
        source.sendSuccess(() -> Component.literal("+" + NF.format(amount) + " unites donnees a " + targetName)
                .withStyle(ChatFormatting.GREEN), true);
        target.sendSystemMessage(Component.literal("[ReCraft Collect] ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                .append(Component.literal("Un administrateur vous a attribue " + NF.format(amount) + " unites !")
                        .withStyle(ChatFormatting.GREEN)));
        return 1;
    }

    private static int takePoints(CommandSourceStack source, ServerPlayer target, long amount) {
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());
        long current = manager.getPlayerScore(target.getUUID());
        long actualRemoved = Math.min(amount, current);

        if (actualRemoved <= 0) {
            source.sendFailure(Component.literal("Ce joueur n'a aucun point a retirer."));
            return 0;
        }

        manager.removeScore(target.getUUID(), actualRemoved);
        ReCraftCollect.updateBossBar(source.getServer());

        String targetName = target.getGameProfile().getName();
        source.sendSuccess(() -> Component.literal("-" + NF.format(actualRemoved) + " unites retirees a " + targetName)
                .withStyle(ChatFormatting.RED), true);
        target.sendSystemMessage(Component.literal("[ReCraft Collect] ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                .append(Component.literal("Un administrateur vous a retire " + NF.format(actualRemoved) + " unites.")
                        .withStyle(ChatFormatting.RED)));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: GOAL
    // ═══════════════════════════════════════════════════════════

    private static int setGoal(CommandSourceStack source, long amount) {
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());
        manager.setGoal(amount);
        ReCraftCollect.updateBossBar(source.getServer());
        source.sendSuccess(() -> Component.literal("Objectif modifie: " + NF.format(amount) + " unites")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: MILESTONES (config file based)
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
            // Also clear reached status
            FoodScoreManager manager = FoodScoreManager.get(source.getServer());
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
            source.sendFailure(Component.literal("Aucun palier a " + NF.format(threshold) + ". Creez-le d'abord avec /fc milestone add."));
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
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());
        List<MilestoneConfig.MilestoneEntry> entries = MilestoneConfig.getMilestones();

        source.sendSuccess(() -> Component.literal("=== Paliers ReCraft Collect ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
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

                // Show commands
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
        source.sendSuccess(() -> Component.literal("Config paliers rechargee: " + count + " palier(s) charges.")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: ITEM VALUES
    // ═══════════════════════════════════════════════════════════

    private static int setItemValue(CommandSourceStack source, int value) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) {
                source.sendFailure(Component.literal("Vous devez tenir un objet en main !"));
                return 0;
            }

            String itemId = ForgeRegistries.ITEMS.getKey(held.getItem()).toString();
            FoodScoreManager manager = FoodScoreManager.get(player.server);
            manager.setItemValue(itemId, value);

            String itemName = held.getHoverName().getString();
            source.sendSuccess(() -> Component.literal("Valeur de " + itemName + " (" + itemId + ") = " + value + " unites")
                    .withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int removeItemValue(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty()) {
                source.sendFailure(Component.literal("Vous devez tenir un objet en main !"));
                return 0;
            }

            String itemId = ForgeRegistries.ITEMS.getKey(held.getItem()).toString();
            FoodScoreManager manager = FoodScoreManager.get(player.server);

            if (manager.removeItemValue(itemId)) {
                String itemName = held.getHoverName().getString();
                source.sendSuccess(() -> Component.literal("Valeur personnalisee de " + itemName + " supprimee.")
                        .withStyle(ChatFormatting.YELLOW), true);
            } else {
                source.sendFailure(Component.literal("Cet objet n'a pas de valeur personnalisee."));
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int listItemValues(CommandSourceStack source) {
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());
        Map<String, Integer> overrides = manager.getItemValueOverrides();

        source.sendSuccess(() -> Component.literal("=== Valeurs personnalisees ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        if (overrides.isEmpty()) {
            source.sendSuccess(() -> Component.literal("  Aucune valeur personnalisee.").withStyle(ChatFormatting.GRAY), false);
        } else {
            for (Map.Entry<String, Integer> entry : overrides.entrySet()) {
                String itemId = entry.getKey();
                int val = entry.getValue();
                source.sendSuccess(() -> Component.literal("  " + itemId).withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(" = " + val + " unites").withStyle(ChatFormatting.YELLOW)), false);
            }
        }
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: COLLECTION POINT / BOSS BAR ZONE
    // ═══════════════════════════════════════════════════════════

    private static int setPoint(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.literal("Regardez un bloc pour definir le point de collecte !"));
                return 0;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            FoodScoreManager manager = FoodScoreManager.get(player.server);
            manager.setCollectionPoint(pos, player.level().dimension());
            source.sendSuccess(() -> Component.literal("Point de collecte defini en [" +
                            pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                    .withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int removePoint(CommandSourceStack source) {
        FoodScoreManager.get(source.getServer()).removeCollectionPoint();
        source.sendSuccess(() -> Component.literal("Point de collecte supprime.").withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    private static int setCenter(CommandSourceStack source, int radius) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.literal("Regardez un bloc pour definir le centre de la boss bar !"));
                return 0;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            FoodScoreManager.get(player.server).setBossBarZone(pos, player.level().dimension(), radius);
            source.sendSuccess(() -> Component.literal("Zone boss bar: [" +
                            pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] rayon " + radius)
                    .withStyle(ChatFormatting.GREEN), true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Cette commande necessite un joueur."));
        }
        return 1;
    }

    private static int removeCenter(CommandSourceStack source) {
        FoodScoreManager.get(source.getServer()).removeBossBarZone();
        source.sendSuccess(() -> Component.literal("Zone boss bar supprimee (visible partout).").withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN: RESET / INFO
    // ═══════════════════════════════════════════════════════════

    private static int resetScores(CommandSourceStack source) {
        FoodScoreManager.get(source.getServer()).resetAllScores();
        ReCraftCollect.updateBossBar(source.getServer());
        source.sendSuccess(() -> Component.literal("Tous les scores ont ete reinitialises !")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("[ReCraft Collect] ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                    .append(Component.literal("Les scores ont ete reinitialises par un administrateur.")
                            .withStyle(ChatFormatting.RED)));
        }
        return 1;
    }

    private static int showInfo(CommandSourceStack source) {
        FoodScoreManager manager = FoodScoreManager.get(source.getServer());

        source.sendSuccess(() -> Component.literal("=== Info ReCraft Collect ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        long goal = manager.getGoal();
        source.sendSuccess(() -> Component.literal("Objectif: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(NF.format(goal) + " unites").withStyle(ChatFormatting.WHITE)), false);

        BlockPos pos = manager.getCollectionPoint();
        if (pos == null) {
            source.sendSuccess(() -> Component.literal("Point de collecte: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("non defini").withStyle(ChatFormatting.RED)), false);
        } else {
            source.sendSuccess(() -> Component.literal("Point de collecte: ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                            .withStyle(ChatFormatting.GREEN)), false);
        }

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
        source.sendSuccess(() -> Component.literal("Valeurs custom: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(manager.getItemValueOverrides().size() + " items").withStyle(ChatFormatting.WHITE)), false);
        source.sendSuccess(() -> Component.literal("Paliers: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(MilestoneConfig.getMilestones().size() + " (config/recraftcollect-milestones.json)")
                        .withStyle(ChatFormatting.WHITE)), false);

        return 1;
    }
}
