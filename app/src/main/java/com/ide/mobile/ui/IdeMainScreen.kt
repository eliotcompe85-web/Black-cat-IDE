package com.ide.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ide.mobile.core.model.ProjectFile
import com.ide.mobile.feature.ai.ui.AiAssistantPanel
import com.ide.mobile.feature.ai.ui.MissionControlDashboard
import com.ide.mobile.feature.compiler.LiveComposePreviewHost
import com.ide.mobile.feature.editor.CodeEditorCore
import com.ide.mobile.feature.explorer.ProjectExplorerDrawer
import com.ide.mobile.feature.keyboard.MobileCodeKeyboardBar
import com.ide.mobile.core.model.ProjectTemplate
import com.ide.mobile.ui.components.CommandPaletteAction
import com.ide.mobile.ui.components.CommandPaletteDialog
import com.ide.mobile.ui.components.BlackCatEditorWatermark
import com.ide.mobile.ui.components.BlackCatLogo
import com.ide.mobile.ui.views.FilesScreen
import com.ide.mobile.ui.views.GitScreen
import com.ide.mobile.ui.views.ProjectLauncherScreen
import com.ide.mobile.ui.views.SearchScreen
import com.ide.mobile.ui.views.SettingsScreen
import com.ide.mobile.ui.views.SnippetVaultScreen
import com.ide.mobile.ui.views.TerminalScreen
import com.ide.mobile.ui.views.aihub.AiPluginsScreen
import com.ide.mobile.ui.views.aihub.ModelManagementScreen
import com.ide.mobile.ui.views.aihub.RuntimeEngineScreen
import com.ide.mobile.ui.views.aihub.SiloLibraryScreen
import com.ide.mobile.viewmodel.AiHubSubPage
import com.ide.mobile.viewmodel.IdeViewModel
import com.ide.mobile.viewmodel.MainNavTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeMainScreen(
    viewModel: IdeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var showMenuOptions by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showImportAgentDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }

    var newFileNameInput by remember { mutableStateOf("") }
    var newFolderNameInput by remember { mutableStateOf("") }
    var importAgentContentInput by remember { mutableStateOf("") }
    var importAgentNameInput by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProjectExplorerDrawer(
                rootProject = uiState.rootProject,
                selectedFile = uiState.activeFile,
                onFileSelect = { file ->
                    viewModel.openFile(file)
                    viewModel.selectNavTab(MainNavTab.EDITOR)
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigatePage = { pageKey ->
                    when (pageKey) {
                        "EDITOR" -> viewModel.selectNavTab(MainNavTab.EDITOR)
                        "FILES" -> viewModel.selectNavTab(MainNavTab.FILES)
                        "AI_ASSISTANT" -> viewModel.selectNavTab(MainNavTab.AI_ASSISTANT)
                        "TERMINAL" -> viewModel.selectNavTab(MainNavTab.TERMINAL)
                        "SEARCH" -> viewModel.selectNavTab(MainNavTab.SEARCH)
                        "GIT" -> viewModel.selectNavTab(MainNavTab.GIT)
                        "MODELS" -> viewModel.selectNavTab(MainNavTab.MODELS)
                        "TELEMETRY" -> viewModel.selectNavTab(MainNavTab.TELEMETRY)
                        "DOCS_RAG" -> viewModel.selectNavTab(MainNavTab.DOCS_RAG)
                        "SETTINGS" -> viewModel.selectNavTab(MainNavTab.SETTINGS)
                        "SNIPPET_VAULT" -> viewModel.selectNavTab(MainNavTab.SNIPPET_VAULT)
                        "MISSION_CONTROL" -> viewModel.selectNavTab(MainNavTab.MISSION_CONTROL)
                    }
                    coroutineScope.launch { drawerState.close() }
                },
                onNewFileClick = {
                    showNewFileDialog = true
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFF0C0D15),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    IdeTopAppBar(
                        activeFile = uiState.activeFile,
                        currentTab = uiState.currentNavTab,
                        isFileModified = uiState.isFileModified,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onRunClick = { viewModel.runSmartRunner() },
                        onAiSparkleClick = { viewModel.selectNavTab(MainNavTab.AI_ASSISTANT) },
                        onMoreClick = { showMenuOptions = true },
                        showMenuOptions = showMenuOptions,
                        onDismissMenu = { showMenuOptions = false },
                        onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                        onFormatCode = {
                            viewModel.formatCurrentCode()
                            coroutineScope.launch { snackbarHostState.showSnackbar("Código formateado") }
                            showMenuOptions = false
                        },
                        onOpenTemplates = {
                            showTemplatesDialog = true
                            showMenuOptions = false
                        },
                        onOpenProjectLauncher = {
                            viewModel.toggleProjectLauncher(true)
                            showMenuOptions = false
                        },
                        onSaveFile = {
                            val saved = viewModel.saveCurrentFile()
                            coroutineScope.launch { snackbarHostState.showSnackbar("Guardado: $saved") }
                            showMenuOptions = false
                        },
                        onNewFile = {
                            showNewFileDialog = true
                            showMenuOptions = false
                        },
                        onNewFolder = {
                            showNewFolderDialog = true
                            showMenuOptions = false
                        },
                        onCloseTab = {
                            viewModel.closeTab(uiState.activeFile)
                            showMenuOptions = false
                        },
                        onOpenTerminal = {
                            viewModel.selectNavTab(MainNavTab.TERMINAL)
                            showMenuOptions = false
                        },
                        onLivePreview = {
                            viewModel.toggleLivePreview()
                            showMenuOptions = false
                        },
                        onOpenAiSubPage = { page ->
                            viewModel.openAiSubPage(page)
                            showMenuOptions = false
                        }
                    )

                    // Barra horizontal de categorías para cambiar entre todas las páginas sin salir de la app
                    IdeCategoryNavBar(
                        selectedTab = uiState.currentNavTab,
                        onTabSelected = { tab -> viewModel.selectNavTab(tab) }
                    )
                }
            },
            bottomBar = {
                IdeBottomNavigationBar(
                    selectedTab = uiState.currentNavTab,
                    onTabSelected = { tab -> viewModel.selectNavTab(tab) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF0C0D15))
            ) {
                // First-Class Dedicated Pages (Clean, isolated, non-overlapping)
                when (uiState.currentNavTab) {
                    MainNavTab.EDITOR -> {
                        EditorMainContent(
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    MainNavTab.FILES -> {
                        FilesScreen(
                            rootProject = uiState.rootProject,
                            selectedFile = uiState.activeFile,
                            onFileSelect = { file ->
                                viewModel.openFile(file)
                                viewModel.selectNavTab(MainNavTab.EDITOR)
                            },
                            onNewFileClick = { showNewFileDialog = true },
                            onNewFolderClick = { showNewFolderDialog = true },
                            onRefreshClick = {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Árbol de archivos sincronizado") }
                            },
                            onDeleteFile = { file ->
                                viewModel.deleteFile(file)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Eliminado: ${file.name}") }
                            },
                            onTemplatesClick = { showTemplatesDialog = true }
                        )
                    }
                    MainNavTab.AI_ASSISTANT -> {
                        AiAssistantPanel(
                            selectedProvider = uiState.selectedAiProvider,
                            onSelectProvider = { provider -> viewModel.selectAiProvider(provider) },
                            activeAgent = uiState.activeAgent,
                            availableAgents = uiState.availableAgents,
                            onSelectAgent = { agent -> viewModel.selectLocalAgent(agent) },
                            downloadableAgents = uiState.downloadableAgents,
                            onDownloadAndActivateAgent = { agent ->
                                viewModel.downloadAndActivateAgent(agent)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Agente ${agent.name} descargado y activado") }
                            },
                            chatMessages = uiState.chatMessages,
                            currentExecutionStep = uiState.currentExecutionStep,
                            isAiLoading = uiState.isAiLoading,
                            onSendMessage = { prompt -> viewModel.askAiAssistant(prompt) },
                            onExecuteAction = { action ->
                                viewModel.executeAgentAction(action)
                            },
                            onViewInTerminal = { action ->
                                viewModel.selectNavTab(MainNavTab.TERMINAL)
                            },
                            onRejectAction = { action ->
                                viewModel.rejectAgentAction(action)
                            },
                            onClearChat = { viewModel.clearChatHistory() },
                            onInsertCode = { code ->
                                viewModel.insertAiCodeIntoEditor(code)
                                viewModel.selectNavTab(MainNavTab.EDITOR)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Código inyectado en el editor") }
                            },
                            onApplyCodeWithTarget = { code, targetPath ->
                                viewModel.applyArtifactCodeToProject(code, targetPath)
                                viewModel.selectNavTab(MainNavTab.EDITOR)
                                coroutineScope.launch {
                                    val destination = targetPath ?: "archivo activo"
                                    snackbarHostState.showSnackbar("✓ Código aplicado a $destination")
                                }
                            },
                            onToggleTask = { msgId, taskId ->
                                viewModel.toggleChecklistTask(msgId, taskId)
                            },
                            onProceedPlan = {
                                viewModel.resumeAgentMission(uiState.activeAgent.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("▶ Antigravity: Ejecutando plan y acciones aprobadas...")
                                }
                            },
                            onClose = { viewModel.selectNavTab(MainNavTab.EDITOR) },
                            onOpenMissionControl = { viewModel.selectNavTab(MainNavTab.MISSION_CONTROL) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MainNavTab.TERMINAL -> {
                        TerminalScreen(
                            logs = uiState.consoleLogs,
                            onSendCommand = { cmd -> viewModel.executeTerminalCommand(cmd) },
                            onClearLogs = { viewModel.clearTerminalLogs() },
                            onClose = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.SEARCH -> {
                        SearchScreen(
                            searchQuery = uiState.searchQuery,
                            searchMatches = uiState.searchMatches,
                            onSearchChange = { query -> viewModel.searchInProject(query) },
                            onMatchClick = { file ->
                                viewModel.openFile(file)
                                viewModel.selectNavTab(MainNavTab.EDITOR)
                            },
                            onBack = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.GIT -> {
                        GitScreen(
                            isFileModified = uiState.isFileModified,
                            activeFileName = uiState.activeFile.path,
                            gitHubConfig = uiState.gitHubConfig,
                            expoDevConfig = uiState.expoDevConfig,
                            railwayConfig = uiState.railwayConfig,
                            deploymentStatus = uiState.deploymentStatus,
                            deploymentMessage = uiState.deploymentMessage,
                            onCommit = { msg ->
                                viewModel.commitChanges(msg)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Cambios confirmados: $msg")
                                }
                            },
                            onPushToGitHub = { msg ->
                                viewModel.pushToGitHub(msg)
                            },
                            onDeployExpoDev = {
                                viewModel.deployToExpoDev()
                            },
                            onDeployRailway = {
                                viewModel.deployToRailway()
                            },
                            onBack = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.MODELS -> {
                        ModelManagementScreen(
                            installedModels = uiState.installedModels,
                            catalogModels = uiState.catalogModels,
                            onRunModel = { id -> viewModel.runModel(id) },
                            onStopModel = { id -> viewModel.stopModel(id) },
                            onDeleteModel = { id -> viewModel.deleteModel(id) },
                            onDownloadModel = { item -> viewModel.downloadModel(item) },
                            onImportLocalFile = { name, size -> viewModel.importLocalModel(name, size) },
                            onBack = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.TELEMETRY -> {
                        RuntimeEngineScreen(
                            metrics = uiState.runtimeMetrics,
                            onUpdateConfig = { threads, context, gpu, engine ->
                                viewModel.updateHardwareConfig(threads, context, gpu, engine)
                            },
                            onBack = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.DOCS_RAG -> {
                        SiloLibraryScreen(
                            documents = uiState.ragDocuments,
                            onAddDocument = { name, content -> viewModel.addRagDocument(name, content) },
                            onRemoveDocument = { id -> viewModel.removeRagDocument(id) },
                            onSearchChunks = { q -> viewModel.searchRagChunks(q) },
                            isInjectionEnabled = uiState.isRagInjectionEnabled,
                            onToggleInjection = { enabled -> viewModel.toggleRagInjection(enabled) },
                            onBack = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.SETTINGS -> {
                        SettingsScreen(
                            apiKeysConfig = uiState.apiKeysConfig,
                            gitHubConfig = uiState.gitHubConfig,
                            expoDevConfig = uiState.expoDevConfig,
                            railwayConfig = uiState.railwayConfig,
                            localHost = uiState.localAiHost,
                            localPort = uiState.localAiPort,
                            selectedModel = uiState.selectedLocalModel,
                            testStatus = uiState.localAiTestStatus,
                            availableAgents = uiState.availableAgents,
                            onSaveApiKeys = { cfg ->
                                viewModel.updateApiKeys(cfg)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Claves de API guardadas") }
                            },
                            onSaveGitHubConfig = { cfg ->
                                viewModel.updateGitHubConfig(cfg)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Configuracion GitHub guardada") }
                            },
                            onSaveExpoDevConfig = { cfg ->
                                viewModel.updateExpoDevConfig(cfg)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Configuracion Expo Dev guardada") }
                            },
                            onSaveRailwayConfig = { cfg ->
                                viewModel.updateRailwayConfig(cfg)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Configuracion Railway guardada") }
                            },
                            onSaveLocalConfig = { host, port, model ->
                                viewModel.updateLocalAiConfig(host, port, model)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Configuracion local guardada") }
                            },
                            onTestConnection = { host, port ->
                                viewModel.testLocalAiConnection(host, port)
                            },
                            onImportAgentClick = { showImportAgentDialog = true },
                            onOpenModelManagement = { viewModel.selectNavTab(MainNavTab.MODELS) },
                            onOpenRuntimeEngine = { viewModel.selectNavTab(MainNavTab.TELEMETRY) },
                            onOpenPluginsRouter = { viewModel.openAiSubPage(AiHubSubPage.PLUGINS_ROUTER) },
                            onOpenSiloLibrary = { viewModel.selectNavTab(MainNavTab.DOCS_RAG) }
                        )
                    }
                    MainNavTab.SNIPPET_VAULT -> {
                        SnippetVaultScreen(
                            onInsertSnippet = { code ->
                                viewModel.insertAiCodeIntoEditor(code)
                                viewModel.selectNavTab(MainNavTab.EDITOR)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Snippet insertado en el editor") }
                            },
                            onBack = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                    MainNavTab.MISSION_CONTROL -> {
                        MissionControlDashboard(
                            activeAgents = uiState.activeMissionAgents,
                            onDelegateTask = {
                                viewModel.selectNavTab(MainNavTab.AI_ASSISTANT)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Selecciona un agente para delegar una nueva tarea") }
                            },
                            onResumeAgent = { agentId ->
                                viewModel.resumeAgentMission(agentId)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Reanudando agente...") }
                            },
                            onClose = { viewModel.selectNavTab(MainNavTab.EDITOR) }
                        )
                    }
                }

                // AI Hub Sub-page Overlays (for SiloLibrary & Router)
                when (uiState.activeAiSubPage) {
                    AiHubSubPage.PLUGINS_ROUTER -> {
                        AiPluginsScreen(
                            config = uiState.routerConfig,
                            onSaveConfig = { cfg -> viewModel.updateRouterConfig(cfg) },
                            onBack = { viewModel.closeAiSubPage() }
                        )
                    }
                    AiHubSubPage.SILO_LIBRARY -> {
                        SiloLibraryScreen(
                            documents = uiState.ragDocuments,
                            onAddDocument = { name, content -> viewModel.addRagDocument(name, content) },
                            onRemoveDocument = { id -> viewModel.removeRagDocument(id) },
                            onSearchChunks = { q -> viewModel.searchRagChunks(q) },
                            isInjectionEnabled = uiState.isRagInjectionEnabled,
                            onToggleInjection = { enabled -> viewModel.toggleRagInjection(enabled) },
                            onBack = { viewModel.closeAiSubPage() }
                        )
                    }
                    else -> { /* handled by direct MainNavTab */ }
                }
            }
        }
    }

    // Dialog: New File
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            containerColor = Color(0xFF141522),
            title = { Text("Crear nuevo archivo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el nombre del archivo (ej. service.dart, widget.kt):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        placeholder = { Text("mi_archivo.dart", color = Color(0xFF64748B)) },
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
                        if (newFileNameInput.isNotBlank()) {
                            viewModel.createNewFile(newFileNameInput)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Archivo creado: $newFileNameInput") }
                            newFileNameInput = ""
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("Crear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: New Folder
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            containerColor = Color(0xFF141522),
            title = { Text("Crear nueva carpeta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el nombre de la carpeta (ej. models, screens, helpers):", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    OutlinedTextField(
                        value = newFolderNameInput,
                        onValueChange = { newFolderNameInput = it },
                        placeholder = { Text("nueva_carpeta", color = Color(0xFF64748B)) },
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
                        if (newFolderNameInput.isNotBlank()) {
                            viewModel.createNewFolder(newFolderNameInput)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Carpeta creada: $newFolderNameInput") }
                            newFolderNameInput = ""
                            showNewFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("Crear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Dialog: Import Agent from Phone
    if (showImportAgentDialog) {
        AlertDialog(
            onDismissRequest = { showImportAgentDialog = false },
            containerColor = Color(0xFF141522),
            title = { Text("Importar Agente Local Antigravity", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pega el JSON del agente o las instrucciones (.md) desde tu teléfono:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    OutlinedTextField(
                        value = importAgentNameInput,
                        onValueChange = { importAgentNameInput = it },
                        label = { Text("Nombre del Agente", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7B61FF),
                            unfocusedBorderColor = Color(0xFF26283C)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = importAgentContentInput,
                        onValueChange = { importAgentContentInput = it },
                        placeholder = { Text("Instrucciones del agente o JSON...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7B61FF),
                            unfocusedBorderColor = Color(0xFF26283C)
                        ),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importAgentContentInput.isNotBlank()) {
                            viewModel.importAgentFromPhone(importAgentContentInput, importAgentNameInput.ifBlank { "Agente Teléfono" })
                            coroutineScope.launch { snackbarHostState.showSnackbar("Agente Antigravity cargado con éxito") }
                            importAgentContentInput = ""
                            importAgentNameInput = ""
                            showImportAgentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("Cargar Agente", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportAgentDialog = false }) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    if (uiState.showCommandPalette) {
        val actions = listOf(
            CommandPaletteAction(
                id = "open_launcher",
                title = "📁 Gestor de Proyectos / Launcher",
                subtitle = "Abrir, crear o importar espacios de trabajo",
                icon = Icons.Default.FolderSpecial,
                tint = Color(0xFF00E5FF),
                onExecute = { viewModel.toggleProjectLauncher(true) }
            ),
            CommandPaletteAction(
                id = "run_smart",
                title = "▶ Ejecutar Proyecto / Archivo",
                subtitle = "Ejecuta con medición de tiempo o vista previa",
                icon = Icons.Default.PlayArrow,
                tint = Color(0xFF60A5FA),
                onExecute = { viewModel.runSmartRunner() }
            ),
            CommandPaletteAction(
                id = "format_code",
                title = "🪄 Formatear Código",
                subtitle = "Corrige indentación y estilo en el editor",
                icon = Icons.Default.AutoAwesome,
                tint = Color(0xFFA78BFA),
                onExecute = {
                    viewModel.formatCurrentCode()
                    coroutineScope.launch { snackbarHostState.showSnackbar("Código formateado") }
                }
            ),
            CommandPaletteAction(
                id = "save_file",
                title = "💾 Guardar Archivo Actual",
                subtitle = uiState.activeFile.path,
                icon = Icons.Default.Save,
                tint = Color(0xFF34D399),
                onExecute = {
                    viewModel.saveCurrentFile()
                    coroutineScope.launch { snackbarHostState.showSnackbar("Archivo guardado") }
                }
            ),
            CommandPaletteAction(
                id = "git_push",
                title = "⬆️ Subir Cambios a GitHub",
                subtitle = "Push a ${uiState.gitHubConfig.defaultBranch}",
                icon = Icons.Default.CloudUpload,
                tint = Color(0xFFFBBF24),
                onExecute = { viewModel.selectNavTab(MainNavTab.GIT) }
            ),
            CommandPaletteAction(
                id = "deploy_expo",
                title = "🚀 Desplegar en Expo Dev",
                subtitle = "EAS Build para Android",
                icon = Icons.Default.StayCurrentPortrait,
                tint = Color(0xFF38BDF8),
                onExecute = { viewModel.deployToExpoDev() }
            ),
            CommandPaletteAction(
                id = "deploy_railway",
                title = "🚂 Desplegar en Railway Cloud",
                subtitle = "Despliegue de microservicio backend",
                icon = Icons.Default.Dns,
                tint = Color(0xFFC084FC),
                onExecute = { viewModel.deployToRailway() }
            ),
            CommandPaletteAction(
                id = "open_terminal",
                title = "💻 Abrir Terminal Interactiva",
                subtitle = "Ejecutar comandos bash, npm, flutter o python",
                icon = Icons.Default.Terminal,
                tint = Color(0xFF34D399),
                onExecute = { viewModel.selectNavTab(MainNavTab.TERMINAL) }
            ),
            CommandPaletteAction(
                id = "catalog_agents",
                title = "📥 Catálogo de Agentes IA",
                subtitle = "Descargar agentes especializados para desarrollo",
                icon = Icons.Default.SmartToy,
                tint = Color(0xFF7B61FF),
                onExecute = { viewModel.selectNavTab(MainNavTab.AI_ASSISTANT) }
            ),
            CommandPaletteAction(
                id = "toggle_preview",
                title = "📱 Alternar Vista Previa en Vivo",
                subtitle = "Visualizar Compose, Web HTML o Markdown",
                icon = Icons.Default.VerticalSplit,
                tint = Color(0xFF38BDF8),
                onExecute = { viewModel.toggleLivePreview() }
            ),
            CommandPaletteAction(
                id = "create_template",
                title = "📦 Nuevo Proyecto desde Plantilla...",
                subtitle = "Flutter, Expo React Native, Railway o Python",
                icon = Icons.Default.AutoAwesomeMotion,
                tint = Color(0xFFF59E0B),
                onExecute = { showTemplatesDialog = true }
            ),
            CommandPaletteAction(
                id = "clear_console",
                title = "🧹 Limpiar Logs de Consola",
                subtitle = "Vaciar el historial de la terminal",
                icon = Icons.Default.DeleteSweep,
                tint = Color(0xFFF87171),
                onExecute = { viewModel.clearTerminalLogs() }
            )
        )

        CommandPaletteDialog(
            rootProject = uiState.rootProject,
            onOpenFile = { file ->
                viewModel.openFile(file)
                viewModel.selectNavTab(MainNavTab.EDITOR)
            },
            actions = actions,
            onDismiss = { viewModel.toggleCommandPalette(false) }
        )
    }

    // Modal: Gestor de Espacios de Trabajo / Project Launcher
    if (uiState.showProjectLauncher) {
        Dialog(
            onDismissRequest = { viewModel.toggleProjectLauncher(false) },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ProjectLauncherScreen(
                workspaceState = uiState.workspaceState,
                onOpenProject = { proj ->
                    viewModel.openWorkspaceProject(proj)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Espacio de trabajo: ${proj.name}") }
                },
                onNewProjectFromTemplate = {
                    viewModel.toggleProjectLauncher(false)
                    showTemplatesDialog = true
                },
                onImportLocalDirectory = { path, name ->
                    viewModel.importLocalDirectory(path, name)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Importando $path...") }
                },
                onCloneRemoteRepo = { url, dest ->
                    viewModel.cloneRemoteRepository(url, dest)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Clonando repositorio...") }
                },
                onDeleteProject = { id -> viewModel.deleteWorkspaceProject(id) },
                onToggleFavorite = { id -> viewModel.toggleProjectFavorite(id) },
                onDismiss = { viewModel.toggleProjectLauncher(false) }
            )
        }
    }

    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesomeMotion, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plantillas de Proyecto", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Selecciona una plantilla para inicializar un proyecto estructurado listo para compilar y desplegar:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    uiState.projectTemplates.forEach { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF222436), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13141F))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(template.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = {
                                            viewModel.loadProjectTemplate(template)
                                            showTemplatesDialog = false
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Proyecto '${template.title}' inicializado")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Crear", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(template.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    template.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF1E2030), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(tag, color = Color(0xFF38BDF8), fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text("Cerrar", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F101A)
        )
    }
}

/**
 * Top App Bar: Minimalist, Intuitive, and Non-Redundant.
 */
@Composable
private fun IdeTopAppBar(
    activeFile: ProjectFile,
    currentTab: MainNavTab,
    isFileModified: Boolean,
    onMenuClick: () -> Unit,
    onRunClick: () -> Unit,
    onAiSparkleClick: () -> Unit,
    onMoreClick: () -> Unit,
    showMenuOptions: Boolean,
    onDismissMenu: () -> Unit,
    onOpenCommandPalette: () -> Unit = {},
    onFormatCode: () -> Unit = {},
    onOpenTemplates: () -> Unit = {},
    onOpenProjectLauncher: () -> Unit = {},
    onSaveFile: () -> Unit,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit,
    onCloseTab: () -> Unit,
    onOpenTerminal: () -> Unit,
    onLivePreview: () -> Unit,
    onOpenAiSubPage: (AiHubSubPage) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF0C0D15))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Drawer Toggle
        IconButton(onClick = onMenuClick, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Center: File Badge / Page Title
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onMenuClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlackCatLogo(size = 32.dp, shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.width(10.dp))

            Column {
                val titleText = when (currentTab) {
                    MainNavTab.EDITOR -> activeFile.name
                    MainNavTab.FILES -> "Archivos"
                    MainNavTab.AI_ASSISTANT -> "✨ Antigravity AI"
                    MainNavTab.TERMINAL -> "Terminal (bash)"
                    MainNavTab.SEARCH -> "Buscar"
                    MainNavTab.GIT -> "Git"
                    MainNavTab.MODELS -> "Gestor de Modelos"
                    MainNavTab.TELEMETRY -> "Telemetría & RAM"
                    MainNavTab.DOCS_RAG -> "Base de Conocimiento RAG"
                    MainNavTab.SETTINGS -> "Ajustes"
                    MainNavTab.SNIPPET_VAULT -> "📦 Snippet Vault"
                    MainNavTab.MISSION_CONTROL -> "🛸 Mission Control"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titleText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (currentTab == MainNavTab.EDITOR) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (isFileModified) Color(0xFFFBBF24) else Color(0xFF7B61FF),
                                    CircleShape
                                )
                        )
                    }
                }

                val subtitleText = if (currentTab == MainNavTab.EDITOR) {
                    if (isFileModified) "modificado" else "lib/"
                } else {
                    "Black Cat IDE • J.COMPE"
                }

                Text(
                    text = subtitleText,
                    fontSize = 10.sp,
                    color = if (isFileModified && currentTab == MainNavTab.EDITOR) Color(0xFFFBBF24) else Color(0xFF64748B)
                )
            }
        }

        // Right Actions (Clean, spacious, non-overlapping)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Command Center Button (Spotlight / Bolt)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E2238), CircleShape)
                    .clickable { onOpenCommandPalette() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, contentDescription = "Command Palette", tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
            }

            // Play Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E2238), CircleShape)
                    .clickable { onRunClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Ejecutar", tint = Color(0xFF60A5FA), modifier = Modifier.size(22.dp))
            }

            // Overflow Menu
            Box {
                IconButton(onClick = onMoreClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                DropdownMenu(
                    expanded = showMenuOptions,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.background(Color(0xFF141522))
                ) {
                    DropdownMenuItem(
                        text = { Text("✨ Formatear Código", color = Color(0xFFA78BFA)) },
                        onClick = onFormatCode
                    )
                    DropdownMenuItem(
                        text = { Text("📁 Gestor de Proyectos / Launcher", color = Color(0xFF00E5FF)) },
                        onClick = onOpenProjectLauncher
                    )
                    DropdownMenuItem(
                        text = { Text("📦 Proyecto desde Plantilla...", color = Color(0xFF38BDF8)) },
                        onClick = onOpenTemplates
                    )
                    DropdownMenuItem(
                        text = { Text("💾 Guardar archivo", color = Color.White) },
                        onClick = onSaveFile
                    )
                    DropdownMenuItem(
                        text = { Text("📄 Nuevo archivo...", color = Color.White) },
                        onClick = onNewFile
                    )
                    DropdownMenuItem(
                        text = { Text("📁 Nueva carpeta...", color = Color.White) },
                        onClick = onNewFolder
                    )
                    DropdownMenuItem(
                        text = { Text("✕ Cerrar pestaña", color = Color(0xFFF87171)) },
                        onClick = onCloseTab
                    )
                    HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = { Text("💻 Abrir Terminal", color = Color(0xFF34D399)) },
                        onClick = onOpenTerminal
                    )
                    DropdownMenuItem(
                        text = { Text("📱 Vista Previa en Vivo", color = Color(0xFF38BDF8)) },
                        onClick = onLivePreview
                    )
                }
            }
        }
    }
}

/**
 * Editor Tabs Row with Tab Close (✕) action.
 */
@Composable
private fun EditorTabsRow(
    openTabs: List<ProjectFile>,
    activeFile: ProjectFile,
    onTabSelect: (ProjectFile) -> Unit,
    onCloseTab: (ProjectFile) -> Unit,
    onSplitViewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF0C0D15))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            openTabs.forEach { tab ->
                val isActive = tab.id == activeFile.id
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) Color(0xFF191B2E) else Color.Transparent)
                        .clickable { onTabSelect(tab) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.name,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) Color.White else Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (openTabs.size > 1) {
                        IconButton(
                            onClick = { onCloseTab(tab) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        IconButton(onClick = onSplitViewClick, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.VerticalSplit, contentDescription = "Dividir vista", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Editor Main Content with zero overlapping buttons!
 */
@Composable
private fun EditorMainContent(
    viewModel: IdeViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val diagState = uiState.diagnosticsState

    Column(modifier = Modifier.fillMaxSize()) {
        EditorTabsRow(
            openTabs = uiState.openTabs,
            activeFile = uiState.activeFile,
            onTabSelect = { viewModel.openFile(it) },
            onCloseTab = { viewModel.closeTab(it) },
            onSplitViewClick = { viewModel.toggleLivePreview() }
        )

        HorizontalDivider(color = Color(0xFF181A28), thickness = 1.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.showLivePreview) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(0.55f).fillMaxWidth()) {
                        CodeEditorCore(
                            textFieldValue = uiState.editorValue,
                            onValueChange = { viewModel.onEditorChange(it) },
                            language = uiState.activeFile.language,
                            diagnostics = diagState.issues,
                            fontSize = 12.sp
                        )
                    }
                    HorizontalDivider(color = Color(0xFF181A28), thickness = 2.dp)
                    Box(modifier = Modifier.weight(0.45f).fillMaxWidth()) {
                        LiveComposePreviewHost(code = uiState.editorValue.text)
                    }
                }
            } else {
                CodeEditorCore(
                    textFieldValue = uiState.editorValue,
                    onValueChange = { viewModel.onEditorChange(it) },
                    language = uiState.activeFile.language,
                    diagnostics = diagState.issues,
                    fontSize = 13.sp
                )
            }
        }

        // Virtual Accessory Keyboard Bar (No overlapping FAB!)
        MobileCodeKeyboardBar(
            currentValue = uiState.editorValue,
            onValueChange = { viewModel.onEditorChange(it) },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo,
            focusedIssue = diagState.issues.firstOrNull { it.quickFix != null },
            onApplyQuickFix = { fix -> viewModel.applyQuickFix(fix) }
        )
    }
}

/**
 * Bottom Navigation Bar: Clean, modern, distinct icons and clear text labels.
 */
@Composable
private fun IdeBottomNavigationBar(
    selectedTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit
) {
    Surface(
        color = Color(0xFF0C0D15),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2135))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(MainNavTab.EDITOR, Icons.Default.Code, "Editor"),
                Triple(MainNavTab.FILES, Icons.Default.Folder, "Archivos"),
                Triple(MainNavTab.AI_ASSISTANT, Icons.Default.AutoFixHigh, "IA & Agentes"),
                Triple(MainNavTab.TERMINAL, Icons.Default.Terminal, "Terminal"),
                Triple(MainNavTab.SETTINGS, Icons.Default.Settings, "Ajustes")
            )

            tabs.forEach { (tab, icon, label) ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color(0xFF7B61FF) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isSelected) Color(0xFF7B61FF) else Color(0xFF64748B),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * Category Navigation Bar: Sleek horizontal scrollable pill row for instant switching across all 10 pages.
 * Crystal-clear icons, Spanish text labels, active glowing indicators, zero overlapping icons.
 */
@Composable
private fun IdeCategoryNavBar(
    selectedTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPages = listOf(
        Triple(MainNavTab.EDITOR, Icons.Default.Code, "Editor"),
        Triple(MainNavTab.FILES, Icons.Default.Folder, "Archivos"),
        Triple(MainNavTab.AI_ASSISTANT, Icons.Default.AutoFixHigh, "IA Agentes"),
        Triple(MainNavTab.TERMINAL, Icons.Default.Terminal, "Terminal"),
        Triple(MainNavTab.SEARCH, Icons.Default.Search, "Buscar"),
        Triple(MainNavTab.GIT, Icons.Default.AccountTree, "Git"),
        Triple(MainNavTab.MODELS, Icons.Default.CloudDownload, "Modelos"),
        Triple(MainNavTab.TELEMETRY, Icons.Default.Speed, "Telemetría"),
        Triple(MainNavTab.DOCS_RAG, Icons.Default.MenuBook, "Docs RAG"),
        Triple(MainNavTab.SETTINGS, Icons.Default.Settings, "Ajustes"),
        Triple(MainNavTab.SNIPPET_VAULT, Icons.Default.DataObject, "Snippets"),
        Triple(MainNavTab.MISSION_CONTROL, Icons.Default.Dashboard, "Misión")
    )

    Surface(
        color = Color(0xFF0C0D15),
        modifier = modifier.fillMaxWidth().height(42.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2135))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allPages.forEach { (tab, icon, label) ->
                val isSelected = selectedTab == tab
                val iconTint = when (tab) {
                    MainNavTab.EDITOR -> Color(0xFF818CF8)
                    MainNavTab.FILES -> Color(0xFFFBBF24)
                    MainNavTab.AI_ASSISTANT -> Color(0xFFC084FC)
                    MainNavTab.TERMINAL -> Color(0xFF34D399)
                    MainNavTab.SEARCH -> Color(0xFF38BDF8)
                    MainNavTab.GIT -> Color(0xFFFB923C)
                    MainNavTab.MODELS -> Color(0xFFF472B6)
                    MainNavTab.TELEMETRY -> Color(0xFF4ADE80)
                    MainNavTab.DOCS_RAG -> Color(0xFFA78BFA)
                    MainNavTab.SETTINGS -> Color(0xFF94A3B8)
                    MainNavTab.SNIPPET_VAULT -> Color(0xFFF59E0B)
                    MainNavTab.MISSION_CONTROL -> Color(0xFFE879F9)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF26193E) else Color(0xFF141522))
                        .border(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) Color(0xFF7B61FF) else Color(0xFF26283C),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

