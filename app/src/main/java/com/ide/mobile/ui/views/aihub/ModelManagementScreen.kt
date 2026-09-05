package com.ide.mobile.ui.views.aihub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.ModelFormat
import com.ide.mobile.core.model.ModelItem
import com.ide.mobile.core.model.ModelStatus
import com.ide.mobile.ui.components.BlackCatLogo
import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun ModelManagementScreen(
    installedModels: List<ModelItem>,
    catalogModels: List<ModelItem>,
    onRunModel: (String) -> Unit,
    onStopModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    onDownloadModel: (ModelItem) -> Unit,
    onImportLocalFile: (String, Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importFileName by remember { mutableStateOf("custom_model.gguf") }
    var importFileSizeMb by remember { mutableStateOf("1200") }

    val filteredInstalled = remember(installedModels, selectedFilter) {
        when (selectedFilter) {
            "GGUF" -> installedModels.filter { it.format == ModelFormat.LLAMA_CPP_GGUF }
            "LITERT" -> installedModels.filter { it.format == ModelFormat.LITERT_LM }
            else -> installedModels
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
    ) {
        // Top Bar
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
                        text = "Gestor de Modelos",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GGUF • LiteRT-LM • Hugging Face",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            BlackCatWatermarkBadge()
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            // Filter Pills & Import Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            label = "Todos",
                            selected = selectedFilter == "TODOS",
                            onClick = { selectedFilter = "TODOS" }
                        )
                        FilterChip(
                            label = "GGUF (llama.cpp)",
                            selected = selectedFilter == "GGUF",
                            onClick = { selectedFilter = "GGUF" }
                        )
                        FilterChip(
                            label = "LiteRT-LM",
                            selected = selectedFilter == "LITERT",
                            onClick = { selectedFilter = "LITERT" }
                        )
                    }

                    // Import SAF Button
                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26193E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Installed Models Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODELOS EN DISPOSITIVO (${filteredInstalled.size})",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Installed Models List
            items(filteredInstalled) { model ->
                InstalledModelCard(
                    model = model,
                    onRun = { onRunModel(model.id) },
                    onStop = { onStopModel(model.id) },
                    onDelete = { onDeleteModel(model.id) }
                )
            }

            // Public Catalogue (Hugging Face Mobile)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATÁLOGO HUGGING FACE MOBILE",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF143026), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Descarga 1-Tap", color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(catalogModels) { catalogItem ->
                val isAlreadyInstalledOrDownloading = installedModels.any { it.name == catalogItem.name || it.id == catalogItem.id }
                val installedVersion = installedModels.firstOrNull { it.name == catalogItem.name || it.id == catalogItem.id }
                val isDownloading = installedVersion?.status == ModelStatus.DOWNLOADING || catalogItem.status == ModelStatus.DOWNLOADING

                CatalogModelCard(
                    model = catalogItem,
                    isDownloading = isDownloading,
                    isInstalled = isAlreadyInstalledOrDownloading && !isDownloading,
                    downloadProgress = installedVersion?.downloadProgress ?: catalogItem.downloadProgress,
                    downloadSpeed = installedVersion?.downloadSpeed ?: "",
                    onDownload = { onDownloadModel(catalogItem) }
                )
            }

            // Watermark footer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Black Cat Model Management • ⭐ CREATED BY J.COMPE",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = Color(0xFF141522),
            title = {
                Text("Importar archivo de pesos (SAF)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Selecciona un archivo .gguf o .litertlm descargado en la memoria de tu teléfono:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = importFileName,
                        onValueChange = { importFileName = it },
                        label = { Text("Nombre del archivo", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7B61FF),
                            unfocusedBorderColor = Color(0xFF26283C)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = importFileSizeMb,
                        onValueChange = { importFileSizeMb = it },
                        label = { Text("Tamaño aproximado (MB)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7B61FF),
                            unfocusedBorderColor = Color(0xFF26283C)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sizeMb = importFileSizeMb.toLongOrNull() ?: 1200L
                        onImportLocalFile(importFileName, sizeMb * 1024 * 1024)
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("Registrar Modelo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF7B61FF) else Color(0xFF141522))
            .border(1.dp, if (selected) Color(0xFF7B61FF) else Color(0xFF26283C), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun InstalledModelCard(
    model: ModelItem,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    val isRunning = model.status == ModelStatus.RUNNING
    val isDownloading = model.status == ModelStatus.DOWNLOADING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141522))
            .border(
                1.dp,
                if (isRunning) Color(0xFF7B61FF) else Color(0xFF26283C),
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Name and format
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.type.icon,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = model.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${model.sizeDisplay} • ${model.quantization} • RAM ~${model.memoryRequiredMb} MB",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Format badge
                Box(
                    modifier = Modifier
                        .background(
                            if (model.format == ModelFormat.LLAMA_CPP_GGUF) Color(0xFF26193E) else Color(0xFF143026),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = model.format.badge,
                        color = if (model.format == ModelFormat.LLAMA_CPP_GGUF) Color(0xFFC084FC) else Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (model.description.isNotBlank()) {
                Text(
                    text = model.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            // Downloading progress bar
            if (isDownloading) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { model.downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF7B61FF),
                        trackColor = Color(0xFF26283C)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Descargando: ${(model.downloadProgress * 100).toInt()}%",
                            color = Color(0xFFC084FC),
                            fontSize = 10.sp
                        )
                        Text(model.downloadSpeed, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

            // Status & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isRunning) Color(0xFF34D399) else if (isDownloading) Color(0xFFC084FC) else Color(0xFF94A3B8),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Activo en memoria" else if (isDownloading) "Descargando..." else "Listo para cargar",
                        color = if (isRunning) Color(0xFF34D399) else if (isDownloading) Color(0xFFC084FC) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isRunning) {
                        Button(
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF361818)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Detener", color = Color(0xFFF87171), fontSize = 11.sp)
                        }
                    } else if (!isDownloading) {
                        Button(
                            onClick = onRun,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cargar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogModelCard(
    model: ModelItem,
    isDownloading: Boolean,
    isInstalled: Boolean,
    downloadProgress: Float,
    downloadSpeed: String,
    onDownload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF10121D))
            .border(1.dp, if (isDownloading) Color(0xFF7B61FF) else Color(0xFF1E2135), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${model.sizeDisplay} • ${model.format.displayName} • ${model.quantization}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = model.description,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF26193E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Descargando...", color = Color(0xFFC084FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isInstalled) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF143026), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("✓ Instalado", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1638)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7B61FF).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFFD8B4FE), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Descargar", color = Color(0xFFD8B4FE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isDownloading) {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF7B61FF),
                    trackColor = Color(0xFF26283C)
                )
            }
        }
    }
}
