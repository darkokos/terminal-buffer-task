package terminalbuffer

data class CellAttributes(
    val foregroundColour: Colour = Colour.DEFAULT,
    val backgroundColour: Colour = Colour.DEFAULT,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
) {
    companion object {
        val DEFAULT = CellAttributes()
    }
}
