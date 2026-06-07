package com.skyjopoints.app.ui.view

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.skyjopoints.app.R
import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.Player

class RoundEntryViewController(
    private val root: View,
    private val game: Game,
    private val initialScores: Map<String, Int> = emptyMap(),
    private val onOpenGridHelper: (Player) -> Unit,
    private val onSave: (Map<String, Int>) -> Unit,
    private val onCancel: () -> Unit
) {
    private val context = root.context
    private val tvTitle: TextView = root.findViewById(R.id.tv_round_entry_title)
    private val roundPlayersContainer: LinearLayout = root.findViewById(R.id.round_players_container)
    private val btnCancel: Button = root.findViewById(R.id.btn_cancel_round)
    private val btnSave: Button = root.findViewById(R.id.btn_save_round)

    private val scoreInputs = mutableMapOf<String, EditText>()

    init {
        tvTitle.text = context.getString(R.string.enter_round_scores, game.rounds.size + 1)

        // 1. Setup Players List
        roundPlayersContainer.removeAllViews()
        val inflater = LayoutInflater.from(context)

        game.players.forEach { player ->
            val row = inflater.inflate(R.layout.item_round_player_input, roundPlayersContainer, false)
            
            val tvName = row.findViewById<TextView>(R.id.tv_player_name)
            tvName.text = player.name

            val etScore = row.findViewById<EditText>(R.id.et_player_score)
            
            // Pre-fill score if provided in initialScores
            val prefilledScore = initialScores[player.id]
            if (prefilledScore != null) {
                etScore.setText(prefilledScore.toString())
            }

            scoreInputs[player.id] = etScore

            val btnGrid = row.findViewById<Button>(R.id.btn_grid_helper)
            btnGrid.setOnClickListener {
                onOpenGridHelper(player)
            }

            roundPlayersContainer.addView(row)
        }

        // 2. Actions
        btnCancel.setOnClickListener { onCancel() }

        btnSave.setOnClickListener {
            val scores = mutableMapOf<String, Int>()
            var allValid = true

            for (player in game.players) {
                val et = scoreInputs[player.id]
                val scoreStr = et?.text?.toString()?.trim() ?: ""
                
                val score = if (scoreStr.isEmpty()) {
                    0
                } else {
                    try {
                        scoreStr.toInt()
                    } catch (e: NumberFormatException) {
                        allValid = false
                        0
                    }
                }
                scores[player.id] = score
            }

            if (!allValid) {
                Toast.makeText(context, "Veuillez entrer des scores valides (nombres entiers).", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onSave(scores)
        }
    }

    /**
     * Saves current inputs so they can be retrieved externally before opening grid.
     */
    fun getEnteredScores(): Map<String, Int> {
        val scores = mutableMapOf<String, Int>()
        for ((playerId, et) in scoreInputs) {
            val text = et.text.toString().trim()
            scores[playerId] = text.toIntOrNull() ?: 0
        }
        return scores
    }

    /**
     * Updates the score EditText for a specific player (called after grid calculation).
     */
    fun updatePlayerScore(playerId: String, score: Int) {
        scoreInputs[playerId]?.setText(score.toString())
    }
}
