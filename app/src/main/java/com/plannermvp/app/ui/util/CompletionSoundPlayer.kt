package com.plannermvp.app.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * Plays a short, quiet task-completion chime.
 *
 * Sound file: app/src/main/res/raw/task_complete.wav (generated) or .mp3
 * The player catches all exceptions — if the file is missing or the device
 * can't play audio, completion still works silently.
 *
 * Triggered ONLY from explicit user checkbox tap (PENDING → COMPLETED).
 * Never plays on: screen open, DB refresh, import, widget update, recomposition.
 */
object CompletionSoundPlayer {

    private const val TAG = "CompletionSound"
    private var pool: SoundPool? = null
    private var soundId = 0
    private var loaded  = false

    fun init(context: Context) {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            pool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attrs)
                .build()
                .also { sp ->
                    sp.setOnLoadCompleteListener { _, _, status ->
                        loaded = (status == 0)
                    }
                    // R.raw.task_complete — WAV or MP3, 200-400ms
                    val resId = context.resources.getIdentifier(
                        "task_complete", "raw", context.packageName
                    )
                    if (resId != 0) {
                        soundId = sp.load(context, resId, 1)
                    } else {
                        Log.w(TAG, "task_complete resource not found — completion will be silent")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "SoundPool init failed — silent mode", e)
        }
    }

    /** Play at 60% volume. Silent no-op if not ready. */
    fun play() {
        if (!loaded) return
        try { pool?.play(soundId, 0.6f, 0.6f, 1, 0, 1.0f) }
        catch (e: Exception) { Log.w(TAG, "play() failed", e) }
    }

    fun release() {
        pool?.release()
        pool   = null
        loaded = false
    }
}
