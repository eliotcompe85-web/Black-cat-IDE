package com.ide.mobile.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.ApiKeysConfig
import com.ide.mobile.core.model.LocalAgentEntity
import com.ide.mobile.ui.components.BlackCatHeroCard
import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun SettingsScreen(
    apiKeysConfig: ApiKeysConfig = ApiKeysConfig(),
    localHost: String = "127.0.0.1",
    localPort: Int = 11434,
    localDashboardPort: Int = 8080,
    selectedModel: String = "Qwen3.5-2B-Q4_0.gguf",
    testStatus: String? = null,
    availableAgents: List<LocalAgentEntity> = emptyList(),
    onSaveApiKeys: (ApiKeysConfig) -> Unit = {},
    onSaveLocalConfig: (String, Int, String) -> Unit = { _, _, _ -> },
    onTestConnection: (String, Int) -> Unit = { _, _ -> },
    onImportAgentClick: () -> Unit = {},
    onOpenModelManagement: () -> Unit = {},
    onOpenRuntimeEngine: () -> Unit = {},
    onOpenPluginsRouter: () -> Unit = {},
    onOpenSiloLibrary: () -> Unit = {}
) {
    var geminiKey by remember { mutableStateOf(apiKeysConfig.geminiApiKey) }
    var openAiKey by remember { mutableStateOf(apiKeysConfig.openAiApiKey) }
    var openAiModel by remember { mutableStateOf(apiKeysConfig.openAiModel) }
    var claudeKey by remember { mutableStateOf(apiKeysConfig.claudeApiKey) }
    var claudeModel by remember { mutableStateOf(apiKeysConfig.claudeModel) }
    var perplexityKey by remember { mutableStateOf(apiKeysConfig.perplexityApiKey) }
    var perplexityModel by remember { mutableStateOf(apiKeysConfig.perplexityModel) }

    var hostInput by remember { mutableStateOf(localHost) }
    var portInput by remember { mutableStateOf(localPort.toString()) }
    var modelInput by remember { mutableStateOf(selectedModel) }

    var autoSave by remember { mutableStateOf(true) }
    var codeSuggestions by remember { mutableStateOf(true) }

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
            Column {
                Text(
                    text = "Ajustes & Configuración",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "APIs en línea • Agentes Locales • Runtime",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            BlackCatWatermarkBadge()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 1. PROVEEDORES DE IA EN LÍNEA (APIs)
        // ==========================================
        Text(
            text = "PROVEEDORES DE IA EN LÍNEA (CLOUD)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = Color(0xFF141522),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF26283C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Gemini API
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Gemini AI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("🟢 Integrado", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                SettingsInputField(
                    label = "Clave API de Gemini",
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    placeholder = "AQ.Ab8RN..."
                )

                HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

                // OpenAI ChatGPT
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OpenAI ChatGPT (GPT-4o)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    if (openAiKey.isNotBlank()) {
                        Text("🟢 Configurado", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SettingsInputField(
                    label = "Clave API OpenAI",
                    value = openAiKey,
                    onValueChange = { openAiKey = it },
                    placeholder = "sk-proj-..."
                )

                HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

                // Anthropic Claude
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎭", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anthropic Claude (3.5 Sonnet)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    if (claudeKey.isNotBlank()) {
                        Text("🟢 Configurado", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SettingsInputField(
                    label = "Clave API de Claude",
                    value = claudeKey,
                    onValueChange = { claudeKey = it },
                    placeholder = "sk-ant-..."
                )

                HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

                // Perplexity AI
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Perplexity AI (Sonar / Web)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    if (perplexityKey.isNotBlank()) {
                        Text("🟢 Configurado", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SettingsInputField(
                    label = "Clave API de Perplexity",
                    value = perplexityKey,
                    onValueChange = { perplexityKey = it },
                    placeholder = "pplx-..."
                )

                // Save APIs Button
                Button(
                    onClick = {
                        onSaveApiKeys(
                            ApiKeysConfig(
                                geminiApiKey = geminiKey,
                                openAiApiKey = openAiKey,
                                openAiModel = openAiModel,
                                claudeApiKey = claudeKey,
                                claudeModel = claudeModel,
                                perplexityApiKey = perplexityKey,
                                perplexityModel = perplexityModel
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Claves de API", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. AGENTES LOCALES DEL TELÉFONO (ANTIGRAVITY)
        // ==========================================
        Text(
            text = "AGENTES LOCALES (ANTIGRAVITY ENGINE)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = Color(0xFF141522),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF26283C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Agentes y Habilidades cargadas desde el almacenamiento del teléfono:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                availableAgents.forEach { agent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F111A), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(agent.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(agent.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(agent.description, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
                            Text("Skills: ${agent.skills.joinToString(", ")}", color = Color(0xFFC084FC), fontSize = 9.sp)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onImportAgentClick,
                    border = BorderStroke(1.dp, Color(0xFF7B61FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar Agente desde el Teléfono (.agent.json / .md)", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 3. ACCESOS DIRECTOS AL AI HUB
        // ==========================================
        Text(
            text = "CENTRO DE MODELOS Y TELEMETRÍA (AI HUB)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpenModelManagement,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1638)),
                border = BorderStroke(1.dp, Color(0xFF7B61FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text("📦 Gestor Modelos", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenRuntimeEngine,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132B3E)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text("⚡ Motor & RAM", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpenPluginsRouter,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF362B15)),
                border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text("🛡️ Anti-OOM Router", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenSiloLibrary,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF143026)),
                border = BorderStroke(1.dp, Color(0xFF34D399)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text("📚 RAG SiloLibrary", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 4. SERVIDOR LOCAL (MOBILE LM SERVER)
        // ==========================================
        Text(
            text = "SERVIDOR LOCAL ON-DEVICE (MOBILE LM)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = Color(0xFF141522),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF26283C)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsInputField(label = "Host / IP del Teléfono", value = hostInput, onValueChange = { hostInput = it }, placeholder = "127.0.0.1")
                SettingsInputField(label = "Puerto API", value = portInput, onValueChange = { portInput = it }, placeholder = "11434")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onTestConnection(hostInput, portInput.toIntOrNull() ?: 11434) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26283C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Probar Conexión", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onSaveLocalConfig(hostInput, portInput.toIntOrNull() ?: 11434, modelInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Guardar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (testStatus != null) {
                    Text(testStatus, color = Color(0xFF38BDF8), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Footer
        BlackCatHeroCard()

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color(0xFF0F111A), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF26283C), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                cursorBrush = SolidColor(Color(0xFF7B61FF)),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, color = Color(0xFF475569), fontSize = 12.sp)
                    inner()
                }
            )
        }
    }
}
