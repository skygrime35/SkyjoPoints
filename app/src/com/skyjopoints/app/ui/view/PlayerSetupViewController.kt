package com.skyjopoints.app.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import com.skyjopoints.app.R

class PlayerSetupViewController(
    private val root: View,
    private val onStartGame: (List<String>) -> Unit,
    private val onViewHistory: () -> Unit
) {
    private val playersListContainer: LinearLayout = root.findViewById(R.id.players_list_container)
    private val btnAddPlayer: Button = root.findViewById(R.id.btn_add_player)
    private val btnStartGame: Button = root.findViewById(R.id.btn_start_game)
    private val btnViewHistory: Button = root.findViewById(R.id.btn_view_history)

    init {
        // Add 2 default players to begin with
        addPlayerInputRow()
        addPlayerInputRow()

        btnAddPlayer.setOnClickListener {
            if (playersListContainer.childCount < 8) {
                addPlayerInputRow()
            } else {
                Toast.makeText(root.context, root.context.getString(R.string.max_players_warn), Toast.LENGTH_SHORT).show()
            }
        }

        btnStartGame.setOnClickListener {
            val names = mutableListOf<String>()
            for (i in 0 until playersListContainer.childCount) {
                val row = playersListContainer.getChildAt(i)
                val etName = row.findViewById<EditText>(R.id.et_player_name)
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    names.add(name)
                }
            }

            if (names.size < 2) {
                Toast.makeText(root.context, root.context.getString(R.string.min_players_warn), Toast.LENGTH_SHORT).show()
            } else {
                onStartGame(names)
            }
        }

        btnViewHistory.setOnClickListener {
            onViewHistory()
        }
    }

    private fun addPlayerInputRow() {
        val context = root.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_player_input, playersListContainer, false)

        val etPlayerName = itemView.findViewById<EditText>(R.id.et_player_name)
        // Set hint with player index
        val playerNum = playersListContainer.childCount + 1
        etPlayerName.hint = "Joueur $playerNum"

        val btnDelete = itemView.findViewById<ImageButton>(R.id.btn_delete_player)
        btnDelete.setOnClickListener {
            if (playersListContainer.childCount > 2) {
                playersListContainer.removeView(itemView)
                // Re-index hints
                reindexPlayerHints()
            } else {
                Toast.makeText(context, context.getString(R.string.min_players_warn), Toast.LENGTH_SHORT).show()
            }
        }

        playersListContainer.addView(itemView)
    }

    private fun reindexPlayerHints() {
        for (i in 0 until playersListContainer.childCount) {
            val row = playersListContainer.getChildAt(i)
            val etName = row.findViewById<EditText>(R.id.et_player_name)
            etName.hint = "Joueur ${i + 1}"
        }
    }
}
