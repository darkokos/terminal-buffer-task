package terminalbuffer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class CellTest {

    @Nested
    inner class Initialisation {

        private val cell = Cell()

        @Test
        fun `default cell holds a whitespace with default attributes`() {
            assertEquals(' ', cell.char)
            assertEquals(CellAttributes.DEFAULT, cell.attributes)
            assertEquals(1, cell.width)
            assertFalse(cell.isContinuation)
        }

        @Test
        fun `EMPTY cell is the same as default cell`() {
            assertEquals(cell, Cell.EMPTY)
        }
    }

    @Test
    fun `continuation factory creates correct cell`() {
        val cell = Cell.continuation(CellAttributes(foregroundColour = Colour.RED))

        assertTrue(cell.isContinuation)
        assertEquals(Cell.EMPTY_CHAR, cell.char)
        assertEquals(CellAttributes(foregroundColour = Colour.RED), cell.attributes)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class WidthTest {

        @Test
        fun `continuation cell has width 1`() {
            val cell = Cell.continuation()

            assertEquals(cell.width, 1)
        }

        fun characterWidths() = listOf(
            // code < 32
            Arguments.of('\u0000', 0),
            Arguments.of('\u001F', 0),
            Arguments.of('\u0020', 1),

            // code == 0x7F
            Arguments.of('\u007E', 1),
            Arguments.of('\u007F', 0),
            Arguments.of('\u0080', 1),

            // 0x00AD soft hyphen
            Arguments.of('\u00AC', 1),
            Arguments.of('\u00AD', 0),
            Arguments.of('\u00AE', 1),

            // 0x0300..0x036F combining diacritical marks
            Arguments.of('\u02FF', 1),
            Arguments.of('\u0300', 0),
            Arguments.of('\u0301', 0),
            Arguments.of('\u036E', 0),
            Arguments.of('\u036F', 0),
            Arguments.of('\u0370', 1),

            // 0x1100..0x115F Hangul Jamo
            Arguments.of('\u10FF', 1),
            Arguments.of('\u1100', 2),
            Arguments.of('\u1101', 2),
            Arguments.of('\u115E', 2),
            Arguments.of('\u115F', 2),
            Arguments.of('\u1160', 1),

            // 0x1AB0..0x1AFF combining diacritical marks extended
            Arguments.of('\u1AAF', 1),
            Arguments.of('\u1AB0', 0),
            Arguments.of('\u1AB1', 0),
            Arguments.of('\u1AFE', 0),
            Arguments.of('\u1AFF', 0),
            Arguments.of('\u1B00', 1),

            // 0x1DC0..0x1DFF combining diacritical marks supplement
            Arguments.of('\u1DBF', 1),
            Arguments.of('\u1DC0', 0),
            Arguments.of('\u1DC1', 0),
            Arguments.of('\u1DFE', 0),
            Arguments.of('\u1DFF', 0),
            Arguments.of('\u1E00', 1),

            // 0x200B..0x200D zero-width characters
            Arguments.of('\u200A', 1),
            Arguments.of('\u200B', 0),
            Arguments.of('\u200C', 0),
            Arguments.of('\u200D', 0),
            Arguments.of('\u200E', 1),

            // 0x2060 word joiner
            Arguments.of('\u205F', 1),
            Arguments.of('\u2060', 0),
            Arguments.of('\u2061', 1),

            // 0x20D0..0x20FF combining marks for symbols
            Arguments.of('\u20CF', 1),
            Arguments.of('\u20D0', 0),
            Arguments.of('\u20D1', 0),
            Arguments.of('\u20FE', 0),
            Arguments.of('\u20FF', 0),
            Arguments.of('\u2100', 1),

            // 0x2329..0x232A angle brackets
            Arguments.of('\u2328', 1),
            Arguments.of('\u2329', 2),
            Arguments.of('\u232A', 2),
            Arguments.of('\u232B', 1),

            // 0x2E80..0x2FFF CJK radicals
            Arguments.of('\u2E7F', 1),
            Arguments.of('\u2E80', 2),
            Arguments.of('\u2E81', 2),
            Arguments.of('\u2FFE', 2),
            Arguments.of('\u2FFF', 2),
            Arguments.of('\u3000', 2),

            // 0x3000..0x33FF CJK symbols, Hiragana, Katakana
            Arguments.of('\u3001', 2),
            Arguments.of('\u33FE', 2),
            Arguments.of('\u33FF', 2),
            Arguments.of('\u3400', 2),

            // 0x3400..0x4DBF CJK extension A
            Arguments.of('\u3400', 2),
            Arguments.of('\u4DBE', 2),
            Arguments.of('\u4DBF', 2),
            Arguments.of('\u4DC0', 1),

            // 0x4E00..0x9FFF CJK unified ideographs
            Arguments.of('\u4DFF', 1),
            Arguments.of('\u4E00', 2),
            Arguments.of('\u4E01', 2),
            Arguments.of('\u9FFE', 2),
            Arguments.of('\u9FFF', 2),
            Arguments.of('\uA000', 1),

            // 0xAC00..0xD7AF Hangul syllables
            Arguments.of('\uABFF', 1),
            Arguments.of('\uAC00', 2),
            Arguments.of('\uAC01', 2),
            Arguments.of('\uD7AE', 2),
            Arguments.of('\uD7AF', 2),
            Arguments.of('\uD7B0', 1),

            // 0xF900..0xFAFF CJK compatibility ideographs
            Arguments.of('\uF8FF', 1),
            Arguments.of('\uF900', 2),
            Arguments.of('\uF901', 2),
            Arguments.of('\uFAFE', 2),
            Arguments.of('\uFAFF', 2),
            Arguments.of('\uFB00', 1),

            // 0xFE00..0xFE0F variation selectors
            Arguments.of('\uFDFF', 1),
            Arguments.of('\uFE00', 0),
            Arguments.of('\uFE01', 0),
            Arguments.of('\uFE0E', 0),
            Arguments.of('\uFE0F', 0),
            Arguments.of('\uFE10', 1),

            // 0xFE20..0xFE2F combining half marks
            Arguments.of('\uFE1F', 1),
            Arguments.of('\uFE20', 0),
            Arguments.of('\uFE21', 0),
            Arguments.of('\uFE2E', 0),
            Arguments.of('\uFE2F', 0),
            Arguments.of('\uFE30', 1),

            // 0xFEFF BOM
            Arguments.of('\uFEFE', 1),
            Arguments.of('\uFEFF', 0),
            Arguments.of('\uFF00', 1),

            // 0xFF01..0xFF60 fullwidth forms
            Arguments.of('\uFF01', 2),
            Arguments.of('\uFF02', 2),
            Arguments.of('\uFF5F', 2),
            Arguments.of('\uFF60', 2),
            Arguments.of('\uFF61', 1),

            // 0xFFE0..0xFFE6 fullwidth signs
            Arguments.of('\uFFDF', 1),
            Arguments.of('\uFFE0', 2),
            Arguments.of('\uFFE1', 2),
            Arguments.of('\uFFE5', 2),
            Arguments.of('\uFFE6', 2),
            Arguments.of('\uFFE7', 1),
        )

        @ParameterizedTest
        @MethodSource("characterWidths")
        fun `cell width is correct for provided char`(char: Char, width: Int) {
            assertEquals(width, Cell(char).width)
        }
    }
}