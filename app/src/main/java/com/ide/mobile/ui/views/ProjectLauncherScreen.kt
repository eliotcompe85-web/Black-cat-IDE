package com.ide.mobile.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ide.mobile.core.model.Project
import com.ide.mobile.core.model.ProjectTemplateType
import com.ide.mobile.core.model.WorkspaceState
import com.ide.mobile.ui.components.BlackCatLogo

/**
 * Pantalla de Inicio y Gestor de Espacios de Trabajo (Project Launcher)
 * con diseño dividido (Split-Screen): Proyectos Recientes a la izquierda
 * y Cuadrícula de Acciones (Plantillas, Importar, Clonar) a la derecha.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectLauncherScreen(
    workspaceState: WorkspaceState,
    onOpenProject: (Project) -> Unit,
    onNewProjectFromTemplate: () -> Unit,
    onImportLocalDirectory: (path: String, name: String?) -> Unit,
    onCloneRemoteRepo: (url: String, destPath: String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit,
    onNewProjectFromScratch: ((name: String, goal: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }
    var showAiCreatorDialog by remember { mutableStateOf(false) }

    var aiProjectNameInput by remember { mutableStateOf("") }
    var aiProjectGoalInput by remember { mutableStateOf("") }

    var importPathInput by remember { mutableStateOf("") }
    var importNameInput by remember { mutableStateOf("") }

    var cloneUrlInput by remember { mutableStateOf("") }
    var cloneDestInput by remember { mutableStateOf("/storage/emulated/0/Projects/ClonedApp") }

    val filteredProjects = remember(workspaceState.recentProjects, searchQuery) {
        if (searchQuery.isBlank()) {
            workspaceState.recentProjects
        } else {
            workspaceState.recentProjects.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.path.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C14))
    ) {
        val isWide = maxWidth >= 680.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Barra Superior con Branding
            Surface(
                color = Color(0xFF121422),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFF22263C))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BlackCatLogo(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Black Cat Workspace Hub",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "⭐ CREATED BY J.COMPE • GESTOR DE PROYECTOS",
                                fontSize = 10.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar Launcher",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // 2. Contenido Dividido (Split Layout)
            if (isWide) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Panel Izquierdo: Proyectos Recientes (50%)
                    RecentProjectsPanel(
                        projects = filteredProjects,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onOpenProject = onOpenProject,
                        onDeleteProject = onDeleteProject,
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier.weight(1f)
                    )

                    // Panel Derecho: Cuadrícula de Acciones (50%)
                    ActionsGridPanel(
                        onOpenAiCreatorDialog = { showAiCreatorDialog = true },
                        onNewProjectFromTemplate = onNewProjectFromTemplate,
                        onOpenImportDialog = { showImportDialog = true },
                        onOpenCloneDialog = { showCloneDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Layout Vertical en Pantallas Móviles Angostas
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Acciones Rápidas Superiores
                    ActionsGridPanel(
                        onOpenAiCreatorDialog = { showAiCreatorDialog = true },
                        onNewProjectFromTemplate = onNewProjectFromTemplate,
                        onOpenImportDialog = { showImportDialog = true },
                        onOpenCloneDialog = { showCloneDialog = true },
                        modifier = Modifier.weight(0.42f)
                    )

                    // Proyectos Recientes Inferiores
                    RecentProjectsPanel(
                        projects = filteredProjects,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onOpenProject = onOpenProject,
                        onDeleteProject = onDeleteProject,
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier.weight(0.58f)
                    )
                }
            }
        }
    }

    // Modal: Importar Directorio Local
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = Color(0xFF141728),
            title = {
                Text(
                    text = "📁 Importar Directorio Local",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Introduce la ruta de la carpeta en el almacenamiento. Black Cat detectará automáticamente marcadores como build.gradle.kts, pubspec.yaml o package.json.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = importPathInput,
                        onValueChange = { importPathInput = it },
                        label = { Text("Ruta del Directorio (ej. /storage/emulated/0/MiApp)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7B61FF),
                            focusedLabelColor = Color(0xFF7B61FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = importNameInput,
                        onValueChange = { importNameInput = it },
                        label = { Text("Nombre del Proyecto (Opcional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7B61FF),
                            focusedLabelColor = Color(0xFF7B61FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importPathInput.isNotBlank()) {
                            onImportLocalDirectory(importPathInput, importNameInput.ifBlank { null })
                            showImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("Importar y Abrir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Modal: Asistente Senior para Crear Proyecto Paso a Paso
    if (showAiCreatorDialog) {
        AlertDialog(
            onDismissRequest = { showAiCreatorDialog = false },
            containerColor = Color(0xFF141728),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Asistente Senior de Proyectos",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Crear proyecto desde cero paso a paso",
                            color = Color(0xFFC084FC),
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "¡Hola! Qué gusto saludarte. Cuéntame qué te gustaría construir hoy y juntos definiremos el nombre de tu proyecto y dejaremos listo tu nuevo espacio de trabajo.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = aiProjectGoalInput,
                        onValueChange = {
                            aiProjectGoalInput = it
                            if (aiProjectNameInput.isBlank() && it.isNotBlank()) {
                                val firstWord = it.trim().split(" ").firstOrNull()?.replaceFirstChar { c -> c.uppercase() } ?: "MiApp"
                                aiProjectNameInput = "${firstWord}App"
                            }
                        },
                        label = { Text("¿Cuál es el objetivo o idea de tu proyecto?") },
                        placeholder = { Text("Ej: Una app para registrar mis gastos diarios") },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC084FC),
                            focusedLabelColor = Color(0xFFC084FC),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = aiProjectNameInput,
                        onValueChange = { aiProjectNameInput = it },
                        label = { Text("Nombre del Proyecto") },
                        placeholder = { Text("Ej: GastosDiariosApp") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7B61FF),
                            focusedLabelColor = Color(0xFF7B61FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = aiProjectNameInput.ifBlank { "MiApp" }
                        val goal = aiProjectGoalInput.ifBlank { "Crear una nueva aplicación" }
                        onNewProjectFromScratch?.invoke(name, goal)
                        showAiCreatorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("✨ Crear y Comenzar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiCreatorDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Modal: Clonar Repositorio Remoto
    if (showCloneDialog) {
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            containerColor = Color(0xFF141728),
            title = {
                Text(
                    text = "🌐 Clonar Repositorio Remoto",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Introduce la URL del repositorio Git remoto (GitHub, GitLab, etc.).",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = cloneUrlInput,
                        onValueChange = { cloneUrlInput = it },
                        label = { Text("URL del Repositorio Git (https://github.com/...)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cloneDestInput,
                        onValueChange = { cloneDestInput = it },
                        label = { Text("Ruta de Destino Local") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            focusedLabelColor = Color(0xFF00E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cloneUrlInput.isNotBlank()) {
                            onCloneRemoteRepo(cloneUrlInput, cloneDestInput)
                            showCloneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
                ) {
                    Text("Clonar Proyecto", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

/**
 * Panel Izquierdo: Lista de Proyectos Recientes con filtrado y tarjetas interactivas.
 */
