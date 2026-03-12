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
}