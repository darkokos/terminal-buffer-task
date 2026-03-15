package terminalbuffer

class Line(val width: Int) {
    init {
        require(width > 0) { "Terminal line width must be at least 1, got $width." }
    }

    private val cells: Array<Cell> = Array(width) { Cell.EMPTY }

    private fun requireValidColumn(column: Int) {
        if (column !in 0..<width) {
            throw IndexOutOfBoundsException("Column $column is out of the bounds of [0, $width).")
        }
    }

    fun getCell(column: Int): Cell {
        requireValidColumn(column)
        return cells[column]
    }

    fun setCell(column: Int, cell: Cell) {
        requireValidColumn(column)
        cells[column] = cell
    }

    fun fill(cell: Cell) {
        cells.fill(cell)
    }

    fun clear() {
        cells.fill(Cell.EMPTY)
    }

    fun clearWideCellAt(column: Int) {
        val cell = getCell(column)
        setCell(column, Cell.EMPTY)
        if (cell.isContinuation) {
            setCell(column - 1, Cell.EMPTY)
        } else {
            setCell(column + 1, Cell.EMPTY)
        }
    }

    fun toText(): String {
        val sb = StringBuilder()
        sb.append(cells.joinToString("") { it.char.toString() })
        return sb.toString()
    }

    fun copyResized(width: Int): Line {
        val newLine = Line(width)
        val copyCount = minOf(this.width, width)
        for (i in 0..<copyCount) {
            newLine.cells[i] = cells[i]
        }

        if (newLine.cells[width - 1].width == 2) {
            newLine.cells[width - 1] = Cell.EMPTY
        }

        return newLine
    }
}