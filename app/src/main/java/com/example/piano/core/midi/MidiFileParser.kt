package com.example.piano.core.midi

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import kotlin.math.min

/**
 * 标准 MIDI 文件解析：从字节数组解析出 (时间ms, MIDI音高, 是否按下) 事件列表，
 * 用于随播放位置高亮键盘。
 */
object MidiFileParser {

    /** 解析结果：某一时刻的音符按下/抬起 */
    data class NoteEvent(
        val timeMs: Long,
        val midiNote: Int,
        val isOn: Boolean
    )

    private const val META_TEMPO = 0x51
    private const val META_END_OF_TRACK = 0x2F
    private const val DEFAULT_TEMPO_US = 500_000L  // 120 BPM
    private const val DEFAULT_DIVISION = 480

    /**
     * 从 MIDI 文件字节解析所有音符事件（Note On/Off），按时间排序。
     * @param bytes 完整 SMF 文件内容
     * @return 按 timeMs 排序的列表；解析失败返回空列表
     */
    fun parseNoteEvents(bytes: ByteArray): List<NoteEvent> {
        if (bytes.size < 14) return emptyList()
        val input = DataInputStream(ByteArrayInputStream(bytes))
        return try {
            val header = ByteArray(4)
            input.readFully(header)
            if (!header.contentEquals("MThd".toByteArray(Charsets.US_ASCII))) return emptyList()
            val headerLength = input.readInt()
            if (headerLength < 6) return emptyList()
            @Suppress("UNUSED_VARIABLE") val format = input.readShort().toInt() and 0xFFFF
            val numTracks = input.readShort().toInt() and 0xFFFF
            var division = input.readShort().toInt() and 0xFFFF
            if (division and 0x8000 != 0) division = DEFAULT_DIVISION
            if (division <= 0) division = DEFAULT_DIVISION

            val allEvents = mutableListOf<NoteEvent>()
            var defaultTempoUs = DEFAULT_TEMPO_US

            for (t in 0 until numTracks) {
                val chunkHeader = ByteArray(4)
                input.readFully(chunkHeader)
                if (!chunkHeader.contentEquals("MTrk".toByteArray(Charsets.US_ASCII))) {
                    val len = input.readInt()
                    input.skipBytes(min(len.toLong(), input.available().toLong()).toInt())
                    continue
                }
                val trackLength = input.readInt()
                val trackData = ByteArray(trackLength)
                input.readFully(trackData)

                var cumulativeTicks = 0L
                var currentTempoUs = defaultTempoUs
                var pos = 0
                var lastStatus = 0

                while (pos < trackData.size) {
                    val (delta, consumed) = readVariableLengthFromArray(trackData, pos)
                    pos += consumed
                    cumulativeTicks += delta
                    if (pos >= trackData.size) break
                    var b = trackData[pos].toInt() and 0xFF
                    if (b >= 0x80) {
                        lastStatus = b
                        pos++
                    }
                    if (pos >= trackData.size) break
                    when (lastStatus) {
                        0xFF -> {
                            val metaType = trackData[pos].toInt() and 0xFF
                            pos++
                            if (pos >= trackData.size) break
                            val metaLen = trackData[pos].toInt() and 0xFF
                            pos++
                            when (metaType) {
                                META_TEMPO -> if (metaLen == 3 && pos + 3 <= trackData.size) {
                                    currentTempoUs = (((trackData[pos].toInt() and 0xFF) shl 16) or
                                        ((trackData[pos + 1].toInt() and 0xFF) shl 8) or
                                        (trackData[pos + 2].toInt() and 0xFF)).toLong()
                                    defaultTempoUs = currentTempoUs
                                }
                                META_END_OF_TRACK -> { }
                            }
                            pos += metaLen
                        }
                        0xF0, 0xF7 -> {
                            val (vlen, vconsumed) = readVariableLengthFromArray(trackData, pos)
                            pos += vconsumed + vlen.toInt()
                        }
                        else -> {
                            val status = lastStatus and 0xF0
                            if (pos >= trackData.size) break
                            val data1 = trackData[pos].toInt() and 0xFF
                            pos++
                            val data2 = if (status != 0xC0 && status != 0xD0) {
                                if (pos >= trackData.size) break
                                trackData[pos].toInt() and 0xFF.also { pos++ }
                            } else 0
                            when (status) {
                                0x80 -> allEvents.add(NoteEvent(ticksToMs(cumulativeTicks, division, currentTempoUs), data1, false))
                                0x90 -> allEvents.add(NoteEvent(ticksToMs(cumulativeTicks, division, currentTempoUs), data1, data2 > 0))
                            }
                        }
                    }
                }
            }
            allEvents.sortedBy { it.timeMs }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ticksToMs(ticks: Long, division: Int, tempoUsPerQuarter: Long): Long {
        if (division <= 0) return 0L
        return (ticks * tempoUsPerQuarter) / (division * 1000L)
    }

    private fun readVariableLengthFromArray(arr: ByteArray, start: Int): Pair<Long, Int> {
        var value = 0L
        var i = start
        while (i < arr.size && i < start + 5) {
            val b = arr[i].toInt() and 0xFF
            i++
            value = (value shl 7) or (b and 0x7F).toLong()
            if (b and 0x80 == 0) return value to (i - start)
        }
        return value to (i - start)
    }
}
