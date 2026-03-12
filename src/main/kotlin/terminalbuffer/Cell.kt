package terminalbuffer

data class Cell(
    val char: Char = EMPTY_CHAR,
    val attributes: CellAttributes = CellAttributes.DEFAULT,
    val isWide: Boolean = false,
    val isContinuation: Boolean = false,
) {
    companion object {
        const val EMPTY_CHAR: Char = ' '
        val EMPTY = Cell()
    }
}
