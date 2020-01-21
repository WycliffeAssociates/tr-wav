package com.matthewrussell.trwav

import sun.nio.cs.US_ASCII
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val BITS_PER_SAMPLE = 16
const val SAMPLE_RATE = 44100
const val NUM_CHANNELS = 1

class WavFileWriter(
    private val metadataMapper: MetadataMapper = MetadataMapper()
) {
    private fun wordAlignedLength(length: Int): Int {
        return length + (4 - length % 4)
    }

    private fun wordAlignString(str: String): String = str.padEnd(
        wordAlignedLength(str.length),
        0.toChar()
    )

    private fun makeMetadataChunk(metadata: Metadata): ByteArray {
        val metadataString = metadataMapper.toJSON(metadata)
        val paddedString = wordAlignString(metadataString)
        val buffer = ByteBuffer.allocate(paddedString.length + 20)
        buffer
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("LIST".toByteArray(US_ASCII()))
            .putInt(12 + paddedString.length)
            .put("INFOIART".toByteArray(US_ASCII()))
            .putInt(paddedString.length)
            .put(paddedString.toByteArray(US_ASCII()))
        return buffer.array()
    }

    private fun makeCueChunk(cues: List<CuePoint>): ByteArray {
        val cueChunkSize = CUE_HEADER_SIZE + (CUE_DATA_SIZE * cues.size)
        val cueChunkBuffer = ByteBuffer.allocate(CHUNK_HEADER_SIZE + cueChunkSize)
        cueChunkBuffer.order(ByteOrder.LITTLE_ENDIAN)
        cueChunkBuffer.put(CUE_LABEL.toByteArray(Charsets.US_ASCII))
        cueChunkBuffer.putInt(CUE_DATA_SIZE * cues.size + 4)
        cueChunkBuffer.putInt(cues.size)
        for (i in cues.indices) {
            cueChunkBuffer.put(makeCueData(i, cues[i]))
        }
        val labelChunkArray = makeLabelChunk(cues)
        val combinedBuffer = ByteBuffer.allocate(cueChunkBuffer.capacity() + labelChunkArray.size)
        combinedBuffer.put(cueChunkBuffer.array())
        combinedBuffer.put(labelChunkArray)
        return combinedBuffer.array()
    }

    private fun makeCueData(cueNumber: Int, cue: CuePoint): ByteArray {
        val buffer = ByteBuffer.allocate(CUE_DATA_SIZE)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(cueNumber)
        buffer.putInt(cue.location)
        buffer.put(DATA_LABEL.toByteArray(Charsets.US_ASCII))
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(cue.location)
        return buffer.array()
    }

    private fun makeLabelChunk(cues: List<CuePoint>): ByteArray {
        val size = 12 * cues.size + computeTextSize(cues) // all strings + (8 for labl header, 4 for cue id) * num cues
        val buffer = ByteBuffer.allocate(size + CHUNK_HEADER_SIZE + 4) // adds LIST header
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(LIST_LABEL.toByteArray(Charsets.US_ASCII))
        buffer.putInt(size + LABEL_SIZE)
        buffer.put(ADTL_LABEL.toByteArray(Charsets.US_ASCII))
        for (i in cues.indices) {
            buffer.put(LABEL_LABEL.toByteArray(Charsets.US_ASCII))
            val label = wordAlignedLabel(cues[i])
            buffer.putInt(4 + label.size) // subchunk size here is label size plus id
            buffer.putInt(i)
            buffer.put(label)
        }
        return buffer.array()
    }

    private fun makeHeader(audioSize: Int, metadataSize: Int): ByteArray {
        return ByteBuffer
            .allocate(44)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("RIFF".toByteArray(US_ASCII()))
            .putInt(audioSize + WAV_HEADER_SIZE - 8 + metadataSize)
            .put("WAVEfmt ".toByteArray(US_ASCII()))
            .putInt(16)
            .putShort(1)
            .putShort(NUM_CHANNELS.toShort())
            .putInt(SAMPLE_RATE)
            .putInt(BITS_PER_SAMPLE * SAMPLE_RATE * NUM_CHANNELS / 8)
            .put((NUM_CHANNELS * BITS_PER_SAMPLE / 8).toByte())
            .put(0)
            .put(BITS_PER_SAMPLE.toByte())
            .put(0)
            .put("data".toByteArray(US_ASCII()))
            .putInt(audioSize)
            .array()
    }

    fun write(data: WavFile, dest: File) {
        val metadataChunk = makeMetadataChunk(data.metadata)
        val labelChunk = makeLabelChunk(data.metadata.markers)
        val cueChunk = makeCueChunk(data.metadata.markers)
        val metadataSize = metadataChunk.size + labelChunk.size + cueChunk.size
        val header = makeHeader(data.audio.size, metadataSize)

        if (!dest.exists()) dest.createNewFile()

        dest.outputStream().use {
            it.write(header)
            it.write(data.audio)
            it.write(cueChunk)
            it.write(labelChunk)
            it.write(metadataChunk)
        }
    }

    private fun computeTextSize(cues: List<CuePoint>): Int {
        var total = 0
        for (i in cues.indices) {
            val length = cues[i].label.length
            total += getWordAlignedLength(length)
        }
        return total
    }

    private fun getWordAlignedLength(length: Int) = if (length % 4 != 0) length + 4 - (length % 4) else length

    private fun wordAlignedLabel(cue: CuePoint): ByteArray {
        val label = cue.label
        var alignedLength = cue.label.length
        if (alignedLength % 4 != 0) {
            alignedLength += 4 - alignedLength % 4
        }
        return label.toByteArray().copyOf(alignedLength)
    }
}