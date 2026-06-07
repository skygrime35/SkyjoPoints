# Pipeline de Compilation (Termux)

L'application SkyjoPoints est compilée directement sur le téléphone via l'environnement Termux en utilisant un script de construction sans Gradle ([build.sh](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/build.sh)).

## 1. Prérequis (Termux)

Les packages suivants doivent être installés dans Termux :
```bash
pkg install aapt2 openjdk-21 kotlin zipalign apksigner zip
```

Le SDK Android requis (`android.jar` version API 34) est localisé à :
`/data/data/com.termux/files/home/android-sdk/platforms/android-34/android.jar`

---

## 2. Étapes de compilation

Le script `build.sh` enchaîne les étapes suivantes :

### Étape 1 : Compilation des ressources (`aapt2 compile`)
Compile l'ensemble des fichiers XML de ressources (layouts, colors, strings, styles) situés dans `app/res/` vers un fichier zip intermédiaire `build/res.zip`.

### Étape 2 : Liaison des ressources (`aapt2 link`)
Génère le fichier APK brut non signé `build/app-unsigned.apk` et le fichier `R.java` contenant les constantes d'identifiants de ressources à l'emplacement temporaire `build/gen`.

### Étape 3 : Compilation Kotlin (`kotlinc`)
Compile les sources Kotlin de l'application (situés dans `app/src/`) ainsi que le fichier de ressources généré `build/gen/.../R.java` vers des fichiers `.class` Java stockés dans `build/classes`. La cible JVM est fixée à `11`.

### Étape 4 : Conversion Dex (`d8`)
Convertit les fichiers `.class` générés ainsi que l'ensemble de la bibliothèque standard Kotlin (`kotlin-stdlib.jar`) en un fichier dex unique `classes.dex` situé dans `build/dex`.

### Étape 5 : Injection Dex dans l'APK
Copie le fichier dex `classes.dex` à l'intérieur du zip de l'APK `build/app-unsigned.apk` à l'aide de la commande `zip`.

### Étape 6 : Alignement des octets (`zipalign`)
Aligne le fichier APK pour optimiser l'utilisation de la mémoire vive par le système Android, créant `build/app-aligned.apk`.

### Étape 7 : Signature numérique (`apksigner`)
Génère un keystore de debug autosigné à la racine du projet (`debug.keystore`) s'il n'existe pas, puis signe l'APK avec ce dernier pour produire l'APK installable final `SkyjoPoints.apk`.

---

## 3. Déploiement et Installation

*   Le script tente de copier l'APK final dans le dossier partagé des téléchargements de votre téléphone :
    `/sdcard/Download/SkyjoPoints.apk` (ou `~/storage/downloads/SkyjoPoints.apk`).
*   Pour l'installer, ouvrez votre explorateur de fichiers Android, naviguez jusqu'au dossier `Téléchargements` et cliquez sur `SkyjoPoints.apk`.
*   Note : Comme le keystore est conservé à la racine du projet entre les builds, la signature reste stable, permettant les mises à jour directes de l'application sans désinstallation préalable.
