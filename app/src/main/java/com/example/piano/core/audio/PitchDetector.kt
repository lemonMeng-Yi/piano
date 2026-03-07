package com.example.piano.core.audio

import kotlin.math.*

/**
 * 从 PCM 短整型缓冲区估计基频（Hz），用于实时音高显示。
 * 使用自相关（autocorrelation）检测周期，适合单音（如钢琴单键）。
 */
object PitchDetector {

    private const val MIN_FREQ_HZ = 55f   // 约 A1
    private const val MAX_FREQ_HZ = 2000f  // 避免泛音误判
    private const val SILENCE_THRESHOLD = 800f  // 低于此视为静音
    private const val CLARITY_THRESHOLD = 0.35f // 自相关峰值清晰度

    /**
     * @param samples 16-bit 单声道 PCM（通常来自 AudioRecord）
     * @param sampleRate 采样率，如 44100
     * @return 估计的基频 Hz，若无有效音高则 null
     */
    fun detectFrequency(samples: ShortArray, sampleRate: Int): Float? {
        if (samples.size < 2) return null
        val rms = sqrt(samples.map { it.toDouble() * it }.average()).toFloat()
        if (rms < SILENCE_THRESHOLD) return null

        val minLag = (sampleRate / MAX_FREQ_HZ).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / MIN_FREQ_HZ).toInt().coerceAtMost(samples.size / 2)

        var bestLag = -1
        var bestCorr = -1.0

        for (lag in minLag..maxLag) {
            var sum = 0.0
            for (i in 0 until samples.size - lag) {
                sum += samples[i].toDouble() * samples[i + lag]
            }
            val corr = sum / (samples.size - lag)
            if (corr > bestCorr) {
                bestCorr = corr
                bestLag = lag
            }
        }

        if (bestLag <= 0) return null
        val zeroCorr = samples.map { it.toDouble() * it }.average()
        if (zeroCorr <= 0) return null
        val clarity = (bestCorr / zeroCorr).toFloat()
        if (clarity < CLARITY_THRESHOLD) return null

        return sampleRate.toFloat() / bestLag
    }

    /**
     * 将频率（Hz）转换为最接近的 MIDI 音高（12-TET，A4=440Hz）。
     */
    fun frequencyToMidi(freqHz: Float): Int {
        if (freqHz <= 0f) return 0
        val midi = 69f + 12f * (ln(freqHz / 440f) / ln(2f))
        return midi.roundToInt().coerceIn(0, 127)
    }
}
