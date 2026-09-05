package com.ide.mobile.feature.compiler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        val textRegex = Regex("Text\\s*\\(\\s*text\\s*=\\s*\"([^\"]+)\"")
        val allTexts = textRegex.findAll(code).map { it.groups[1]?.value ?: "" }.toList()

        val title = if (allTexts.isNotEmpty()) allTexts[0] else "Hello, Android Smartphone Developer!"
        val subtitle = if (allTexts.size > 1) allTexts[1] else "Construido en Mobile IDE con Compose"

        val hasButton = code.contains("Button")
        val buttonText = if (code.contains("Button")) "Pulsar Acción" else ""

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
    val model = remember(code) { LiveComposeParser.parse(code) }
    var clickCounter by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141517))
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
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color(0xFF6AAB73),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE PREVIEW: @${model.composableName}",
                    color = Color(0xFF6AAB73),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = { clickCounter = 0 },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset preview state",
                    tint = Color(0xFF8C909A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Mock Smartphone Device Frame
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .weight(1f)
                .border(2.dp, Color(0xFF2B2D30), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F22))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Simulated App UI rendered dynamically
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF3574F0), CircleShape),
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
                    color = Color(0xFF8C909A),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { clickCounter++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3574F0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (clickCounter == 0) "Interactuar (0 clics)" else "Clics: $clickCounter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
