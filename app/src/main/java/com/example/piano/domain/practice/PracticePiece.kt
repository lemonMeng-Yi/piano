package com.example.piano.domain.practice

/**
 * 练习曲目：标题 + 按顺序跟弹的音符序列
 */
data class PracticePiece(
    val id: String,
    val title: String,
    val notes: List<Note>
)
