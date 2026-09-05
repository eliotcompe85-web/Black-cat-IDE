package com.ide.mobile.feature.compiler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ParsedComposeModel(
    val composableName: String,
    val titleText: String,
    val subtitleText: String,
    val hasButton: Boolean,
    val buttonText: String
)

object LiveComposeParser {
    fun parse(code: String): ParsedComposeModel {
        val funcRegex = Regex("@Composable\\s+fun\\s+([a-zA-Z0-9_]+)")
        val funcMatch = funcRegex.find(code)
        val funcName = funcMatch?.groups?.get(1)?.value ?: "AppPreview"

        // Extract text inside Text(...) calls
        val textRegex = Regex("Text\\s*\\(\\s*(?:text\\s*=\\s*)?['\"]([^'\"]+)['\"]")
        val allTexts = textRegex.findAll(code).map { it.groups[1]?.value ?: "" }.toList()

        val title = if (allTexts.isNotEmpty()) allTexts[0] else "🐾 Black Cat Mobile Preview"
        val subtitle = if (allTexts.size > 1) allTexts[1] else "Componente renderizado en vivo"

        val hasButton = code.contains("Button") || code.contains("ElevatedButton")
        val buttonText = if (hasButton) "Pulsar Acción" else ""

        return ParsedComposeModel(
            composableName = funcName,
            titleText = title,
            subtitleText = subtitle,
            hasButton = hasButton,
            buttonText = buttonText
        )
    }
}

@Composable
fun LiveComposePreviewHost(
    code: String,
    modifier: Modifier = Modifier
) {
    val isMarkdown = remember(code) {
        code.contains("# ") || code.contains("## ") || code.contains("```")
    }

    val isHtmlWeb = remember(code) {
        code.contains("<html", ignoreCase = true) ||
        code.contains("<!DOCTYPE", ignoreCase = true) ||
        (code.contains("<div", ignoreCase = true) && code.contains("</div>", ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F101A))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preview Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        isHtmlWeb -> Icons.Default.Language
                        isMarkdown -> Icons.Default.Description
                        else -> Icons.Default.PhoneAndroid
                    },
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isHtmlWeb -> "VISTA PREVIA WEB / HTML"
                        isMarkdown -> "DOCUMENTO MARKDOWN"
                        else -> "VISTA PREVIA MÓVIL"
                    },
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when {
            isHtmlWeb -> HtmlLivePreview(code = code)
            isMarkdown -> MarkdownLivePreview(code = code)
            else -> MobileDeviceLivePreview(code = code)
        }
    }
}

@Composable
private fun MobileDeviceLivePreview(code: String) {
    val model = remember(code) { LiveComposeParser.parse(code) }
    var clickCounter by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight()
            .border(2.dp, Color(0xFF222436), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13141F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF7B61FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = model.titleText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.subtitleText,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { clickCounter++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (clickCounter == 0) "Interactuar (0 toques)" else "Toques: $clickCounter",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MarkdownLivePreview(code: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(1.dp, Color(0xFF222436), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13141F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            code.lines().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("# ") -> {
                        Text(trimmed.removePrefix("# "), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    trimmed.startsWith("## ") -> {
                        Text(trimmed.removePrefix("## "), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    trimmed.startsWith("### ") -> {
                        Text(trimmed.removePrefix("### "), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7B61FF))
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                        Row(modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)) {
                            Text("• ", color = Color(0xFF7B61FF), fontWeight = FontWeight.Bold)
                            Text(trimmed.drop(2), color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                    }
                    trimmed.startsWith("> ") -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E2030), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(trimmed.removePrefix("> "), color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    trimmed.isBlank() -> {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    else -> {
                        Text(trimmed, color = Color(0xFFE2E8F0), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HtmlLivePreview(code: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .border(1.dp, Color(0xFF222436), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13141F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated browser bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF1A1B28), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEAB308), CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
                Spacer(modifier = Modifier.width(10.dp))
                Text("localhost:3000", color = Color(0xFF64748B), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extracted web elements
            val titleMatch = Regex("<h1[^>]*>(.*?)</h1>", RegexOption.IGNORE_CASE).find(code)
            val h1Text = titleMatch?.groups?.get(1)?.value ?: "🐾 Black Cat Web App"

            val pMatch = Regex("<p[^>]*>(.*?)</p>", RegexOption.IGNORE_CASE).find(code)
            val pText = pMatch?.groups?.get(1)?.value ?: "Servidor web interactivo simulado en tiempo real."

            Text(
                text = h1Text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pText,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Botón Web Interactivo", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
