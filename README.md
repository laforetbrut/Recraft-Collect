# ReCraft Collect

![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red) ![Version](https://img.shields.io/badge/version-1.0.0-blue)

**[EN]** Server-side community food collection mod for Minecraft Forge 1.20.1.
**[FR]** Mod de collecte communautaire de nourriture server-side pour Minecraft Forge 1.20.1.

---

## 🇺🇸 English (US)

### Features
- **Community Collection**: Players deposit food at a shared collection point at spawn.
- **Dynamic Boss Bar**: Real-time progress bar with color changes at milestones (White → Blue → Yellow → Green).
- **Leaderboards**: Track top contributors with `/fc top`.
- **Configurable Milestones**: Set thresholds with custom messages and automatic server commands.
- **Custom Item Values**: Override food values or make non-food items depositable.
- **100% Server-Side**: Players don't need to install anything on their client.

### How It Works

An admin designates an existing block as the collection point. Players interact with it:

| Action | Effect |
|--------|--------|
| **Right-click** with food in hand | Deposits the held stack |
| **Sneak + Right-click** | Deposits **all** food from inventory |
| **Right-click** empty-handed | Shows personal and global score |

Each food item is converted to units based on its **nutritive value** (hunger points restored) × quantity. The community works together to reach the season's goal (default: 1,000,000 units).

### Unit Examples

| Food | Nutrition | 1 Stack (64) |
|------|-----------|-------------|
| Steak | 8 | 512 units |
| Cooked Chicken | 6 | 384 units |
| Bread | 5 | 320 units |
| Apple | 4 | 256 units |
| Golden Carrot | 6 | 384 units |

### Commands

#### Player Commands

| Command | Description |
|---------|-------------|
| `/fc` | Display help |
| `/fc score` | Show your personal score, global score and progress |
| `/fc top` | Show top 10 contributors |
| `/fc deposit` | Deposit food held in main hand |
| `/fc depositall` | Deposit all food from inventory |
| `/fc bossbar` | Toggle boss bar visibility (per player) |

#### Admin Commands *(OP 2+)*

| Command | Description |
|---------|-------------|
| `/fc setpoint` | Set the looked-at block as collection point |
| `/fc removepoint` | Remove the collection point |
| `/fc setgoal <amount>` | Set the goal (e.g., `/fc setgoal 2000000`) |
| `/fc give <player> <amount>` | Give points to a player |
| `/fc take <player> <amount>` | Remove points from a player |
| `/fc setvalue <value>` | Set custom value for held item (units per item) |
| `/fc removevalue` | Remove custom value for held item |
| `/fc listvalues` | List all custom item values |
| `/fc setcenter <radius>` | Set boss bar visibility zone |
| `/fc removecenter` | Remove boss bar zone (visible everywhere) |
| `/fc milestone add <threshold> <message>` | Add or modify a milestone |
| `/fc milestone remove <threshold>` | Remove a milestone |
| `/fc milestone addcmd <threshold> <command>` | Add a server command to a milestone |
| `/fc milestone removecmd <threshold> <index>` | Remove a command by index |
| `/fc milestone list` | List all milestones |
| `/fc milestone reload` | Reload milestones from config |
| `/fc info` | Show detailed information |
| `/fc reset confirm` | Reset all scores and milestones *(OP 3, irreversible)* |

---

## 🇫🇷 Français (FR)

### Fonctionnalités
- **Collecte Communautaire** : Les joueurs déposent de la nourriture à un point de collecte au spawn.
- **Boss Bar Dynamique** : Barre de progression en temps réel avec changements de couleur aux paliers (Blanc → Bleu → Jaune → Vert).
- **Classements** : Suivez les meilleurs contributeurs avec `/fc top`.
- **Paliers Configurables** : Définissez des seuils avec messages personnalisés et commandes serveur automatiques.
- **Valeurs d'Items Personnalisées** : Modifiez les valeurs de nourriture ou rendez des items non-nourriture déposables.
- **100% Server-Side** : Les joueurs n'ont rien à installer sur leur client.

### Fonctionnement

Un administrateur désigne un bloc existant comme point de collecte. Les joueurs interagissent avec :

| Action | Effet |
|--------|-------|
| **Clic droit** avec nourriture en main | Dépose le stack tenu |
| **Sneak + Clic droit** | Dépose **toute** la nourriture de l'inventaire |
| **Clic droit** main vide | Affiche le score personnel et global |

Chaque aliment est converti en unités selon sa **valeur nutritive** (points de faim restaurés) × quantité. La communauté travaille ensemble pour atteindre l'objectif de la saison (défaut : 1 000 000 unités).

### Exemples d'Unités

| Aliment | Nutrition | 1 Stack (64) |
|---------|-----------|-------------|
| Steak | 8 | 512 unités |
| Poulet Cuit | 6 | 384 unités |
| Pain | 5 | 320 unités |
| Pomme | 4 | 256 unités |
| Carotte Dorée | 6 | 384 unités |

### Commandes

#### Commandes Joueurs

| Commande | Description |
|----------|-------------|
| `/fc` | Affiche l'aide |
| `/fc score` | Affiche votre score personnel, le score global et la progression |
| `/fc top` | Affiche le classement des 10 meilleurs joueurs |
| `/fc deposit` | Dépose la nourriture tenue en main |
| `/fc depositall` | Dépose toute la nourriture de l'inventaire |
| `/fc bossbar` | Affiche ou masque la barre de progression (toggle par joueur) |

#### Commandes Admin *(OP 2+)*

| Commande | Description |
|----------|-------------|
| `/fc setpoint` | Définit le bloc regardé comme point de collecte |
| `/fc removepoint` | Supprime le point de collecte |
| `/fc setgoal <montant>` | Modifie l'objectif (ex : `/fc setgoal 2000000`) |
| `/fc give <joueur> <montant>` | Attribue des points à un joueur |
| `/fc take <joueur> <montant>` | Retire des points à un joueur |
| `/fc setvalue <valeur>` | Définit la valeur de l'item tenu en main |
| `/fc removevalue` | Supprime la valeur personnalisée de l'item tenu |
| `/fc listvalues` | Liste toutes les valeurs personnalisées |
| `/fc setcenter <rayon>` | Définit la zone de visibilité de la boss bar |
| `/fc removecenter` | Supprime la zone (boss bar visible partout) |
| `/fc milestone add <seuil> <message>` | Ajoute ou modifie un palier |
| `/fc milestone remove <seuil>` | Supprime un palier |
| `/fc milestone addcmd <seuil> <commande>` | Ajoute une commande serveur à un palier |
| `/fc milestone removecmd <seuil> <index>` | Supprime une commande par index |
| `/fc milestone list` | Liste tous les paliers |
| `/fc milestone reload` | Recharge les paliers depuis le fichier config |
| `/fc milestone resetreached` | Remet tous les paliers a "non atteint" (re-declenchables) |
| `/fc leaderboard set` | Place le leaderboard (top 10 armor stands) sur le bloc regarde |
| `/fc leaderboard remove` | Supprime le leaderboard |
| `/fc leaderboard refresh` | Force la mise a jour du leaderboard |
| `/fc info` | Affiche les informations détaillées |
| `/fc reset confirm` | Réinitialise tous les scores et paliers *(OP 3, irréversible)* |

---

### Author / Auteur
**@author vyrriox**

### Links / Liens
- **Website**: [Arcadia: Echoes Of Power](https://arcadia-echoes-of-power.fr/)
- **Support**: [Discord](https://discord.gg/xjF8Rtzyd4)
- **Donation**: [Stripe](https://buy.stripe.com/3cI3co6X97Vy4IK50QfIs00)
