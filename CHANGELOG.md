# Changelog

## v1.2.0

### Nouveautes

- **Paliers en fichier config** : les paliers sont maintenant stockes dans `config/recraftcollect-milestones.json`
  - Le fichier est cree automatiquement avec les paliers par defaut au premier lancement
  - Modifiable en jeu ou directement dans le fichier JSON
  - `/fc milestone reload` pour recharger le fichier sans redemarrer le serveur
- **Commandes automatiques par palier** : chaque palier peut declencher des commandes serveur
  - `/fc milestone addcmd <seuil> <commande>` pour ajouter une commande a un palier
  - `/fc milestone removecmd <seuil> <index>` pour retirer une commande
  - Les commandes s'executent automatiquement quand le palier est atteint
  - Exemples : give, title, effect, playsound, say, etc.
  - `/fc milestone list` affiche les commandes associees a chaque palier

### Exemple de fichier config

```json
[
  {
    "threshold": 100000,
    "message": "100 000 unites ! Un dixieme de l'objectif !",
    "commands": [
      "title @a title {\"text\":\"100K!\",\"color\":\"gold\"}",
      "give @a minecraft:golden_apple 1"
    ]
  }
]
```

### Ameliorations

- Les definitions de paliers ne sont plus dans la sauvegarde du monde mais dans le fichier config (compatible avec les sauvegardes v1.0.0 et v1.1.0)

---

## v1.1.0

### Nouveautes

- **Objectif configurable** : `/fc setgoal <montant>` permet de modifier l'objectif en jeu (defaut: 1 000 000)
- **Gestion des points joueurs** :
  - `/fc give <joueur> <montant>` pour attribuer des points
  - `/fc take <joueur> <montant>` pour retirer des points
  - Le joueur cible est notifie dans le chat
- **Paliers configurables en jeu** :
  - `/fc milestone add <seuil> <message>` pour ajouter ou modifier un palier
  - `/fc milestone remove <seuil>` pour supprimer un palier
  - `/fc milestone list` pour lister tous les paliers et leur statut
- **Valeurs personnalisees d'items** :
  - `/fc setvalue <valeur>` pour definir la valeur de l'item tenu en main
  - `/fc removevalue` pour supprimer la valeur personnalisee
  - `/fc listvalues` pour lister toutes les valeurs personnalisees
  - Permet de rendre deposable n'importe quel item (meme non-nourriture)
  - Permet de mettre un item a 0 pour l'exclure

### Ameliorations

- L'objectif, les paliers et les valeurs d'items sont tous persistants dans la sauvegarde
- Compatibilite ascendante avec les sauvegardes v1.0.0

---

## v1.0.0

### Fonctionnalites initiales

- **Point de collecte** : bloc designe par un admin ou les joueurs deposent de la nourriture
  - Clic droit avec nourriture = deposer le stack
  - Sneak + clic droit = deposer tout l'inventaire
  - Clic droit main vide = voir les scores
- **Calcul des unites** base sur la valeur nutritive des aliments
- **Scores** : score individuel par joueur + score global
- **Boss bar** : barre de progression visible par tous les joueurs
  - Couleur dynamique selon la progression (blanc, bleu, jaune, vert)
  - Zone de visibilite configurable avec `/fc setcenter <rayon>`
  - Toggle par joueur avec `/fc bossbar`
- **Paliers par defaut** : 8 paliers de 10 000 a 1 000 000 avec annonces chat et sons
- **Commandes joueurs** : `/fc score`, `/fc top`, `/fc deposit`, `/fc depositall`, `/fc bossbar`
- **Commandes admin** : `/fc setpoint`, `/fc removepoint`, `/fc setcenter`, `/fc removecenter`, `/fc info`, `/fc reset confirm`
- **100% server-side** : aucune installation requise cote client
