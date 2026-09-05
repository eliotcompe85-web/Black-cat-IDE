package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.LocalAgentEntity
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
    selectedProvider: ProviderType = ProviderType.GEMINI_API,
    onSelectProvider: (ProviderType) -> Unit = {},
    activeAgent: LocalAgentEntity? = null,
    availableAgents: List<LocalAgentEntity> = emptyList(),
    onSelectAgent: (LocalAgentEntity) -> Unit = {},
    aiResponse: String? = null,
    isAiLoading: Boolean = false,
    onActionClick: (String) -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onInsertCode: (String) -> Unit = {},
    onClose: () -> Unit = {},
    onOpenHubPage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val actions = listOf(
        AiActionItem("EXPLAIN", Icons.Default.Code, Color(0xFFC084FC), Color(0xFF26193E), "Explicar código", "Analiza el archivo activo"),
        AiActionItem("GENERATE", Icons.Default.AutoFixHigh, Color(0xFF34D399), Color(0xFF143026), "Generar código", "Crea widgets o lógica"),
        AiActionItem("FIND_BUGS", Icons.Default.BugReport, Color(0xFF38BDF8), Color(0xFF132B3E), "Auditar fallas", "Encuentra posibles bugs"),
        AiActionItem("REFACTOR", Icons.Default.Refresh, Color(0xFFFBBF24), Color(0xFF362B15), "Refactorizar", "Mejora calidad y modularidad")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF0F111A))
            .border(1.dp, Color(0xFF2E324E), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF4A4D68))
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✨ Antigravity AI Engine",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1638), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = selectedProvider.badge,
                        color = Color(0xFFC084FC),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Provider Selector Horizontal Carousel (Intuitive, non-redundant)
        val providerList = listOf(
            ProviderType.LOCAL_AGENT to "🤖 Agentes Antigravity",
            ProviderType.GEMINI_API to "⚡ Gemini AI",
            ProviderType.OPENAI_API to "🧠 ChatGPT (GPT-4o)",
            ProviderType.CLAUDE_API to "🎭 Claude (3.5)",
            ProviderType.PERPLEXITY_API to "🔍 Perplexity",
            ProviderType.LOCAL_GGUF to "📱 Local LM"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            providerList.forEach { (type, label) ->
                val isSelected = selectedProvider == type
                Surface(
                    color = if (isSelected) Color(0xFF7B61FF).copy(alpha = 0.25f) else Color(0xFF141522),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF7B61FF) else Color(0xFF24263A)),
                    modifier = Modifier.clickable { onSelectProvider(type) }
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // When Local Agent is active: show active agent banner & agent selector
        if (selectedProvider == ProviderType.LOCAL_AGENT && activeAgent != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF141522),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF7B61FF).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activeAgent.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activeAgent.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(activeAgent.description, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
                        }
                    }

                    // Agent selector chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableAgents.forEach { agent ->
                            val isAgentActive = agent.id == activeAgent.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isAgentActive) Color(0xFF7B61FF) else Color(0xFF1E2135))
                                    .clickable { onSelectAgent(agent) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${agent.icon} ${agent.name}",
                                    color = if (isAgentActive) Color.White else Color(0xFFC084FC),
                                    fontSize = 10.sp,
                                    fontWeight = if (isAgentActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Quick Chips (Clean 4 Actions)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions.forEach { action ->
                Surface(
                    color = action.iconBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, action.iconTint.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { onActionClick(action.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(action.icon, contentDescription = null, tint = action.iconTint, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(action.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Response or Loading Display
        if (isAiLoading || !aiResponse.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color(0xFF141522),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF26283C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isAiLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color(0xFF7B61FF),
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generando respuesta en streaming...", color = Color(0xFFC084FC), fontSize = 11.sp)
                        }
                    }

                    if (!aiResponse.isNullOrBlank()) {
                        Text(
                            text = aiResponse,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily.Default
                        )

                        // If response contains code block, show Insert Button
                        if (aiResponse.contains("```")) {
                            val extractedCode = aiResponse.substringAfter("```")
                                .substringAfter("\n")
                                .substringBefore("```")

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onInsertCode(extractedCode) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Insertar en Editor", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF141522))
                .border(1.dp, Color(0xFF26283C), RoundedCornerShape(22.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = promptText,
                onValueChange = { promptText = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                cursorBrush = SolidColor(Color(0xFF7B61FF)),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (promptText.isEmpty()) {
                        val placeholder = when (selectedProvider) {
                            ProviderType.LOCAL_AGENT -> "Pregúntale a ${activeAgent?.name ?: "Antigravity"}..."
                            ProviderType.GEMINI_API -> "Consulta rápida a Gemini Flash..."
                            ProviderType.OPENAI_API -> "Pregúntale a ChatGPT (GPT-4o)..."
                            ProviderType.CLAUDE_API -> "Pregúntale a Claude 3.5 Sonnet..."
                            ProviderType.PERPLEXITY_API -> "Búsqueda web con Perplexity Sonar..."
                            else -> "Escribe tu consulta de código..."
                        }
                        Text(placeholder, color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = {
                    if (promptText.isNotBlank()) {
                        onSendMessage(promptText)
                        promptText = ""
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFF38BDF8))))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
