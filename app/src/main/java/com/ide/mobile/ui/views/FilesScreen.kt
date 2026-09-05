package com.ide.mobile.ui.views

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.ProjectFile

@Composable
fun FilesScreen(
    rootProject: ProjectFile,
    selectedFile: ProjectFile,
    onFileSelect: (ProjectFile) -> Unit,
    onNewFileClick: () -> Unit = {},
    onNewFolderClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Archivos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNewFileClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Nuevo archivo",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onNewFolderClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Nueva carpeta",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
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
                onFileSelect = onFileSelect
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            com.ide.mobile.ui.components.BlackCatWatermarkBadge(
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
    onFileSelect: (ProjectFile) -> Unit
) {
    var isExpanded by remember { mutableStateOf(file.isExpanded) }
    val isSelected = file.id == selectedFile.id

    val paddingStart = (depth * 16).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                if (isSelected) Color(0xFF191B2E) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                if (file.isDirectory) {
                    isExpanded = !isExpanded
                    file.isExpanded = isExpanded
                } else {
                    onFileSelect(file)
                }
            }
            .padding(start = paddingStart, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.isDirectory) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(20.dp))
            val (icon, tint) = when {
                file.name.endsWith(".dart") -> Icons.Default.Code to Color(0xFF38BDF8)
                file.name.endsWith(".kt") -> Icons.Default.Code to Color(0xFFC084FC)
                file.name.endsWith(".yaml") -> Icons.AutoMirrored.Filled.InsertDriveFile to Color(0xFFFBBF24)
                file.name.endsWith(".md") -> Icons.AutoMirrored.Filled.InsertDriveFile to Color(0xFF34D399)
                else -> Icons.AutoMirrored.Filled.InsertDriveFile to Color(0xFF94A3B8)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF7B61FF) else tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = file.name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFC084FC) else Color(0xFFE2E8F0)
        )
    }

    if (file.isDirectory && isExpanded) {
        file.children.forEach { child ->
            FileItemRow(
                file = child,
                depth = depth + 1,
                selectedFile = selectedFile,
                onFileSelect = onFileSelect
            )
        }
    }
}
