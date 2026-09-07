package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.ActionStatus
import com.ide.mobile.core.model.AgentAction
import com.ide.mobile.core.model.AgentActionType

/**
 * Tarjeta interactiva para la aprobación y visualización de acciones autónomas
 * propuestas por los agentes Antigravity.
 */
@Composable
fun ActionCardItem(
    action: AgentAction,
    onApprove: (AgentAction) -> Unit,
    onReject: (AgentAction) -> Unit,
    onViewInTerminal: ((AgentAction) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val actionIcon: ImageVector = when (action.type) {
        AgentActionType.RUN_COMMAND -> Icons.Default.Terminal
        AgentActionType.CREATE_FOLDER -> Icons.Default.CreateNewFolder
        AgentActionType.CREATE_FILE -> Icons.Default.Add
        AgentActionType.MODIFY_FILE -> Icons.Default.Edit
        AgentActionType.INSTALL_DEPENDENCY -> Icons.Default.Download
        AgentActionType.HTTP_REQUEST -> Icons.Default.Cloud
    }

    val (statusColor, statusText) = when (action.status) {
        ActionStatus.PROPOSED -> Color(0xFFF59E0B) to "Esperando Aprobación"
        ActionStatus.RUNNING -> Color(0xFF00E5FF) to "Ejecutando..."
        ActionStatus.SUCCESS -> Color(0xFF10B981) to "Ejecutado con Éxito"
        ActionStatus.FAILED -> Color(0xFFEF4444) to "Fallido"
        ActionStatus.REJECTED -> Color(0xFF6B7280) to "Rechazado"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF131522),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Cabecera: Icono, Título y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF7B61FF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = action.type.name,
                                tint = Color(0xFFB4A5FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = action.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val path = action.targetPath
                        if (!path.isNullOrBlank()) {
                            Text(
                                text = path,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Badge de estado
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            val desc = action.description
            if (!desc.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            // Bloque de Payload (Comando bash o contenido)
            if (action.payload.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF090A10),
                    border = BorderStroke(0.5.dp, Color(0xFF202336)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = action.payload,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Salida de la consola (si está disponible)
            if (!action.output.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Salida: ${action.output}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Botones de acción interactiva si está propuesto
            if (action.status == ActionStatus.PROPOSED) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (action.type == AgentActionType.RUN_COMMAND && onViewInTerminal != null) {
                        OutlinedButton(
                            onClick = { onViewInTerminal(action) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Terminal", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Botón Rechazar
                    OutlinedButton(
                        onClick = { onReject(action) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Rechazar",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón Aprobar
                    Button(
                        onClick = { onApprove(action) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Aprobar",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
