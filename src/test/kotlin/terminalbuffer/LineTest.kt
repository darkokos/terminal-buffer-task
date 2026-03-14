package terminalbuffer

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Order

class LineTest {

    @Nested
    inner class LineInitialisation {

        @ParameterizedTest
        @ValueSource(ints = [-1, 0])
        fun `line width must be at least 1`(width: Int) {
            assertThrows<IllegalArgumentException> { Line(width) }
        }
    }

    @Nested
    inner class LineMethods {

        private val line = Line(10)

        @Test
        @Order(1)
        fun `getCell returns Cell at specified column`() {
            assertEquals(Cell.EMPTY, line.getCell(3))
        }

        @Test
        @Order(1)
        fun `setCell sets cell value at specified column`() {
            assertDoesNotThrow {
                line.setCell(3, Cell('A', CellAttributes(foregroundColour = Colour.RED)))
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 10, 11])
        @Order(1)
        fun `getCell must receive column in range 0 to width - 1 inclusive`(column: Int) {
            assertThrows<IndexOutOfBoundsException> { line.getCell(column)  }
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 10, 11])
        @Order(1)
        fun `setCell must receive column in range 0 to width - 1 inclusive`(column: Int) {
            assertThrows<IndexOutOfBoundsException> { line.setCell(column, Cell.EMPTY)  }
        }

        @Test
        fun `clearWideCellAtSource clears the next cell if the current one is wide`() {
            line.setCell(0, Cell('\uFF01'))
            line.setCell(1, Cell.continuation())
            line.clearWideCellAt(0)
            assertEquals(Cell.EMPTY, line.getCell(0))
            assertEquals(Cell.EMPTY, line.getCell(0))
        }

        @Test
        fun `clearWideCellAtSource clears the previous cell if the current one is a continuation`() {
            line.setCell(0, Cell('\uFF01'))
            line.setCell(1, Cell.continuation())
            line.clearWideCellAt(1)
            assertEquals(Cell.EMPTY, line.getCell(0))
            assertEquals(Cell.EMPTY, line.getCell(0))
        }

        @Test
        fun `toText outputs the correct string representation of the line`() {
            line.setCell(0, Cell('.'))
            assertEquals(".         ", line.toText())
        }
    }
}