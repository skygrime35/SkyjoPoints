package com.skyjopoints.app.ui.view

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.skyjopoints.app.R
import com.skyjopoints.app.domain.port.GameRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewController(
    private val root: View,
    private val repository: GameRepository,
    private val onBack: () -> Unit
) {
    private val context = root.context
    private val historyListContainer: LinearLayout = root.findViewById(R.id.history_list_container)
    private val tvEmptyHistory: TextView = root.findViewById(R.id.tv_empty_history)
    private val btnClearHistory: Button = root.findViewById(R.id.btn_clear_history)
    private val btnBack: Button = root.findViewById(R.id.btn_back_from_history)

    init {
        btnBack.setOnClickListener { onBack() }
        btnClearHistory.setOnClickListener {
            repository.clearHistory()
            refreshList()
        }
        refreshList()
    }

    private fun refreshList() {
        historyListContainer.removeAllViews()
        val games = repository.getGameHistory()

        if (games.isEmpty()) {
            tvEmptyHistory.visibility = View.VISIBLE
            btnClearHistory.visibility = View.GONE
            return
        }

        tvEmptyHistory.visibility = View.GONE
        btnClearHistory.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(context)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        games.forEach { game ->
            val row = inflater.inflate(R.layout.item_history_game, historyListContainer, false)
            
            val tvDate = row.findViewById<TextView>(R.id.tv_history_date)
            tvDate.text = dateFormat.format(Date(game.createdAt))

            val tvPlayers = row.findViewById<TextView>(R.id.tv_history_players)
            val playersStr = game.players.joinToString(", ") { it.name }
            tvPlayers.text = playersStr

            val tvWinner = row.findViewById<TextView>(R.id.tv_history_winner)
            val winner = game.getWinner()
            if (winner != null) {
                val winnerScore = game.getPlayerTotalScore(winner.id)
                tvWinner.text = context.getString(R.string.winner_label, winner.name, winnerScore)
            }

            val tvRounds = row.findViewById<TextView>(R.id.tv_history_rounds_count)
            tvRounds.text = "${game.rounds.size} manches"

            historyListContainer.addView(row)
        }
    }
}
