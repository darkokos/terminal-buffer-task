package terminalbuffer

class Line(width: Int) {
    private val cells: Array<Cell> = Array(width) { Cell.EMPTY }
}