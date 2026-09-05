package com.ide.mobile.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun TerminalScreen(
    logs: List<String> = emptyList(),
    onClose: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val defaultLogs = listOf(
        "[BLACK CAT IDE v1.0 • KERNEL READY]",
        "[⭐ CREATED BY J.COMPE]",
        "$ flutter run",
        "Launching lib/main.dart on Pixel 6 in debug mode...",
        "Running Gradle task 'assembleDebug'...",
        "✓ Built build/app/outputs/flutter-apk/app-debug.apk.",
        "Connecting to VM Service at ws://127.0.0.1:41235/ws...",
        "I/flutter (12345): ¡Aplicación iniciada correctamente! 🚀",
        "D/AndroidIDE: On-device runtime connected.",
        "Awaiting hot reload (press 'r')..."
    )

    val displayLogs = if (logs.isNotEmpty()) logs else defaultLogs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B10))
    ) {
        // Terminal Header Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFF141522))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1C1D2E), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Terminal",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
            
            BlackCatWatermarkBadge()

            Row {
                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "New tab", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        // Terminal Output Console
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            displayLogs.forEach { log ->
                val color = when {
                    log.startsWith("$") -> Color(0xFFC084FC)
                    log.contains("✓") || log.contains("SUCCESS") || log.contains("iniciada") -> Color(0xFF34D399)
                    log.contains("FAILED") || log.contains("Error") -> Color(0xFFF87171)
                    log.contains("Gradle") || log.contains("Launching") -> Color(0xFFFBBF24)
                    else -> Color(0xFFCBD5E1)
                }

                Text(
                    text = log,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = color,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Bottom Shell Tool Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color(0xFF141522))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("bash", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
        }
    }
}
