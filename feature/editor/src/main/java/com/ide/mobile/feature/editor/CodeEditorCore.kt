package com.ide.mobile.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.LanguageType
import com.ide.mobile.core.model.Severity
import com.ide.mobile.feature.editor.syntax.DartLexer
import com.ide.mobile.feature.editor.syntax.KotlinLexer
import com.ide.mobile.feature.editor.syntax.SyntaxTheme
import com.ide.mobile.feature.editor.syntax.XmlLexer

@Composable
fun CodeEditorCore(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    language: LanguageType = LanguageType.DART,
    diagnostics: List<DiagnosticIssue> = emptyList(),
    theme: SyntaxTheme = SyntaxTheme.AiDark,
    fontSize: TextUnit = 13.sp,
    showLineNumbers: Boolean = true
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lineCount = remember(textFieldValue.text) {
        val count = textFieldValue.text.count { it == '\n' } + 1
        maxOf(count, 1)
    }

    // Determine currently focused active line from cursor position
    val activeLine = remember(textFieldValue.selection, textFieldValue.text) {
        val cursor = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
        textFieldValue.text.substring(0, cursor).count { it == '\n' } + 1
    }

    val lineDiagnosticsMap = remember(diagnostics) {
        val map = mutableMapOf<Int, Severity>()
        diagnostics.forEach { diag ->
            val existing = map[diag.line]
            if (existing == null || (diag.severity == Severity.ERROR && existing != Severity.ERROR)) {
                map[diag.line] = diag.severity
            }
        }
        map
    }

    val visualTransformation = remember(language, theme, diagnostics) {
        VisualTransformation { rawText ->
            val highlighted = when (language) {
                LanguageType.KOTLIN -> KotlinLexer.highlight(rawText.text, theme, diagnostics)
                LanguageType.DART -> DartLexer.highlight(rawText.text, theme, diagnostics)
                LanguageType.XML -> XmlLexer.highlight(rawText.text, theme, diagnostics)
                else -> rawText
            }
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }

    val lineHeight = (fontSize.value * 1.5).sp

    val codeStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = theme.text
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Line numbers gutter with exact active line indicator matching reference screenshot
        if (showLineNumbers) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(theme.gutterBackground)
                    .verticalScroll(verticalScroll)
                    .padding(top = 12.dp, bottom = 12.dp)
                    .widthIn(min = 44.dp)
            ) {
                for (line in 1..lineCount) {
                    val isLineActive = line == activeLine
                    val severity = lineDiagnosticsMap[line]

                    Row(
                        modifier = Modifier
                            .height((fontSize.value * 1.5).dp)
                            .fillMaxWidth()
                            .background(if (isLineActive) theme.currentLineHighlight else Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Purple active line left vertical accent bar
                        if (isLineActive) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(theme.currentLineBorder)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        // Diagnostic dot
                        if (severity != null) {
                            val dotColor = if (severity == Severity.ERROR) theme.errorColor else theme.warningColor
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(5.dp)
                                    .background(dotColor, CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(9.dp))
                        }

                        // Line number text
                        Text(
                            text = line.toString(),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = (fontSize.value * 0.9).sp,
                                lineHeight = lineHeight,
                                color = if (isLineActive) Color(0xFFE2E8F0) else theme.gutterText,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        )
                    }
                }
            }
        }

        // Code Editor text input area with synchronized scrolling
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = onValueChange,
                modifier = Modifier.width(IntrinsicSize.Max),
                textStyle = codeStyle,
                cursorBrush = SolidColor(theme.currentLineBorder),
                visualTransformation = visualTransformation
            )
        }
    }
}
