package com.example.tileterritory.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

enum class SoundEffect { SELECT, CLAIM, PASS, GAME_END }

/**
 * Plays short, soft synthesized tones. Sounds are rendered once into static AudioTracks,
 * so no audio assets are needed and playback latency stays low.
 */
class SoundPlayer {

    private val executor = Executors.newSingleThreadExecutor()
    private val tracks = HashMap<SoundEffect, AudioTrack>()
    @Volatile private var released = false

    init {
        executor.execute {
            SoundEffect.entries.forEach { effect ->
                runCatching { buildTrack(render(effect)) }.getOrNull()?.let { tracks[effect] = it }
            }
        }
    }

    fun play(effect: SoundEffect) {
        if (released) return
        runCatching {
            executor.execute {
                val track = tracks[effect] ?: return@execute
                runCatching {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
                    track.reloadStaticData()
                    track.play()
                }
            }
        }
    }

    fun release() {
        released = true
        executor.execute {
            tracks.values.forEach { runCatching { it.release() } }
            tracks.clear()
        }
        executor.shutdown()
    }

    private data class Note(val frequency: Double, val startMs: Int, val durationMs: Int, val gain: Double)

    private fun render(effect: SoundEffect): ShortArray {
        val notes = when (effect) {
            SoundEffect.SELECT -> listOf(Note(1320.0, 0, 45, 0.16))
            SoundEffect.CLAIM -> listOf(Note(523.25, 0, 150, 0.22), Note(784.0, 0, 120, 0.08))
            SoundEffect.PASS -> listOf(Note(440.0, 0, 140, 0.16), Note(349.23, 110, 200, 0.16))
            SoundEffect.GAME_END -> listOf(
                Note(392.0, 0, 260, 0.16),
                Note(493.88, 120, 260, 0.15),
                Note(587.33, 240, 520, 0.16)
            )
        }
        val totalMs = notes.maxOf { it.startMs + it.durationMs } + 20
        val samples = DoubleArray(SAMPLE_RATE * totalMs / 1000)
        for (note in notes) {
            val start = SAMPLE_RATE * note.startMs / 1000
            val length = SAMPLE_RATE * note.durationMs / 1000
            val attack = SAMPLE_RATE * 4 / 1000
            for (i in 0 until length) {
                val index = start + i
                if (index >= samples.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = min(1.0, i.toDouble() / attack) * exp(-5.0 * i / length)
                val tone = sin(2 * PI * note.frequency * t) + 0.18 * sin(4 * PI * note.frequency * t)
                samples[index] += tone * envelope * note.gain
            }
        }
        return ShortArray(samples.size) { i ->
            (samples[i].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun buildTrack(pcm: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        return track
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
