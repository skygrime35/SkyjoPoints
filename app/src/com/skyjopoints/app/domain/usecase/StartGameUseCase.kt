package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.Player
import com.skyjopoints.app.domain.port.GameRepository

class StartGameUseCase(private val repository: GameRepository) {
    operator fun invoke(playerNames: List<String>): Game {
        val players = playerNames.mapIndexed { index, name ->
            Player(id = "p_${index}_${System.currentTimeMillis()}", name = name.trim())
        }
        val newGame = Game(
            id = "g_${System.currentTimeMillis()}",
            players = players,
            rounds = emptyList(),
            createdAt = System.currentTimeMillis()
        )
        repository.saveActiveGame(newGame)
        return newGame
    }
}
