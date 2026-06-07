# Module UI (Présentation)

L'interface de l'application utilise une architecture **Single-Activity** et un découpage par contrôleurs de vues pour éviter de créer un fichier d'activité géant (monolithique).

## 1. Activité Principale (`ui/MainActivity.kt`)

L'activité [MainActivity.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/MainActivity.kt) gère :
*   L'initialisation globale du `ServiceLocator`.
*   Le routage principal entre les écrans représentés par l'énumération `Screen` :
    *   `SETUP` : Écran d'accueil et configuration des joueurs.
    *   `SCOREBOARD` : Tableau des scores cumulés.
    *   `ROUND_ENTRY` : Saisie des scores de la manche en cours.
    *   `GRID_CALCULATOR` : Aide à la saisie interactive sous forme de grille 3x4.
    *   `HISTORY` : Historique des parties terminées.
*   **La préservation de l'état temporaire (`tempRoundScores`) :**
    Lorsqu'un utilisateur saisit les scores d'une manche et bascule sur le calculateur de grille pour l'un des joueurs, l'état saisi des autres joueurs est sauvegardé temporairement dans `MainActivity` puis réinjecté lors du retour à l'écran de saisie de manche. Cela évite les pertes de saisie intempestives.
*   La gestion intelligente du bouton de retour physique (`onBackPressed`) selon l'écran affiché.

---

## 2. Contrôleurs de Vues (`ui/view`)

Chaque écran possède son propre contrôleur de vue qui prend en paramètre une vue racine (`root: View`), récupère ses éléments d'interface par `findViewById` et configure les liaisons de données et les écouteurs de clics.

*   **[PlayerSetupViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/PlayerSetupViewController.kt) :**
    *   Layout : [screen_player_setup.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_player_setup.xml)
    *   Gère la liste dynamique des joueurs (minimum 2, maximum 8) en injectant les éléments [item_player_input.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/item_player_input.xml).
*   **[ScoreboardViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/ScoreboardViewController.kt) :**
    *   Layout : [screen_scoreboard.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_scoreboard.xml)
    *   Dessine dynamiquement le tableau des scores (`TableLayout`) avec les manches en lignes et les joueurs en colonnes.
    *   Affiche une superposition d'écran (overlay) célébrant le vainqueur avec son score final quand `game.isFinished()` renvoie vrai.
*   **[RoundEntryViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/RoundEntryViewController.kt) :**
    *   Layout : [screen_round_entry.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_round_entry.xml)
    *   Permet d'associer les scores par joueur via [item_round_player_input.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/item_round_player_input.xml).
*   **[GridCalculatorViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/GridCalculatorViewController.kt) :**
    *   Layout : [screen_grid_calculator.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_grid_calculator.xml)
    *   Propose une grille interactive 3x4. L'appui sur une case de carte puis sur une touche du clavier de sélection (-2 à 12, ou vider) met à jour la carte.
    *   **Avance automatique :** Après la saisie d'une valeur, la sélection se déplace automatiquement à la case suivante (balayage gauche-droite puis haut-bas) pour fluidifier la saisie.
    *   **Élimination visuelle :** Si une colonne complète contient 3 valeurs identiques, ses cartes sont masquées par un `x` grisé à 40% d'opacité.
*   **[HistoryViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/HistoryViewController.kt) :**
    *   Layout : [screen_history.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_history.xml)
    *   Affiche l'historique trié des parties via [item_history_game.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/item_history_game.xml).
