package com.skyjopoints.app.domain.port

import com.skyjopoints.app.domain.model.Game

interface GameRepository {
    fun getActiveGame(): Game?
    fun saveActiveGame(game: Game?)
    fun getGameHistory(): List<Game>
    fun saveGameToHistory(game: Game)
    fun clearHistory()
}
