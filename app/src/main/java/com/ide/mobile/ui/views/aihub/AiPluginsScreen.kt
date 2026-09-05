package com.ide.mobile.ui.views.aihub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.RouteDestination
import com.ide.mobile.core.model.RouterConfig
import com.ide.mobile.feature.ai.router.SmartInferenceRouter
import com.ide.mobile.ui.components.BlackCatLogo
import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun AiPluginsScreen(
    config: RouterConfig,
    onSaveConfig: (RouterConfig) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var isRouterEnabled by remember { mutableStateOf(config.isRouterEnabled) }
    var tokenThreshold by remember { mutableStateOf(config.localTokenThreshold) }
    var compressionEnabled by remember { mutableStateOf(config.promptCompressionEnabled) }
    var compressionRatio by remember { mutableStateOf(config.compressionRatio) }
    var selectedCloudProvider by remember { mutableStateOf(config.selectedCloudProvider) }

    // Interactive Router Simulator
    var testPrompt by remember { mutableStateOf("Quiero optimizar esta función en Dart para reducir reconstrucciones de widgets.") }
    val routerEngine = remember(isRouterEnabled, tokenThreshold, compressionEnabled, compressionRatio) {
        SmartInferenceRouter(
            RouterConfig(
                isRouterEnabled = isRouterEnabled,
                localTokenThreshold = tokenThreshold,
                promptCompressionEnabled = compressionEnabled,
                compressionRatio = compressionRatio,
                selectedCloudProvider = selectedCloudProvider
            )
        )
    }

    val routeResult = remember(testPrompt, routerEngine) {
        routerEngine.evaluateRoute(testPrompt)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF141522))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                BlackCatLogo(size = 28.dp, shape = RoundedCornerShape(6.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Plugins & Enrutador",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Smart Router Anti-OOM • Compresión • Cloud API",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            BlackCatWatermarkBadge()
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Smart Inference Router Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141522))
                    .border(1.dp, Color(0xFF26283C), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔀", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Enrutador Inteligente (Anti-OOM)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Desvía consultas masivas a la nube para evitar cierres por RAM",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = isRouterEnabled,
                            onCheckedChange = { isRouterEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF7B61FF),
                                uncheckedTrackColor = Color(0xFF26283C)
                            )
                        )
                    }

                    if (isRouterEnabled) {
                        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Umbral de Tokens para Ejecución Local", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                Text("$tokenThreshold tokens", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = tokenThreshold.toFloat(),
                                onValueChange = { tokenThreshold = it.toInt() },
                                valueRange = 512f..4096f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF34D399),
                                    activeTrackColor = Color(0xFF34D399),
                                    inactiveTrackColor = Color(0xFF26283C)
                                )
                            )
                            Text(
                                text = "Prompts menores a $tokenThreshold tokens se procesan en local (privado). Superiores se desvían a $selectedCloudProvider.",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Interactive Simulator Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10121D))
                    .border(1.dp, Color(0xFF1E2135), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SIMULADOR DE ENRUTAMIENTO EN VIVO",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = testPrompt,
                        onValueChange = { testPrompt = it },
                        placeholder = { Text("Escribe un prompt para evaluar la decisión del enrutador...", color = Color(0xFF64748B), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7B61FF),
                            unfocusedBorderColor = Color(0xFF26283C)
                        ),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Presets
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { testPrompt = "Explica qué es un StatefulWidget en Flutter." },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1D2E)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Consulta corta (12 tok)", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        }
                        Button(
                            onClick = {
                                testPrompt = "Analiza el siguiente código extenso de 250 líneas y refactoriza la arquitectura completa:\n" +
                                        "class HeavyState extends State<MyWidget> {\n".repeat(80) + "}"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1D2E)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Código masivo (2400 tok)", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        }
                    }

                    // Route Decision Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(routeResult.destination.colorHex).copy(alpha = 0.12f))
                            .border(1.dp, Color(routeResult.destination.colorHex).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DESTINO: ${routeResult.destination.label}",
                                    color = Color(routeResult.destination.colorHex),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "~${routeResult.estimatedTokens} tokens",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = routeResult.reason,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Prompt Compression Plugin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141522))
                    .border(1.dp, Color(0xFF26283C), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🗜️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Compresión de Prompts (Middleware)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Condensa historiales extensos antes de enviarlos al modelo local",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = compressionEnabled,
                            onCheckedChange = { compressionEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF7B61FF),
                                uncheckedTrackColor = Color(0xFF26283C)
                            )
                        )
                    }

                    if (compressionEnabled) {
                        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ratio de reducción de tokens", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("${(compressionRatio * 100).toInt()}%", color = Color(0xFFC084FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = compressionRatio,
                            onValueChange = { compressionRatio = it },
                            valueRange = 0.2f..0.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFC084FC),
                                activeTrackColor = Color(0xFFC084FC),
                                inactiveTrackColor = Color(0xFF26283C)
                            )
                        )
                    }
                }
            }

            // Cloud Model Providers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141522))
                    .border(1.dp, Color(0xFF26283C), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MODELOS EN LA NUBE (PROVEEDORES CONFIGURADOS)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    CloudProviderRow(
                        name = "Google Gemini Flash",
                        status = "🟢 Conectado y Verificado",
                        desc = "Clave API activa • projects/577789803126",
                        isSelected = selectedCloudProvider.contains("Gemini"),
                        onSelect = { selectedCloudProvider = "Google Gemini Flash" }
                    )

                    CloudProviderRow(
                        name = "OpenAI API",
                        status = "⚪ Configurable (Endpoint /v1)",
                        desc = "Compatible con GPT-4o y servidores externos",
                        isSelected = selectedCloudProvider.contains("OpenAI"),
                        onSelect = { selectedCloudProvider = "OpenAI API" }
                    )

                    CloudProviderRow(
                        name = "Anthropic Claude",
                        status = "⚪ Respaldo Cloud",
                        desc = "Claude 3.5 Sonnet para tareas de arquitectura",
                        isSelected = selectedCloudProvider.contains("Claude"),
                        onSelect = { selectedCloudProvider = "Anthropic Claude" }
                    )
                }
            }

            // Save Action Button
            Button(
                onClick = {
                    val newConfig = RouterConfig(
                        isRouterEnabled = isRouterEnabled,
                        localTokenThreshold = tokenThreshold,
                        fallbackToCloudOnOom = true,
                        promptCompressionEnabled = compressionEnabled,
                        compressionRatio = compressionRatio,
                        selectedCloudProvider = selectedCloudProvider
                    )
                    onSaveConfig(newConfig)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Guardar Configuración de Plugins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Watermark footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Black Cat AI Router & Plugins • ⭐ CREATED BY J.COMPE",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CloudProviderRow(
    name: String,
    status: String,
    desc: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF1E1638) else Color(0xFF10121D))
            .border(
                1.dp,
                if (isSelected) Color(0xFF7B61FF) else Color(0xFF1E2135),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text(status, color = Color(0xFF34D399), fontSize = 9.sp)
        }
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF7B61FF))
        )
    }
}
