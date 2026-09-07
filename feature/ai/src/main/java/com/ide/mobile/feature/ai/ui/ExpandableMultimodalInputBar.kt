package com.ide.mobile.feature.ai.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private const val IMAGE_MIME_TYPE = "image/*"

/**
 * Barra de Entrada Multimodal expandible con soporte para adjuntar imágenes
 * (capturas de pantalla, mockups, diagramas de arquitectura) para asistentes de IA con visión.
 */
@Composable
fun ExpandableMultimodalInputBar(
    onSendMessage: (text: String, attachments: List<Uri>) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var textState by remember { mutableStateOf("") }
    // Lista de URIs adjuntas (capturas de pantalla, esquemas, etc.)
    var attachedUris by remember { mutableStateOf(listOf<Uri>()) }

    // Selector de archivos del sistema (imágenes para la IA con visión)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachedUris = attachedUris + uris
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // 1. Fila de Miniaturas de Archivos Adjuntos (Visión Multimodal)
            if (attachedUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachedUris.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            // Renderizado de miniatura (Requiere dependencia de Coil)
                            AsyncImage(
                                model = uri,
                                contentDescription = "Imagen adjunta para análisis de IA",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Botón para eliminar el adjunto
                            IconButton(
                                onClick = { attachedUris = attachedUris - uri },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover adjunto",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1.5 Fila de Comandos Rápidos Slash (Antigravity & Kiro) y Modo Agéntico
            var isPlanningMode by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de Modo: Planning vs Fast
                Surface(
                    color = if (isPlanningMode) Color(0xFF26193E) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPlanningMode) Color(0xFFC084FC) else Color(0xFF475569)),
                    modifier = Modifier.clickable { isPlanningMode = !isPlanningMode }
                ) {
                    Text(
                        text = if (isPlanningMode) "🛠️ Planning Mode" else "⚡ Fast Mode",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlanningMode) Color(0xFFE9D5FF) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Chips de Slash Commands Antigravity
                val slashCommands = listOf(
                    "/plan" to "📋 /plan",
                    "/review" to "🔍 /review",
                    "/fix" to "🛠️ /fix",
                    "/test" to "🧪 /test",
                    "/run" to "▶️ /run",
                    "/diff" to "📑 /diff",
                    "/clear" to "🧹 /clear"
                )

                slashCommands.forEach { (cmd, label) ->
                    Surface(
                        color = Color(0xFF161826),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2E334D)),
                        modifier = Modifier.clickable {
                            textState = if (textState.isBlank()) "$cmd " else "$cmd $textState"
                        }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Barra de Entrada de Texto Dinámica y Controles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Botón para adjuntar imágenes (Dispara capacidades multimodales del ApiAgent)
                IconButton(
                    onClick = { imagePickerLauncher.launch(IMAGE_MIME_TYPE) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Adjuntar diseño o captura para la IA",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Campo de texto expansible (Escalable de 1 a 6 líneas de forma fluida)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (textState.isEmpty()) {
                        Text(
                            text = "Pregúntale al agente o arrastra un diseño...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 6, // Transición fluida controlada por restricciones del layout
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón de Envío
                val isEnabled = (textState.isNotBlank() || attachedUris.isNotEmpty()) && !isLoading
                FloatingActionButton(
                    onClick = {
                        if (isEnabled) {
                            val promptToSend = if (isPlanningMode && !textState.startsWith("/")) {
                                "/plan $textState"
                            } else {
                                textState
                            }
                            onSendMessage(promptToSend, attachedUris)
                            textState = ""
                            attachedUris = emptyList() // Limpiar tras enviar
                        }
                    },
                    containerColor = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar mensaje al agente",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
