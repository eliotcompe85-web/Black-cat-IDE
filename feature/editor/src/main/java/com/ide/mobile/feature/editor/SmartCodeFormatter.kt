package com.ide.mobile.feature.editor

import com.ide.mobile.core.model.LanguageType

/**
 * Motor de formateo y embellecimiento sintáctico de código para Black Cat IDE.
 * Soporta Dart, Kotlin, JavaScript, Python, JSON y XML con indentación inteligente.
 */
object SmartCodeFormatter {

    fun format(code: String, language: LanguageType): String {
        if (code.isBlank()) return code

        return when (language) {
            LanguageType.DART -> formatBraceLanguage(code, indentSize = 2)
            LanguageType.KOTLIN -> formatBraceLanguage(code, indentSize = 4)
            LanguageType.PYTHON -> formatPython(code)
            LanguageType.XML -> formatXmlLike(code, indentSize = 2)
            LanguageType.OTHER -> {
                // Auto-detect JSON or standard brace
                if (code.trimStart().startsWith("{") || code.trimStart().startsWith("[")) {
                    formatJsonLike(code)
                } else {
                    formatBraceLanguage(code, indentSize = 2)
                }
            }
        }
    }

    private fun formatBraceLanguage(code: String, indentSize: Int): String {
        val lines = code.lines()
        val formatted = StringBuilder()
        var indentLevel = 0
        var consecutiveEmptyLines = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                consecutiveEmptyLines++
                if (consecutiveEmptyLines <= 1) {
                    formatted.append("\n")
                }
                continue
            }
            consecutiveEmptyLines = 0

            // If line starts with closing brace/bracket, decrease indent before printing
            val startsWithCloser = trimmed.startsWith("}") || trimmed.startsWith(")") || trimmed.startsWith("]")
            val effectiveIndent = if (startsWithCloser) maxOf(0, indentLevel - 1) else indentLevel

            val indentString = " ".repeat(effectiveIndent * indentSize)
            formatted.append(indentString).append(trimmed).append("\n")

            // Recalculate indent level for next lines
            var delta = 0
            var inString = false
            var stringChar = ' '

            for (i in trimmed.indices) {
                val c = trimmed[i]
                if ((c == '"' || c == '\'') && (i == 0 || trimmed[i - 1] != '\\')) {
                    if (!inString) {
                        inString = true
                        stringChar = c
                    } else if (stringChar == c) {
                        inString = false
                    }
                }
                if (!inString) {
                    when (c) {
                        '{', '(', '[' -> delta++
                        '}', ')', ']' -> delta--
                    }
                }
            }

            indentLevel = maxOf(0, indentLevel + delta)
        }

        return formatted.toString().trimEnd() + "\n"
    }

    private fun formatPython(code: String): String {
        val lines = code.lines()
        val formatted = StringBuilder()
        var consecutiveEmptyLines = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                consecutiveEmptyLines++
                if (consecutiveEmptyLines <= 2) {
                    formatted.append("\n")
                }
                continue
            }
            consecutiveEmptyLines = 0

            // Keep original leading spaces normalized to 4-space increments
            val leadingSpaces = rawLine.takeWhile { it == ' ' }.length
            val tabIndent = (leadingSpaces / 4) * 4
            val indentString = " ".repeat(tabIndent)

            formatted.append(indentString).append(trimmed).append("\n")
        }

        return formatted.toString().trimEnd() + "\n"
    }

    private fun formatJsonLike(code: String): String {
        val trimmed = code.trim()
        val result = StringBuilder()
        var indent = 0
        var inString = false

        for (i in trimmed.indices) {
            val c = trimmed[i]
            if (c == '"' && (i == 0 || trimmed[i - 1] != '\\')) {
                inString = !inString
                result.append(c)
                continue
            }

            if (inString) {
                result.append(c)
                continue
            }

            when (c) {
                '{', '[' -> {
                    result.append(c)
                    indent++
                    result.append("\n").append(" ".repeat(indent * 2))
                }
                '}', ']' -> {
                    indent = maxOf(0, indent - 1)
                    result.append("\n").append(" ".repeat(indent * 2))
                    result.append(c)
                }
                ',' -> {
                    result.append(c)
                    result.append("\n").append(" ".repeat(indent * 2))
                }
                ':' -> {
                    result.append(": ")
                }
                ' ', '\n', '\r', '\t' -> {
                    // Skip excessive whitespaces outside strings
                }
                else -> {
                    result.append(c)
                }
            }
        }

        return result.toString().trimEnd() + "\n"
    }

    private fun formatXmlLike(code: String, indentSize: Int): String {
        val lines = code.lines()
        val formatted = StringBuilder()
        var indentLevel = 0

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            val isClosingTag = trimmed.startsWith("</")
            if (isClosingTag) {
                indentLevel = maxOf(0, indentLevel - 1)
            }

            val indent = " ".repeat(indentLevel * indentSize)
            formatted.append(indent).append(trimmed).append("\n")

            val isOpeningTag = trimmed.startsWith("<") && !trimmed.startsWith("</") && !trimmed.endsWith("/>") && !trimmed.startsWith("<?")
            if (isOpeningTag) {
                indentLevel++
            }
        }

        return formatted.toString().trimEnd() + "\n"
    }
}
