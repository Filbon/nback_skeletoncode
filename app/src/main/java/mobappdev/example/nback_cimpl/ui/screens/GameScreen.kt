package mobappdev.example.nback_cimpl.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.model.AudioPlayer
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import mobappdev.example.nback_cimpl.model.GameType

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GameScreen(vm: GameViewModel, context: Context) {
    val gameState by vm.gameState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val audioPlayer = remember { AudioPlayer(context) }


    val isEventShown = remember { mutableStateOf(false) }
    val buttonColor = remember { mutableStateOf(Color.White) } // Default color
    val shouldResetColor = remember { mutableStateOf(false) } // Flag to trigger color reset

    // Reset button color after 1 second when triggered
    LaunchedEffect(shouldResetColor.value) {
        if (shouldResetColor.value) {
            delay(1000) // Wait for 1 second
            buttonColor.value = Color.White // Reset to original color
            shouldResetColor.value = false // Reset the flag
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display current event number and score
            Text(
                text = "${gameState.round} / ${gameState.numberOfEvents} | Correct Responses: ${vm.score.collectAsState().value}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Game Mode Display
            when (gameState.gameType) {
                GameType.Audio -> {

                }
                GameType.Visual -> {
                    VisualGrid(highlightedPosition = gameState.eventValue, isEventShown = isEventShown.value)
                }
            }

            // Button to start a new round
            Button(
                onClick = {
                    vm.startGame() // Function to start a new round
                    isEventShown.value = false // Reset event shown state
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Start New Round")
            }

            // Button to check for a match
            Button(
                onClick = {
                    val isMatch = vm.checkMatch() // Check for match and return if correct
                    buttonColor.value = if (isMatch) Color.Green else Color.Red
                    shouldResetColor.value = true // Trigger the reset of the button color
                },
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize() // Animate size change for feedback
                    .background(buttonColor.value, shape = RoundedCornerShape(8.dp))
            ) {
                Text("Check Match")
            }
            // Trigger the next event display
            LaunchedEffect(gameState.eventValue) {
                isEventShown.value = true
                delay(1500)
                isEventShown.value = false
            }
        }
    }
}



@Composable
fun VisualGrid(highlightedPosition: Int, isEventShown: Boolean) {
    // Display a 3x3 grid with the highlighted cell for the current event
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 3) {
                    val position = row * 3 + col
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                when {
                                    isEventShown && position == highlightedPosition -> Color.Blue
                                    else -> Color.Gray
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }
}




