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
        private set

    private val screen: MutableList<Line> = MutableList(height) { Line(width) }
    private val scrollback: MutableList<Line> = mutableListOf()

    var currentCellAttributes: CellAttributes = CellAttributes.DEFAULT

    var cursorRow: Int = 0
        private set

    var cursorColumn: Int = 0
        private set
}