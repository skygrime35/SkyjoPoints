package com.skyjopoints.app.ui.view

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import com.skyjopoints.app.domain.model.VictoryCondition
import com.skyjopoints.app.domain.model.StopCondition
import com.skyjopoints.app.R

class PlayerSetupViewController(
    private val root: View,
    private val onStartGame: (List<String>, Int?, String?, VictoryCondition, Int?, StopCondition?) -> Unit,
    private val onViewHistory: () -> Unit,
    private val onToggleTheme: () -> Unit
) {
    private val playersListContainer: LinearLayout = root.findViewById(R.id.players_list_container)
    private val btnAddPlayer: Button = root.findViewById(R.id.btn_add_player)
    private val btnStartGame: Button = root.findViewById(R.id.btn_start_game)
    private val btnViewHistory: Button = root.findViewById(R.id.btn_view_history)
    private val btnThemeToggle: Button = root.findViewById(R.id.btn_theme_toggle)
    private val etMaxPoints: EditText = root.findViewById(R.id.et_max_points)
    private val etGameName: EditText = root.findViewById(R.id.et_game_name)
    private val btnToggleSettings: Button = root.findViewById(R.id.btn_toggle_settings)
    private val layoutGameSettings: LinearLayout = root.findViewById(R.id.layout_game_settings)
    private val rgVictoryCondition: RadioGroup = root.findViewById(R.id.rg_victory_condition)
    private val etMaxRounds: EditText = root.findViewById(R.id.et_max_rounds)
    private val layoutStopCondition: LinearLayout = root.findViewById(R.id.layout_stop_condition)
    private val rgStopCondition: RadioGroup = root.findViewById(R.id.rg_stop_condition)

    init {
        // Configure theme toggle button emoji
        val isLightTheme = com.skyjopoints.app.di.ServiceLocator.getRepository().getThemeMode() == "light"
        btnThemeToggle.text = if (isLightTheme) "🌙" else "☀️"
        btnThemeToggle.setOnClickListener { onToggleTheme() }

        // Setup togglable options panel
        btnToggleSettings.setOnClickListener {
            if (layoutGameSettings.visibility == View.VISIBLE) {
                layoutGameSettings.visibility = View.GONE
                btnToggleSettings.text = "Paramètres de la partie ⚙️"
            } else {
                layoutGameSettings.visibility = View.VISIBLE
                btnToggleSettings.text = "Masquer les paramètres ⚙️"
            }
        }

        // Dynamically show/hide stop condition based on max points limit presence
        etMaxPoints.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasMaxPoints = s?.toString()?.trim()?.isNotEmpty() ?: false
                layoutStopCondition.visibility = if (hasMaxPoints) View.VISIBLE else View.GONE
            }
        })

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
                val etName = row.findViewById<AutoCompleteTextView>(R.id.et_player_name)
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    names.add(name)
                }
            }

            if (names.size < 2) {
                Toast.makeText(root.context, root.context.getString(R.string.min_players_warn), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uniqueNames = names.distinct()
            if (uniqueNames.size != names.size) {
                Toast.makeText(root.context, "Les noms des joueurs doivent être uniques.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val maxPointsStr = etMaxPoints.text.toString().trim()
            val maxPoints = if (maxPointsStr.isNotEmpty()) maxPointsStr.toIntOrNull() else null
            val gameName = etGameName.text.toString().trim()

            val victoryCondition = when (rgVictoryCondition.checkedRadioButtonId) {
                R.id.rb_victory_most -> VictoryCondition.MOST_POINTS
                else -> VictoryCondition.FEWEST_POINTS
            }

            val maxRoundsStr = etMaxRounds.text.toString().trim()
            val maxRounds = if (maxRoundsStr.isNotEmpty()) maxRoundsStr.toIntOrNull() else null

            val stopCondition = if (maxPoints != null) {
                when (rgStopCondition.checkedRadioButtonId) {
                    R.id.rb_stop_all -> StopCondition.ALL_EXCEED
                    else -> StopCondition.ANY_EXCEEDS
                }
            } else null

            onStartGame(names, maxPoints, gameName, victoryCondition, maxRounds, stopCondition)
        }

        btnViewHistory.setOnClickListener {
            onViewHistory()
        }
    }

    private fun addPlayerInputRow() {
        val context = root.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.item_player_input, playersListContainer, false)

        val etPlayerName = itemView.findViewById<AutoCompleteTextView>(R.id.et_player_name)
        // Set hint with player index
        val playerNum = playersListContainer.childCount + 1
        etPlayerName.hint = "Joueur $playerNum"

        // Set up autocomplete suggestions
        val savedNames = com.skyjopoints.app.di.ServiceLocator.getRepository().getSavedPlayerNames()
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, savedNames)
        etPlayerName.setAdapter(adapter)

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
            val etName = row.findViewById<AutoCompleteTextView>(R.id.et_player_name)
            etName.hint = "Joueur ${i + 1}"
        }
    }
}
