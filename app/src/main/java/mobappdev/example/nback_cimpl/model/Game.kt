package mobappdev.example.nback_cimpl.model

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mobappdev.example.nback_cimpl.NBackHelper

data class GameState(
    val gameType: GameType = GameType.Visual,
    val eventValue: Int = -1,
    val nValue: Int = 2,
    val timeBetweenEvents: Long = 2000L,
    val numberOfEvents: Int = 10,
    var round: Int = 0
)

enum class GameType {
    Audio,
    Visual
}

class Game(
    private val nBackHelper: NBackHelper = NBackHelper(),
    private val audioPlayer: AudioPlayer, // Inject the AudioPlayer
    private val nBack: Int = 2,
    private val eventInterval: Long = 2000L
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> get() = _gameState

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> get() = _score

    private var events = emptyArray<Int>()  // Array with all events

    fun setGameType(gameType: GameType) {
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    fun startGame() {
        _score.value = 0  // Reset score
        _gameState.value = _gameState.value.copy(round = 0)

        // Generate events
        events = nBackHelper.generateNBackString(10, 8, 30, nBack).toList().toTypedArray()
    }

    fun checkMatch(): Boolean {
        return if (events.isNotEmpty()) {
            val currentEventValue = _gameState.value.eventValue
            val expectedValue = events.getOrNull((_gameState.value.round-1) - nBack)
            val isMatch = currentEventValue == expectedValue

            if (isMatch) {
                _score.value += 1
            }
            isMatch
        } else {
            false
        }
    }

    suspend fun runVisualGame() {
        for (value in events) {
            _gameState.value = _gameState.value.copy(round = _gameState.value.round + 1)
            _gameState.value = _gameState.value.copy(eventValue = value)
            delay(eventInterval)
        }
    }

    suspend fun runAudioGame() {
        val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I")

        for (value in events) {
            _gameState.value = _gameState.value.copy(round = _gameState.value.round + 1)
            _gameState.value = _gameState.value.copy(eventValue = value)

            val letterToPlay = letters.getOrNull(value)

            letterToPlay?.let { audioPlayer.playLetter(it) }

            delay(eventInterval)
        }
    }

}
