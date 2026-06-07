# AGENTS.md — SkyjoPoints

> Source unique de vérité pour les agents d'IA (Claude Code, Cursor, Antigravity, etc.) travaillant sur ce projet.

## Comment coder ici (méta-règles)

Ces règles privilégient la rigueur et la simplicité sur la vitesse.

### 1. Réfléchir avant de coder
*   **Ne supposez pas.** En cas de doute, posez des questions à l'utilisateur.
*   Si plusieurs choix d'implémentation existent, présentez les compromis.

### 2. La simplicité d'abord
*   **Code minimal** répondant au problème. Pas de fonctionnalités superflues ni d'abstractions prématurées.
*   Si une classe ou une fonction devient trop complexe, divisez-la. Pas de fichier monolithique.

### 3. Modifications chirurgicales
*   Ne modifiez que ce qui est nécessaire. Respectez le style de code existant.
*   Toute ligne modifiée doit remonter directement à une demande utilisateur ou à un correctif de bug.

---

## Qu'est-ce que SkyjoPoints ?

**SkyjoPoints** est une application mobile Android native et hors-ligne écrite en Kotlin, conçue pour compter les points lors des parties du jeu de société **Skyjo**.

L'application prend en charge :
1.  **Une gestion de partie :** Choix des noms des joueurs (2 à 8 joueurs), détection de fin de partie (quand un joueur atteint ou dépasse 100 points), et classement automatique.
2.  **La saisie des scores par manche :**
    *   *Saisie rapide :* Saisie manuelle du score total de la manche pour chaque joueur.
    *   *Calculateur de grille interactif :* Représentation 3x4 des cartes du joueur (valeurs de -2 à 12). Gestion automatique des colonnes identiques éliminées (0 pt) et calcul du total.
3.  **La persistance locale :** Sauvegarde automatique de la partie en cours pour ne pas perdre l'état en cas de fermeture, et historique des parties terminées.

---

## Architecture (Hexagonale / Ports & Adapteurs)

```
com.skyjopoints.app/
├── domain/                 # Métier pur, ZERO import android.*
│   ├── model/              # Player, Round, Game (entités riches)
│   ├── port/               # GameRepository (interface de stockage)
│   └── usecase/            # StartGame, AddRound, DeleteRound, ResetGame, etc.
├── adapter/                # Infrastructure Android
│   └── store/              # SharedPrefsGameRepository (SharedPreferences + org.json)
├── ui/                     # Interface Utilisateur (Driving adapters)
│   ├── MainActivity        # Point d'entrée unique & routage d'écrans
│   └── view/               # Contrôleurs d'écrans (Setup, Scoreboard, Entry, Grid, History)
└── di/
    └── ServiceLocator      # Injection de dépendances manuelle (basée sur Context)
```

### Règles de dépendance
*   **`domain/`** ne doit importer **aucun** package `android.*`. C'est du Kotlin/Java pur.
*   **`adapter/`** et **`ui/`** peuvent importer les API Android.
*   Les communications se font à travers les ports (interfaces). La classe `ServiceLocator` se charge de câbler les adapteurs concrets aux cas d'usage.

---

## Structure du Projet (`AGENTS/`)

Le dossier `AGENTS/` contient la documentation détaillée par module. **Mettez-les à jour** lors de chaque modification de code pour maintenir une documentation parfaite.

| Fichier | Rôle |
|---|---|
| [`AGENTS/domain.md`](AGENTS/domain.md) | Détail des modèles métier (`domain/model`), ports, et cas d'usage (`domain/usecase`). |
| [`AGENTS/adapters.md`](AGENTS/adapters.md) | Détail de la persistance (`SharedPrefsGameRepository`) et de l'injection (`ServiceLocator`). |
| [`AGENTS/ui.md`](AGENTS/ui.md) | Détail de l'architecture Single-Activity, des layouts XML, et des contrôleurs de vues. |
| [`AGENTS/build.md`](AGENTS/build.md) | Détail du script de compilation Termux sans Gradle (`build.sh`). |

---

## Conventions du projet

*   **Kotlin 2.x / JVM Target 11**, `minSdk 26`, `targetSdk 34`.
*   **Pas de dépendance externe** autre que `android.jar` et `kotlin-stdlib.jar`.
*   **Aucun fichier monolithique** : séparez proprement les responsabilités dans des fichiers distincts.
