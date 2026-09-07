package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Componente interactivo para renderizar bloques de código generados por la IA.
 * Incluye barra de herramientas con detección de lenguaje, botón de copiado rápido
 * y acción de inserción/aplicación directa al editor activo.
 */
@Composable
fun CodeBlockCard(
    code: String,
    language: String = "kotlin",
    onApplyCode: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val langTag = language.uppercase().ifBlank { "CODE" }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0D0F18),
        border = BorderStroke(1.dp, Color(0xFF262A40))
    ) {
        Column {
            // Barra de herramientas superior del bloque de código
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF151828))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta del Lenguaje
                Surface(
                    color = Color(0xFF7B61FF).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF7B61FF).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = langTag,
                        color = Color(0xFFB4A5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Botones de acción
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón Copiar al Portapapeles
                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            isCopied = true
                            coroutineScope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isCopied) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFF20243B),
                            contentColor = if (isCopied) Color(0xFF34D399) else Color(0xFFCBD5E1)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copiar código",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copiado!" else "Copiar",
                            fontSize = 11.sp
                        )
                    }

                    if (onApplyCode != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        // Botón Aplicar al Editor
                        Button(
                            onClick = { onApplyCode(code) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B61FF),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Aplicar al Editor",
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Aplicar",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Cuerpo del Código con scroll horizontal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
