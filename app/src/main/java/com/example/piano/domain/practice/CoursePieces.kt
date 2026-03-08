package com.example.piano.domain.practice

/**
 * 课程曲目集合：提供按 id 获取 [PracticePiece]，null 时返回默认 C 大调音阶。
 */
object CoursePieces {
    /** C 大调音阶（默认曲目） */
    val C_MAJOR_SCALE = PracticePiece(
        id = "C_MAJOR_SCALE",
        title = "C 大调音阶",
        notes = listOf(60, 62, 64, 65, 67, 69, 71, 72).map { Note(it) }
    )

    /** 欢乐颂开头 */
    val ODE_TO_JOY_OPENING = PracticePiece(
        id = "ODE_TO_JOY_OPENING",
        title = "欢乐颂（开头）",
        notes = listOf(64, 64, 65, 67, 67, 65, 64, 62, 60, 60, 62, 64, 64, 62, 62).map { Note(it) }
    )

    private val allPieces = listOf(C_MAJOR_SCALE, ODE_TO_JOY_OPENING)
    private val byId = allPieces.associateBy { it.id }

    fun getPiece(pieceId: String?): PracticePiece =
        if (pieceId == null) C_MAJOR_SCALE else byId[pieceId] ?: C_MAJOR_SCALE

    fun contains(id: String): Boolean = id in byId
}
