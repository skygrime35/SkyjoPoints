# Module UI (Présentation)

L'interface de l'application utilise une architecture **Single-Activity** et un découpage par contrôleurs de vues pour éviter de créer un fichier d'activité géant (monolithique).

## 1. Activité Principale (`ui/MainActivity.kt`)

L'activité [MainActivity.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/MainActivity.kt) gère :
*   L'initialisation globale du `ServiceLocator`.
*   **La gestion dynamique des thèmes :** L'application applique le thème Sombre par défaut et permet de basculer vers un thème Clair. Le choix est persisté en base et réappliqué au démarrage ou lors du clic sur le bouton de bascule par un appel à `recreate()`.
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

## 1b. Thèmes & Design Visuel Modernisé
L'application implémente un design moderne avec des angles arrondis, des ombres douces et une structure en cartes :
*   **Drawables personnalisés :** 
    *   [bg_rounded_card.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/drawable/bg_rounded_card.xml) : Fond de carte avec angles arrondis de `16dp` et bordure.
    *   [bg_rounded_button_primary.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/drawable/bg_rounded_button_primary.xml) & [bg_rounded_button_secondary.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/drawable/bg_rounded_button_secondary.xml) : Boutons stylisés avec angles de `14dp` et effet ripple.
    *   [bg_input_field.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/drawable/bg_input_field.xml) : Champs de texte avec bordure interactive s'illuminant au focus.
*   **Attributs dynamiques :** Déclarés dans [attrs.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/values/attrs.xml) pour permettre à tous les layouts de s'adapter automatiquement au changement de thème (Sombre/Clair) sans duplication de fichiers XML.


---

## 2. Contrôleurs de Vues (`ui/view`)

Chaque écran possède son propre contrôleur de vue qui prend en paramètre une vue racine (`root: View`), récupère ses éléments d'interface par `findViewById` et configure les liaisons de données et les écouteurs de clics.

*   **[PlayerSetupViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/PlayerSetupViewController.kt) :**
    *   Layout : [screen_player_setup.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_player_setup.xml) : Enveloppe la totalité des éléments de configuration (liste des joueurs, boutons d'ajout et d'action, paramètres pliables) dans un seul `ScrollView` pour rendre tout le contenu scrollable lorsque le clavier est ouvert ou que les paramètres sont affichés.
    *   Gère la liste dynamique des joueurs (minimum 2, maximum 8) en injectant les éléments [item_player_input.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/item_player_input.xml).
    *   **Saisie prédictive (Autocomplete) :** Le champ de texte utilise désormais un `AutoCompleteTextView` connecté à la liste des noms déjà utilisés lors des anciennes parties pour afficher un menu déroulant de suggestions dès la première lettre saisie.
    *   **Sécurité anti-doublon :** Bloque le démarrage de la partie et affiche un Toast si deux joueurs différents ont exactement le même nom.
    *   **Paramètres masquables :** Les paramètres (nom de la partie et score max de fin) sont masqués par défaut derrière un bouton "Paramètres de la partie ⚙️" pour maximiser l'espace d'écriture des joueurs.
    *   **Nommage de la partie :** Permet de donner un nom personnalisé à la partie via le champ `et_game_name` de la section des paramètres.
    *   **Score max de fin de partie :** Permet de configurer la limite de points pour terminer la partie via le champ `et_max_points`. Si vide, la partie continue sans limite automatique (infini).
    *   **Condition globale de victoire :** L'utilisateur choisit à la base si le gagnant est celui qui a le moins de points (par défaut) ou le plus de points.
    *   **Nombre max de manches :** Permet de configurer le nombre maximum de manches via le champ `et_max_rounds`. Si vide, la partie continue sans limite de manches.
    *   **Score max de fin de partie :** Permet de configurer la limite de points pour terminer la partie via le champ `et_max_points`. Si vide, la partie continue sans limite automatique (infini).
    *   **Condition d'arrêt dynamique :** Dès qu'un score max est saisi, un conteneur `layout_stop_condition` apparaît dynamiquement et propose 2 conditions d'arrêt : dès qu'un joueur dépasse le score max, ou quand tout le monde a dépassé le score max. Une fois la partie arrêtée par cette limite de score ou de manches, le vainqueur est élu en appliquant la condition globale de victoire.
