package com.ide.mobile.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
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
import com.ide.mobile.core.model.ProjectFile
import com.ide.mobile.ui.components.BlackCatWatermarkBadge

@Composable
fun FilesScreen(
    rootProject: ProjectFile,
    selectedFile: ProjectFile,
    onFileSelect: (ProjectFile) -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDeleteFile: (ProjectFile) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top action row (Clean & non-redundant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Explorador de Archivos",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${rootProject.name} • Flutter / Android",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onNewFileClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Nuevo archivo",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onNewFolderClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Nueva carpeta",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onRefreshClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Project Tree
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            FileItemRow(
                file = rootProject,
                depth = 0,
                selectedFile = selectedFile,
                onFileSelect = onFileSelect,
                onDeleteFile = onDeleteFile
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            BlackCatWatermarkBadge(
                text = "BLACK CAT IDE • CREATED BY J.COMPE"
            )
        }
    }
}

@Composable
private fun FileItemRow(
    file: ProjectFile,
    depth: Int,
    selectedFile: ProjectFile,
    onFileSelect: (ProjectFile) -> Unit,
    onDeleteFile: (ProjectFile) -> Unit
) {
    var isExpanded by remember { mutableStateOf(file.isExpanded) }
    val isSelected = file.id == selectedFile.id

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 14).dp, top = 2.dp, bottom = 2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) Color(0xFF26193E) else Color.Transparent)
                .clickable {
                    if (file.isDirectory) {
                        isExpanded = !isExpanded
                    } else {
                        onFileSelect(file)
                    }
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand arrow for folders
            if (file.isDirectory) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp))
                val iconTint = when {
                    file.name.endsWith(".dart") -> Color(0xFF38BDF8)
                    file.name.endsWith(".kt") || file.name.endsWith(".kts") -> Color(0xFFC084FC)
                    file.name.endsWith(".xml") -> Color(0xFFFB923C)
                    file.name.endsWith(".yaml") || file.name.endsWith(".json") -> Color(0xFF34D399)
                    else -> Color(0xFF94A3B8)
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = file.name,
                color = if (isSelected) Color(0xFFC084FC) else Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            // Delete action for files (except root)
            if (!file.isDirectory && file.id != "root") {
                IconButton(
                    onClick = { onDeleteFile(file) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Render children if folder is expanded
        if (file.isDirectory && isExpanded) {
            file.children.forEach { child ->
                FileItemRow(
                    file = child,
                    depth = depth + 1,
                    selectedFile = selectedFile,
                    onFileSelect = onFileSelect,
                    onDeleteFile = onDeleteFile
                )
            }
        }
    }
}
