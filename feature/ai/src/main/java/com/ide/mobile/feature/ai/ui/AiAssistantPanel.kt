package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.feature.ai.ProviderType

data class AiActionItem(
    val id: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val title: String,
    val description: String
)

@Composable
fun AiAssistantPanel(
    onActionClick: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit,
    onHistoryClick: () -> Unit = {},
    selectedProvider: ProviderType = ProviderType.LOCAL_GGUF,
    onSelectProvider: (ProviderType) -> Unit = {},
    selectedLocalModel: String = "Qwen3.5-2B-Q4_0.gguf",
    onSelectLocalModel: (String) -> Unit = {},
    onOpenHubPage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }

    val actions = listOf(
        AiActionItem(
            id = "EXPLAIN",
            icon = Icons.Default.Code,
            iconTint = Color(0xFFC084FC),
            iconBg = Color(0xFF26193E),
            title = "Explicar código",
            description = "Explica el código seleccionado"
        ),
        AiActionItem(
            id = "GENERATE",
            icon = Icons.Default.AutoFixHigh,
            iconTint = Color(0xFF34D399),
            iconBg = Color(0xFF143026),
            title = "Generar código",
            description = "Genera código a partir de una descripción"
        ),
        AiActionItem(
            id = "FIND_BUGS",
            icon = Icons.Default.BugReport,
            iconTint = Color(0xFF38BDF8),
            iconBg = Color(0xFF132B3E),
            title = "Encontrar problemas",
            description = "Detecta posibles errores y mejoras"
        ),
        AiActionItem(
            id = "REFACTOR",
            icon = Icons.Default.Refresh,
            iconTint = Color(0xFFFBBF24),
            iconBg = Color(0xFF362B15),
            title = "Refactorizar",
            description = "Mejora el código seleccionado"
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF11121C))
            .border(1.dp, Color(0xFF232538), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Drag handle bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(4.dp)
                .background(Color(0xFF3A3D52), RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Header: Sparkle + "AI Assistant", History icon, Close icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gradient sparkle icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Black Cat AI",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1638), RoundedCornerShape(10.dp))
                        .border(0.8.dp, Color(0xFF7B61FF).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⭐ CREATED BY J.COMPE",
                        color = Color(0xFFD8B4FE),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onHistoryClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Historial",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Model Switcher Chips (Qwen3.5-2B, Gemma-4, Gemini)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isQwen = selectedProvider == ProviderType.LOCAL_GGUF && selectedLocalModel.contains("Qwen", ignoreCase = true)
            val isGemma = selectedProvider == ProviderType.LOCAL_GGUF && selectedLocalModel.contains("Gemma", ignoreCase = true)
            val isGemini = selectedProvider == ProviderType.GEMINI_API

            // Chip 1: Qwen3.5-2B (llama.cpp Local)
            Surface(
                color = if (isQwen) Color(0xFF26193E) else Color(0xFF141522),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isQwen) Color(0xFF7B61FF) else Color(0xFF24263A)),
                modifier = Modifier.clickable {
                    onSelectProvider(ProviderType.LOCAL_GGUF)
                    onSelectLocalModel("Qwen3.5-2B-Q4_0.gguf")
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 Qwen3.5-2B (Local)",
                        fontSize = 11.sp,
                        color = if (isQwen) Color(0xFFC084FC) else Color(0xFF94A3B8),
                        fontWeight = if (isQwen) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Chip 2: Gemma-4 (LiteRT-LM Local)
            Surface(
                color = if (isGemma) Color(0xFF143026) else Color(0xFF141522),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isGemma) Color(0xFF34D399) else Color(0xFF24263A)),
                modifier = Modifier.clickable {
                    onSelectProvider(ProviderType.LOCAL_GGUF)
                    onSelectLocalModel("Gemma-4-E2B-IT")
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧠 Gemma-4 (LiteRT)",
                        fontSize = 11.sp,
                        color = if (isGemma) Color(0xFF34D399) else Color(0xFF94A3B8),
                        fontWeight = if (isGemma) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Chip 3: Gemini Flash (Cloud)
            Surface(
                color = if (isGemini) Color(0xFF132B3E) else Color(0xFF141522),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isGemini) Color(0xFF38BDF8) else Color(0xFF24263A)),
                modifier = Modifier.clickable {
                    onSelectProvider(ProviderType.GEMINI_API)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Gemini Flash (Cloud)",
                        fontSize = 11.sp,
                        color = if (isGemini) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        fontWeight = if (isGemini) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // AI Hub Quick Shortcut Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF181A28),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF2E324E)),
                modifier = Modifier.clickable { onOpenHubPage("MODELS") }
            ) {
                Text(
                    text = "📦 Gestor Modelos",
                    fontSize = 10.sp,
                    color = Color(0xFFC084FC),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = Color(0xFF181A28),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF2E324E)),
                modifier = Modifier.clickable { onOpenHubPage("RUNTIME") }
            ) {
                Text(
                    text = "⚡ Telemetría & GPU",
                    fontSize = 10.sp,
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = Color(0xFF181A28),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF2E324E)),
                modifier = Modifier.clickable { onOpenHubPage("ROUTER") }
            ) {
                Text(
                    text = "🔀 Smart Router",
                    fontSize = 10.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = Color(0xFF181A28),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, Color(0xFF2E324E)),
                modifier = Modifier.clickable { onOpenHubPage("SILO") }
            ) {
                Text(
                    text = "📚 SiloLibrary RAG",
                    fontSize = 10.sp,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Greeting text with active model indicator
        val activeModelLabel = when {
            selectedProvider == ProviderType.GEMINI_API -> "Google Gemini Flash (Cloud)"
            selectedLocalModel.contains("Gemma") -> "Gemma-4-E2B-IT (LiteRT-LM • Local 11434)"
            else -> "Qwen3.5-2B (llama.cpp • Local 11434)"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¡Hola! ¿En qué puedo ayudarte hoy?",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = activeModelLabel,
                fontSize = 10.sp,
                color = Color(0xFF7B61FF),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Action Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActionClick(item.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181926)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF24263A)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(item.iconBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prompt Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(Color(0xFF141522), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF26283C), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (promptText.isEmpty()) {
                    Text(
                        text = "Pregunta algo o describe lo que necesitas...",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
                BasicTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    cursorBrush = SolidColor(Color(0xFF7B61FF)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Gradient Send Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF3B82F6))
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        if (promptText.isNotBlank()) {
                            onSendMessage(promptText)
                            promptText = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Black Cat AI • ⭐ CREATED BY J.COMPE",
                color = Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}
