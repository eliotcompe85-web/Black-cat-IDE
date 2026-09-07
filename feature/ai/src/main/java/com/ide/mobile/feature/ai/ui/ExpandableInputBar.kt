package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Barra de entrada de texto expandible dinámicamente de 1 a 6 líneas
 * con botón de envío reactivo y estética moderna en tema oscuro.
 */
@Composable
fun ExpandableInputBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isLoading: Boolean = false,
    placeholderText: String = "Pregunta al agente o pide una acción en el código...",
    modifier: Modifier = Modifier
) {
    val canSend = promptText.isNotBlank() && !isLoading

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131625),
        border = BorderStroke(1.dp, if (canSend) Color(0xFF7B61FF).copy(alpha = 0.6f) else Color(0xFF262B44))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Campo de Texto Expandible (1 a 6 líneas)
            TextField(
                value = promptText,
                onValueChange = onPromptChange,
                placeholder = {
                    Text(
                        text = placeholderText,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 150.dp),
                minLines = 1,
                maxLines = 6,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color(0xFF00E5FF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                enabled = !isLoading
            )

            // Botón Limpiar (si hay texto)
            if (promptText.isNotEmpty()) {
                IconButton(
                    onClick = { onPromptChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar texto",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Botón Enviar con estilo reactivo
            FilledIconButton(
                onClick = {
                    if (canSend) {
                        onSendMessage(promptText)
                    }
                },
                enabled = canSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (canSend) Color(0xFF7B61FF) else Color(0xFF1E2238),
                    contentColor = if (canSend) Color.White else Color(0xFF475569),
                    disabledContainerColor = Color(0xFF1B1E32),
                    disabledContentColor = Color(0xFF475569)
                ),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Color(0xFF7B61FF),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar mensaje",
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
