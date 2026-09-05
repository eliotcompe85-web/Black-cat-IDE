package com.ide.mobile.feature.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AutoFixHigh
import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.QuickFix

@Composable
fun MobileCodeKeyboardBar(
    currentValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    focusedIssue: DiagnosticIssue? = null,
    onApplyQuickFix: (QuickFix) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val symbolList = listOf(
        "{", "}",
        "(", ")",
        "[", "]",
        "\"",
        ";", ":",
        ".", ",",
        "=", "->",
        "?", "!",
        "<", ">",
        "+", "-",
        "*", "/",
        "//"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF26282E))
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Contextual Quick Fix Assistant Button (displayed when cursor is on an error)
        if (focusedIssue?.quickFix != null) {
            TextButton(
                onClick = { onApplyQuickFix(focusedIssue.quickFix) },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF3574F0),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = focusedIssue.quickFix.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        // Tab Key (4 spaces)
        TextButton(
            onClick = { onValueChange(SmartSymbolHandler.handleTab(currentValue)) },
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF32353B),
                contentColor = Color(0xFFDFE1E5)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text("Tab", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        // Micro-cursor movement
        TextButton(
            onClick = { onValueChange(SmartSymbolHandler.moveCursor(currentValue, -1)) },
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF32353B),
                contentColor = Color(0xFFDFE1E5)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cursor Left",
                modifier = Modifier.size(16.dp)
            )
        }

        TextButton(
            onClick = { onValueChange(SmartSymbolHandler.moveCursor(currentValue, 1)) },
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF32353B),
                contentColor = Color(0xFFDFE1E5)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Cursor Right",
                modifier = Modifier.size(16.dp)
            )
        }

        // Undo & Redo
        TextButton(
            onClick = onUndo,
            enabled = canUndo,
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF32353B),
                contentColor = if (canUndo) Color(0xFFDFE1E5) else Color(0xFF6C707E)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                modifier = Modifier.size(16.dp)
            )
        }

        TextButton(
            onClick = onRedo,
            enabled = canRedo,
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color(0xFF32353B),
                contentColor = if (canRedo) Color(0xFFDFE1E5) else Color(0xFF6C707E)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                modifier = Modifier.size(16.dp)
            )
        }

        // Symbol buttons with auto-pairing and jump-over closing bracket logic
        symbolList.forEach { sym ->
            TextButton(
                onClick = {
                    onValueChange(SmartSymbolHandler.handleSymbol(currentValue, sym))
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF32353B),
                    contentColor = Color(0xFFCFD3DC)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = sym,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
