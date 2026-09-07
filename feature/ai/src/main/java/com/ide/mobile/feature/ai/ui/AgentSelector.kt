package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.Agent
import com.ide.mobile.core.model.ApiAgent
import com.ide.mobile.core.model.LocalAgent

/**
 * Componente selector de Agentes con menú desplegable, indicadores visuales
 * de estado (Local vs Cloud, isReady activa, modelo o binario) y registro de nuevos ApiAgents.
 */
@Composable
fun AgentSelector(
    selectedAgent: Agent?,
    availableAgents: List<Agent>,
    onSelectAgent: (Agent) -> Unit,
    onAddNewApiAgent: (Agent.ApiAgent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Botón principal del Selector
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF16192A),
            border = BorderStroke(1.dp, Color(0xFF2C324E)),
            modifier = Modifier
                .clickable { isExpanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador visual de estado activo / isReady (Verde si isReady, ámbar si no)
                val isReady = selectedAgent?.isReady == true
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isReady) Color(0xFF10B981) else Color(0xFFF59E0B),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Icono del Agente
                Text(
                    text = selectedAgent?.icon ?: "🤖",
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Nombre del Agente
                Text(
                    text = selectedAgent?.name ?: "Seleccionar Agente",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Distintivo Visual (Badge Local vs API)
                if (selectedAgent != null) {
                    val isLocal = selectedAgent is LocalAgent
                    Surface(
                        color = if (isLocal) Color(0xFF7B61FF).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            0.5.dp,
                            if (isLocal) Color(0xFF7B61FF).copy(alpha = 0.6f) else Color(0xFF38BDF8).copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = if (isLocal) "LOCAL" else "API",
                            color = if (isLocal) Color(0xFFC4B5FD) else Color(0xFF7DD3FC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Desplegar agentes",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Menú Desplegable con opciones
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .background(Color(0xFF151828))
                .widthIn(min = 280.dp)
        ) {
            Text(
                text = "SELECCIONA UN AGENTE ANTIGRAVITY",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            availableAgents.forEach { agent ->
                val isSelected = agent.id == selectedAgent?.id
                val isLocal = agent is LocalAgent

                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(agent.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = agent.name,
                                        color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    val subText = when (agent) {
                                        is LocalAgent -> "Binario: ${agent.executablePath}"
                                        is ApiAgent -> "Modelo: ${agent.modelName}"
                                    }
                                    Text(
                                        text = subText,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isLocal) Color(0xFF7B61FF).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isLocal) "LOCAL" else "API",
                                        color = if (isLocal) Color(0xFFC4B5FD) else Color(0xFF7DD3FC),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Indicador de disponibilidad (isReady)
                                val readyColor = if (agent.isReady) Color(0xFF10B981) else Color(0xFFEF4444)
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = if (agent.isReady) "Listo" else "No configurado",
                                    tint = readyColor,
                                    modifier = Modifier.size(14.dp)
                                )

                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        onSelectAgent(agent)
                        isExpanded = false
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color(0xFF23273D)
            )

            // Acción para añadir nuevo Agente API
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Añadir nuevo Agente API",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                },
                onClick = {
                    isExpanded = false
                    showAddDialog = true
                }
            )
        }
    }

    if (showAddDialog) {
        AddApiAgentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newAgent ->
                onAddNewApiAgent(newAgent)
                showAddDialog = false
            }
        )
    }
}
