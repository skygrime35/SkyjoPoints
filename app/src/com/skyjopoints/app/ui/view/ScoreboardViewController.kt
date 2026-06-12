package com.skyjopoints.app.ui.view

import android.app.AlertDialog
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
    private val onEditRound: (Int) -> Unit,
    private val onEndGame: () -> Unit,
    private val onNewGame: () -> Unit,
    private val onQuitGame: () -> Unit,
    private val onAcknowledgeWinner: () -> Unit,
    private val onToggleTheme: () -> Unit
) {
    private val context = root.context
    private val tableScoreboard: TableLayout = root.findViewById(R.id.table_scoreboard)
    private val btnAddRound: Button = root.findViewById(R.id.btn_add_round)
    private val btnEndGame: Button = root.findViewById(R.id.btn_end_game)
    private val btnNewGame: Button = root.findViewById(R.id.btn_new_game)
    private val btnQuitGame: Button = root.findViewById(R.id.btn_quit_game)
    private val btnThemeToggle: Button = root.findViewById(R.id.btn_theme_toggle)
    
    private val tvTitle: TextView = root.findViewById(R.id.tv_scoreboard_title)
    
    // Winner overlay
    private val overlayWinner: FrameLayout = root.findViewById(R.id.overlay_winner)
    private val tvWinnerDetails: TextView = root.findViewById(R.id.tv_winner_details)
    private val btnWinnerOk: Button = root.findViewById(R.id.btn_winner_ok)

    fun updateGame(game: Game) {
        if (!game.name.isNullOrBlank()) {
            tvTitle.text = game.name
        } else {
            tvTitle.text = context.getString(R.string.scoreboard)
        }
        renderTable(game)

        if (game.isFinished()) {
            val winner = game.getWinner()
            if (winner != null) {
                val sb = StringBuilder()
                val score = game.getPlayerTotalScore(winner.id)
                sb.append("🏆 Félicitations à ${winner.name} qui remporte la partie avec $score points !\n\n")
                
                sb.append("Classement final :\n")
                val leaderboard = game.getLeaderboard()
                leaderboard.forEachIndexed { index, (player, pScore) ->
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "${index + 1}."
                    }
                    sb.append("$medal ${player.name} : $pScore points\n")
                }
                
                tvWinnerDetails.text = sb.toString()
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
        
        btnEndGame.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Terminer la partie")
                .setMessage("Êtes-vous sûr de vouloir terminer la partie maintenant ?")
                .setPositiveButton("Terminer") { _, _ -> onEndGame() }
                .setNegativeButton("Annuler", null)
                .show()
        }

        btnNewGame.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Nouvelle partie")
                .setMessage("Êtes-vous sûr de vouloir commencer une nouvelle partie ? La partie en cours sera conservée dans l'historique.")
                .setPositiveButton("Nouvelle Partie") { _, _ -> onNewGame() }
                .setNegativeButton("Annuler", null)
                .show()
        }

        btnQuitGame.setOnClickListener { onQuitGame() }

        val isLightTheme = com.skyjopoints.app.di.ServiceLocator.getRepository().getThemeMode() == "light"
        btnThemeToggle.text = if (isLightTheme) "🌙" else "☀️"
        btnThemeToggle.setOnClickListener { onToggleTheme() }
        
        // Disable end game button if there are no rounds to calculate a result
        btnEndGame.isEnabled = game.rounds.isNotEmpty()
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
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        val selectableBackground = typedValue.resourceId

        game.rounds.forEachIndexed { index, round ->
            val row = TableRow(context).apply {
                setPadding(0, 2, 0, 2)
                setBackgroundResource(selectableBackground)
                isClickable = true
                setOnClickListener {
                    onEditRound(index)
                }
            }
            
            row.addView(createCell("M${index + 1}", isBold = true))
            game.players.forEach { player ->
                val score = round.scores[player.id] ?: 0
                row.addView(createCell(score.toString()))
            }
            tableScoreboard.addView(row)
        }

        // 3. Total Row
        val totalRow = TableRow(context).apply {
            setPadding(0, 6, 0, 6)
            setBackgroundColor(0x1F808080) // Light grey highlight
        }
        totalRow.addView(createCell("Total", isBold = true))
        game.players.forEach { player ->
            val totalScore = game.getPlayerTotalScore(player.id)
            totalRow.addView(createCell(totalScore.toString(), isBold = true))
        }
        tableScoreboard.addView(totalRow)
    }

    private fun createHeaderCell(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)
            setTextColor(0xFF808080.toInt()) // Mid grey
            textSize = 14f
        }
    }

    private fun createCell(text: String, isBold: Boolean = false): TextView {
        val textColorAttr = if (isBold) R.attr.themeTextPrimary else R.attr.themeTextSecondary
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(textColorAttr, typedValue, true)
        val color = typedValue.data

        return TextView(context).apply {
            this.text = text
            if (isBold) {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            gravity = Gravity.CENTER
            setPadding(16, 12, 16, 12)
            setTextColor(color)
            textSize = 15f
        }
    }
}
