package com.matthewrussell.trwav

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.util.regex.Pattern

data class Metadata(
    var anthology: String = "",
    var language: String = "",
    var version: String = "",
    @JsonAlias("book")
    var slug: String = "",
    @JsonProperty("book_number")
    var bookNumber: String = "",
    var mode: String = "",
    var chapter: String = "",
    var startv: String = "",
    var endv: String = "",
    var contributor: String = "",
    @JsonDeserialize(using = MarkerListDeserializer::class)
    @JsonSerialize(using = MarkerListSerializer::class)
    var markers: MutableList<CuePoint> = mutableListOf()
) {
    fun toFilename(takeInfo: String, chapterWidth: Int = 2, verseWidth: Int = 2): String {
        val paddedStartV = startv.padStart(verseWidth, '0')
        val paddedEndV = endv.padStart(verseWidth, '0')
        return listOf(
            language,
            version,
            "b${bookNumber.padStart(2, '0')}",
            slug,
            "c${chapter.padStart(chapterWidth, '0')}",
            if (startv != endv) "v$paddedStartV-$paddedEndV" else "v$paddedStartV",
            takeInfo
        ).joinToString("_").plus(".wav")
    }

    fun fromFilename(name: String): Metadata {
        val underscore = "_"
        val language = "([a-zA-Z]{2,3}(?:-[\\da-zA-Z]+)*)"
        val anthology = "(?:_(nt|ot|obs))?"
        val version = "([\\da-zA-Z]{2,3})"
        val bookNumber = "b([\\d]{2})"
        val book = "([\\da-zA-Z]+)"
        val chapter = "(?:_c([\\d]{2,3}))?"
        val verse = "(?:_v([\\d]{2,3})(?:-([\\d]{2,3}))?)?"
        val take = "(?:_t([\\d]{2}))?"
        val pattern = language + underscore + anthology + version + underscore +
                bookNumber + underscore + book +
                chapter + verse + take + ".*"
        val p = Pattern.compile(pattern)
        val m = p.matcher(name)

        if(m.find()) {
            this.language = if (m.group(1) != null) m.group(1) else ""
            this.version = if (m.group(3) != null) m.group(3) else ""
            this.bookNumber = if (m.group(4) != null) m.group(4) else "0"
            this.slug = if (m.group(5) != null) m.group(5) else ""
            this.chapter = if (m.group(6) != null) m.group(6) else ""
            this.startv = if (m.group(7) != null) m.group(7) else ""
            this.endv = if (m.group(8) != null) m.group(8) else this.startv

            val anth = if(this.bookNumber.toInt() > 39) "nt" else "ot"
            this.anthology = if (m.group(2) != null) m.group(2) else anth
        }

        return this
    }
}