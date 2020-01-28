package com.matthewrussell.trwav

import java.util.regex.Pattern

data class CuePoint(
    var location: Int = 0,
    var label: String = ""
): Comparable<CuePoint> {
    override fun compareTo(other: CuePoint): Int {
        // Sort Cues by location first then by Label

        val locationComparator: Comparator<CuePoint> = Comparator.comparing(CuePoint::location)

        val labelComparator: Comparator<CuePoint> = Comparator { o1, o2 ->
            val pattern = Pattern.compile("\\d+")
            val matcher = pattern.matcher(o1.label)
            val matcher2 = pattern.matcher(o2.label)

            var o1Match = "";
            while(matcher.find()) {
                o1Match += matcher.group()
            }

            var o2Match = "";
            while(matcher2.find()) {
                o2Match += matcher2.group()
            }

            if(o1Match != "" && o2Match != "") {
                val o1Num = o1Match.toLong()
                val o2Num = o2Match.toLong()

                // Sort by integer representation of the label
                return@Comparator (o1Num - o2Num).toInt()
            } else {
                // Sort normally
                return@Comparator o1.label.compareTo(o2.label)
            }
        }

        return locationComparator.thenComparing(labelComparator).compare(this, other)
    }

}