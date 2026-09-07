package com.ide.mobile.feature.ai.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ide.mobile.core.model.ActiveAgentContext
import com.ide.mobile.core.model.AgentState

@Composable
fun MissionControlDashboard(
    activeAgents: List<ActiveAgentContext>,
    onDelegateTask: () -> Unit,
    onResumeAgent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp) // Ancho típico de panel lateral
            .background(Color(0xFF0D0F18))
            .padding(16.dp)
    ) {
        // Cabecera del Mission Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mission Control",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDelegateTask) {
                Icon(
                    imageVector = Icons.Default.AddTask,
                    contentDescription = "Delegar nueva tarea",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Lista de Agentes Activos
        if (activeAgents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay agentes en ejecución activa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeAgents, key = { it.agentId }) { agent ->
                    AgentStatusCard(
                        agent = agent,
                        onResumeClick = { onResumeAgent(agent.agentId) }
                    )
                }
            }
        }
    }
}

@Composable
fun AgentStatusCard(
    agent: ActiveAgentContext,
    onResumeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151828)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Nombre e Icono de Tipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (agent.isLocal) Icons.Default.Computer else Icons.Default.Cloud,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = agent.agentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila inferior: Estado animado
            AnimatedContent(targetState = agent.currentState, label = "AgentStateAnimation") { state ->
                when (state) {
                    is AgentState.Idle -> StatusBadge(Icons.Default.PauseCircle, "Inactivo", MaterialTheme.colorScheme.outline)
                    is AgentState.Planning -> StatusBadge(Icons.Default.Psychology, "Planificando: ${state.currentThought}", MaterialTheme.colorScheme.tertiary)
                    is AgentState.Executing -> {
                        Column {
                            StatusBadge(Icons.Default.Build, state.taskDescription, MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            val animatedProgress by animateFloatAsState(targetValue = state.progress, label = "Progress")
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                    is AgentState.Verifying -> StatusBadge(Icons.Default.Verified, "Validando: ${state.verificationStep}", MaterialTheme.colorScheme.secondary)
                    is AgentState.WaitingForUser -> {
                        Column {
                            StatusBadge(Icons.Default.Warning, state.actionRequired, MaterialTheme.colorScheme.error)
                            if (state.canResume) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onResumeClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text("Resolver y Continuar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
