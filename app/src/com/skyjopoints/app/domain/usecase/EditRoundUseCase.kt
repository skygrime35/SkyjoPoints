package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.Round
import com.skyjopoints.app.domain.port.GameRepository

class EditRoundUseCase(private val repository: GameRepository) {
    operator fun invoke(roundIndex: Int, scores: Map<String, Int>): Game? {
        val activeGame = repository.getActiveGame() ?: return null
        if (roundIndex !in activeGame.rounds.indices) return null
        
        val updatedRounds = activeGame.rounds.toMutableList()
        updatedRounds[roundIndex] = Round(scores)
        
        val updatedGame = activeGame.copy(rounds = updatedRounds)
        repository.saveActiveGame(updatedGame)
        repository.saveGameToHistory(updatedGame)
        return updatedGame
    }
}
