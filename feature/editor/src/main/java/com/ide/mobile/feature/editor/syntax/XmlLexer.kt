package com.ide.mobile.feature.editor.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.Severity

object XmlLexer {
    private val tokenRegex = Regex(
        "(<!--[\\s\\S]*?-->)|" +                                              // 1: Comments
        "(\"(?:\\\\.|[^\"\\\\])*\")|" +                                      // 2: Attribute values
        "(</?[a-zA-Z0-9_\\-.:]+)|" +                                         // 3: Tags (<tag, </tag)
        "([a-zA-Z0-9_\\-]+:[a-zA-Z0-9_\\-]+|[a-zA-Z0-9_\\-]+(?=\\=))|" +    // 4: Attributes
        "(/?>)"                                                               // 5: Closing tag (> or />)
    )

    fun highlight(
        xml: String,
        theme: SyntaxTheme = SyntaxTheme.Darcula,
        diagnostics: List<DiagnosticIssue> = emptyList()
    ): AnnotatedString {
        return buildAnnotatedString {
            append(xml)
            tokenRegex.findAll(xml).forEach { match ->
                val range = match.range
                val style = when {
                    match.groups[1] != null -> theme.comment
                    match.groups[2] != null -> theme.xmlValue
                    match.groups[3] != null -> theme.xmlTag
                    match.groups[4] != null -> theme.xmlAttribute
                    match.groups[5] != null -> theme.xmlTag
                    else -> null
                }
                if (style != null) {
                    addStyle(style, range.first, range.last + 1)
                }
            }

            // Real-time diagnostics
            val lines = xml.split("\n")
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
                    val startOffset = (lineStart + diag.column).coerceIn(0, xml.length)
                    val endOffset = (startOffset + diag.length).coerceIn(startOffset, xml.length)
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
