package terminalbuffer

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Order
import org.junit.jupiter.params.provider.CsvSource

class TerminalBufferTest {

    @Nested
    inner class TerminalBufferInitialisation {

        @ParameterizedTest
        @ValueSource(ints = [-1, 0])
        fun `terminal width must be at least 1`(width: Int) {
            assertThrows<IllegalArgumentException> { TerminalBuffer(width, 1) }
        }

        @ParameterizedTest
        @ValueSource(ints = [-1, 0])
        fun `terminal height must be at least 1`(height: Int) {
            assertThrows<IllegalArgumentException> { TerminalBuffer(1, height) }
        }

        @Test
        fun `terminal max scrollback must be non-negative`() {
            assertThrows<IllegalArgumentException> { TerminalBuffer(1, 1, -1) }
        }

        @Test
        fun `terminal max scrollback can be zero`() {
            assertDoesNotThrow { TerminalBuffer(1, 1, 0) }
        }
    }

    @Nested
    inner class Methods {

        private val terminalBuffer = TerminalBuffer(80, 60, 10)

        @Nested
        inner class MaxScrollback {

            @Test
            fun `set terminal max scrollback must be non-negative`() {
                assertThrows<IllegalArgumentException> { terminalBuffer.maxScrollback = -1 }
            }

            @Test
            fun `set terminal max scrollback can be zero`() {
                assertDoesNotThrow { terminalBuffer.maxScrollback = 0 }
            }

            @Test
            fun `reducing maxScrollback removes oldest lines`() {
            }
        }

        @Nested
        inner class Attributes {

            @Test
            fun `setForegroundColour changes only foreground colour`() {
                terminalBuffer.setForegroundColour(Colour.BLUE)
                assertEquals(Colour.BLUE, terminalBuffer.currentCellAttributes.foregroundColour)
                assertEquals(
                    CellAttributes.DEFAULT.backgroundColour,
                    terminalBuffer.currentCellAttributes.backgroundColour,
                )
                assertEquals(CellAttributes.DEFAULT.bold, terminalBuffer.currentCellAttributes.bold)
                assertEquals(
                    CellAttributes.DEFAULT.italic,
                    terminalBuffer.currentCellAttributes.italic,
                )
                assertEquals(
                    CellAttributes.DEFAULT.underline,
                    terminalBuffer.currentCellAttributes.underline,
                )
            }

            @Test
            fun `setBackgroundColour changes only background colour`() {
                terminalBuffer.setBackgroundColour(Colour.BLUE)
                assertEquals(
                    CellAttributes.DEFAULT.foregroundColour,
                    terminalBuffer.currentCellAttributes.foregroundColour
                )
                assertEquals(Colour.BLUE, terminalBuffer.currentCellAttributes.backgroundColour)
                assertEquals(CellAttributes.DEFAULT.bold, terminalBuffer.currentCellAttributes.bold)
                assertEquals(
                    CellAttributes.DEFAULT.italic,
                    terminalBuffer.currentCellAttributes.italic,
                )
                assertEquals(
                    CellAttributes.DEFAULT.underline,
                    terminalBuffer.currentCellAttributes.underline,
                )
            }

            @Test
            fun `setBold changes only bold`() {
                terminalBuffer.setBold(true)
                assertEquals(
                    CellAttributes.DEFAULT.foregroundColour,
                    terminalBuffer.currentCellAttributes.foregroundColour
                )
                assertEquals(
                    CellAttributes.DEFAULT.backgroundColour,
                    terminalBuffer.currentCellAttributes.backgroundColour,
                )
                assertTrue(terminalBuffer.currentCellAttributes.bold)
                assertEquals(
                    CellAttributes.DEFAULT.italic,
                    terminalBuffer.currentCellAttributes.italic,
                )
                assertEquals(
                    CellAttributes.DEFAULT.underline,
                    terminalBuffer.currentCellAttributes.underline,
                )
            }

            @Test
            fun `setItalic changes only italic`() {
                terminalBuffer.setItalic(true)
                assertEquals(
                    CellAttributes.DEFAULT.foregroundColour,
                    terminalBuffer.currentCellAttributes.foregroundColour
                )
                assertEquals(
                    CellAttributes.DEFAULT.backgroundColour,
                    terminalBuffer.currentCellAttributes.backgroundColour,
                )
                assertEquals(CellAttributes.DEFAULT.bold, terminalBuffer.currentCellAttributes.bold)
                assertTrue(terminalBuffer.currentCellAttributes.italic)
                assertEquals(
                    CellAttributes.DEFAULT.underline,
                    terminalBuffer.currentCellAttributes.underline,
                )
            }

            @Test
            fun `setUnderline changes only underline`() {
                terminalBuffer.setUnderline(true)
                assertEquals(
                    CellAttributes.DEFAULT.foregroundColour,
                    terminalBuffer.currentCellAttributes.foregroundColour
                )
                assertEquals(
                    CellAttributes.DEFAULT.backgroundColour,
                    terminalBuffer.currentCellAttributes.backgroundColour,
                )
                assertEquals(CellAttributes.DEFAULT.bold, terminalBuffer.currentCellAttributes.bold)
                assertEquals(
                    CellAttributes.DEFAULT.italic,
                    terminalBuffer.currentCellAttributes.italic,
                )
                assertTrue(terminalBuffer.currentCellAttributes.underline)
            }
        }

        @Nested
        inner class Cursor {

            @Test
            @Order(1)
            fun `setCursorPosition with values in bounds of buffer works as expected`() {
                terminalBuffer.setCursorPosition(10, 5)
                assertEquals(10, terminalBuffer.cursorRow)
                assertEquals(5, terminalBuffer.cursorColumn)
            }

            @ParameterizedTest
            @CsvSource(
                "60, 80",
                "61, 81",
            )
            @Order(1)
            fun `setCursorPosition clamps cursor to height and width of buffer`(row: Int, column: Int) {
                terminalBuffer.setCursorPosition(row, column)
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(terminalBuffer.width - 1, terminalBuffer.cursorColumn)
            }

            @Test
            @Order(1)
            fun `setCursorPosition clamps cursor to zero`() {
                terminalBuffer.setCursorPosition(-1, -1)
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(0, terminalBuffer.cursorColumn)
            }

            @Test
            @Order(2)
            fun `moveCursorUp decreases cursor row`() {
                terminalBuffer.setCursorPosition(10, 5)
                terminalBuffer.moveCursorUp()
                assertEquals(9, terminalBuffer.cursorRow)
            }

            @Test
            fun `moveCursorUp does not move past top row`() {
                terminalBuffer.moveCursorUp()
                assertEquals(0, terminalBuffer.cursorRow)
            }

            @Test
            fun `moveCursorUp does not work with negative value`() {
                assertThrows<IllegalArgumentException> { terminalBuffer.moveCursorUp(-1) }
            }

            @Test
            @Order(2)
            fun `moveCursorDown increases cursor row`() {
                terminalBuffer.setCursorPosition(10, 5)
                terminalBuffer.moveCursorDown()
                assertEquals(11, terminalBuffer.cursorRow)
            }

            @Test
            fun `moveCursorDown does not move past bottom row`() {
                terminalBuffer.moveCursorDown(100)
                assertEquals(59, terminalBuffer.cursorRow)
            }

            @Test
            fun `moveCursorDown does not work with negative value`() {
                assertThrows<IllegalArgumentException> { terminalBuffer.moveCursorDown(-1) }
            }

            @Test
            @Order(2)
            fun `moveCursorLeft decreases cursor column`() {
                terminalBuffer.setCursorPosition(10, 5)
                terminalBuffer.moveCursorLeft()
                assertEquals(4, terminalBuffer.cursorColumn)
            }

            @Test
            fun `moveCursorLeft does not move past leftmost column`() {
                terminalBuffer.moveCursorLeft()
                assertEquals(0, terminalBuffer.cursorColumn)
            }

            @Test
            fun `moveCursorLeft does not work with negative value`() {
                assertThrows<IllegalArgumentException> { terminalBuffer.moveCursorLeft(-1) }
            }

            @Test
            @Order(2)
            fun `moveCursorRight increases cursor column`() {
                terminalBuffer.setCursorPosition(10, 5)
                terminalBuffer.moveCursorRight()
                assertEquals(6, terminalBuffer.cursorColumn)
            }

            @Test
            fun `moveCursorRight does not move past rightmost column`() {
                terminalBuffer.moveCursorRight(100)
                assertEquals(79, terminalBuffer.cursorColumn)
            }

            @Test
            fun `moveCursorRight does not work with negative value`() {
                assertThrows<IllegalArgumentException> { terminalBuffer.moveCursorRight(-1) }
            }
        }
    }
}