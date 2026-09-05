package com.ide.mobile.ui.views.aihub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.RuntimeMetrics
import com.ide.mobile.ui.components.BlackCatLogo
import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun RuntimeEngineScreen(
    metrics: RuntimeMetrics,
    onUpdateConfig: (threads: Int, contextWindow: Int, gpuLayers: Int, engineName: String) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var selectedEngine by remember { mutableStateOf(metrics.activeEngineName) }
    var cpuThreads by remember { mutableStateOf(metrics.threadsAllocated) }
    var contextWindow by remember { mutableStateOf(metrics.contextWindow) }
    var gpuLayers by remember { mutableStateOf(metrics.gpuLayers) }
    var temperature by remember { mutableStateOf(0.2f) }
    var topP by remember { mutableStateOf(0.9f) }

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
                        text = "Motor & Telemetría",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "llama.cpp • LiteRT-LM • Aceleración Hardware",
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
            // Runtime Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141522), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF26283C), RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EngineTab(
                    title = "llama.cpp",
                    subtitle = "ARM NEON / OpenCL",
                    icon = "⚡",
                    isSelected = selectedEngine.contains("llama"),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedEngine = "llama.cpp (ARM NEON)"
                        onUpdateConfig(cpuThreads, contextWindow, gpuLayers, selectedEngine)
                    }
                )
                EngineTab(
                    title = "LiteRT-LM",
                    subtitle = "Google NPU / GPU",
                    icon = "🧠",
                    isSelected = selectedEngine.contains("LiteRT"),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedEngine = "LiteRT-LM (Google NPU)"
                        onUpdateConfig(cpuThreads, contextWindow, gpuLayers, selectedEngine)
                    }
                )
            }

            // Live Telemetry Dashboard
            Text(
                text = "TELEMETRÍA DE HARDWARE EN TIEMPO REAL",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RAM Gauge Card
                TelemetryMetricCard(
                    title = "MEMORIA RAM",
                    value = String.format("%.2f GB", metrics.ramUsedMb / 1024f),
                    subvalue = "de 8.00 GB (${(metrics.ramUsedMb / 8192f * 100).toInt()}%)",
                    progress = metrics.ramUsedMb / 8192f,
                    progressColor = if (metrics.ramUsedMb < 4000f) Color(0xFF34D399) else Color(0xFFFBBF24),
                    icon = Icons.Default.Memory,
                    modifier = Modifier.weight(1f)
                )

                // Speed Gauge Card
                TelemetryMetricCard(
                    title = "VELOCIDAD TOKENS",
                    value = String.format("%.1f tok/s", metrics.tokensPerSecond),
                    subvalue = "Generación en tiempo real",
                    progress = (metrics.tokensPerSecond / 30f).coerceIn(0f, 1f),
                    progressColor = Color(0xFFC084FC),
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU & NPU Load
                TelemetryMetricCard(
                    title = "USO DE CPU",
                    value = "${metrics.cpuPercent}%",
                    subvalue = if (metrics.isNpuActive) "NPU Acelerador Activo" else "CPU Only",
                    progress = metrics.cpuPercent / 100f,
                    progressColor = Color(0xFF38BDF8),
                    icon = Icons.Default.DeveloperBoard,
                    modifier = Modifier.weight(1f)
                )

                // Thermal Status
                TelemetryMetricCard(
                    title = "ESTADO TÉRMICO",
                    value = String.format("%.1f °C", metrics.temperatureCelsius),
                    subvalue = "Óptimo • Sin throttling",
                    progress = (metrics.temperatureCelsius / 70f).coerceIn(0f, 1f),
                    progressColor = Color(0xFF34D399),
                    icon = Icons.Default.Thermostat,
                    modifier = Modifier.weight(1f)
                )
            }

            // Hardware Tuning Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141522))
                    .border(1.dp, Color(0xFF26283C), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "AJUSTES DE EJECUCIÓN NATIVA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // CPU Threads
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hilos de CPU asignados", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("$cpuThreads núcleos", color = Color(0xFFC084FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = cpuThreads.toFloat(),
                            onValueChange = {
                                cpuThreads = it.toInt()
                                onUpdateConfig(cpuThreads, contextWindow, gpuLayers, selectedEngine)
                            },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF7B61FF),
                                activeTrackColor = Color(0xFF7B61FF),
                                inactiveTrackColor = Color(0xFF26283C)
                            )
                        )
                    }

                    // Context Window Size
                    Column {
                        Text("Ventana de Contexto (Tokens)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1024, 2048, 4096, 8192).forEach { size ->
                                val isSelected = contextWindow == size
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF7B61FF) else Color(0xFF1C1D2E))
                                        .clickable {
                                            contextWindow = size
                                            onUpdateConfig(cpuThreads, contextWindow, gpuLayers, selectedEngine)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$size",
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // GPU Offload Layers
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Capas descargadas a GPU (Offload)", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("$gpuLayers capas", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = gpuLayers.toFloat(),
                            onValueChange = {
                                gpuLayers = it.toInt()
                                onUpdateConfig(cpuThreads, contextWindow, gpuLayers, selectedEngine)
                            },
                            valueRange = 0f..33f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF34D399),
                                activeTrackColor = Color(0xFF34D399),
                                inactiveTrackColor = Color(0xFF26283C)
                            )
                        )
                    }

                    // Temperature Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Temperatura de Generación", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text(String.format("%.2f", temperature), color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFBBF24),
                                activeTrackColor = Color(0xFFFBBF24),
                                inactiveTrackColor = Color(0xFF26283C)
                            )
                        )
                    }
                }
            }

            // Watermark footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Black Cat Runtime Engine • ⭐ CREATED BY J.COMPE",
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
private fun EngineTab(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF26193E) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) Color(0xFF7B61FF) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = subtitle,
                color = if (isSelected) Color(0xFFC084FC) else Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    title: String,
    value: String,
    subvalue: String,
    progress: Float,
    progressColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141522))
            .border(1.dp, Color(0xFF26283C), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, tint = progressColor, modifier = Modifier.size(16.dp))
            }

            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = Color(0xFF26283C)
            )

            Text(subvalue, color = Color(0xFF64748B), fontSize = 9.sp)
        }
    }
}
