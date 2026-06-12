package com.skyjopoints.app.domain.model

enum class VictoryCondition {
    FEWEST_POINTS,
    MOST_POINTS
}

enum class StopCondition {
    ANY_EXCEEDS,
    ALL_EXCEED
}

data class Game(
    val id: String,
    val players: List<Player>,
    val rounds: List<Round>,
    val createdAt: Long,
    val maxPoints: Int? = null,
    val name: String? = null,
    val victoryCondition: VictoryCondition = VictoryCondition.FEWEST_POINTS,
    val maxRounds: Int? = null,
    val stopCondition: StopCondition? = null,
    val manuallyFinished: Boolean = false
) {
    fun getPlayerTotalScore(playerId: String): Int {
        return rounds.sumOf { it.scores[playerId] ?: 0 }
    }

    fun isFinished(): Boolean {
        if (manuallyFinished) return true
        if (rounds.isEmpty()) return false
        
        // 1. Check max rounds count first
        val roundsLimit = maxRounds
        if (roundsLimit != null && rounds.size >= roundsLimit) {
            return true
        }

        // 2. Check max points limit
        val limit = maxPoints
        if (limit != null) {
            val pointsFinished = when (stopCondition ?: StopCondition.ANY_EXCEEDS) {
                StopCondition.ANY_EXCEEDS -> {
                    players.any { getPlayerTotalScore(it.id) >= limit }
                }
                StopCondition.ALL_EXCEED -> {
                    players.all { getPlayerTotalScore(it.id) >= limit }
                }
            }
            if (pointsFinished) return true
        }

        return false
    }

    fun getWinner(): Player? {
        if (!isFinished()) return null
        
        return when (victoryCondition) {
            VictoryCondition.FEWEST_POINTS -> players.minByOrNull { getPlayerTotalScore(it.id) }
            VictoryCondition.MOST_POINTS -> players.maxByOrNull { getPlayerTotalScore(it.id) }
        }
    }

    fun getLeaderboard(): List<Pair<Player, Int>> {
        val list = players.map { it to getPlayerTotalScore(it.id) }
        val ascending = victoryCondition == VictoryCondition.FEWEST_POINTS
        return if (ascending) list.sortedBy { it.second } else list.sortedByDescending { it.second }
    }
}
