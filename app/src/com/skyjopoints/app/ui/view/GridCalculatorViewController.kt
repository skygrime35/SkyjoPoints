package com.skyjopoints.app.ui.view

import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.skyjopoints.app.R
import com.skyjopoints.app.di.ServiceLocator
import com.skyjopoints.app.domain.model.Player

class GridCalculatorViewController(
    private val root: View,
    private val player: Player,
    private val onConfirm: (Int) -> Unit,
    private val onCancel: () -> Unit
) {
    private val context = root.context
    private val tvTitle: TextView = root.findViewById(R.id.tv_grid_title)
    private val tvSummary: TextView = root.findViewById(R.id.tv_grid_summary)
    
    // 3x4 card buttons matrix
    private val cardButtons = arrayOf(
        arrayOf(root.findViewById<Button>(R.id.card_0_0), root.findViewById<Button>(R.id.card_0_1), root.findViewById<Button>(R.id.card_0_2), root.findViewById<Button>(R.id.card_0_3)),
        arrayOf(root.findViewById<Button>(R.id.card_1_0), root.findViewById<Button>(R.id.card_1_1), root.findViewById<Button>(R.id.card_1_2), root.findViewById<Button>(R.id.card_1_3)),
        arrayOf(root.findViewById<Button>(R.id.card_2_0), root.findViewById<Button>(R.id.card_2_1), root.findViewById<Button>(R.id.card_2_2), root.findViewById<Button>(R.id.card_2_3))
    )

    // Selection position
    private var selectedRow = 0
    private var selectedCol = 0

    // Grid values (3x4 list)
    private val gridValues = List(3) { MutableList<Int?>(4) { null } }

    private val calculateGridScoreUseCase = ServiceLocator.provideCalculateGridScoreUseCase()

    init {
        tvTitle.text = context.getString(R.string.grid_helper_title, player.name)

        // Setup Card buttons click listeners
        for (r in 0..2) {
            for (c in 0..3) {
                val button = cardButtons[r][c]
                button.setOnClickListener {
                    selectedRow = r
                    selectedCol = c
                    refreshUI()
                }
            }
        }

        // Setup Keypad value listeners
        setupValueButton(R.id.btn_val_neg2, -2)
        setupValueButton(R.id.btn_val_neg1, -1)
        setupValueButton(R.id.btn_val_0, 0)
        setupValueButton(R.id.btn_val_1, 1)
        setupValueButton(R.id.btn_val_2, 2)
        setupValueButton(R.id.btn_val_3, 3)
        setupValueButton(R.id.btn_val_4, 4)
        setupValueButton(R.id.btn_val_5, 5)
        setupValueButton(R.id.btn_val_6, 6)
        setupValueButton(R.id.btn_val_7, 7)
        setupValueButton(R.id.btn_val_8, 8)
        setupValueButton(R.id.btn_val_9, 9)
        setupValueButton(R.id.btn_val_10, 10)
        setupValueButton(R.id.btn_val_11, 11)
        setupValueButton(R.id.btn_val_12, 12)

        root.findViewById<Button>(R.id.btn_val_clear).setOnClickListener {
            setCardValue(null)
        }

        // Setup Actions
        root.findViewById<Button>(R.id.btn_cancel_grid).setOnClickListener { onCancel() }
        root.findViewById<Button>(R.id.btn_confirm_grid).setOnClickListener {
            val totalScore = calculateGridScoreUseCase(gridValues)
            onConfirm(totalScore)
        }

        refreshUI()
    }

    private fun setupValueButton(btnId: Int, value: Int) {
        root.findViewById<Button>(btnId).setOnClickListener {
            setCardValue(value)
        }
    }

    private fun setCardValue(value: Int?) {
        gridValues[selectedRow][selectedCol] = value
        
        // Auto-advance selection to next card (left-to-right, top-to-bottom)
        advanceSelection()
        
        refreshUI()
    }

    private fun advanceSelection() {
        if (selectedCol < 3) {
            selectedCol++
        } else if (selectedRow < 2) {
            selectedRow++
            selectedCol = 0
        } else {
            // Reached last card, loop back or stay
            selectedRow = 0
            selectedCol = 0
        }
    }

    private fun refreshUI() {
        // 1. Identify which columns are discarded
        val discardedColumns = BooleanArray(4) { false }
        var discardedCount = 0
        for (c in 0..3) {
            val v0 = gridValues[0][c]
            val v1 = gridValues[1][c]
            val v2 = gridValues[2][c]
            if (v0 != null && v0 == v1 && v1 == v2) {
                discardedColumns[c] = true
                discardedCount++
            }
        }

        // 2. Draw card buttons
        for (r in 0..2) {
            for (c in 0..3) {
                val button = cardButtons[r][c]
                val value = gridValues[r][c]
                val isSelected = (r == selectedRow && c == selectedCol)
                val isDiscarded = discardedColumns[c]

                // Configure Text
                val baseText = value?.toString() ?: "?"
                button.text = if (isSelected) "[$baseText]" else baseText

                // Configure Colors
                val bgColor: Int
                val textColor: Int

                if (isDiscarded) {
                    // Eliminate visual clutter by fading out discarded cards
                    bgColor = resolveColorAttr(R.attr.themeSurfaceCard)
                    textColor = resolveColorAttr(R.attr.themeTextSecondary)
                    button.alpha = 0.4f
                    // If discarded, maybe show strike-through text
                    button.text = if (isSelected) "[x]" else "x"
                } else {
                    button.alpha = 1.0f
                    if (value == null) {
                        bgColor = resolveColorAttr(R.attr.themeSurfaceCard)
                        textColor = resolveColorAttr(R.attr.themeTextSecondary)
                    } else {
                        when (value) {
                            -2 -> {
                                bgColor = context.getColor(R.color.card_blue_dark)
                                textColor = 0xFFFFFFFF.toInt() // White text for colored card
                            }
                            -1, 0 -> {
                                bgColor = context.getColor(R.color.card_cyan)
                                textColor = 0xFFFFFFFF.toInt() // White text for colored card
                            }
                            in 1..4 -> {
                                bgColor = context.getColor(R.color.card_green)
                                textColor = 0xFFFFFFFF.toInt() // White text for colored card
                            }
                            in 5..8 -> {
                                bgColor = context.getColor(R.color.card_yellow)
                                textColor = 0xFF121214.toInt() // Dark charcoal text for yellow card legibility
                            }
                            else -> {
                                bgColor = context.getColor(R.color.card_red)
                                textColor = 0xFFFFFFFF.toInt() // White text for colored card
                            }
                        }
                    }
                }

                // Apply Colors
                button.backgroundTintList = ColorStateList.valueOf(bgColor)
                button.setTextColor(textColor)

                // Highlight selected card border / stroke if it's selected
                if (isSelected && !isDiscarded) {
                    button.setTextColor(resolveColorAttr(R.attr.themeAccent))
                }
            }
        }

        // 3. Update summary text
        val currentScore = calculateGridScoreUseCase(gridValues)
        if (discardedCount > 0) {
            tvSummary.text = "Score : $currentScore pts | $discardedCount col. éliminée(s) (0 pt)"
        } else {
            tvSummary.text = "Score : $currentScore pts | Saisir les cartes"
        }
    }

    private fun resolveColorAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
