# Changelog

All notable changes to ReCraft Collect — Zombie Purge are documented here.

---

## [2.0.0] - 2026-05-07

### Changed (BREAKING)

- **Complete pivot from food collection to zombie kill tracking** — The mod no longer counts deposited food. It now counts zombie kills using a tiered point system. Existing player scores from v1.x are preserved (same `recraftcollect_scores` save file) but they no longer represent food units.
- **Command root renamed** — `/fc` → `/zk` (zombie kill).
- **Boss bar objective renamed** — "ReCraft Collect: X / Y unites" → "Purger le monde: X / Y points".
- **Display name updated** — `ReCraft Collect` → `ReCraft Collect - Zombie Purge`.

### Added

- **Tiered zombie kill scoring** — Five point tiers (1 / 15 / 50 / 100 / 1000) covering vanilla zombies and modded zombies (Zombie Extreme, Undead Revamp 2, Apocalypse Now, Spawn Eggs).
- **`config/recraftcollect-zombievalues.json`** — Per-entity point values, hot-reloadable, supports `modid:*` wildcards to apply a default to every entity of a given mod.
- **Direct-kill enforcement** — Only kills where the player is the direct damage source count. Pets, projectiles fired by mobs, and environmental deaths (lava, fall, suffocation) do not award points. Anti-AFK-farm by design.
- **Per-kill feedback** — Action-bar `+N pts (total)` colored by tier (green/yellow/red/gold/purple) with soul particles on the victim. Higher tiers play a confirmation sound.
- **`/zk setvalue <entity_id> <points>`** — Set or update the point value for an entity id (admin).
- **`/zk removevalue <entity_id>`** — Remove an entry from the value config (admin).
- **`/zk listvalues`** — List all configured entity values, sorted by tier (admin).
- **`/zk reloadvalues`** — Reload the values config file without restarting the server (admin).
- **`/zk leaderboard show`** — Display the leaderboard in chat to the caller.

### Removed

- **Food collection point** — No more right-click block deposit. `/fc setpoint`, `/fc removepoint`, `/fc deposit`, `/fc depositall` are gone.
- **Per-item value override** — Replaced by per-entity value config. `/fc setvalue` (item-tied) → `/zk setvalue <entity_id>`.
- **Food unit calculator** — Nutrition-based scoring is no longer relevant.

### Performance

- **Lighter event surface** — Replaced `RightClickBlock` handler (fires on every block interaction) with `LivingDeathEvent` (fires only on entity death). Lower per-tick cost.

---

## [2.0.0] - 2026-05-07

### Modifications (CASSANT)

- **Pivot complet de la collecte de nourriture vers le comptage de kills de zombies** — Le mod ne compte plus les depots de nourriture. Il compte les kills de zombies avec un bareme par categorie. Les scores joueurs de la v1.x sont conserves (meme fichier de sauvegarde `recraftcollect_scores`) mais ne representent plus des unites de nourriture.
- **Commande racine renommee** — `/fc` → `/zk` (zombie kill).
- **Nom de l'objectif boss bar renomme** — "ReCraft Collect: X / Y unites" → "Purger le monde: X / Y points".
- **Nom d'affichage du mod mis a jour** — `ReCraft Collect` → `ReCraft Collect - Zombie Purge`.

### Ajouts

