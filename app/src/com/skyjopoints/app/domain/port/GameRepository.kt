package com.skyjopoints.app.domain.port

import com.skyjopoints.app.domain.model.Game

interface GameRepository {
    fun getActiveGame(): Game?
    fun saveActiveGame(game: Game?)
    fun getGameHistory(): List<Game>
    fun saveGameToHistory(game: Game)
    fun deleteGameFromHistory(gameId: String)
    fun clearHistory()
    fun getThemeMode(): String
    fun setThemeMode(mode: String)
    fun getSavedPlayerNames(): List<String>
    fun savePlayerNames(names: List<String>)
}
