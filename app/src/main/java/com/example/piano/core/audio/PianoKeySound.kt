package com.example.piano.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * 虚拟钢琴键按下时播放短音，模拟真琴触键反馈。
 * 使用多谐波合成 + 击弦瞬态 + 指数衰减包络，使音色更接近真实钢琴。
 */
class PianoKeySound {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sampleRate = 44100
    private val durationMs = 420
    private val numSamples = sampleRate * durationMs / 1000

    /** MIDI 音高转频率（Hz），A4 = 69 = 440Hz */
    private fun midiToFreq(midi: Int): Float {
        if (midi !in 0..127) return 440f
        return (440f * 2f.pow((midi - 69) / 12f))
    }

    /**
     * 异步播放指定 MIDI 音高的短音，不阻塞 UI。
     */
    fun playNote(midi: Int) {
        scope.launch {
            try {
                val freq = midiToFreq(midi)
                val buffer = generatePianoTone(freq)
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
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(buffer, 0, buffer.size)
                track.play()
                Thread.sleep(durationMs.toLong())
                track.stop()
                track.release()
            } catch (_: Exception) {
                // 忽略播放失败
            }
        }
    }

    /**
     * 生成更贴近真实钢琴的短音：
     * - 基波 + 5 个谐波，比例参考钢琴频谱
     * - 高次谐波衰减更快（起振亮、尾音偏柔）
     * - 极短击弦噪声（2ms）增强触键感
     * - 指数衰减包络
     */
    private fun generatePianoTone(freqHz: Float): ShortArray {
        val out = ShortArray(numSamples)

        // 谐波相对振幅（偏亮：加强 2–4 次谐波，高次略提）
        val harmonicAmps = doubleArrayOf(
            1.00,   // 基波
            0.72,   // 2
            0.42,   // 3
            0.24,   // 4
            0.12,   // 5
            0.06    // 6
        )

        val attackSamples = (sampleRate * 0.002).toInt().coerceAtLeast(1)  // 约 2ms 起振
        val mainDecayPerSec = 6.5   // 主包络衰减速度
        val harmonicDecayPerSec = 8.5   // 高次谐波衰减放慢，整体更亮
        val strikeNoiseSamples = (sampleRate * 0.002).toInt().coerceAtLeast(1)  // 2ms 击弦噪声

        for (i in 0 until numSamples) {
            val tSec = i.toDouble() / sampleRate

            // 主包络：极短线性起振 + 指数衰减
            val envelope = when {
                i < attackSamples -> i.toDouble() / attackSamples
                else -> exp(-mainDecayPerSec * tSec)
            }

            var sample = 0.0
            for (n in harmonicAmps.indices) {
                val harmonicNum = n + 1
                val freqN = freqHz * harmonicNum
                // 高次谐波衰减更快，尾音更偏基波
                val harmonicEnvelope = exp(-harmonicDecayPerSec * tSec * n)
                sample += sin(2.0 * PI * freqN * tSec) * harmonicAmps[n] * envelope * harmonicEnvelope
            }

            // 击弦瞬态：开头极短白噪声，减弱以降低击弦感
            if (i < strikeNoiseSamples) {
                val strikeEnv = 1.0 - i.toDouble() / strikeNoiseSamples
                sample += (Random.nextDouble() - 0.5) * 0.18 * strikeEnv
            }

            // 归一化到约 -1..1 再缩放到 16bit，避免削波
            val norm = 2.6
            val scaled = (sample / norm).coerceIn(-1.0, 1.0) * 0.85 * Short.MAX_VALUE
            out[i] = scaled.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }
}
