package com.skyjopoints.app.domain.usecase

import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.VictoryCondition
import com.skyjopoints.app.domain.model.StopCondition
import com.skyjopoints.app.domain.model.Player
import com.skyjopoints.app.domain.port.GameRepository

class StartGameUseCase(private val repository: GameRepository) {
    operator fun invoke(
        playerNames: List<String>,
        maxPoints: Int? = null,
        gameName: String? = null,
        victoryCondition: VictoryCondition = VictoryCondition.FEWEST_POINTS,
        maxRounds: Int? = null,
        stopCondition: StopCondition? = null
    ): Game {
        val players = playerNames.mapIndexed { index, name ->
            Player(id = "p_${index}_${System.currentTimeMillis()}", name = name.trim())
        }
        val newGame = Game(
            id = "g_${System.currentTimeMillis()}",
            players = players,
            rounds = emptyList(),
            createdAt = System.currentTimeMillis(),
            maxPoints = maxPoints,
            name = gameName?.trim()?.takeIf { it.isNotEmpty() },
            victoryCondition = victoryCondition,
            maxRounds = maxRounds,
            stopCondition = stopCondition
        )
        repository.saveActiveGame(newGame)
        repository.saveGameToHistory(newGame)
        repository.savePlayerNames(playerNames)
        return newGame
    }
}
