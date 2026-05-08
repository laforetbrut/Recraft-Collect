# ReCraft Collect — Zombie Purge

![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red) ![Version](https://img.shields.io/badge/version-2.0.0-blue) ![Forge](https://img.shields.io/badge/Forge-1.20.1--47.2.0-orange)

**[EN]** Server-side community zombie-kill scoring mod for Minecraft Forge 1.20.1.
**[FR]** Mod server-side de comptage communautaire de kills de zombies pour Minecraft Forge 1.20.1.

---

## Features

- **Tiered zombie kill scoring** — Vanilla and modded zombies grant 1, 15, 50, 100 or 1000 points based on tier.
- **"Purger le monde" boss bar** — Real-time global progress bar with dynamic color (Red → Purple → Yellow → Green) toward 1,000,000 points.
- **In-game armor-stand leaderboard** — Visual top-10 placed in the world.
- **Configurable milestones** — JSON-defined thresholds with custom messages and per-milestone server commands.
- **Direct-kill enforcement** — Only the player who lands the killing blow earns points. No pet farming, no environment kills.
- **Hot-reloadable point config** — `config/recraftcollect-zombievalues.json` supports per-entity values and `modid:*` wildcards.
- **100% server-side** — Players don't need to install anything on their client.

## How It Works

When a player kills a zombie (vanilla or from a supported mod), the mod looks up the entity's registry id in `recraftcollect-zombievalues.json` and awards the matching points to the killer. Resolution order: exact id → `modid:*` wildcard → 0 (not counted). The community works together to reach the season's goal (default: 1,000,000 points).

### Point Tiers

| Tier | Points | Examples |
|------|--------|----------|
| Normal | **1** | Vanilla zombies, husks, drowned, zombie villagers, all Spawn Eggs zombies, all Apocalypse Now mobs, basic Zombie Extreme infected |
| Superior 1 | **15** | Runner, Infected Police, The Bomber, The Wolf, The Wheezer, Sucker… |
| Superior 2 | **50** | Boomer, Chainsaw, Clicker, Inflated, Spitter, Royal, Hunter, Rabidus… |
| Superior 3 | **100** | Juggernaut, Ram, Night Hunter, Faceless, Pregnant, Gliter, Bigsucker, Lechery, Posessive, Clogger… |
| Boss | **1000** | The Heavy, Demolisher, Rat King, Zero Patient, The Lurker |

The full default mapping ships in `config/recraftcollect-zombievalues.json`. Modify it in-game with `/zk setvalue <entity_id> <points>` or edit the file directly and reload with `/zk reloadvalues`.

### Anti-Farm

- Pets / wolves / iron golems do not award points.
- Environmental deaths (lava, fall, suffocation) do not award points.
- Only `event.getSource().getEntity() instanceof ServerPlayer` triggers a payout.

## Installation

1. Install **Minecraft Forge 1.20.1-47.2.0** on your server.
2. Drop `recraftcollect-2.0.0.jar` in the `mods/` folder.
3. Start the server. `config/recraftcollect-milestones.json` and `config/recraftcollect-zombievalues.json` are generated automatically on first launch.
4. *(Optional)* Adjust thresholds, messages, or per-mod entity ids in those JSON files. Reload in-game with `/zk milestone reload` and `/zk reloadvalues`.

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/zk` | Show help |
| `/zk score` | Show your personal score, global score and progress |
| `/zk top` | Show top-10 hunters |
| `/zk bossbar` | Toggle boss bar visibility (per player) |

### Admin Commands *(OP 2+)*

| Command | Description |
|---------|-------------|
| `/zk setgoal <amount>` | Set the global goal (default 1,000,000) |
| `/zk give <player> <amount>` | Award points to a player |
| `/zk take <player> <amount>` | Remove points from a player |
| `/zk setvalue <entity_id> <points>` | Set or update an entity's point value (e.g. `/zk setvalue minecraft:zombie 1`). For wildcards containing `*`, see `/zk setmodvalue`. |
| `/zk setmodvalue <modid> <points>` | Apply a wildcard value to **every** entity of a mod (writes `<modid>:*`). Avoids the Brigadier limitation that rejects unquoted `*`. Example: `/zk setmodvalue apocalypsenow 1`. |
| `/zk removevalue <entity_id>` | Remove an entity entry from the config |
| `/zk listvalues` | List all configured entity values, sorted by tier |
| `/zk reloadvalues` | Reload `recraftcollect-zombievalues.json` from disk |
| `/zk listentities <modid>` | **Diagnostic.** Dump every entity id registered for a mod (e.g. `/zk listentities undead_revamp2`). Shows current point value next to each id. Use this to find the real ids when defaults are wrong. |
| `/zk identify` | **Diagnostic.** Identify the entity in your line of sight (32 blocks). Shows its registry id and current point value. The fastest way to find a mob's real id in-world. |
| `/zk resetvalues confirm` | **(OP 3)** Force-regenerate the default v2.0.0 entity-value mapping, overwriting user changes. |
| `/zk milestone resetdefaults confirm` | **(OP 3)** Force-regenerate the default v2.0.0 milestone messages, overwriting user changes (use this if upgrading from v1.x and milestones still show food-themed text). |
| `/zk milestone add <threshold> <message>` | Add or update a milestone |
| `/zk milestone remove <threshold>` | Remove a milestone |
| `/zk milestone addcmd <threshold> <command>` | Add a server command triggered when the milestone is reached |
| `/zk milestone removecmd <threshold> <index>` | Remove a milestone command by index |
| `/zk milestone list` | List all milestones with their commands |
| `/zk milestone reload` | Reload `recraftcollect-milestones.json` from disk |
| `/zk milestone resetreached` | Mark all milestones as not-reached so they can re-trigger |
| `/zk leaderboard set` | Place an armor-stand top-10 above the looked-at block |
| `/zk leaderboard remove` | Remove the armor-stand leaderboard |
| `/zk leaderboard refresh` | Force a leaderboard refresh |
| `/zk leaderboard show` | Display the leaderboard in chat |
| `/zk setcenter <radius>` | Restrict boss bar visibility to a radius around the looked-at block |
| `/zk removecenter` | Make the boss bar visible everywhere |
| `/zk info` | Show detailed mod info |
| `/zk reset confirm` | **(OP 3, irreversible)** Reset every score and reached milestone |

## Credits

- **Author**: vyrriox
- **License**: All Rights Reserved
- **Forge**: [Minecraft Forge](https://files.minecraftforge.net/) 1.20.1-47.2.0
- **Compatible with**: Zombie Extreme, Undead Revamp 2, Apocalypse Now, Spawn Eggs, and any zombie-adding mod (add the entity ids manually)

---

# ReCraft Collect — Zombie Purge (Version Française)

Mod server-side de comptage communautaire de kills de zombies pour Minecraft Forge 1.20.1. Tuez des zombies pour faire monter le compteur global "Purger le monde" jusqu'à 1 000 000 de points.

## Caractéristiques

- **Bareme par catégorie** — Les zombies vanilla et modded rapportent 1, 15, 50, 100 ou 1000 points selon leur dangerosité.
- **Boss bar "Purger le monde"** — Barre de progression globale en temps réel avec couleur dynamique (Rouge → Violet → Jaune → Vert).
- **Leaderboard armor-stand en jeu** — Top 10 visuel placé dans le monde.
- **Paliers configurables** — Seuils définis en JSON avec messages personnalisés et commandes serveur exécutées au franchissement.
- **Kill direct uniquement** — Seul le joueur qui porte le coup fatal gagne les points. Pas de farm via pets ni via l'environnement.
- **Config rechargeable à chaud** — `config/recraftcollect-zombievalues.json` supporte les valeurs par entité et les jokers `modid:*`.
- **100% server-side** — Aucune installation côté client.

## Fonctionnement

Quand un joueur tue un zombie (vanilla ou d'un mod supporté), le mod cherche l'identifiant de l'entité dans `recraftcollect-zombievalues.json` et attribue les points correspondants au tueur. Ordre de résolution : id exact → joker `modid:*` → 0 (non comptabilisé). La communauté coopère pour atteindre l'objectif de la saison (défaut : 1 000 000 points).

### Catégories de points

| Catégorie | Points | Exemples |
|-----------|--------|----------|
| Normal | **1** | Zombies vanilla, husks, drowned, zombie villagers, tous les zombies Spawn Eggs, tous les mobs Apocalypse Now, infected basiques de Zombie Extreme |
| Supérieur 1 | **15** | Runner, Infected Police, The Bomber, The Wolf, The Wheezer, Sucker… |
| Supérieur 2 | **50** | Boomer, Chainsaw, Clicker, Inflated, Spitter, Royal, Hunter, Rabidus… |
| Supérieur 3 | **100** | Juggernaut, Ram, Night Hunter, Faceless, Pregnant, Gliter, Bigsucker, Lechery, Posessive, Clogger… |
| Boss | **1000** | The Heavy, Demolisher, Rat King, Zero Patient, The Lurker |

Le mapping complet par défaut est livré dans `config/recraftcollect-zombievalues.json`. Modifiez en jeu avec `/zk setvalue <entity_id> <points>` ou éditez le fichier puis rechargez avec `/zk reloadvalues`.

### Anti-farm

- Pets, loups, golems de fer : ne donnent pas de points.
- Morts environnementales (lave, chute, suffocation) : ne donnent pas de points.
- Seul `event.getSource().getEntity() instanceof ServerPlayer` déclenche une attribution.

## Installation

1. Installez **Minecraft Forge 1.20.1-47.2.0** sur votre serveur.
2. Déposez `recraftcollect-2.0.0.jar` dans le dossier `mods/`.
3. Démarrez le serveur. `config/recraftcollect-milestones.json` et `config/recraftcollect-zombievalues.json` sont générés automatiquement au premier lancement.
4. *(Optionnel)* Ajustez les seuils, messages, ou IDs d'entités par mod dans ces fichiers JSON. Rechargez en jeu avec `/zk milestone reload` et `/zk reloadvalues`.

## Commandes

### Commandes joueurs

| Commande | Description |
|----------|-------------|
| `/zk` | Affiche l'aide |
| `/zk score` | Votre score, score global et progression |
| `/zk top` | Top 10 des chasseurs |
| `/zk bossbar` | Affiche/masque la boss bar (par joueur) |

### Commandes admin *(OP 2+)*

| Commande | Description |
|----------|-------------|
| `/zk setgoal <montant>` | Modifie l'objectif global (défaut 1 000 000) |
| `/zk give <joueur> <montant>` | Attribue des points à un joueur |
| `/zk take <joueur> <montant>` | Retire des points à un joueur |
| `/zk setvalue <entity_id> <points>` | Définit/met à jour la valeur d'une entité (ex: `/zk setvalue minecraft:zombie 1`). Pour les wildcards contenant `*`, utilisez `/zk setmodvalue`. |
| `/zk setmodvalue <modid> <points>` | Applique une valeur wildcard à **toutes** les entités d'un mod (écrit `<modid>:*`). Contourne la limitation Brigadier qui refuse `*` non-quoté. Exemple : `/zk setmodvalue apocalypsenow 1`. |
| `/zk removevalue <entity_id>` | Retire une entrée du config |
| `/zk listvalues` | Liste toutes les valeurs, triées par catégorie |
| `/zk reloadvalues` | Recharge `recraftcollect-zombievalues.json` |
| `/zk listentities <modid>` | **Diagnostic.** Liste toutes les entités enregistrées pour un mod (ex: `/zk listentities undead_revamp2`). Affiche la valeur actuelle de chacune. À utiliser pour trouver les vrais IDs quand les défauts sont faux. |
| `/zk identify` | **Diagnostic.** Identifie l'entité dans votre ligne de mire (32 blocs). Affiche son ID de registre et sa valeur actuelle. La méthode la plus rapide pour trouver le vrai ID d'un mob en jeu. |
| `/zk resetvalues confirm` | **(OP 3)** Force la régénération du mapping par défaut v2.0.0, écrase les modifs utilisateur. |
| `/zk milestone resetdefaults confirm` | **(OP 3)** Force la régénération des messages de paliers par défaut v2.0.0 (utile pour upgrader depuis v1.x si les paliers affichent encore du texte food). |
| `/zk milestone add <seuil> <message>` | Ajoute ou met à jour un palier |
| `/zk milestone remove <seuil>` | Supprime un palier |
| `/zk milestone addcmd <seuil> <commande>` | Ajoute une commande serveur déclenchée au franchissement |
| `/zk milestone removecmd <seuil> <index>` | Retire une commande de palier par index |
| `/zk milestone list` | Liste tous les paliers avec leurs commandes |
| `/zk milestone reload` | Recharge `recraftcollect-milestones.json` |
| `/zk milestone resetreached` | Remet tous les paliers à "non atteint" pour qu'ils se redéclenchent |
| `/zk leaderboard set` | Place un top 10 en armor stands au-dessus du bloc regardé |
| `/zk leaderboard remove` | Supprime le leaderboard armor stand |
| `/zk leaderboard refresh` | Force la mise à jour du leaderboard |
| `/zk leaderboard show` | Affiche le classement en chat |
| `/zk setcenter <rayon>` | Restreint la visibilité de la boss bar à un rayon autour du bloc regardé |
| `/zk removecenter` | Boss bar visible partout |
| `/zk info` | Informations détaillées |
| `/zk reset confirm` | **(OP 3, irréversible)** Réinitialise tous les scores et paliers atteints |

## Credits

- **Author**: vyrriox
- **License**: All Rights Reserved
- **Forge**: [Minecraft Forge](https://files.minecraftforge.net/) 1.20.1-47.2.0
- **Compatible avec**: Zombie Extreme, Undead Revamp 2, Apocalypse Now, Spawn Eggs, et tout mod ajoutant des zombies (ajoutez les IDs manuellement)

### Liens
- **Site**: [Arcadia: Echoes Of Power](https://arcadia-echoes-of-power.fr/)
- **Discord**: [Support](https://discord.gg/xjF8Rtzyd4)
- **Don**: [Stripe](https://buy.stripe.com/3cI3co6X97Vy4IK50QfIs00)
