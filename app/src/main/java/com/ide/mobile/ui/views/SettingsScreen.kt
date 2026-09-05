package com.ide.mobile.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    localHost: String = "127.0.0.1",
    localPort: Int = 11434,
    localDashboardPort: Int = 8080,
    selectedModel: String = "Qwen3.5-2B-Q4_0.gguf",
    testStatus: String? = null,
    onSaveLocalConfig: (String, Int, String) -> Unit = { _, _, _ -> },
    onTestConnection: (String, Int) -> Unit = { _, _ -> },
    onOpenModelManagement: () -> Unit = {},
    onOpenRuntimeEngine: () -> Unit = {},
    onOpenPluginsRouter: () -> Unit = {},
    onOpenSiloLibrary: () -> Unit = {}
) {
    var hostInput by remember { mutableStateOf(localHost) }
    var portInput by remember { mutableStateOf(localPort.toString()) }
    var dashboardPortInput by remember { mutableStateOf(localDashboardPort.toString()) }
    var modelInput by remember { mutableStateOf(selectedModel) }
    var useLocalByDefault by remember { mutableStateOf(true) }

    var autoSave by remember { mutableStateOf(true) }
    var codeSuggestions by remember { mutableStateOf(true) }
    var autoCompletion by remember { mutableStateOf(true) }
    var autoExplanation by remember { mutableStateOf(false) }
    var cloudSync by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configuración",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            com.ide.mobile.ui.components.BlackCatWatermarkBadge()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // CENTRO DE IA Y MODELOS BLACK CAT
        // ==========================================
        Text(
            text = "CENTRO DE IA Y MODELOS BLACK CAT",
            fontSize = 11.sp,
            color = Color(0xFFC084FC),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AiHubNavCard(
                title = "Gestor Modelos",
                subtitle = "GGUF / LiteRT • Hugging Face",
                icon = "📦",
                badge = "Modelos",
                badgeColor = Color(0xFFC084FC),
                modifier = Modifier.weight(1f),
                onClick = onOpenModelManagement
            )
            AiHubNavCard(
                title = "Motor Inferencia",
                subtitle = "llama.cpp • NPU/GPU • Telemetría",
                icon = "⚡",
                badge = "Hardware",
                badgeColor = Color(0xFF34D399),
                modifier = Modifier.weight(1f),
                onClick = onOpenRuntimeEngine
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AiHubNavCard(
                title = "Smart Router",
                subtitle = "Anti-OOM • Compresión • Cloud",
                icon = "🔀",
                badge = "Plugins",
                badgeColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                onClick = onOpenPluginsRouter
            )
            AiHubNavCard(
                title = "SiloLibrary",
                subtitle = "Base de Conocimiento RAG",
                icon = "📚",
                badge = "Vector RAG",
                badgeColor = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f),
                onClick = onOpenSiloLibrary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // SECCIÓN DESTACADA: API PARA INTELIGENCIA LOCAL
        // ==========================================
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "API para Inteligencia Local",
                fontSize = 13.sp,
                color = Color(0xFFC084FC),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = Color(0xFF26193E),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Mobile LM Server",
                    color = Color(0xFFC084FC),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141522), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF24263A), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            // Description badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181A2A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Conecta con la app Mobile LM Server que corre en tu teléfono (compatible con OpenAI / Ollama API).",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Host Presets
            Text("Dirección IP / Host:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = if (hostInput == "127.0.0.1") Color(0xFF26193E) else Color(0xFF1B1C2C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (hostInput == "127.0.0.1") Color(0xFF7B61FF) else Color(0xFF2E3146)),
                    modifier = Modifier.clickable { hostInput = "127.0.0.1" }
                ) {
                    Text("127.0.0.1 (Localhost)", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }

                Surface(
                    color = if (hostInput == "192.168.1.4") Color(0xFF26193E) else Color(0xFF1B1C2C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (hostInput == "192.168.1.4") Color(0xFF7B61FF) else Color(0xFF2E3146)),
                    modifier = Modifier.clickable { hostInput = "192.168.1.4" }
                ) {
                    Text("192.168.1.4 (LAN Teléfono)", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Editable Host Field
            EditableFieldBox(
                value = hostInput,
                onValueChange = { hostInput = it },
                placeholder = "Ej: 192.168.1.4 o 127.0.0.1"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ports Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Puerto API (Ollama/OpenAI):", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    EditableFieldBox(
                        value = portInput,
                        onValueChange = { portInput = it },
                        placeholder = "11434"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Puerto Dashboard:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    EditableFieldBox(
                        value = dashboardPortInput,
                        onValueChange = { dashboardPortInput = it },
                        placeholder = "8080"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Model Selection
            Text("Modelo Local Seleccionado:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isQwen = modelInput.contains("Qwen", ignoreCase = true)
                val isGemma = modelInput.contains("Gemma", ignoreCase = true)

                Surface(
                    color = if (isQwen) Color(0xFF26193E) else Color(0xFF1B1C2C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isQwen) Color(0xFF7B61FF) else Color(0xFF2E3146)),
                    modifier = Modifier.clickable { modelInput = "Qwen3.5-2B-Q4_0.gguf" }
                ) {
                    Text("Qwen3.5-2B-Q4_0 (llama.cpp)", fontSize = 10.sp, color = if (isQwen) Color(0xFFC084FC) else Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                }

                Surface(
                    color = if (isGemma) Color(0xFF143026) else Color(0xFF1B1C2C),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isGemma) Color(0xFF34D399) else Color(0xFF2E3146)),
                    modifier = Modifier.clickable { modelInput = "Gemma-4-E2B-IT" }
                ) {
                    Text("Gemma-4-E2B-IT (LiteRT)", fontSize = 10.sp, color = if (isGemma) Color(0xFF34D399) else Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            EditableFieldBox(
                value = modelInput,
                onValueChange = { modelInput = it },
                placeholder = "Nombre del archivo GGUF / LiteRT"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Test Connection & Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val port = portInput.toIntOrNull() ?: 11434
                        onTestConnection(hostInput.trim(), port)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B5CF6)),
                    border = BorderStroke(1.dp, Color(0xFF7B61FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Probar Conexión", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        val port = portInput.toIntOrNull() ?: 11434
                        onSaveLocalConfig(hostInput.trim(), port, modelInput.trim())
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Connection Status Feedback
            if (!testStatus.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF1B1D2E),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = testStatus,
                        color = if (testStatus.contains("🟢")) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // GRUPO: IA EN LA NUBE (GOOGLE GEMINI)
        // ==========================================
        Text("IA en la Nube (Google Gemini)", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141522), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            SettingRowText("Modelo de IA", "Gemini Flash (Online 🟢)")
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowText("Clave de API", "AQ.Ab8RN...PtmA (Conectada ✓)")
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowText("Proyecto Google Cloud", "577789803126")
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowSwitch("Completado automático", autoCompletion) { autoCompletion = it }
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowSwitch("Explicaciones automáticas", autoExplanation) { autoExplanation = it }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Group: Editor
        Text("Editor", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141522), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            SettingRowText("Tema", "Oscuro >")
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowText("Tamaño de fuente", "14 pt")
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowSwitch("Auto-guardado", autoSave) { autoSave = it }
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowSwitch("Sugerencias de código", codeSuggestions) { codeSuggestions = it }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Group: General
        Text("General", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141522), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            SettingRowText("Idioma", "Español >")
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowSwitch("Sincronización en la nube", cloudSync) { cloudSync = it }
            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
            SettingRowSwitch("Notificaciones", notifications) { notifications = it }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Acerca de Black Cat IDE
        com.ide.mobile.ui.components.BlackCatHeroCard(
            modifier = Modifier.fillMaxWidth(),
            logoSize = 100.dp
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EditableFieldBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF0F101A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2E3148), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = Color(0xFF55596E), fontSize = 12.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
            cursorBrush = SolidColor(Color(0xFF7B61FF)),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingRowText(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
        Text(text = value, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

@Composable
private fun SettingRowSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF7B61FF),
                uncheckedTrackColor = Color(0xFF2E3146)
            )
        )
    }
}

@Composable
private fun AiHubNavCard(
    title: String,
    subtitle: String,
    icon: String,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141522))
            .border(1.dp, Color(0xFF26283C), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}

