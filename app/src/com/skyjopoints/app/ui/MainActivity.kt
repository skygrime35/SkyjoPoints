package com.skyjopoints.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.skyjopoints.app.R
import com.skyjopoints.app.di.ServiceLocator
import com.skyjopoints.app.domain.model.Game
import com.skyjopoints.app.domain.model.Player
import com.skyjopoints.app.ui.view.*

class MainActivity : Activity() {

    private lateinit var container: FrameLayout

    // Lazy load use cases
    private val startGameUseCase by lazy { ServiceLocator.provideStartGameUseCase() }
    private val addRoundUseCase by lazy { ServiceLocator.provideAddRoundUseCase() }
    private val deleteRoundUseCase by lazy { ServiceLocator.provideDeleteRoundUseCase() }
    private val resetGameUseCase by lazy { ServiceLocator.provideResetGameUseCase() }
    private val archiveGameUseCase by lazy { ServiceLocator.provideArchiveGameUseCase() }

    // State preservation for active round entry
    private val tempRoundScores = mutableMapOf<String, Int>()
    
    private var roundEntryController: RoundEntryViewController? = null

    enum class Screen {
        SETUP,
        SCOREBOARD,
        ROUND_ENTRY,
        GRID_CALCULATOR,
        HISTORY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ServiceLocator.init(this)
        
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container)

        // Resume active game if present, else show player setup
        val activeGame = ServiceLocator.getRepository().getActiveGame()
        if (activeGame != null) {
            navigateTo(Screen.SCOREBOARD)
        } else {
            navigateTo(Screen.SETUP)
        }
    }

    private fun navigateTo(screen: Screen, extraData: Any? = null) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(this)

        when (screen) {
            Screen.SETUP -> {
                val view = inflater.inflate(R.layout.screen_player_setup, container, false)
                container.addView(view)
                PlayerSetupViewController(
                    root = view,
                    onStartGame = { playerNames ->
                        startGameUseCase(playerNames)
                        navigateTo(Screen.SCOREBOARD)
                    },
                    onViewHistory = {
                        navigateTo(Screen.HISTORY)
                    }
                )
            }
            Screen.SCOREBOARD -> {
                val game = ServiceLocator.getRepository().getActiveGame()
                if (game == null) {
                    navigateTo(Screen.SETUP)
                    return
                }
                val view = inflater.inflate(R.layout.screen_scoreboard, container, false)
                container.addView(view)
                val controller = ScoreboardViewController(
                    root = view,
                    onAddRound = {
                        tempRoundScores.clear()
                        navigateTo(Screen.ROUND_ENTRY)
                    },
                    onDeleteLastRound = {
                        deleteRoundUseCase()
                        navigateTo(Screen.SCOREBOARD)
                    },
                    onResetGame = {
                        resetGameUseCase()
                        navigateTo(Screen.SCOREBOARD)
                    },
                    onQuitGame = {
                        archiveGameUseCase()
                        navigateTo(Screen.SETUP)
                    },
                    onAcknowledgeWinner = {
                        archiveGameUseCase()
                        navigateTo(Screen.SETUP)
                    }
                )
                controller.updateGame(game)
            }
            Screen.ROUND_ENTRY -> {
                val game = ServiceLocator.getRepository().getActiveGame()
                if (game == null) {
                    navigateTo(Screen.SETUP)
                    return
                }
                val view = inflater.inflate(R.layout.screen_round_entry, container, false)
                container.addView(view)
                
                roundEntryController = RoundEntryViewController(
                    root = view,
                    game = game,
                    initialScores = tempRoundScores,
                    onOpenGridHelper = { player ->
                        saveTempInputs()
                        navigateTo(Screen.GRID_CALCULATOR, player)
                    },
                    onSave = { scores ->
                        addRoundUseCase(scores)
                        navigateTo(Screen.SCOREBOARD)
                    },
                    onCancel = {
                        navigateTo(Screen.SCOREBOARD)
                    }
                )
            }
            Screen.GRID_CALCULATOR -> {
                val player = extraData as? Player
                if (player == null) {
                    navigateTo(Screen.ROUND_ENTRY)
                    return
                }
                val view = inflater.inflate(R.layout.screen_grid_calculator, container, false)
                container.addView(view)
                GridCalculatorViewController(
                    root = view,
                    player = player,
                    onConfirm = { calculatedScore ->
                        tempRoundScores[player.id] = calculatedScore
                        navigateTo(Screen.ROUND_ENTRY)
                    },
                    onCancel = {
                        navigateTo(Screen.ROUND_ENTRY)
                    }
                )
            }
            Screen.HISTORY -> {
                val view = inflater.inflate(R.layout.screen_history, container, false)
                container.addView(view)
                HistoryViewController(
                    root = view,
                    repository = ServiceLocator.getRepository(),
                    onBack = {
                        navigateTo(Screen.SETUP)
                    }
                )
            }
        }
    }

    private fun saveTempInputs() {
        roundEntryController?.let { controller ->
            tempRoundScores.clear()
            tempRoundScores.putAll(controller.getEnteredScores())
        }
    }

    override fun onBackPressed() {
        val currentScreen = getCurrentScreen()
        when (currentScreen) {
            Screen.SCOREBOARD -> {
                // Save progress to history on back-press from scoreboard
                archiveGameUseCase()
                navigateTo(Screen.SETUP)
            }
            Screen.ROUND_ENTRY -> {
                navigateTo(Screen.SCOREBOARD)
            }
            Screen.GRID_CALCULATOR -> {
                navigateTo(Screen.ROUND_ENTRY)
            }
            Screen.HISTORY -> {
                navigateTo(Screen.SETUP)
            }
            Screen.SETUP -> {
                super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    private fun getCurrentScreen(): Screen? {
        if (container.childCount == 0) return null
        val child = container.getChildAt(0)
        return when {
            child.findViewById<View>(R.id.players_list_container) != null -> Screen.SETUP
            child.findViewById<View>(R.id.table_scoreboard) != null -> Screen.SCOREBOARD
            child.findViewById<View>(R.id.round_players_container) != null -> Screen.ROUND_ENTRY
            child.findViewById<View>(R.id.grid_table) != null -> Screen.GRID_CALCULATOR
            child.findViewById<View>(R.id.history_list_container) != null -> Screen.HISTORY
            else -> null
        }
    }
}
