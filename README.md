# SkyjoPoints 🃏🏆

> Application Android native développée en Kotlin pour compter et suivre facilement les scores du jeu de cartes **Skyjo**, avec une architecture hexagonale stricte et une compilation rapide sans Gradle.

![min Android](https://img.shields.io/badge/min%20Android-8.0-blue) ![build](https://img.shields.io/badge/build-Termux%20on--device-orange) ![langage](https://img.shields.io/badge/lang-Kotlin%202.x-purple)

---

## 🌟 Fonctionnalités

*   **Gestion complète de partie :**
    *   Saisie simple des noms des joueurs (de 2 à 8 joueurs).
    *   Calculatrice automatique des scores cumulés manche après manche.
    *   Détection de fin de partie (quand un joueur atteint ou dépasse 100 points) et célébration du vainqueur.
    *   Classement en temps réel.
*   **Double mode de saisie des points :**
    *   *Saisie rapide :* Entrez directement le score total cumulé de la manche pour chaque joueur.
    *   *Calculateur de grille interactif :* Touchez les cartes d'une grille 3x4 (valeurs de -2 à 12). Le calculateur élimine automatiquement les colonnes de 3 cartes identiques (comptées pour 0 pt) et somme les points restants.

*   **Sauvegarde et Historique :**
    *   Sauvegarde automatique : Reprenez votre partie là où vous l'avez laissée si l'application s'est fermée.
    *   Historique des parties : Consultez la liste de vos anciennes parties avec la date, les joueurs, le vainqueur et le nombre de manches.

---

## 🛠️ Installation et Compilation

L'application se compile directement sur votre appareil Android via l'application **Termux**.

### 1. Prérequis Termux (une fois)
Dans Termux sur votre téléphone :
```bash
pkg install aapt2 openjdk-21 kotlin zipalign apksigner zip
```

### 2. Cloner et Builder
Naviguez dans votre dossier de projets et lancez la compilation :
```bash
cd ~/Projects/SkyjoPoints
bash build.sh
```

Le script exécute le pipeline de compilation native en quelques secondes (`aapt2` ➔ `kotlinc` ➔ `d8` ➔ `zipalign` ➔ `apksigner`).

### 3. Installer l'APK
Ouvrez l'explorateur de fichiers de votre téléphone :
*   Allez dans le dossier **Téléchargements** (ou `Download`).
*   Cliquez sur le fichier `SkyjoPoints.apk` et validez l'installation (autorisez l'installation d'applications de sources inconnues si Android le demande).

---

## 📐 Architecture Hexagonale

L'application est découpée en couches strictes afin de préserver l'indépendance de la logique métier (le domaine) par rapport à l'infrastructure Android.

```
SkyjoPoints/
├── app/
│   ├── AndroidManifest.xml                 # Déclaration des composants Android
│   ├── res/                                # Ressources XML (styles, colors, layouts)
│   └── src/com/skyjopoints/app/
│       ├── domain/                         # Métier pur (Kotlin standard, ZERO import android.*)
│       │   ├── model/                      # Player, Round, Game (Entités)
│       │   ├── port/                       # Dépôt de données (Contrats d'interface)
│       │   └── usecase/                    # Actions utilisateur (StartGame, AddRound...)
│       ├── adapter/                        # Infrastructure Android (Implémentation SharedPreferences)
│       ├── ui/                             # Interface graphique (MainActivity & view controllers)
│       └── di/                             # Injection de dépendances manuelle (ServiceLocator)
└── build.sh                                # Script de build sans Gradle
```

Pour plus de détails techniques sur le fonctionnement et les modifications du code, veuillez vous référer au fichier [AGENTS.md](AGENTS.md).
