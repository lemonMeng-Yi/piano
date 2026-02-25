package com.example.piano.domain.practice

/**
 * 音符，用 MIDI 音高表示（60 = C4, 72 = C5）
 */
data class Note(val midi: Int) {
    fun displayName(): String = Note.midiToName(midi)

    companion object {
        private val NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

        fun midiToName(midi: Int): String {
            if (midi !in 0..127) return "?"
            val octave = midi / 12 - 1
            return "${NAMES[midi % 12]}$octave"
        }
    }
}
