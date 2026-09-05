package com.ide.mobile.feature.keyboard

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartSymbolHandlerTest {

    @Test
    fun testAutoPairCurlyBraces() {
        val initial = TextFieldValue("")
        val result = SmartSymbolHandler.handleSymbol(initial, "{")
        assertEquals("{}", result.text)
        assertEquals(TextRange(1), result.selection)
    }

    @Test
    fun testJumpOverClosingCurlyBrace() {
        // Cursor is between { and }
        val inside = TextFieldValue("{}", TextRange(1))
        // User presses }
        val result = SmartSymbolHandler.handleSymbol(inside, "}")
        // Should NOT duplicate }, but jump over it to index 2!
        assertEquals("{}", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun testJumpOverClosingParenthesis() {
        val inside = TextFieldValue("()", TextRange(1))
        val result = SmartSymbolHandler.handleSymbol(inside, ")")
        assertEquals("()", result.text)
        assertEquals(TextRange(2), result.selection)
    }

    @Test
    fun testWrapSelection() {
        val selected = TextFieldValue("val x = 1", TextRange(0, 9))
        val result = SmartSymbolHandler.handleSymbol(selected, "(")
        assertEquals("(val x = 1)", result.text)
        assertEquals(TextRange(11), result.selection)
    }

    @Test
    fun testTabInsertion() {
        val initial = TextFieldValue("")
        val result = SmartSymbolHandler.handleTab(initial)
        assertEquals("    ", result.text)
        assertEquals(TextRange(4), result.selection)
    }
}
