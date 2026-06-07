package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.Round
import com.skyjopoints.app.domain.port.GameRepository

class AddRoundUseCase(private val repository: GameRepository) {
    operator fun invoke(scores: Map<String, Int>): Game? {
        val activeGame = repository.getActiveGame() ?: return null
        val newRound = Round(scores)
        val updatedGame = activeGame.copy(rounds = activeGame.rounds + newRound)
        repository.saveActiveGame(updatedGame)
        if (updatedGame.isFinished()) {
            repository.saveGameToHistory(updatedGame)
        }
        return updatedGame
    }
}
