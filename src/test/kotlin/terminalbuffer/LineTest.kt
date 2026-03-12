package terminalbuffer

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Assertions.*

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
        fun `getCell returns Cell at specified column`() {
            assertEquals(Cell.EMPTY, line.getCell(3))
        }

        @Test
        fun `setCell sets cell value at specified column`() {
            assertDoesNotThrow {
                line.setCell(3, Cell('A', CellAttributes(foregroundColour = Colour.RED)))
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 10, 11])
        fun `getCell must receive column in range 0 to width - 1 inclusive`(column: Int) {
            assertThrows<IndexOutOfBoundsException> { line.getCell(column)  }
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 10, 11])
        fun `setCell must receive column in range 0 to width - 1 inclusive`(column: Int) {
            assertThrows<IndexOutOfBoundsException> { line.setCell(column, Cell.EMPTY)  }
        }
    }
}