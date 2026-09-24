package com.mubashshir.novafocus.data.model

/**
 * Items appearing on the vertical scrubber bar.
 */
sealed class ScrubberItem(val symbol: String, val letterChar: Char?) {
    data object Star : ScrubberItem(symbol = "☆", letterChar = null)
    data class Letter(val char: Char) : ScrubberItem(symbol = char.toString(), letterChar = char)
    data object Dot : ScrubberItem(symbol = "•", letterChar = null)

    companion object {
        val ALL_ITEMS: List<ScrubberItem> = buildList {
            add(Star)
            for (c in 'A'..'Z') {
                add(Letter(c))
            }
            add(Dot)
        }
    }
}