- **Bareme de points par categorie de zombie** — Cinq paliers (1 / 15 / 50 / 100 / 1000) couvrant les zombies vanilla et modded (Zombie Extreme, Undead Revamp 2, Apocalypse Now, Spawn Eggs).
- **`config/recraftcollect-zombievalues.json`** — Valeurs en points par entite, rechargeable a chaud, supporte les jokers `modid:*` pour appliquer une valeur par defaut a toutes les entites d'un mod.
- **Kill direct uniquement** — Seuls les kills ou le joueur est la source directe des degats comptent. Les pets, projectiles tires par des mobs, et morts environnementales (lave, chute, suffocation) ne donnent pas de points. Anti-farm AFK par design.
- **Feedback par kill** — Action-bar `+N pts (total)` colore par categorie (vert/jaune/rouge/or/violet) avec particules d'ame sur la victime. Les categories elevees jouent un son de confirmation.
- **`/zk setvalue <entity_id> <points>`** — Definit ou met a jour la valeur en points d'une entite (admin).
- **`/zk removevalue <entity_id>`** — Retire une entree de la config des valeurs (admin).
- **`/zk listvalues`** — Liste toutes les valeurs d'entites, triees par palier (admin).
- **`/zk reloadvalues`** — Recharge le fichier de config sans redemarrer le serveur (admin).
- **`/zk leaderboard show`** — Affiche le classement en chat a l'appelant.

### Suppressions

- **Point de collecte de nourriture** — Plus de depot par clic droit. `/fc setpoint`, `/fc removepoint`, `/fc deposit`, `/fc depositall` ont disparu.
- **Valeur personnalisee par item tenu** — Remplace par une config par entite. `/fc setvalue` (sur item) → `/zk setvalue <entity_id>`.
- **Calculateur d'unites alimentaires** — Le scoring base sur la nutrition n'est plus pertinent.

### Performance

- **Surface d'evenements allegee** — Remplacement du handler `RightClickBlock` (declenche a chaque interaction de bloc) par `LivingDeathEvent` (declenche uniquement a la mort d'une entite). Cout par tick reduit.

---

## [1.3.0] - 2026-04-16

### Added

- **In-game armor stand leaderboard** — Visual top 10 displayed in the world via `/fc leaderboard set/remove/refresh`.
- **Reset of reached milestones** — `/fc milestone resetreached` and `/fc reset confirm` now also clear reached milestones so they can re-trigger.

### Fixed

- **Milestones not re-triggering after reset** — Bug fixed.

### Ajouts

- **Leaderboard armor stand en jeu** — Top 10 visuel dans le monde via `/fc leaderboard set/remove/refresh`.
- **Reset des paliers atteints** — `/fc milestone resetreached` et `/fc reset confirm` reinitialisent maintenant aussi les paliers atteints.

### Correctifs

- **Paliers non redeclenches apres reset** — Bug corrige.

---

## [1.2.0]

### Added

- **Milestones in config file** — `config/recraftcollect-milestones.json`, hot-reloadable with `/fc milestone reload`.
- **Configurable per-milestone commands** — `/fc milestone addcmd/removecmd`.

### Ajouts

- **Paliers en fichier config** — `config/recraftcollect-milestones.json`, rechargeable avec `/fc milestone reload`.
- **Commandes automatiques par palier** — `/fc milestone addcmd/removecmd`.

---

## [1.1.0]

### Added

- **Configurable goal** — `/fc setgoal <amount>`.
- **Player point management** — `/fc give`, `/fc take`.
- **In-game configurable milestones** — `/fc milestone add/remove/list`.
- **Custom item values** — `/fc setvalue`, `/fc removevalue`, `/fc listvalues`.

### Ajouts

- **Objectif configurable** — `/fc setgoal <montant>`.
- **Gestion des points joueurs** — `/fc give`, `/fc take`.
- **Paliers configurables en jeu** — `/fc milestone add/remove/list`.
- **Valeurs personnalisees d'items** — `/fc setvalue`, `/fc removevalue`, `/fc listvalues`.

---

## [1.0.0]

### Added

- **Food collection point** — Right-click block deposit.
- **Nutrition-based unit calculation**.
- **Player and global scores**.
- **Boss bar with dynamic color**.
- **Default milestones** from 10 000 to 1 000 000.

### Ajouts

- **Point de collecte de nourriture** — Depot par clic droit.
- **Calcul des unites par valeur nutritive**.
- **Scores individuel et global**.
- **Boss bar a couleur dynamique**.
- **Paliers par defaut** de 10 000 a 1 000 000.
