package com.example.piano.domain.practice

/**
 * 单次按键的纠错记录
 */
data class CorrectionRecord(
    val index: Int,
    val expected: Note,
    val actual: Note,
    val isCorrect: Boolean
) {
    fun errorMessage(): String = "你按了 ${actual.displayName()}，应为 ${expected.displayName()}"
}
