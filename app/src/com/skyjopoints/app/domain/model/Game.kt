package com.skyjopoints.app.domain.model

data class Game(
    val id: String,
    val players: List<Player>,
    val rounds: List<Round>,
    val createdAt: Long
) {
    fun getPlayerTotalScore(playerId: String): Int {
        return rounds.sumOf { it.scores[playerId] ?: 0 }
    }

    fun isFinished(): Boolean {
        if (rounds.isEmpty()) return false
        return players.any { getPlayerTotalScore(it.id) >= 100 }
    }

    fun getWinner(): Player? {
        if (!isFinished()) return null
        return players.minByOrNull { getPlayerTotalScore(it.id) }
    }

    fun getLeaderboard(): List<Pair<Player, Int>> {
        return players.map { it to getPlayerTotalScore(it.id) }
            .sortedBy { it.second }
    }
}
