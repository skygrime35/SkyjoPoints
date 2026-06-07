# Module Adapters & DI (Infrastructure)

Ce module implémente la couche externe d'infrastructure, adaptant les ports du domaine au système Android, et gère le câblage des dépendances.

## 1. Adapteurs de Stockage (`adapter/store`)

### [SharedPrefsGameRepository.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/adapter/store/SharedPrefsGameRepository.kt)
Cet adapteur implémente l'interface `GameRepository`. Il sauvegarde les données localement dans les `SharedPreferences` privées de l'application sous le nom de fichier `skyjo_prefs`.

#### Sérialisation JSON
Pour éviter les frameworks lourds comme Gson ou Moshi qui posent des problèmes de configuration et de poids de compilation sans Gradle, cet adapteur utilise les classes JSON natives d'Android (`org.json.JSONObject` et `org.json.JSONArray`).

*   **Format de l'état actif (`key_active_game`) :**
    ```json
    {
      "id": "g_1700000000000",
      "createdAt": 1700000000000,
      "players": [
        { "id": "p_0_...", "name": "Alice" },
        { "id": "p_1_...", "name": "Bob" }
      ],
      "rounds": [
        {
          "scores": { "p_0_...": 15, "p_1_...": 3 }
        }
      ]
    }
    ```
*   **Format de l'historique (`key_game_history`) :**
    Un tableau `JSONArray` d'objets `Game` sérialisés de la même manière.
*   **Résolution des conflits d'historique :**
    Lors de l'appel à `saveGameToHistory(game)`, la liste existante est chargée. Si une partie possédant le même `id` existe déjà, elle est remplacée par la nouvelle version. Sinon, la nouvelle partie est ajoutée. Cela évite les doublons et permet de modifier des scores sur une partie finie (par exemple en annulant la dernière manche).

---

## 2. Injection de dépendances (`di`)

### [ServiceLocator.kt](file:///data/data/com.termux/files/home/Projects/SkyjoPoints/app/src/com/skyjopoints/app/di/ServiceLocator.kt)
Le `ServiceLocator` est un singleton (objet Kotlin) responsable de l'instanciation et du câblage de toutes les dépendances de l'application.

*   `init(context)`: Initialise le dépôt de stockage avec un contexte applicatif global (pour éviter les fuites de mémoire).
*   `provide<UseCase>()`: Fournit des instances à la demande des cas d'usage requis par l'activité principale.
*   Ce mécanisme de localisation manuelle garantit un couplage faible entre la couche UI et les adapteurs sans l'aide de bibliothèques tierces comme Dagger ou Hilt.
