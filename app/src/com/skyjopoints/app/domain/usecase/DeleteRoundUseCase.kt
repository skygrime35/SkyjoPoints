package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.port.GameRepository

class DeleteRoundUseCase(private val repository: GameRepository) {
    operator fun invoke(index: Int? = null): Game? {
        val activeGame = repository.getActiveGame() ?: return null
        if (activeGame.rounds.isEmpty()) return activeGame
        
        val updatedRounds = if (index != null && index in activeGame.rounds.indices) {
            val list = activeGame.rounds.toMutableList()
            list.removeAt(index)
            list
        } else {
            activeGame.rounds.dropLast(1)
        }
        
        val updatedGame = activeGame.copy(rounds = updatedRounds)
        repository.saveActiveGame(updatedGame)
        repository.saveGameToHistory(updatedGame)
        return updatedGame
    }
}
