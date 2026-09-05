package com.ide.mobile.feature.editor.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

data class SyntaxTheme(
    val background: Color,
    val text: Color,
    val gutterBackground: Color,
    val gutterText: Color,
    val currentLineHighlight: Color,
    val currentLineBorder: Color,
    val selectionColor: Color,
    val errorColor: Color,
    val warningColor: Color,
    val keyword: SpanStyle,
    val string: SpanStyle,
    val comment: SpanStyle,
    val number: SpanStyle,
    val function: SpanStyle,
    val typeOrClass: SpanStyle,
    val annotation: SpanStyle,
    val punctuation: SpanStyle,
    val xmlTag: SpanStyle,
    val xmlAttribute: SpanStyle,
    val xmlValue: SpanStyle
) {
    companion object {
        // Theme exactly matching the reference APK UI screenshot (Dark Violet / AI Glow)
        val AiDark = SyntaxTheme(
            background = Color(0xFF0C0D15),
            text = Color(0xFFE2E8F0),
            gutterBackground = Color(0xFF0C0D15),
            gutterText = Color(0xFF4A4C62),
            currentLineHighlight = Color(0xFF191B2E),
            currentLineBorder = Color(0xFF7B61FF),
            selectionColor = Color(0xFF2E3258),
            errorColor = Color(0xFFF87171),
            warningColor = Color(0xFFFBBF24),
            keyword = SpanStyle(color = Color(0xFFC084FC), fontWeight = FontWeight.Bold),
            string = SpanStyle(color = Color(0xFF2DD4BF)),
            comment = SpanStyle(color = Color(0xFF64748B)),
            number = SpanStyle(color = Color(0xFF38BDF8)),
            function = SpanStyle(color = Color(0xFFFDE047)),
            typeOrClass = SpanStyle(color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold),
            annotation = SpanStyle(color = Color(0xFF818CF8)),
            punctuation = SpanStyle(color = Color(0xFFCBD5E1)),
            xmlTag = SpanStyle(color = Color(0xFFC084FC), fontWeight = FontWeight.Bold),
            xmlAttribute = SpanStyle(color = Color(0xFFFBBF24)),
            xmlValue = SpanStyle(color = Color(0xFF2DD4BF))
        )

        val Darcula = AiDark
    }
}
