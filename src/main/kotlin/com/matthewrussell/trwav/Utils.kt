package com.matthewrussell.trwav

class Utils {

    companion object {
        fun toNumeric(str: String): String {
            return str
                .replace("[^0-9]".toRegex(), "") // remove all letters
                .replace("^[0]+".toRegex(), "") // remove leading zeroes
        }
    }
}