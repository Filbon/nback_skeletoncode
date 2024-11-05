package mobappdev.example.nback_cimpl.model

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class AudioPlayer(private val context: Context) {
    // Play the sound corresponding to the letter
    fun playLetter(letter: String) {
        // Assuming you have audio files named a.mp3, b.mp3, etc., in the res/raw folder
        val resId = context.resources.getIdentifier(letter.lowercase(), "raw", context.packageName)
        if (resId != 0) {
            MediaPlayer.create(context, resId)?.apply {
                start()
                setOnCompletionListener { release() }
            }
        } else {
            Log.e("AudioPlayer", "Audio resource not found for letter: $letter")
        }
    }

}
