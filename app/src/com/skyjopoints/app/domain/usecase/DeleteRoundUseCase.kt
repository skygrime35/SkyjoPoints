package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.port.GameRepository

class DeleteRoundUseCase(private val repository: GameRepository) {
    operator fun invoke(): Game? {
        val activeGame = repository.getActiveGame() ?: return null
        if (activeGame.rounds.isEmpty()) return activeGame
        val updatedGame = activeGame.copy(rounds = activeGame.rounds.dropLast(1))
        repository.saveActiveGame(updatedGame)
        if (updatedGame.isFinished()) {
            repository.saveGameToHistory(updatedGame)
        }
        return updatedGame
    }
}
