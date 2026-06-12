package com.skyjopoints.app.adapter.store

import android.content.Context
import android.content.SharedPreferences
import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.VictoryCondition
import com.skyjopoints.app.domain.model.StopCondition
import com.skyjopoints.app.domain.model.Player
import com.skyjopoints.app.domain.model.Round
import com.skyjopoints.app.domain.port.GameRepository
import org.json.JSONArray
import org.json.JSONObject

class SharedPrefsGameRepository(context: Context) : GameRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("skyjo_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_GAME = "key_active_game"
        private const val KEY_GAME_HISTORY = "key_game_history"
    }

    override fun getActiveGame(): Game? {
        val jsonStr = prefs.getString(KEY_ACTIVE_GAME, null) ?: return null
        return try {
            jsonToGame(JSONObject(jsonStr))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun saveActiveGame(game: Game?) {
        if (game == null) {
            prefs.edit().remove(KEY_ACTIVE_GAME).apply()
        } else {
            try {
                val jsonStr = gameToJson(game).toString()
                prefs.edit().putString(KEY_ACTIVE_GAME, jsonStr).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getGameHistory(): List<Game> {
        val jsonStr = prefs.getString(KEY_GAME_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<Game>()
            for (i in 0 until arr.length()) {
                list.add(jsonToGame(arr.getJSONObject(i)))
            }
            list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override fun saveGameToHistory(game: Game) {
        val history = getGameHistory().toMutableList()
        val index = history.indexOfFirst { it.id == game.id }
        if (index != -1) {
            history[index] = game
        } else {
            history.add(game)
        }

        try {
            val arr = JSONArray()
            history.forEach { arr.put(gameToJson(it)) }
            prefs.edit().putString(KEY_GAME_HISTORY, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun deleteGameFromHistory(gameId: String) {
        val history = getGameHistory().filter { it.id != gameId }
        try {
            val arr = JSONArray()
            history.forEach { arr.put(gameToJson(it)) }
            prefs.edit().putString(KEY_GAME_HISTORY, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val active = getActiveGame()
        if (active?.id == gameId) {
            saveActiveGame(null)
        }
    }

    override fun clearHistory() {
        prefs.edit().remove(KEY_GAME_HISTORY).apply()
    }

    override fun getThemeMode(): String {
        return prefs.getString("key_theme_mode", "dark") ?: "dark"
    }

    override fun setThemeMode(mode: String) {
        prefs.edit().putString("key_theme_mode", mode).apply()
    }

    override fun getSavedPlayerNames(): List<String> {
        val set = prefs.getStringSet("key_saved_player_names", emptySet()) ?: emptySet()
        return set.toList().sorted()
    }

    override fun savePlayerNames(names: List<String>) {
        val existing = prefs.getStringSet("key_saved_player_names", emptySet()) ?: emptySet()
        val newSet = existing.toMutableSet()
        newSet.addAll(names.map { it.trim() }.filter { it.isNotEmpty() })
        prefs.edit().putStringSet("key_saved_player_names", newSet).apply()
    }

    // JSON Helper Methods

    private fun playerToJson(player: Player): JSONObject {
        return JSONObject().apply {
            put("id", player.id)
            put("name", player.name)
        }
    }

    private fun jsonToPlayer(obj: JSONObject): Player {
        return Player(
            id = obj.getString("id"),
            name = obj.getString("name")
        )
    }

    private fun roundToJson(round: Round): JSONObject {
        return JSONObject().apply {
            val scoresJson = JSONObject()
            round.scores.forEach { (k, v) -> scoresJson.put(k, v) }
            put("scores", scoresJson)
        }
    }

    private fun jsonToRound(obj: JSONObject): Round {
        val scores = mutableMapOf<String, Int>()
        val scoresJson = when {
            obj.has("scores") -> obj.getJSONObject("scores")
            obj.has("finalScores") -> obj.getJSONObject("finalScores")
            else -> obj.getJSONObject("rawScores")
        }
        scoresJson.keys().forEach { key ->
            scores[key] = scoresJson.getInt(key)
        }
        return Round(scores)
    }

    private fun gameToJson(game: Game): JSONObject {
        return JSONObject().apply {
            put("id", game.id)
            put("createdAt", game.createdAt)
            put("maxPoints", game.maxPoints ?: -1)
            if (game.name != null) {
                put("name", game.name)
            }
            put("victoryCondition", game.victoryCondition.name)
            put("maxRounds", game.maxRounds ?: -1)
            if (game.stopCondition != null) {
                put("stopCondition", game.stopCondition.name)
            }
            put("manuallyFinished", game.manuallyFinished)
            
            val playersArr = JSONArray()
            game.players.forEach { playersArr.put(playerToJson(it)) }
            put("players", playersArr)

            val roundsArr = JSONArray()
            game.rounds.forEach { roundsArr.put(roundToJson(it)) }
            put("rounds", roundsArr)
        }
    }

    private fun jsonToGame(obj: JSONObject): Game {
        val id = obj.getString("id")
        val createdAt = obj.getLong("createdAt")
        val maxPoints = if (obj.has("maxPoints")) {
            val limit = obj.getInt("maxPoints")
            if (limit <= 0) null else limit
        } else {
            100 // Backward compatibility
        }
        val name = if (obj.has("name")) obj.getString("name").takeIf { it.isNotEmpty() } else null
        
        val victoryCondition = if (obj.has("victoryCondition")) {
            try {
                VictoryCondition.valueOf(obj.getString("victoryCondition"))
            } catch (e: Exception) {
                VictoryCondition.FEWEST_POINTS
            }
        } else {
            val ruleStr = if (obj.has("rule")) obj.getString("rule") else "FEWEST_WHEN_ANY_EXCEEDS"
            val maxRoundsRuleStr = if (obj.has("maxRoundsRule")) obj.getString("maxRoundsRule") else null
            if (ruleStr == "FIRST_TO_EXCEED" || ruleStr == "MOST_WHEN_ALL_EXCEED" || maxRoundsRuleStr == "MOST_POINTS") {
                VictoryCondition.MOST_POINTS
            } else {
                VictoryCondition.FEWEST_POINTS
            }
        }

        val maxRounds = if (obj.has("maxRounds")) {
            val limit = obj.getInt("maxRounds")
            if (limit <= 0) null else limit
        } else null

        val manuallyFinished = if (obj.has("manuallyFinished")) obj.getBoolean("manuallyFinished") else false

        val stopCondition = if (obj.has("stopCondition")) {
            try {
                StopCondition.valueOf(obj.getString("stopCondition"))
            } catch (e: Exception) {
                StopCondition.ANY_EXCEEDS
            }
        } else if (maxPoints != null) {
            val ruleStr = if (obj.has("rule")) obj.getString("rule") else "FEWEST_WHEN_ANY_EXCEEDS"
            if (ruleStr == "MOST_WHEN_ALL_EXCEED") {
                StopCondition.ALL_EXCEED
            } else {
                StopCondition.ANY_EXCEEDS
            }
        } else null

        val players = mutableListOf<Player>()
        val playersArr = obj.getJSONArray("players")
        for (i in 0 until playersArr.length()) {
            players.add(jsonToPlayer(playersArr.getJSONObject(i)))
        }

        val rounds = mutableListOf<Round>()
        val roundsArr = obj.getJSONArray("rounds")
        for (i in 0 until roundsArr.length()) {
            rounds.add(jsonToRound(roundsArr.getJSONObject(i)))
        }

        return Game(id, players, rounds, createdAt, maxPoints, name, victoryCondition, maxRounds, stopCondition, manuallyFinished)
    }
}
