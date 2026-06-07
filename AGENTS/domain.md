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
*   `getPlayerTotalScore(playerId)`: Somme des scores de toutes les manches pour ce joueur.
*   `isFinished()`: Détermine si la partie est terminée. C'est le cas si au moins une manche a été jouée et qu'au moins un joueur a atteint ou dépassé 100 points au total.
*   `getWinner()`: Renvoie le joueur ayant le score le plus bas lorsque la partie est finie.
*   `getLeaderboard()`: Renvoie la liste des joueurs avec leurs scores cumulés, triée dans l'ordre croissant (le score le plus bas est le premier).

---

## 2. Ports (`domain/port`)

Les ports définissent les contrats d'interfaces requis par le domaine pour communiquer avec l'extérieur (stockage, matériel, horloge).

### [GameRepository.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/port/GameRepository.kt)
Interface décrivant les opérations de stockage :
*   `getActiveGame()`: Récupère la partie en cours.
*   `saveActiveGame(game)`: Enregistre ou supprime la partie active.
*   `getGameHistory()`: Liste les parties archivées.
*   `saveGameToHistory(game)`: Enregistre ou met à jour une partie dans l'historique (sans doublon sur l'ID).
*   `clearHistory()`: Supprime tout l'historique.

---

## 3. Cas d'usage (`domain/usecase`)

Les cas d'usage orchestrent les flux métiers et appliquent les règles en modifiant l'état du domaine et en appelant les ports.

*   **[StartGameUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/StartGameUseCase.kt)** : Crée une nouvelle entité `Game` avec des ID de joueurs uniques basés sur le temps, et la sauvegarde comme partie active.
*   **[AddRoundUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/AddRoundUseCase.kt)** : Crée une manche à partir des scores saisis, l'ajoute à la partie active, puis l'archive automatiquement dans l'historique si la partie est terminée.
*   **[DeleteRoundUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/DeleteRoundUseCase.kt)** : Supprime la dernière manche de la partie active (permet d'annuler une erreur de saisie).
*   **[ResetGameUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/ResetGameUseCase.kt)** : Supprime toutes les manches de la partie active en gardant les mêmes joueurs (recommencer à zéro).
*   **[ArchiveGameUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/ArchiveGameUseCase.kt)** : Enregistre la partie active dans l'historique (si elle contient des manches) puis réinitialise l'état actif (retour au menu principal).
*   **[CalculateGridScoreUseCase](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/domain/usecase/CalculateGridScoreUseCase.kt)** : Calcule le score d'une grille 3x4 de cartes. Élimine les colonnes composées de trois cartes identiques (elles valent alors 0 point). Traite les cellules vides (nulles) comme 0 point.
