package com.skyjopoints.app.ui.view

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.skyjopoints.app.R
import com.skyjopoints.app.domain.port.GameRepository
import com.skyjopoints.app.domain.model.Game
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewController(
    private val root: View,
    private val repository: GameRepository,
    private val onBack: () -> Unit,
    private val onResumeGame: (Game) -> Unit
) {
    private val context = root.context
    private val historyListContainer: LinearLayout = root.findViewById(R.id.history_list_container)
    private val tvEmptyHistory: TextView = root.findViewById(R.id.tv_empty_history)
    private val btnClearHistory: Button = root.findViewById(R.id.btn_clear_history)
    private val btnBack: Button = root.findViewById(R.id.btn_back_from_history)

    init {
        btnBack.setOnClickListener { onBack() }
        btnClearHistory.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Effacer tout l'historique")
                .setMessage("Voulez-vous supprimer toutes les parties enregistrées ?")
                .setPositiveButton("Tout effacer") { _, _ ->
                    AlertDialog.Builder(context)
                        .setTitle("Confirmation définitive")
                        .setMessage("Êtes-vous vraiment sûr de vouloir effacer tout l'historique ? Cette action est définitive.")
                        .setPositiveButton("Confirmer") { _, _ ->
                            repository.clearHistory()
                            refreshList()
                        }
                        .setNegativeButton("Annuler", null)
                        .show()
                }
                .setNegativeButton("Annuler", null)
                .show()
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

        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        val selectableBackground = typedValue.resourceId

        games.forEach { game ->
            val row = inflater.inflate(R.layout.item_history_game, historyListContainer, false)
            row.setBackgroundResource(selectableBackground)
            row.isClickable = true
            row.setOnClickListener {
                onResumeGame(game)
            }
            
            val tvDate = row.findViewById<TextView>(R.id.tv_history_date)
            tvDate.text = dateFormat.format(Date(game.createdAt))

            val tvPlayers = row.findViewById<TextView>(R.id.tv_history_players)
            val playersStr = game.players.joinToString(", ") { it.name }
            if (!game.name.isNullOrBlank()) {
                tvPlayers.text = "${game.name} (${playersStr})"
            } else {
                tvPlayers.text = playersStr
            }

            val tvWinner = row.findViewById<TextView>(R.id.tv_history_winner)
            val winner = game.getWinner()
            if (winner != null) {
                val winnerScore = game.getPlayerTotalScore(winner.id)
                tvWinner.text = context.getString(R.string.winner_label, winner.name, winnerScore)
            } else {
                tvWinner.text = "En cours..."
            }

            val tvRounds = row.findViewById<TextView>(R.id.tv_history_rounds_count)
            tvRounds.text = "${game.rounds.size} manches"

            val btnDelete = row.findViewById<Button>(R.id.btn_delete_game)
            btnDelete.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Supprimer la partie")
                    .setMessage("Voulez-vous supprimer cette partie de l'historique ?")
                    .setPositiveButton("Supprimer") { _, _ ->
                        AlertDialog.Builder(context)
                            .setTitle("Confirmation définitive")
                            .setMessage("Êtes-vous vraiment sûr ? Cette action est irréversible.")
                            .setPositiveButton("Confirmer") { _, _ ->
                                repository.deleteGameFromHistory(game.id)
                                refreshList()
                            }
                            .setNegativeButton("Annuler", null)
                            .show()
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }

            historyListContainer.addView(row)
        }
    }
}
