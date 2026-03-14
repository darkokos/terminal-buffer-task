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

    val totalLines: Int get() = scrollback.size + height

    // Private utilities

    private fun pushToScrollback(line: Line) {
        if (maxScrollback == 0) return

        if (scrollback.size >= maxScrollback) {
            scrollback.removeFirst()
        }

        scrollback.add(line)
    }

    private fun requireValidScreenRow(row: Int) {
        if (row !in 0..<height) {
            throw IndexOutOfBoundsException("Row $row is out of the bounds of [0, $height).")
        }
    }

    private fun requireValidScreenPosition(row: Int, column: Int) {
        if (column !in 0..<width) {
            throw IndexOutOfBoundsException("Column $column is out of the bounds of [0, $width).")
        }

        requireValidScreenRow(row)
    }

    private fun getLineFromAllAt(row: Int): Line {
        if (row !in 0..<totalLines) {
            throw IndexOutOfBoundsException("Row $row is out of bounds of screen and scrollback [0, $totalLines).")
        }

        return if (row < scrollback.size) {
            scrollback[row]
        } else {
            screen[row - scrollback.size]
        }
    }

    private fun advanceToNextLine() {
        cursorColumn = 0
        if (cursorRow >= height - 1) {
            insertLineAtBottom()
        } else {
            cursorRow++
        }
    }

    private fun writeCell(cell: Cell) {
        when (cell.width) {
            0 -> return
            2 -> {
                if (cursorColumn + 1 >= width) {
                    if (width < 2) return
                    advanceToNextLine()
                }

                val line = screen[cursorRow]
                if (line.getCell(cursorColumn).isPartOfWideChar) line.clearWideCellAt(cursorColumn)
                if (line.getCell(cursorColumn + 1).isPartOfWideChar)
                    line.clearWideCellAt(cursorColumn + 1)

                line.setCell(cursorColumn, cell)
                line.setCell(cursorColumn + 1, Cell.continuation(currentCellAttributes))
                cursorColumn += 2
            }
            else -> {
                if (cursorColumn >= width) advanceToNextLine()

                val line = screen[cursorRow]
                if (line.getCell(cursorColumn).isPartOfWideChar) line.clearWideCellAt(cursorColumn)

                line.setCell(cursorColumn, cell)
                cursorColumn++
            }
        }
    }

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

    fun write(text: String) {
        for (char in text) {
            writeCell(Cell(char, currentCellAttributes))
        }
    }

    fun insert(text: String) {
        if (text.isEmpty()) return

        val savedCells = mutableListOf<Cell>()
        for (row in cursorRow..<height) {
            val line = screen[row]

            val startColumn = if (row == cursorRow) cursorColumn else 0
            for (column in startColumn..<width) {
                val cell = line.getCell(column)
                if (cell.isContinuation) continue

                savedCells.add(cell)
            }
        }

        while (savedCells.isNotEmpty() && savedCells.last().isEmpty()) {
            savedCells.removeLast()
        }

        write(text)

        val previousCellAttributes = currentCellAttributes
        for (cell in savedCells) {
            currentCellAttributes = cell.attributes
            writeCell(cell)
        }
        currentCellAttributes = previousCellAttributes
    }

    fun fillLine(char: Char) {
        val line = screen[cursorRow]
        if (char == ' ') {
            line.clear()
        } else {
            val cell = Cell(char, currentCellAttributes)
            line.fill(cell)
        }
    }

    fun insertLineAtBottom() {
        pushToScrollback(screen.removeFirst())
        screen.add(Line(width))
    }

    fun clearScreen() {
        for (line in screen) {
            line.clear()
        }
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    fun getCharAt(row: Int, column: Int): Char {
        requireValidScreenPosition(row, column)
        return screen[row].getCell(column).char
    }

    fun getAttributesAt(row: Int, column: Int): CellAttributes {
        requireValidScreenPosition(row, column)
        return screen[row].getCell(column).attributes
    }

    fun getCellAt(row: Int, column: Int): Cell {
        requireValidScreenPosition(row, column)
        return screen[row].getCell(column)
    }

    fun getCharFromAllAt(row: Int, column: Int): Char {
        return getLineFromAllAt(row).getCell(column).char
    }

    fun getAttributesFromAllAt(row: Int, column: Int): CellAttributes {
        return getLineFromAllAt(row).getCell(column).attributes
    }

    fun getCellFromAllAt(row: Int, column: Int): Cell {
        return getLineFromAllAt(row).getCell(column)
    }

    fun getLineTextAt(row: Int): String {
        requireValidScreenRow(row)
        return screen[row].toText()
    }

    fun getLineTextFromAllAt(row: Int): String {
        return getLineFromAllAt(row).toText()
    }

    fun getScreenText(): String {
        return screen.joinToString("\n") { it.toText() }
    }

    fun getTextFromAll(): String {
        val sb = StringBuilder()
        sb.appendLine(scrollback.joinToString("\n") { it.toText() })
        sb.append(getScreenText())
        return sb.toString()
    }
}