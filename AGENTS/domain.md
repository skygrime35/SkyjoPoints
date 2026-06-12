# Module Domain (Métier)

Le sous-dossier `domain/` contient le noyau métier pur de l'application. Il est écrit en Kotlin standard, sans aucune dépendance envers les frameworks ou les bibliothèques Android (aucun import de type `android.*`).

## 1. Modèles (`domain/model`)

### [Player.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/model/Player.kt)
Représente un joueur participant à la partie.
*   `id`: Identifiant unique (format `p_<index>_<timestamp>`).
*   `name`: Nom d'affichage renseigné par l'utilisateur.

### [Round.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/model/Round.kt)
Représente les scores d'une manche spécifique.
*   `scores`: Table de scores (map associant l'ID du joueur à ses points de la manche).

### [Game.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/model/Game.kt)
Représente une partie de Skyjo complète, agrégeant les manches.
*   `id`: Identifiant unique de la partie.
*   `players`: Liste ordonnée des joueurs.
*   `rounds`: Liste des manches jouées.
*   `createdAt`: Horodatage de création.
*   `maxPoints`: Limite optionnelle de points pour terminer la partie (si `null`, la partie continue sans limite automatique).
*   `name`: Nom personnalisé de la partie (facultatif).
*   `victoryCondition`: Condition globale de victoire (valeur de `VictoryCondition` : `FEWEST_POINTS` ou `MOST_POINTS`).
*   `maxRounds`: Limite optionnelle de manches pour terminer la partie (si `null`, pas de limite de manches).
*   `stopCondition`: Règle optionnelle d'arrêt par points (valeur de `StopCondition` : `ANY_EXCEEDS` ou `ALL_EXCEED`).
*   `getPlayerTotalScore(playerId)`: Somme des scores de toutes les manches pour ce joueur.
*   `isFinished()`: Détermine si la partie est terminée (nombre maximum de manches atteint ou condition de score max satisfaite).
*   `getWinner()`: Renvoie le gagnant de la partie en fonction de la condition de victoire globale (`victoryCondition`).
*   `getLeaderboard()`: Renvoie la liste des joueurs avec leurs scores cumulés, triée dans le bon ordre selon `victoryCondition` (croissant si le gagnant a le moins de points, décroissant si le gagnant a le plus de points).

---

## 2. Ports (`domain/port`)

Les ports définissent les contrats d'interfaces requis par le domaine pour communiquer avec l'extérieur (stockage, matériel, horloge).

### [GameRepository.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/port/GameRepository.kt)
Interface décrivant les opérations de stockage :
*   `getActiveGame()`: Récupère la partie en cours.
*   `saveActiveGame(game)`: Enregistre ou supprime la partie active.
*   `getGameHistory()`: Liste les parties archivées.
*   `saveGameToHistory(game)`: Enregistre ou met à jour une partie dans l'historique (sans doublon sur l'ID).
*   `deleteGameFromHistory(gameId)`: Supprime une partie spécifique de l'historique.
*   `clearHistory()`: Supprime tout l'historique.
*   `getThemeMode()`: Récupère le mode de thème actuel (Sombre ou Clair).
*   `setThemeMode(mode)`: Enregistre la préférence de thème de l'utilisateur.
*   `getSavedPlayerNames()`: Récupère la liste de tous les noms de joueurs déjà enregistrés.
*   `savePlayerNames(names)`: Enregistre un ensemble de noms de joueurs (les ajouts se font de manière unique).

---

## 3. Cas d'usage (`domain/usecase`)

Les cas d'usage orchestrent les flux métiers et appliquent les règles en modifiant l'état du domaine et en appelant les ports.

*   **[StartGameUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/StartGameUseCase.kt)** : Crée une nouvelle entité `Game` avec un nom de partie personnalisé (facultatif), des ID de joueurs uniques basés sur le temps, la sauvegarde comme partie active, et enregistre les noms saisis dans l'historique des joueurs pour les suggestions futures.
*   **[AddRoundUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/AddRoundUseCase.kt)** : Crée une manche à partir des scores saisis, l'ajoute à la partie active, puis l'archive automatiquement dans l'historique si la partie est terminée.
*   **[DeleteRoundUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/DeleteRoundUseCase.kt)** : Supprime la dernière manche de la partie active (permet d'annuler une erreur de saisie).
*   **[ArchiveGameUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/ArchiveGameUseCase.kt)** : Enregistre la partie active dans l'historique (si elle contient des manches) puis réinitialise l'état actif (retour au menu principal).
*   **[CalculateGridScoreUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/CalculateGridScoreUseCase.kt)** : Calcule le score d'une grille 3x4 de cartes. Élimine les colonnes composées de trois cartes identiques (elles valent alors 0 point). Traite les cellules vides (nulles) comme 0 point.
*   **[EditRoundUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/EditRoundUseCase.kt)** : Permet de modifier les scores d'une manche existante par son index dans la partie active et met à jour l'historique si la partie se termine.

