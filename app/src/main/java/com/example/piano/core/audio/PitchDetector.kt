package com.example.piano.core.audio

import kotlin.math.*

/**
 * 从 PCM 短整型缓冲区估计基频（Hz），用于实时音高显示。
 * 使用 YIN 算法：差分 + 累积均值归一化，取 cmnd 全局最小作为周期，减少泛音导致的高八度误判。
 */
object PitchDetector {

    private const val MIN_FREQ_HZ = 55f
    private const val MAX_FREQ_HZ = 2000f
    private const val SILENCE_THRESHOLD = 800f
    /** 清晰度：谷值深度不足（cmnd 最小仍 > 此值）则判为无效 */
    private const val CLARITY_MAX_CMND = 0.55

    fun detectFrequency(samples: ShortArray, sampleRate: Int): Float? {
        if (samples.size < 2) return null
        val rms = sqrt(samples.map { it.toDouble() * it }.average()).toFloat()
        if (rms < SILENCE_THRESHOLD) return null

        val minLag = (sampleRate / MAX_FREQ_HZ).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / MIN_FREQ_HZ).toInt().coerceAtMost(samples.size / 2)
        if (minLag > maxLag) return null

        // 1) 差分函数 d(τ) = Σ (x_j - x_{j+τ})²
        val diff = DoubleArray(maxLag + 1)
        for (tau in 1..maxLag) {
            var sum = 0.0
            val limit = samples.size - tau
            if (limit <= 0) break
            for (j in 0 until limit) {
                val d = samples[j].toDouble() - samples[j + tau]
                sum += d * d
            }
            diff[tau] = sum
        }

        // 2) 累积均值归一化 d'(τ) = d(τ) / [ (1/τ) Σ_{k=1}^{τ} d(k) ]
        val cmnd = DoubleArray(maxLag + 1)
        cmnd[0] = 1.0
        var runningSum = 0.0
        for (tau in 1..maxLag) {
            runningSum += diff[tau]
            cmnd[tau] = if (runningSum > 0) diff[tau] * tau / runningSum else 1.0
        }

        // 3) 取 d'(τ) 全局最小作为周期
        var bestLag = -1
        var bestVal = 1.0
        for (tau in minLag..maxLag) {
            if (cmnd[tau] < bestVal) {
                bestVal = cmnd[tau]
                bestLag = tau
            }
        }
        if (bestLag <= 0) return null

        if (bestVal > CLARITY_MAX_CMND) return null

        // 4) 抛物线插值得到亚采样周期
        val tau0 = bestLag
        val y0 = cmnd[tau0]
        val yL = if (tau0 > 1) cmnd[tau0 - 1] else y0
        val yR = if (tau0 < maxLag) cmnd[tau0 + 1] else y0
        val denom = yL - 2 * y0 + yR
        val delta = if (abs(denom) > 1e-12) 0.5 * (yL - yR) / denom else 0.0
        val refinedTau = (tau0 + delta).coerceIn(tau0 - 0.5, tau0 + 0.5)

        return (sampleRate / refinedTau).toFloat()
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
