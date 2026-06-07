package com.skyjopoints.app.domain.model

data class Round(
    val scores: Map<String, Int> // playerId -> score in this round
)
