package com.skyjopoints.app.ui.view

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.skyjopoints.app.R
import com.skyjopoints.app.domain.model.Game

class ScoreboardViewController(
    private val root: View,
    private val onAddRound: () -> Unit,
    private val onDeleteLastRound: () -> Unit,
    private val onResetGame: () -> Unit,
    private val onQuitGame: () -> Unit,
    private val onAcknowledgeWinner: () -> Unit
) {
    private val context = root.context
    private val tableScoreboard: TableLayout = root.findViewById(R.id.table_scoreboard)
    private val btnAddRound: Button = root.findViewById(R.id.btn_add_round)
    private val btnDeleteRound: Button = root.findViewById(R.id.btn_delete_round)
    private val btnResetGame: Button = root.findViewById(R.id.btn_reset_game)
    private val btnQuitGame: Button = root.findViewById(R.id.btn_quit_game)
    
    // Winner overlay
    private val overlayWinner: FrameLayout = root.findViewById(R.id.overlay_winner)
    private val tvWinnerDetails: TextView = root.findViewById(R.id.tv_winner_details)
    private val btnWinnerOk: Button = root.findViewById(R.id.btn_winner_ok)

    fun updateGame(game: Game) {
        renderTable(game)

        if (game.isFinished()) {
            val winner = game.getWinner()
            if (winner != null) {
                val score = game.getPlayerTotalScore(winner.id)
                tvWinnerDetails.text = context.getString(R.string.end_game_msg, winner.name, score)
                overlayWinner.visibility = View.VISIBLE
            }
        } else {
            overlayWinner.visibility = View.GONE
        }

        btnWinnerOk.setOnClickListener {
            overlayWinner.visibility = View.GONE
            onAcknowledgeWinner()
        }

        btnAddRound.setOnClickListener { onAddRound() }
        btnDeleteRound.setOnClickListener { onDeleteLastRound() }
        btnResetGame.setOnClickListener { onResetGame() }
        btnQuitGame.setOnClickListener { onQuitGame() }
        
        // Disable delete round button if there are no rounds to delete
        btnDeleteRound.isEnabled = game.rounds.isNotEmpty()
    }

    private fun renderTable(game: Game) {
        tableScoreboard.removeAllViews()

        // 1. Header Row (Rounds | Player 1 | Player 2 ...)
        val headerRow = TableRow(context).apply {
            setPadding(0, 4, 0, 4)
        }
        headerRow.addView(createHeaderCell("Manches"))
        game.players.forEach { player ->
            headerRow.addView(createHeaderCell(player.name))
        }
        tableScoreboard.addView(headerRow)

        // 2. Round rows
        game.rounds.forEachIndexed { index, round ->
            val row = TableRow(context).apply {
                setPadding(0, 2, 0, 2)
            }
            row.addView(createCell("M${index + 1}", isBold = false))
            
            game.players.forEach { player ->
                val score = round.scores[player.id] ?: 0
                row.addView(createCell(score.toString(), isBold = false))
            }
            tableScoreboard.addView(row)
        }

        // 3. Totals Row
        val totalRow = TableRow(context).apply {
            setBackgroundColor(context.getColor(R.color.surface_card))
            setPadding(0, 6, 0, 6)
        }
        totalRow.addView(createCell("Total", isBold = true, textColor = context.getColor(R.color.text_secondary)))
        game.players.forEach { player ->
            val totalScore = game.getPlayerTotalScore(player.id)
            totalRow.addView(createCell(totalScore.toString(), isBold = true, textColor = context.getColor(R.color.accent)))
        }
        tableScoreboard.addView(totalRow)
    }

    private fun createHeaderCell(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            this.setPadding(24, 16, 24, 16)
            this.textSize = 15f
            this.typeface = Typeface.DEFAULT_BOLD
            this.setTextColor(context.getColor(R.color.primary))
            this.gravity = Gravity.CENTER
        }
    }

    private fun createCell(text: String, isBold: Boolean, textColor: Int = context.getColor(R.color.text_primary)): TextView {
        return TextView(context).apply {
            this.text = text
            this.setPadding(24, 16, 24, 16)
            this.textSize = 15f
            if (isBold) {
                this.typeface = Typeface.DEFAULT_BOLD
            }
            this.setTextColor(textColor)
            this.gravity = Gravity.CENTER
        }
    }
}
