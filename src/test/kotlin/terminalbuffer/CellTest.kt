package terminalbuffer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CellTest {

    private val cell = Cell()

    @Test
    fun `default cell holds a whitespace with default attributes`() {
        assertEquals(' ', cell.char)
        assertEquals(CellAttributes.DEFAULT, cell.attributes)
        assertFalse(cell.isWide)
        assertFalse(cell.isContinuation)
    }

    @Test
    fun `EMPTY cell is the same as default cell`() {
        assertEquals(cell, Cell.EMPTY)
    }
}