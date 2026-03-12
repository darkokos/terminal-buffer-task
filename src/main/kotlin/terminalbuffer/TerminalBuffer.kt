package terminalbuffer

class TerminalBuffer(
    width: Int,
    height: Int,
    maxScrollback: Int = 1000
) {
    init {
        require(width > 0) { "Terminal width must be at least 1, got $width." }
        require(height > 0) { "Terminal height must be at least 1, got $height." }
        require(maxScrollback >= 0) { "Terminal max scrollback must be at least 0, got $maxScrollback." }
    }

    var width: Int = width
        private set

    var height: Int = height
        private set

    var maxScrollback: Int = maxScrollback
        set(value) {
            require(value >= 0) { "Terminal max scrollback must be at least 0, got $value." }
            field = value
            while (scrollback.size > value) {
                scrollback.removeFirst()
            }
        }

    private val screen: MutableList<Line> = MutableList(height) { Line(width) }
    private val scrollback: MutableList<Line> = mutableListOf()

    var currentCellAttributes: CellAttributes = CellAttributes.DEFAULT

    var cursorRow: Int = 0
        private set

    var cursorColumn: Int = 0
        private set

    fun setForegroundColour(colour: Colour) {
        currentCellAttributes = currentCellAttributes.copy(foregroundColour = colour)
    }

    fun setBackgroundColour(colour: Colour) {
        currentCellAttributes = currentCellAttributes.copy(backgroundColour = colour)
    }

    fun setBold(bold: Boolean) {
        currentCellAttributes = currentCellAttributes.copy(bold = bold)
    }

    fun setItalic(italic: Boolean) {
        currentCellAttributes = currentCellAttributes.copy(italic = italic)
    }

    fun setUnderline(underline: Boolean) {
        currentCellAttributes = currentCellAttributes.copy(underline = underline)
    }

    fun setCursorPosition(row: Int, column: Int) {
        cursorRow = row.coerceIn(0, height - 1)
        cursorColumn = column.coerceIn(0, width - 1)
    }

    fun moveCursorUp(n: Int = 1) {
        require(n > 0) { "Cursor must move at least one line up, got $n." }
        cursorRow = (cursorRow - n).coerceAtLeast(0)
    }

    fun moveCursorDown(n: Int = 1) {
        require(n > 0) { "Cursor must move at least one line down, got $n." }
        cursorRow = (cursorRow + n).coerceAtMost(height - 1)
    }

    fun moveCursorLeft(n: Int = 1) {
        require(n > 0) { "Cursor must move at least one column left, got $n." }
        cursorColumn = (cursorColumn - n).coerceAtLeast(0)
    }

    fun moveCursorRight(n: Int = 1) {
        require(n > 0) { "Cursor must move at least one column right, got $n." }
        cursorColumn = (cursorColumn + n).coerceAtMost(width - 1)
    }
}