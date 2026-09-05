package com.ide.mobile.feature.keyboard

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object SmartSymbolHandler {

    private val closingPairs = mapOf(
        "{" to "}",
        "(" to ")",
        "[" to "]",
        "\"" to "\"",
        "'" to "'"
    )

    private val closingChars = setOf('}', ')', ']', '"', '\'')

    /**
     * Handles symbol insertion with smart auto-pairing, selection wrapping,
     * and "jump over" existing closing brackets.
     */
    fun handleSymbol(
        current: TextFieldValue,
        symbol: String
    ): TextFieldValue {
        val text = current.text
        val selection = current.selection
        val cursor = selection.start

        // 1. Check if user is typing a closing symbol and the next character is already that symbol
        // (JUMP OVER CLOSING BRACKET)
        if (selection.collapsed && symbol.length == 1 && symbol[0] in closingChars) {
            if (cursor < text.length && text[cursor] == symbol[0]) {
                // Jump over without inserting duplicate!
                return current.copy(selection = TextRange(cursor + 1))
            }
        }

        // 2. Auto-pair with wrap or cursor inside
        val closePair = closingPairs[symbol]
        if (closePair != null) {
            if (!selection.collapsed) {
                // Wrap current selection
                val selectedText = text.substring(selection.min, selection.max)
                val newText = text.replaceRange(selection.min, selection.max, "$symbol$selectedText$closePair")
                val newCursor = selection.min + symbol.length + selectedText.length + closePair.length
                return current.copy(
                    text = newText,
                    selection = TextRange(newCursor)
                )
            } else {
                // Auto-insert pair and place cursor between
                val newText = text.replaceRange(cursor, cursor, "$symbol$closePair")
                return current.copy(
                    text = newText,
                    selection = TextRange(cursor + symbol.length)
                )
            }
        }

        // 3. Regular symbol insertion
        val newText = text.replaceRange(selection.min, selection.max, symbol)
        return current.copy(
            text = newText,
            selection = TextRange(selection.min + symbol.length)
        )
    }

    /**
     * Insert Tab (4 spaces) or indent
     */
    fun handleTab(current: TextFieldValue): TextFieldValue {
        val text = current.text
        val selection = current.selection
        val tabSpaces = "    "
        val newText = text.replaceRange(selection.min, selection.max, tabSpaces)
        return current.copy(
            text = newText,
            selection = TextRange(selection.min + tabSpaces.length)
        )
    }

    /**
     * Move cursor left or right by delta steps
     */
    fun moveCursor(current: TextFieldValue, delta: Int): TextFieldValue {
        val currentPos = current.selection.start
        val targetPos = (currentPos + delta).coerceIn(0, current.text.length)
        return current.copy(selection = TextRange(targetPos))
    }
}
