package terminalbuffer

data class Cell(
    val char: Char = EMPTY_CHAR,
    val attributes: CellAttributes = CellAttributes.DEFAULT,
    val isContinuation: Boolean = false,
) {
    val width: Int = if (isContinuation) 1 else run {
        val code = char.code
        when {
            code < 32 || code == 0x7F -> 0 // Control characters
            code == 0x00AD -> 0 // Soft hyphen
            code in 0x0300..0x036F -> 0 // Combining diacritical marks
            code in 0x1AB0..0x1AFF -> 0 // Combining diacritical marks ext
            code in 0x1DC0..0x1DFF -> 0 // Combining diacritical marks sup
            code in 0x200B..0x200D -> 0 // Zero-width space, non-joiner, joiner
            code == 0x2060 -> 0 // Word joiner
            code in 0x20D0..0x20FF -> 0 // Combining diacritical marks for symbols
            code in 0xFE00..0xFE0F -> 0 // Variation selectors
            code in 0xFE20..0xFE2F -> 0 // Combining half marks
            code == 0xFEFF -> 0 // Zero-width no-break space (BOM)
            code in 0x1100..0x115F -> 2 // Hangul Jamo
            code in 0x2329..0x232A -> 2 // Left-pointing angle bracket and right-pointing angle bracket
            code in 0x2E80..0x2FFF -> 2 // CJK radicals supplement, Kangxi radicals
            code in 0x3000..0x33FF -> 2 // CJK symbols, Hiragana, Katakana, Bopomofo, CJK compat
            code in 0x3400..0x4DBF -> 2 // CJK unified ideographs extension A
            code in 0x4E00..0x9FFF -> 2 // CJK unified ideographs
            code in 0xAC00..0xD7AF -> 2 // Hangul syllables
            code in 0xF900..0xFAFF -> 2 // CJK compatibility ideographs
            code in 0xFF01..0xFF60 -> 2 // Fullwidth forms
            code in 0xFFE0..0xFFE6 -> 2 // Fullwidth signs
            else -> 1
        }
    }

    companion object {
        const val EMPTY_CHAR: Char = ' '
        val EMPTY = Cell()
        fun continuation(attributes: CellAttributes = CellAttributes.DEFAULT) =
            Cell(EMPTY_CHAR, attributes, isContinuation = true)
    }
}