*   **[ScoreboardViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/ScoreboardViewController.kt) :**
    *   Layout : [screen_scoreboard.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_scoreboard.xml)
    *   Dessine dynamiquement le tableau des scores (`TableLayout`) avec les manches en lignes et les joueurs en colonnes.
    *   Affiche une superposition d'écran (overlay) célébrant le vainqueur avec son score final et le classement complet (leaderboard ordonné) quand `game.isFinished()` renvoie vrai.
    *   **Double validation de sécurité :** Les actions "Terminer la partie" et "Nouvelle partie" affichent une boîte de dialogue de confirmation native (`AlertDialog`) pour éviter les erreurs de manipulation.
    *   **Terminer la partie manuellement :** Le bouton "Terminer la partie" permet de forcer la fin de la partie immédiatement avec le classement final.
    *   **Nouvelle partie avec conservation :** Le bouton "Nouvelle Partie" permet d'archiver la partie en cours (qui reste intacte dans l'historique) et de retourner à l'écran de configuration pour lancer une nouvelle session de jeu.
    *   **Modification des manches :** Chaque ligne de manche dans le tableau est cliquable (avec un effet de surbrillance ripple). Un clic ouvre l'éditeur de manche pré-rempli avec les scores de la manche sélectionnée.
*   **[RoundEntryViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/RoundEntryViewController.kt) :**
    *   Layout : [screen_round_entry.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_round_entry.xml)
    *   Permet d'associer les scores par joueur via [item_round_player_input.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/item_round_player_input.xml).
    *   **Support d'édition & suppression :** Adapte dynamiquement son titre et pré-remplit les scores de la manche existante si un index d'édition est spécifié. De plus, un bouton de suppression apparaît uniquement en mode édition permettant de supprimer directement cette manche spécifique avec confirmation native.
    *   **Double validation à l'enregistrement :** Affiche un `AlertDialog` de confirmation avant d'enregistrer les modifications apportées à une manche.

*   **[GridCalculatorViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/GridCalculatorViewController.kt) :**
    *   Layout : [screen_grid_calculator.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_grid_calculator.xml)
    *   Propose une grille interactive 3x4. L'appui sur une case de carte puis sur une touche du clavier de sélection (-2 à 12, ou vider) met à jour la carte.
    *   **Avance automatique :** Après la saisie d'une valeur, la sélection se déplace automatiquement à la case suivante (balayage gauche-droite puis haut-bas) pour fluidifier la saisie.
    *   **Élimination visuelle :** Si une colonne complète contient 3 valeurs identiques, ses cartes sont masquées par un `x` grisé à 40% d'opacité.
*   **[HistoryViewController.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/ui/view/HistoryViewController.kt) :**
    *   Layout : [screen_history.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/screen_history.xml)
    *   Affiche l'historique trié des parties via [item_history_game.xml](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/res/layout/item_history_game.xml).
    *   **Reprise de partie (Resume) :** Chaque ligne d'historique (terminée ou en cours) est cliquable avec un effet ripple. Un clic charge la partie sélectionnée comme partie active et redirige vers le tableau des scores.
    *   **Suppression de partie :** Un bouton individuel (corbeille 🗑️) permet de supprimer une partie de l'historique, protégé par une double boîte de dialogue de confirmation native.
    *   **Affichage du nom de la partie :** Affiche le nom personnalisé de la partie si défini, à côté de la liste des joueurs.
    *   **Statut dynamique :** Affiche le gagnant pour les parties finies, ou la mention "En cours..." pour les parties non finalisées.
