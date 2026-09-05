package com.ide.mobile.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun TerminalScreen(
    logs: List<String> = emptyList(),
    onSendCommand: (String) -> Unit = {},
    onClearLogs: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var inputCommand by remember { mutableStateOf("") }

    val defaultLogs = listOf(
        "[BLACK CAT IDE v1.0 • KERNEL READY]",
        "[⭐ CREATED BY J.COMPE]",
        "$ flutter run",
        "Launching lib/main.dart on Pixel 6 in debug mode...",
        "Running Gradle task 'assembleDebug'...",
        "✓ Built build/app/outputs/flutter-apk/app-debug.apk.",
        "Connecting to VM Service at ws://127.0.0.1:41235/ws...",
        "I/flutter: ¡Aplicación iniciada correctamente! 🚀",
        "Hot reload ready. Pulse 'r' para recarga rápida."
    )

    val displayLogs = if (logs.isNotEmpty()) logs else defaultLogs

    LaunchedEffect(displayLogs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B10))
    ) {
        // Terminal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFF141522))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF34D399), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Terminal (bash)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onClearLogs, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpiar consola",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        // Terminal Logs Body
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
                    log.contains("✓") || log.contains("SUCCESS") || log.contains("iniciada") || log.contains("🟢") -> Color(0xFF34D399)
                    log.contains("FAILED") || log.contains("Error") || log.contains("🔴") -> Color(0xFFF87171)
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

        // Shortcut Command Chips
        val commandChips = listOf("flutter run", "git status", "ls", "help", "clear")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F111A))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            commandChips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E2135))
                        .clickable {
                            onSendCommand(chip)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(chip, color = Color(0xFFC084FC), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        // Interactive Command Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141522))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = Color(0xFF34D399),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            BasicTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(Color(0xFF7B61FF)),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (inputCommand.isEmpty()) {
                        Text("Escribe un comando...", color = Color(0xFF64748B), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = {
                    if (inputCommand.isNotBlank()) {
                        onSendCommand(inputCommand)
                        inputCommand = ""
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Ejecutar comando",
                    tint = Color(0xFF7B61FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
