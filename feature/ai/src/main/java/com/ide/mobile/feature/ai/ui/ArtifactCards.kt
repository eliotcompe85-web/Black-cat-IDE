package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ide.mobile.core.model.ChatContent

@Composable
fun ActionChecklistCard(
    content: ChatContent.ActionChecklist,
    onHumanTaskToggled: (taskId: String, isCompleted: Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Checklist",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content.tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.requiresHuman) {
                        // Tarea manual: El usuario puede hacer clic
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onHumanTaskToggled(task.id, it) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
                        )
                    } else {
                        // Tarea del Agente: Indicador visual de estado (completado o pendiente)
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(20.dp)
                        )
                    }

                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (task.requiresHuman) {
                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                            Text(
                                text = "Tu turno",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeBlockArtifactCard(
    content: ChatContent.CodeBlock,
    onApplyToWorkspace: (code: String, path: String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F111E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Cabecera del bloque de código
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = content.targetFilePath ?: content.language,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón de Copiar
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(content.code))
                            copied = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = if (copied) "Copiado" else "Copiar",
                            tint = if (copied) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Si el agente identificó el archivo destino, mostrar botón de Aplicar
                    val targetPath = content.targetFilePath
                    if (targetPath != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = { onApplyToWorkspace(content.code, targetPath) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aplicar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Cuerpo del código
            Text(
                text = content.code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
