package terminalbuffer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CellAttributesTest {

    @Test
    fun `DEFAULT cell attributes are without colours or styles`() {
        val attributes = CellAttributes.DEFAULT
        assertEquals(Colour.DEFAULT, attributes.foregroundColour)
        assertEquals(Colour.DEFAULT, attributes.backgroundColour)
        assertFalse(attributes.bold)
        assertFalse(attributes.italic)
        assertFalse(attributes.underline)
    }
}