@Composable
private fun RecentProjectsPanel(
    projects: List<Project>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenProject: (Project) -> Unit,
    onDeleteProject: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF111322),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF22263D)),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Proyectos recientes",
                        tint = Color(0xFF7B61FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Proyectos Recientes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF7B61FF).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${projects.size}",
                        color = Color(0xFFC4B5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de Búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar proyecto reciente...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7B61FF),
                    unfocusedBorderColor = Color(0xFF22263C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0B0D17),
                    unfocusedContainerColor = Color(0xFF0B0D17)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Sin proyectos",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No se encontraron proyectos recientes",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onClick = { onOpenProject(project) },
                            onDelete = { onDeleteProject(project.id) },
                            onToggleFavorite = { onToggleFavorite(project.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual para un Proyecto Reciente.
 */
@Composable
private fun ProjectItemCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeIcon, typeColor, typeBadge) = when (project.projectType) {
        ProjectTemplateType.FLUTTER_MOBILE -> Triple(Icons.Default.PhoneAndroid, Color(0xFF38BDF8), "FLUTTER")
        ProjectTemplateType.EXPO_REACT_NATIVE -> Triple(Icons.Default.Devices, Color(0xFF00E5FF), "EXPO DEV")
        ProjectTemplateType.RAILWAY_NODE_API -> Triple(Icons.Default.CloudQueue, Color(0xFFF43F5E), "RAILWAY")
        ProjectTemplateType.PYTHON_AI_SERVICE -> Triple(Icons.Default.Terminal, Color(0xFFFBBF24), "PYTHON")
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF151829),
        border = BorderStroke(1.dp, Color(0xFF252A42)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icono Tecnológico
                Surface(
                    color = typeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = typeBadge,
                            tint = typeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = typeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = typeBadge,
                                color = typeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = project.displayPath,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.formattedDate,
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                        if (!project.gitBranch.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = " ${project.gitBranch}",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Acciones de Favorito y Borrar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (project.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (project.isFavorite) Color(0xFFFBBF24) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar de recientes",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Panel Derecho: Cuadrícula de Acciones (Crear desde Plantilla, Importar Local, Clonar Remoto).
 */
@Composable
private fun ActionsGridPanel(
    onOpenAiCreatorDialog: () -> Unit,
    onNewProjectFromTemplate: () -> Unit,
    onOpenImportDialog: () -> Unit,
    onOpenCloneDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF111322),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF22263D)),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Acciones de Workspace",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar Nuevo Espacio de Trabajo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Crea tu proyecto con el asistente senior, usa plantillas o importa una carpeta.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            // Acción 0: Asistente Senior para Crear Proyecto Paso a Paso
            ActionCardButton(
                title = "✨ Asistente IA: Crear Proyecto Paso a Paso",
                description = "🧙‍♂️ Cuéntale tu idea al asistente senior. Elegiremos el nombre y crearemos tu espacio de trabajo listo para programar.",
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFC084FC),
                onClick = onOpenAiCreatorDialog,
                modifier = Modifier.weight(1f)
            )

            // Acción 1: Nuevo Proyecto desde Plantilla
            ActionCardButton(
                title = "Nuevo Proyecto desde Plantilla",
                description = "📱 Flutter Mobile, ⚛️ Expo React Native, 🚂 Railway Express o 🐍 Python AI.",
                icon = Icons.Default.AutoAwesomeMotion,
                accentColor = Color(0xFF7B61FF),
                onClick = onNewProjectFromTemplate,
                modifier = Modifier.weight(1f)
            )

            // Acción 2: Importar Directorio Local
            ActionCardButton(
                title = "Importar Directorio Local",
                description = "📁 Abre una carpeta del dispositivo. Detecta automáticamente build.gradle.kts y dependencias.",
                icon = Icons.Default.FolderOpen,
                accentColor = Color(0xFF00E5FF),
                onClick = onOpenImportDialog,
                modifier = Modifier.weight(1f)
            )

            // Acción 3: Clonar Repositorio Remoto
            ActionCardButton(
                title = "Clonar Repositorio Remoto",
                description = "🌐 Clona un repositorio Git público o privado desde GitHub con un solo clic.",
                icon = Icons.Default.CloudDownload,
                accentColor = Color(0xFF10B981),
                onClick = onOpenCloneDialog,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Botón tarjeta interactivo para cada acción de la cuadrícula.
 */
@Composable
private fun ActionCardButton(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF151829),
        border = BorderStroke(1.dp, Color(0xFF252A42)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
