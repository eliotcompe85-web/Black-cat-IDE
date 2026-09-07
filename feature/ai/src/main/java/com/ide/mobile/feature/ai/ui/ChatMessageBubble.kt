package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente de burbuja de conversación para mensajes individuales.
 * Soporta alineación adaptativa según emisor (Usuario vs Agente), avatar,
 * e iteración reactiva sobre contenidos estructurados [ChatContent]
 * (Texto, Bloques de Código interactivos y Tarjetas de Acción de Terminal).
 */
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onApproveAction: (AgentAction) -> Unit,
    onRejectAction: (AgentAction) -> Unit,
    onViewInTerminal: ((AgentAction) -> Unit)? = null,
    onApplyCode: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormatter.format(Date(message.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Encabezado de la Burbuja: Avatar, Nombre y Hora
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp)
        ) {
            if (!isUser) {
                // Avatar del Agente
                Surface(
                    color = Color(0xFF7B61FF).copy(alpha = 0.2f),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, Color(0xFF7B61FF)),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = message.agentIcon ?: "🤖",
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.agentName ?: "Antigravity Agent",
                    color = Color(0xFFC7D2FE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Tú",
                    color = Color(0xFF93C5FD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = Color(0xFF2563EB).copy(alpha = 0.25f),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, Color(0xFF3B82F6)),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeStr,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }

        // Cuerpo de la Burbuja
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = when {
                isUser -> Color(0xFF1E2640)
                isSystem -> Color(0xFF181B28)
                else -> Color(0xFF151828)
            },
            border = BorderStroke(
                1.dp,
                when {
                    isUser -> Color(0xFF384B70)
                    isSystem -> Color(0xFF2A2E44)
                    else -> Color(0xFF262B44)
                }
            ),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Renderizado secuencial de ChatContent
                val contents = message.resolvedContents
                if (contents.isEmpty() && message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    contents.forEachIndexed { index, content ->
                        when (content) {
                            is ChatContent.Text -> {
                                if (content.text.isNotBlank()) {
                                    Text(
                                        text = content.text,
                                        color = if (isUser) Color(0xFFF1F5F9) else Color(0xFFE2E8F0),
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                            is ChatContent.CodeBlock -> {
                                CodeBlockCard(
                                    code = content.code,
                                    language = content.language,
                                    onApplyCode = onApplyCode,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            is ChatContent.ActionCard -> {
                                ActionCardItem(
                                    action = content.action,
                                    onApprove = onApproveAction,
                                    onReject = onRejectAction,
                                    onViewInTerminal = onViewInTerminal,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (message.isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "● ● ●",
                        color = Color(0xFF7B61FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
