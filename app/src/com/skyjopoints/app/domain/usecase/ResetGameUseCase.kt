package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.port.GameRepository

class ResetGameUseCase(private val repository: GameRepository) {
    operator fun invoke(): Game? {
        val activeGame = repository.getActiveGame() ?: return null
        val resetGame = activeGame.copy(rounds = emptyList())
        repository.saveActiveGame(resetGame)
        return resetGame
    }
}
