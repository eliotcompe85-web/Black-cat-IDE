package com.ide.mobile.feature.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.HorizontalDivider
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
fun ProjectExplorerDrawer(
    rootProject: ProjectFile,
    selectedFile: ProjectFile?,
    onFileSelect: (ProjectFile) -> Unit,
    onNewFileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF0C0D15))
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        // Black Cat IDE Drawer Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
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

                IconButton(
                    onClick = onNewFileClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "New File",
                        tint = Color(0xFF7B61FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF222436), thickness = 1.dp)

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

        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF141522), RoundedCornerShape(12.dp))
                    .border(0.8.dp, Color(0xFF7B61FF).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "⭐ CREATED BY J.COMPE",
                    color = Color(0xFFD8B4FE),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
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

    val backgroundColor = when {
        isSelected -> Color(0xFF2E436E)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(4.dp))
            .clickable {
                if (file.isDirectory) {
                    isExpanded = !isExpanded
                    file.isExpanded = isExpanded
                } else {
                    onFileSelect(file)
                }
            }
            .padding(
                start = (depth * 14 + 10).dp,
                end = 12.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.isDirectory) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF7A7E85),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFFE8C375),
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(20.dp))
            val (icon, iconTint) = when {
                file.name.endsWith(".kt") -> Pair(Icons.Default.Code, Color(0xFF7F52FF))
                file.name.endsWith(".xml") -> Pair(Icons.AutoMirrored.Filled.InsertDriveFile, Color(0xFFE8BF6A))
                file.name.endsWith(".kts") -> Pair(Icons.Default.Code, Color(0xFF02303A))
                else -> Pair(Icons.AutoMirrored.Filled.InsertDriveFile, Color(0xFF7A7E85))
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = file.name,
            color = if (isSelected) Color.White else Color(0xFFBCBEC4),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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
