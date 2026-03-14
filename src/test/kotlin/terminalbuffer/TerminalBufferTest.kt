package terminalbuffer

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Order
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

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

    companion object {

        @JvmStatic
        fun invalidScreenPositionSource() = listOf(
            Arguments.of(5, -1),
            Arguments.of(5, 60),
            Arguments.of(5, 61),
            Arguments.of(-1, 5),
            Arguments.of(80, 5),
            Arguments.of(81, 5),
        )

        @JvmStatic
        fun invalidBufferPositionSource() = listOf(
            Arguments.of(5, -1),
            Arguments.of(5, 70),
            Arguments.of(5, 71),
            Arguments.of(-1, 5),
            Arguments.of(80, 5),
            Arguments.of(81, 5),
        )
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
                for (i in 0..<3) {
                    terminalBuffer.write("Line$i")
                    terminalBuffer.insertLineAtBottom()
                    if (i != 2) terminalBuffer.setCursorPosition(0, 0)
                }

                terminalBuffer.maxScrollback = 2
                assertEquals(
                    "Line1${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(0)
                )
                assertEquals(
                    "Line2${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(1)
                )
            }

            @Test
            fun `increasing maxScrollback preserves existing lines`() {
                for (i in 0..<3) {
                    terminalBuffer.write("Line$i")
                    terminalBuffer.insertLineAtBottom()
                    if (i != 2) terminalBuffer.setCursorPosition(0, 0)
                }

                terminalBuffer.maxScrollback = 20
                assertEquals(
                    "Line0${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(0)
                )
                assertEquals(
                    "Line1${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(1)
                )
                assertEquals(
                    "Line2${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(2)
                )
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
                terminalBuffer.setCursorPosition(5, 10)
                assertEquals(10, terminalBuffer.cursorRow)
                assertEquals(5, terminalBuffer.cursorColumn)
            }

            @ParameterizedTest
            @CsvSource(
                "80, 60",
                "81, 61",
            )
            @Order(1)
            fun `setCursorPosition clamps cursor to height and width of buffer`(column: Int, row: Int) {
                terminalBuffer.setCursorPosition(column, row)
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
                terminalBuffer.setCursorPosition(5, 10)
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
                terminalBuffer.setCursorPosition(5, 10)
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
                terminalBuffer.setCursorPosition(5, 10)
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
                terminalBuffer.setCursorPosition(5, 10)
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

        @Nested
        inner class Write {

            @Test
            fun `write does not write control character`() {
                terminalBuffer.write("\u0000")
                assertEquals(' ', terminalBuffer.getCharAt(0, 0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(0, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write does write wide character to buffer with width of 2`() {
                val narrowTerminalBuffer = TerminalBuffer(2, 10, 10)
                narrowTerminalBuffer.write("\uFF01")
                assertEquals("\uFF01 ", narrowTerminalBuffer.getLineTextAt(0))
                assertEquals(0, narrowTerminalBuffer.cursorRow)
                assertEquals(2, narrowTerminalBuffer.cursorColumn)
            }

            @Test
            fun `write does write wide character to buffer with width of 3`() {
                val narrowTerminalBuffer = TerminalBuffer(3, 10, 10)
                narrowTerminalBuffer.write("\uFF01")
                assertEquals("\uFF01  ", narrowTerminalBuffer.getLineTextAt(0))
                assertEquals(0, narrowTerminalBuffer.cursorRow)
                assertEquals(2, narrowTerminalBuffer.cursorColumn)
            }

            @Test
            fun `write does not write wide character to buffer with width of 1`() {
                val narrowTerminalBuffer = TerminalBuffer(1, 10, 10)
                narrowTerminalBuffer.write("\uFF01")
                assertEquals(" ", narrowTerminalBuffer.getLineTextAt(0))
                assertEquals(0, narrowTerminalBuffer.cursorRow)
                assertEquals(0, narrowTerminalBuffer.cursorColumn)
            }

            @Test
            fun `write does not push to scrollback when wide character exactly fits the last line of the screen`() {
                terminalBuffer.setCursorPosition(terminalBuffer.width - 2, terminalBuffer.height - 1)
                terminalBuffer.write("\uFF01")
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(
                    " ".repeat(80),
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 2)
                )
                assertEquals(
                    "${" ".repeat(78)}\uFF01 ",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 1)
                )
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(terminalBuffer.width, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write pushes to scrollback when wide character wraps the last line of screen`() {
                terminalBuffer.setCursorPosition(terminalBuffer.width - 1, terminalBuffer.height- 1)
                terminalBuffer.write("\uFF01")
                assertEquals(1, terminalBuffer.scrollbackSize)
                assertEquals(
                    " ".repeat(80),
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 2)
                )
                assertEquals(
                    "\uFF01 ${" ".repeat(78)}",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 1)
                )
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write does not wrap to next line when wide character fits screen width exactly`() {
                terminalBuffer.write("\uFF01".repeat(40))
                assertEquals("\uFF01 ".repeat(40), terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(80, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write wraps to next line when continuation exceeds width`() {
                terminalBuffer.write("\uFF01".repeat(39))
                terminalBuffer.write("A\uFF01")
                assertEquals("${"\uFF01 ".repeat(39)}A ", terminalBuffer.getLineTextAt(0))
                assertEquals("\uFF01 ${" ".repeat(78)}", terminalBuffer.getLineTextAt(1))
                assertEquals(1, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write wraps to next line when wide character exceeds width`() {
                terminalBuffer.write("\uFF01".repeat(41))
                assertEquals("\uFF01 ".repeat(40), terminalBuffer.getLineTextAt(0))
                assertEquals("\uFF01 ${" ".repeat(78)}", terminalBuffer.getLineTextAt(1))
                assertEquals(1, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a wide character over a wide character`() {
                terminalBuffer.write("\uFF01")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("\uFF02")
                assertEquals("\uFF02 ${" ".repeat(78)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a wide character over a continuation followed by a wide character`() {
                terminalBuffer.write("\uFF01\uFF02")
                terminalBuffer.setCursorPosition(1, 0)
                terminalBuffer.write("\uFF03")
                assertEquals(" \uFF03 ${" ".repeat(77)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(3, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a wide character over a character with width of 1 followed by a wide character`() {
                terminalBuffer.write("A\uFF01")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("\uFF02")
                assertEquals("\uFF02 ${" ".repeat(78)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a wide character over a continuation followed by a character with width of 1`() {
                terminalBuffer.write("\uFF01A")
                terminalBuffer.setCursorPosition(1, 0)
                terminalBuffer.write("\uFF02")
                assertEquals(" \uFF02 ${" ".repeat(77)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(3, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a wide character over two consecutive characters with width of 1`() {
                terminalBuffer.write("AA")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("\uFF01")
                assertEquals("\uFF01 ${" ".repeat(78)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write does not push to scrollback when character with width of 1 exactly fits the last line of the screen`() {
                terminalBuffer.setCursorPosition(terminalBuffer.width - 1, terminalBuffer.height - 1)
                terminalBuffer.write("A")
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(
                    " ".repeat(80),
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 2)
                )
                assertEquals(
                    "${" ".repeat(79)}A",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 1)
                )
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(terminalBuffer.width, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write pushes to scrollback when character with width of 1 wraps the last line of screen`() {
                terminalBuffer.setCursorPosition(terminalBuffer.width - 1, terminalBuffer.height - 1)
                terminalBuffer.write("AA")
                assertEquals(1, terminalBuffer.scrollbackSize)
                assertEquals(
                    "${" ".repeat(79)}A",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 2)
                )
                assertEquals(
                    "A${" ".repeat(79)}",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 1)
                )
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(1, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write does not wrap to next line when character with width of 1 fits the buffer width exactly`() {
                terminalBuffer.write("A".repeat(80))
                assertEquals("A".repeat(80), terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(80, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write wraps to next line when character with width of 1 exceeds buffer width`() {
                terminalBuffer.write("A".repeat(81))
                assertEquals("A".repeat(80), terminalBuffer.getLineTextAt(0))
                assertEquals("A${" ".repeat(79)}", terminalBuffer.getLineTextAt(1))
                assertEquals(1, terminalBuffer.cursorRow)
                assertEquals(1, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a character with width of 1 over a character with width of 1`() {
                terminalBuffer.write("A")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("B")
                assertEquals("B${" ".repeat(79)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(1, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a character with width of 1 over a wide character`() {
                terminalBuffer.write("\uFF01")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("A")
                assertEquals("A ${" ".repeat(78)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(1, terminalBuffer.cursorColumn)
            }

            @Test
            fun `write correctly writes a character with width of 1 over a continuation`() {
                terminalBuffer.write("\uFF01")
                terminalBuffer.setCursorPosition(1, 0)
                terminalBuffer.write("A")
                assertEquals(" A${" ".repeat(78)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
            }
        }

        @Nested
        inner class Insert {

            @Test
            fun `insert does not write empty string`() {
                terminalBuffer.insert("")
                assertEquals(" ".repeat(80), terminalBuffer.getLineTextAt(0))
            }

            @Test
            fun `insert at end of buffer content appends text to buffer content`() {
                terminalBuffer.insert("A")
                assertEquals("A${" ".repeat(79)}", terminalBuffer.getLineTextAt(0))
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(1, terminalBuffer.cursorColumn)
                assertEquals(terminalBuffer.currentCellAttributes, CellAttributes.DEFAULT)
            }

            @Test
            fun `insert at end of buffer content appends text to buffer and does not push to scrollback`() {
                terminalBuffer.setCursorPosition(terminalBuffer.width - 1, terminalBuffer.height - 1)
                terminalBuffer.insert("A")
                assertEquals(
                    " ".repeat(80),
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 2)
                )
                assertEquals(
                    "${" ".repeat(79)}A",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 1)
                )
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(terminalBuffer.width, terminalBuffer.cursorColumn)
                assertEquals(terminalBuffer.currentCellAttributes, CellAttributes.DEFAULT)
            }

            @Test
            fun `insert past end of buffer content appends text to buffer and pushes to scrollback`() {
                terminalBuffer.setCursorPosition(terminalBuffer.width - 1, terminalBuffer.height - 1)
                terminalBuffer.insert("AA")
                assertEquals(
                    "${" ".repeat(79)}A",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 2)
                )
                assertEquals(
                    "A${" ".repeat(79)}",
                    terminalBuffer.getLineTextAt(terminalBuffer.height - 1)
                )
                assertEquals(1, terminalBuffer.scrollbackSize)
                assertEquals(terminalBuffer.height - 1, terminalBuffer.cursorRow)
                assertEquals(1, terminalBuffer.cursorColumn)
                assertEquals(terminalBuffer.currentCellAttributes, CellAttributes.DEFAULT)
            }

            @Test
            fun `insert preserves shifted wide characters`() {
                terminalBuffer.insert("\uFF01")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.insert("A")
                assertEquals(Cell('A'), terminalBuffer.getCellAt(0, 0))
                assertEquals(Cell('\uFF01'), terminalBuffer.getCellAt(1, 0))
                assertEquals(Cell.continuation(), terminalBuffer.getCellAt(2, 0))
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(3, terminalBuffer.cursorColumn)
                assertEquals(terminalBuffer.currentCellAttributes, CellAttributes.DEFAULT)
            }

            @Test
            fun `insert shifts cells past insertion point and preserves their and buffer cell attributes`() {
                terminalBuffer.setForegroundColour(Colour.MAGENTA)
                terminalBuffer.insert("\uFF01")
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.setForegroundColour(Colour.GREEN)
                terminalBuffer.insert("A")
                assertEquals(
                    Cell('A', CellAttributes(foregroundColour = Colour.GREEN)),
                    terminalBuffer.getCellAt(0, 0)
                )
                assertEquals(
                    Cell('\uFF01', CellAttributes(foregroundColour = Colour.MAGENTA)),
                    terminalBuffer.getCellAt(1, 0)
                )
                assertEquals(
                    Cell.continuation(CellAttributes(foregroundColour = Colour.MAGENTA)),
                    terminalBuffer.getCellAt(2, 0)
                )
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(3, terminalBuffer.cursorColumn)
                assertEquals(
                    terminalBuffer.currentCellAttributes,
                    CellAttributes(foregroundColour = Colour.GREEN)
                )
            }

            @Test
            fun `insert shifts cells that span multiple lines past insertion point and preserves their and buffer cell attributes`() {
                terminalBuffer.setForegroundColour(Colour.MAGENTA)
                terminalBuffer.insert("B".repeat(81))
                terminalBuffer.setCursorPosition(3, 0)
                terminalBuffer.setForegroundColour(Colour.GREEN)
                terminalBuffer.insert("A")
                for (i in 0..<3) {
                    assertEquals(
                        Cell('B', CellAttributes(foregroundColour = Colour.MAGENTA)),
                        terminalBuffer.getCellAt(i, 0)
                    )
                }
                assertEquals(
                    Cell('A', CellAttributes(foregroundColour = Colour.GREEN)),
                    terminalBuffer.getCellAt(3, 0)
                )
                for (i in 4..<80) {
                    assertEquals(
                        Cell('B', CellAttributes(foregroundColour = Colour.MAGENTA)),
                        terminalBuffer.getCellAt(i, 0)
                    )
                }
                for (i in 0..<2) {
                    assertEquals(
                        Cell('B', CellAttributes(foregroundColour = Colour.MAGENTA)),
                        terminalBuffer.getCellAt(i, 1)
                    )
                }
                assertEquals(0, terminalBuffer.scrollbackSize)
                assertEquals(1, terminalBuffer.cursorRow)
                assertEquals(2, terminalBuffer.cursorColumn)
                assertEquals(
                    terminalBuffer.currentCellAttributes,
                    CellAttributes(foregroundColour = Colour.GREEN)
                )
            }
        }

        @Nested
        inner class FillLine {

            @Test
            @Order(1)
            fun `fillLine fills a line with the specified character`() {
                terminalBuffer.fillLine('a')
                assertEquals(
                    "a".repeat(80),
                    terminalBuffer.getLineTextAt(terminalBuffer.cursorRow)
                )
                assertEquals(0, terminalBuffer.cursorRow)
                assertEquals(0, terminalBuffer.cursorColumn)
            }

            @Test
            @Order(2)
            fun `fillLine clears a line if called with a whitespace`() {
                terminalBuffer.fillLine('a')
                terminalBuffer.fillLine(' ')
                assertEquals(
                    " ".repeat(80),
                    terminalBuffer.getLineTextAt(terminalBuffer.cursorRow)
                )
            }
        }

        @Nested
        inner class InsertLineAtBottom {

            @Test
            fun `insertLineAtBottom moves the top line to scrollback and adds an empty line at the bottom of the screen`() {
                terminalBuffer.write("asd")
                terminalBuffer.insertLineAtBottom()
                assertEquals("asd${" ".repeat(77)}", terminalBuffer.getLineTextFromAllAt(0))
                assertEquals(" ".repeat(80), terminalBuffer.getLineTextAt(0))
            }
        }

        @Nested
        inner class Clear {

            @Test
            fun `clearScreen clears all screen lines`() {
                terminalBuffer.write("Hello")
                terminalBuffer.setCursorPosition(0, 1)
                terminalBuffer.write("Earth")
                terminalBuffer.clearScreen()

                for (i in 0..<60) {
                    assertEquals(" ".repeat(80), terminalBuffer.getLineTextAt(i))
                }
            }

            @Test
            fun `clearAll clears all screen and scrollback lines`() {
                terminalBuffer.write("Hello")
                terminalBuffer.insertLineAtBottom()
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("Earth")
                terminalBuffer.clearAll()
                for (i in 0..<60) {
                    assertEquals(" ".repeat(80), terminalBuffer.getLineTextFromAllAt(i))
                }
                assertEquals(0, terminalBuffer.scrollbackSize)
            }
        }

        @Nested
        inner class ContentAccess {

            @Test
            fun `getCharAt returns correct character`() {
                terminalBuffer.write("Hello")
                assertEquals('e', terminalBuffer.getCharAt(1, 0))
            }

            @ParameterizedTest
            @MethodSource("terminalbuffer.TerminalBufferTest#invalidScreenPositionSource")
            fun `getCharAt throws if called with position outside of screen`(column: Int, row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getCharAt(column, row) }
            }

            @Test
            fun `getAttributesAt returns correct attributes`() {
                terminalBuffer.setForegroundColour(Colour.MAGENTA)
                terminalBuffer.write("Hello")
                assertEquals(
                    CellAttributes(foregroundColour = Colour.MAGENTA),
                    terminalBuffer.getAttributesAt(1, 0)
                )
            }

            @ParameterizedTest
            @MethodSource("terminalbuffer.TerminalBufferTest#invalidScreenPositionSource")
            fun `getAttributesAt throws if called with position outside of screen`(column: Int, row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getAttributesAt(column, row) }
            }

            @Test
            fun `getCellAt returns correct cell`() {
                terminalBuffer.write("Hello")
                assertEquals(Cell('e'), terminalBuffer.getCellAt(1, 0))
            }

            @ParameterizedTest
            @MethodSource("terminalbuffer.TerminalBufferTest#invalidScreenPositionSource")
            fun `getCellAt throws if called with position outside of screen`(column: Int, row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getCellAt(column, row) }
            }

            @Test
            fun `getCharFromAllAt returns correct character from scrollback`() {
                terminalBuffer.write("Hello")
                terminalBuffer.insertLineAtBottom()
                assertEquals('e', terminalBuffer.getCharFromAllAt(1, 0))
            }

            @Test
            fun `getCharFromAllAt returns correct character from screen`() {
                terminalBuffer.write("Hello")
                assertEquals('e', terminalBuffer.getCharFromAllAt(1, 0))
            }

            @ParameterizedTest
            @MethodSource("terminalbuffer.TerminalBufferTest#invalidBufferPositionSource")
            fun `getCharFromAllAt throws if called with position outside of buffer`(column: Int, row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getCharFromAllAt(column, row) }
            }

            @Test
            fun `getAttributesFromAllAt returns correct attributes from scrollback`() {
                terminalBuffer.setForegroundColour(Colour.MAGENTA)
                terminalBuffer.write("Hello")
                terminalBuffer.insertLineAtBottom()
                assertEquals(
                    CellAttributes(foregroundColour = Colour.MAGENTA),
                    terminalBuffer.getAttributesFromAllAt(1, 0)
                )
            }

            @Test
            fun `getAttributesFromAllAt returns correct attributes from screen`() {
                terminalBuffer.setForegroundColour(Colour.MAGENTA)
                terminalBuffer.write("Hello")
                assertEquals(
                    CellAttributes(foregroundColour = Colour.MAGENTA),
                    terminalBuffer.getAttributesFromAllAt(1, 0)
                )
            }

            @ParameterizedTest
            @MethodSource("terminalbuffer.TerminalBufferTest#invalidBufferPositionSource")
            fun `getAttributesFromAllAt throws if called with position outside of buffer`(column: Int, row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getAttributesFromAllAt(column, row) }
            }

            @Test
            fun `getCellFromAllAt returns correct cell from scrollback`() {
                terminalBuffer.write("Hello")
                terminalBuffer.insertLineAtBottom()
                assertEquals(Cell('e'), terminalBuffer.getCellFromAllAt(1, 0))
            }

            @Test
            fun `getCellFromAllAt returns correct cell from screen`() {
                terminalBuffer.write("Hello")
                assertEquals(Cell('e'), terminalBuffer.getCellFromAllAt(1, 0))
            }

            @ParameterizedTest
            @MethodSource("terminalbuffer.TerminalBufferTest#invalidBufferPositionSource")
            fun `getCellFromAllAt throws if called with position outside of buffer`(column: Int, row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getCellFromAllAt(column, row) }
            }

            @Test
            fun `getLineTextAt returns string representation of line`() {
                terminalBuffer.write("Hello")
                assertEquals("Hello${" ".repeat(75)}", terminalBuffer.getLineTextAt(0))
            }

            @ParameterizedTest
            @ValueSource(ints = [-1, 60, 61])
            fun `getLineTextAt throws if called with row outside of screen`(row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getLineTextAt(row) }
            }

            @Test
            fun `getLineTextFromAllAt returns string representation of line from the scrollback`() {
                terminalBuffer.write("Hello")
                terminalBuffer.insertLineAtBottom()
                assertEquals(
                    "Hello${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(0)
                )
            }

            @Test
            fun `getLineTextFromAllAt returns string representation of line from the screen`() {
                terminalBuffer.write("Hello")
                assertEquals(
                    "Hello${" ".repeat(75)}",
                    terminalBuffer.getLineTextFromAllAt(0)
                )
            }

            @ParameterizedTest
            @ValueSource(ints = [-1, 70, 71])
            fun `getLineTextFromAllAt throws if called with row outside of buffer`(row: Int) {
                assertThrows<IndexOutOfBoundsException> { terminalBuffer.getLineTextFromAllAt(row) }
            }

            @Test
            fun `getScreenText returns string representation of screen`() {
                terminalBuffer.write("Hello")
                terminalBuffer.setCursorPosition(0, 1)
                terminalBuffer.write("Earth")

                val sb = StringBuilder()
                sb.appendLine("Hello${" ".repeat(75)}")
                sb.appendLine("Earth${" ".repeat(75)}")
                for (i in 0..<58) {
                    sb.append(" ".repeat(80))
                    if (i != 57) {
                        sb.append("\n")
                    }
                }

                assertEquals(sb.toString(), terminalBuffer.getScreenText())
            }

            @Test
            fun `getTextFromAll returns string representation of buffer`() {
                terminalBuffer.write("Hello")
                terminalBuffer.insertLineAtBottom()
                terminalBuffer.setCursorPosition(0, 0)
                terminalBuffer.write("Earth")

                val sb = StringBuilder()
                sb.appendLine("Hello${" ".repeat(75)}")
                sb.appendLine("Earth${" ".repeat(75)}")
                for (i in 0..<59) {
                    sb.append(" ".repeat(80))
                    if (i != 58) {
                        sb.append("\n")
                    }
                }

                assertEquals(sb.toString(), terminalBuffer.getTextFromAll())
            }
        }
    }
}