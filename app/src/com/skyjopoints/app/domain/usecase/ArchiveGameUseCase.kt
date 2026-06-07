package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.port.GameRepository

class ArchiveGameUseCase(private val repository: GameRepository) {
    operator fun invoke() {
        val activeGame = repository.getActiveGame()
        if (activeGame != null) {
            if (activeGame.rounds.isNotEmpty()) {
                repository.saveGameToHistory(activeGame)
            }
            repository.saveActiveGame(null)
        }
    }
}
