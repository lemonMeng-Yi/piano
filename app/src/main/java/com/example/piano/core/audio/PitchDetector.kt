package com.example.piano.core.audio

import kotlin.math.*

/**
 * 从 PCM 短整型缓冲区估计基频（Hz），用于实时音高显示。
 * 可选 YIN 或自相关；YIN 理论更稳，若实际不准可切回自相关。
 */
object PitchDetector {

    /** true = YIN，false = 自相关（原逻辑） */
    private const val USE_YIN = true

    private const val MIN_FREQ_HZ = 55f
    private const val MAX_FREQ_HZ = 2000f
    private const val SILENCE_THRESHOLD = 800f
    /** YIN：取 cmnd 全局最小作为周期，避免「第一个低于阈值」选到泛音导致高八度 */
    /** 清晰度：谷值深度不足（cmnd 最小仍 > 此值）则判为无效 */
    private const val CLARITY_MAX_CMND = 0.55

    /** 自相关清晰度（仅 USE_YIN=false 时用） */
    private const val AUTOCORR_CLARITY_THRESHOLD = 0.35f

    fun detectFrequency(samples: ShortArray, sampleRate: Int): Float? {
        return if (USE_YIN) detectFrequencyYin(samples, sampleRate)
        else detectFrequencyAutocorr(samples, sampleRate)
    }

    private fun detectFrequencyYin(samples: ShortArray, sampleRate: Int): Float? {
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

        // 3) 取 d'(τ) 全局最小作为周期（减少泛音导致的高八度误判）
        var bestLag = -1
        var bestVal = 1.0
        for (tau in minLag..maxLag) {
            if (cmnd[tau] < bestVal) {
                bestVal = cmnd[tau]
                bestLag = tau
            }
        }
        if (bestLag <= 0) return null

        // 清晰度：谷不够深则视为无效
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

    private fun detectFrequencyAutocorr(samples: ShortArray, sampleRate: Int): Float? {
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
        if (clarity < AUTOCORR_CLARITY_THRESHOLD) return null

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
