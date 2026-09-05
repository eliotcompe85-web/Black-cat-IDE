package com.ide.mobile.feature.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.ProjectFile

@Composable
fun ProjectExplorerDrawer(
    rootProject: ProjectFile,
    selectedFile: ProjectFile?,
    onFileSelect: (ProjectFile) -> Unit,
    onNavigatePage: (String) -> Unit = {},
    onNewFileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var activeSection by remember { mutableStateOf("FILES") } // "FILES" or "PAGES"

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(Color(0xFF0C0D15))
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        // Drawer Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF26193E), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐱", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BLACK CAT IDE",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "⭐ CREATED BY J.COMPE",
                            color = Color(0xFFC084FC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (activeSection == "FILES") {
                    IconButton(
                        onClick = onNewFileClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Nuevo archivo",
                            tint = Color(0xFF7B61FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section Toggle: [Archivos] vs [Todas las Páginas]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .background(Color(0xFF141522), RoundedCornerShape(8.dp))
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeSection == "FILES") Color(0xFF26193E) else Color.Transparent)
                    .clickable { activeSection = "FILES" }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📁 Archivos",
                    color = if (activeSection == "FILES") Color(0xFFC084FC) else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeSection == "PAGES") Color(0xFF26193E) else Color.Transparent)
                    .clickable { activeSection = "PAGES" }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📑 Páginas",
                    color = if (activeSection == "PAGES") Color(0xFFC084FC) else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        // Main Drawer Content
        if (activeSection == "FILES") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 6.dp)
            ) {
                FileTreeNode(
                    file = rootProject,
                    depth = 0,
                    selectedFile = selectedFile,
                    onFileSelect = onFileSelect
                )
            }
        } else {
            // Dedicated Pages Menu (Clean, organized, with clear icons & labels)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawerNavigationItem(icon = Icons.Default.Code, label = "Editor de Código", tint = Color(0xFF7B61FF)) { onNavigatePage("EDITOR") }
                DrawerNavigationItem(icon = Icons.Default.Folder, label = "Explorador de Archivos", tint = Color(0xFFFBBF24)) { onNavigatePage("FILES") }
                DrawerNavigationItem(icon = Icons.Default.AutoFixHigh, label = "Asistente IA & Agentes", tint = Color(0xFFC084FC)) { onNavigatePage("AI_ASSISTANT") }
                DrawerNavigationItem(icon = Icons.Default.Terminal, label = "Terminal Interactiva", tint = Color(0xFF34D399)) { onNavigatePage("TERMINAL") }
                DrawerNavigationItem(icon = Icons.Default.Search, label = "Búsqueda en Proyecto", tint = Color(0xFF38BDF8)) { onNavigatePage("SEARCH") }
                DrawerNavigationItem(icon = Icons.Default.AccountTree, label = "Control Git", tint = Color(0xFFF97316)) { onNavigatePage("GIT") }

                HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                DrawerNavigationItem(icon = Icons.Default.CloudDownload, label = "Gestor de Modelos GGUF", tint = Color(0xFFC084FC)) { onNavigatePage("MODELS") }
                DrawerNavigationItem(icon = Icons.Default.Speed, label = "Telemetría RAM & Motor", tint = Color(0xFF34D399)) { onNavigatePage("TELEMETRY") }
                DrawerNavigationItem(icon = Icons.Default.MenuBook, label = "Base de Conocimiento RAG", tint = Color(0xFFA78BFA)) { onNavigatePage("DOCS_RAG") }
                DrawerNavigationItem(icon = Icons.Default.Settings, label = "Ajustes & Claves API", tint = Color(0xFF94A3B8)) { onNavigatePage("SETTINGS") }
            }
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF141522), RoundedCornerShape(12.dp))
                    .border(0.8.dp, Color(0xFF7B61FF).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⭐ CREATED BY J.COMPE",
                    color = Color(0xFFC084FC),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FileTreeNode(
    file: ProjectFile,
    depth: Int,
    selectedFile: ProjectFile?,
    onFileSelect: (ProjectFile) -> Unit
) {
    var isExpanded by remember { mutableStateOf(file.isExpanded) }
    val isSelected = selectedFile?.id == file.id

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
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.isDirectory) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
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
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (file.isDirectory && isExpanded) {
            file.children.forEach { child ->
                FileTreeNode(
                    file = child,
                    depth = depth + 1,
                    selectedFile = selectedFile,
                    onFileSelect = onFileSelect
                )
            }
        }
    }
}
