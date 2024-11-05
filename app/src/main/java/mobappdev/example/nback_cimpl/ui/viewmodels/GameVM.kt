package mobappdev.example.nback_cimpl.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

import mobappdev.example.nback_cimpl.model.Game
import mobappdev.example.nback_cimpl.model.GameState
import mobappdev.example.nback_cimpl.model.GameType
import mobappdev.example.nback_cimpl.model.AudioPlayer // Import the AudioPlayer class

/**
 * This is the GameViewModel.
 *
 * Interface for the GameViewModel to facilitate testing and separation of concerns.
 */
interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int

    fun setGameType(gameType: GameType)
    fun startGame()
    fun checkMatch(): Boolean
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val audioPlayer: AudioPlayer // Add AudioPlayer as a dependency
) : GameViewModel, ViewModel() {

    private val game = Game(audioPlayer = audioPlayer)  // Pass AudioPlayer to Game instance

    override val gameState: StateFlow<GameState> get() = game.gameState
    override val score: StateFlow<Int> get() = game.score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int> get() = _highscore

    override val nBack: Int = game.gameState.value.nValue

    private var gameJob: Job? = null  // Job for managing game loop

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }

    override fun setGameType(gameType: GameType) {
        game.setGameType(gameType)
    }

    override fun startGame() {
        // Cancel any ongoing game job to avoid multiple rounds
        gameJob?.cancel()

        // Reset the game state
        game.startGame()

        // Start new game loop
        gameJob = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> game.runAudioGame()
                GameType.Visual -> game.runVisualGame()
            }

            // Update high score if needed
            if (score.value > highscore.value) {
                _highscore.value = score.value
                userPreferencesRepository.saveHighScore(score.value)
            }
        }
    }

    override fun checkMatch(): Boolean {
        return game.checkMatch()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY] as? GameApplication)
                return GameVM(application.userPreferencesRespository, AudioPlayer(application)) as T // Pass the AudioPlayer
            }
        }
    }
}




