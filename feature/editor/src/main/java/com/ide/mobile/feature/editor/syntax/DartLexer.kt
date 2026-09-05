package com.ide.mobile.feature.editor.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.Severity

object DartLexer {
    private val tokenRegex = Regex(
        "(//.*|/\\*[\\s\\S]*?\\*/)|" +                                                       // 1: Comments
        "(\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*')|" + // 2: Strings
        "(@[a-zA-Z_][a-zA-Z0-9_]*)|" +                                                      // 3: Annotations (@override)
        "(\\b(?:import|library|part|export|class|extends|with|implements|void|return|" +
        "const|final|var|late|required|this|super|new|if|else|switch|case|default|" +
        "for|while|do|break|continue|try|catch|finally|throw|rethrow|async|await|" +
        "yield|get|set|static|abstract|true|false|null|is|as)\\b)|" +                         // 4: Keywords
        "(\\b\\d+(?:\\.\\d+)?\\b)|" +                                                        // 5: Numbers
        "(\\b[A-Z][a-zA-Z0-9_]*\\b)|" +                                                      // 6: Types / Classes
        "(\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\\())|" +                                         // 7: Function calls
        "([{}()\\[\\];,.:?+\\-*/%<>=!&|^~])"                                                // 8: Punctuation
    )

    fun highlight(
        code: String,
        theme: SyntaxTheme = SyntaxTheme.AiDark,
        diagnostics: List<DiagnosticIssue> = emptyList()
    ): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            tokenRegex.findAll(code).forEach { match ->
                val range = match.range
                val style = when {
                    match.groups[1] != null -> theme.comment
                    match.groups[2] != null -> theme.string
                    match.groups[3] != null -> theme.annotation
                    match.groups[4] != null -> theme.keyword
                    match.groups[5] != null -> theme.number
                    match.groups[6] != null -> theme.typeOrClass
                    match.groups[7] != null -> theme.function
                    match.groups[8] != null -> theme.punctuation
                    else -> null
                }
                if (style != null) {
                    addStyle(style, range.first, range.last + 1)
                }
            }

            // Diagnostic Underlines
            val lines = code.split("\n")
            val lineStartOffsets = ArrayList<Int>(lines.size + 1)
            var accum = 0
            for (line in lines) {
                lineStartOffsets.add(accum)
                accum += line.length + 1
            }

            diagnostics.forEach { diag ->
                val lineIdx = diag.line - 1
                if (lineIdx in lineStartOffsets.indices) {
                    val lineStart = lineStartOffsets[lineIdx]
                    val startOffset = (lineStart + diag.column).coerceIn(0, code.length)
                    val endOffset = (startOffset + diag.length).coerceIn(startOffset, code.length)
                    if (startOffset < endOffset) {
                        val diagStyle = when (diag.severity) {
                            Severity.ERROR -> androidx.compose.ui.text.SpanStyle(
                                color = theme.errorColor,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                            Severity.WARNING -> androidx.compose.ui.text.SpanStyle(
                                color = theme.warningColor,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                            Severity.INFO -> null
                        }
                        if (diagStyle != null) {
                            addStyle(diagStyle, startOffset, endOffset)
                        }
                    }
                }
            }
        }
    }
}
