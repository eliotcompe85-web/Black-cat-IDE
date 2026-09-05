package com.ide.mobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Warning
import com.ide.mobile.feature.compiler.LiveComposePreviewHost
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ide.mobile.core.model.ProjectFile
import com.ide.mobile.feature.ai.ui.AiAssistantPanel
import com.ide.mobile.feature.editor.CodeEditorCore
import com.ide.mobile.feature.explorer.ProjectExplorerDrawer
import com.ide.mobile.feature.keyboard.MobileCodeKeyboardBar
import com.ide.mobile.ui.views.FilesScreen
import com.ide.mobile.ui.views.GitScreen
import com.ide.mobile.ui.views.SearchScreen
import com.ide.mobile.ui.views.SettingsScreen
import com.ide.mobile.ui.views.TerminalScreen
import com.ide.mobile.ui.views.aihub.ModelManagementScreen
import com.ide.mobile.ui.views.aihub.RuntimeEngineScreen
import com.ide.mobile.ui.views.aihub.AiPluginsScreen
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
                onNewFileClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Nuevo archivo creado en lib/")
                    }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFF0C0D15),
            topBar = {
                IdeTopAppBar(
                    activeFile = uiState.activeFile,
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onRunClick = { viewModel.runProject() },
                    onAiSparkleClick = { viewModel.toggleAiPanel() },
                    onMoreClick = { showMenuOptions = true },
                    showMenuOptions = showMenuOptions,
                    onDismissMenu = { showMenuOptions = false },
                    onOpenTerminal = {
                        viewModel.toggleTerminal()
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
                // Switch content based on active Bottom Navigation Tab
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
                            onNewFileClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Crear archivo en proyecto")
                                }
                            }
                        )
                    }
                    MainNavTab.SEARCH -> {
                        SearchScreen()
                    }
                    MainNavTab.GIT -> {
                        GitScreen(
                            onCommit = { msg ->
                                viewModel.commitChanges(msg)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Cambios confirmados: $msg")
                                }
                            }
                        )
                    }
                    MainNavTab.SETTINGS -> {
                        SettingsScreen(
                            localHost = uiState.localAiHost,
                            localPort = uiState.localAiPort,
                            localDashboardPort = uiState.localAiDashboardPort,
                            selectedModel = uiState.selectedLocalModel,
                            testStatus = uiState.localAiTestStatus,
                            onSaveLocalConfig = { host, port, model ->
                                viewModel.updateLocalAiConfig(host, port, model)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Guardado: $host:$port ($model)")
                                }
                            },
                            onTestConnection = { host, port ->
                                viewModel.testLocalAiConnection(host, port)
                            },
                            onOpenModelManagement = { viewModel.openAiSubPage(AiHubSubPage.MODEL_MANAGEMENT) },
                            onOpenRuntimeEngine = { viewModel.openAiSubPage(AiHubSubPage.RUNTIME_ENGINE) },
                            onOpenPluginsRouter = { viewModel.openAiSubPage(AiHubSubPage.PLUGINS_ROUTER) },
                            onOpenSiloLibrary = { viewModel.openAiSubPage(AiHubSubPage.SILO_LIBRARY) }
                        )
                    }
                }

                // AI Hub Sub-page Full-screen Overlays
                when (uiState.activeAiSubPage) {
                    AiHubSubPage.MODEL_MANAGEMENT -> {
                        ModelManagementScreen(
                            installedModels = uiState.installedModels,
                            catalogModels = uiState.catalogModels,
                            onRunModel = { id -> viewModel.runModel(id) },
                            onStopModel = { id -> viewModel.stopModel(id) },
                            onDeleteModel = { id -> viewModel.deleteModel(id) },
                            onDownloadModel = { item -> viewModel.downloadModel(item) },
                            onImportLocalFile = { name, size -> viewModel.importLocalModel(name, size) },
                            onBack = { viewModel.closeAiSubPage() }
                        )
                    }
                    AiHubSubPage.RUNTIME_ENGINE -> {
                        RuntimeEngineScreen(
                            metrics = uiState.runtimeMetrics,
                            onUpdateConfig = { threads, context, gpu, engine ->
                                viewModel.updateHardwareConfig(threads, context, gpu, engine)
                            },
                            onBack = { viewModel.closeAiSubPage() }
                        )
                    }
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
                    AiHubSubPage.NONE -> { /* handled by bottom nav tabs */ }
                }

                // Overlay Terminal when toggled
                if (uiState.showTerminal) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0A0B10))
                    ) {
                        TerminalScreen(
                            logs = uiState.consoleLogs,
                            onClose = { viewModel.toggleTerminal() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top App Bar matching Image 1:
 * - Hamburger menu
 * - File badge: Rounded purple square with `< >`, file name `main.dart` with purple dot `•`, subtitle `lib/`
 * - Actions: Play button pill/circle, Sparkles AI button, 3-dots overflow menu
 */
@Composable
private fun IdeTopAppBar(
    activeFile: ProjectFile,
    onMenuClick: () -> Unit,
    onRunClick: () -> Unit,
    onAiSparkleClick: () -> Unit,
    onMoreClick: () -> Unit,
    showMenuOptions: Boolean,
    onDismissMenu: () -> Unit,
    onOpenTerminal: () -> Unit,
    onLivePreview: () -> Unit,
    onOpenAiSubPage: (AiHubSubPage) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0D15))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Center-Left: File Badge with purple rounded icon + file info
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onMenuClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Black Cat IDE Logo
                com.ide.mobile.ui.components.BlackCatLogo(
                    size = 36.dp,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeFile.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Purple active / unsaved dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF8B5CF6), CircleShape)
                        )
                    }

                    val dirSubtitle = if (activeFile.path.contains("/lib/")) {
                        "lib/"
                    } else {
                        val parts = activeFile.path.trim('/').split('/')
                        if (parts.size > 1) "${parts[parts.size - 2]}/" else "root/"
                    }

                    Text(
                        text = dirSubtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Right Actions: Play button, Sparkles AI button, More Options (⋮)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play Button (pill / dark rounded circle with blue play icon)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E2238), CircleShape)
                        .clickable { onRunClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Ejecutar",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // AI Sparkles Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E1F30), CircleShape)
                        .clickable { onAiSparkleClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "AI Assistant",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 3-dots Menu
                Box {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenuOptions,
                        onDismissRequest = onDismissMenu,
                        modifier = Modifier.background(Color(0xFF181926))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Abrir Terminal", color = Color.White) },
                            onClick = onOpenTerminal
                        )
                        DropdownMenuItem(
                            text = { Text("Vista previa en vivo", color = Color.White) },
                            onClick = onLivePreview
                        )
                        HorizontalDivider(color = Color(0xFF2E324E), thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = { Text("📦 Gestor de Modelos", color = Color(0xFFC084FC)) },
                            onClick = {
                                onOpenAiSubPage(AiHubSubPage.MODEL_MANAGEMENT)
                                onDismissMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚡ Motor & Telemetría", color = Color(0xFF34D399)) },
                            onClick = {
                                onOpenAiSubPage(AiHubSubPage.RUNTIME_ENGINE)
                                onDismissMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🔀 Smart Router & Plugins", color = Color(0xFF38BDF8)) },
                            onClick = {
                                onOpenAiSubPage(AiHubSubPage.PLUGINS_ROUTER)
                                onDismissMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📚 SiloLibrary (RAG)", color = Color(0xFFFBBF24)) },
                            onClick = {
                                onOpenAiSubPage(AiHubSubPage.SILO_LIBRARY)
                                onDismissMenu()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Editor Tabs Row matching Image 1:
 * - `main.dart` with glowing purple underline
 * - `theme.dart` (grey inactive)
 * - `router.dart` (grey inactive)
 * - Right icon: Split layout `◫`
 */
@Composable
private fun EditorTabsRow(
    openTabs: List<ProjectFile>,
    activeFile: ProjectFile,
    onTabSelect: (ProjectFile) -> Unit,
    onSplitViewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF0C0D15))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            openTabs.forEach { tab ->
                val isActive = tab.id == activeFile.id
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { onTabSelect(tab) }
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = tab.name,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) Color.White else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Active purple underline
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(2.5.dp)
                                .background(Color(0xFF7B61FF), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.5.dp))
                    }
                }
            }
        }

        // Far right: Split screen / dual panel icon
        IconButton(
            onClick = onSplitViewClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VerticalSplit,
                contentDescription = "Dividir vista",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Editor Main Content (Image 1):
 * - Tabs row at top
 * - Code editor in center with line numbers & active line 13 accent
 * - Floating AI FAB at bottom right
 * - Docked AI Assistant bottom sheet panel
 * - Virtual accessory keyboard bar with quickfix
 */
@Composable
private fun EditorMainContent(
    viewModel: IdeViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val diagState = uiState.diagnosticsState

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Tabs Bar
        EditorTabsRow(
            openTabs = uiState.openTabs,
            activeFile = uiState.activeFile,
            onTabSelect = { viewModel.openFile(it) },
            onSplitViewClick = { viewModel.toggleLivePreview() }
        )

        HorizontalDivider(color = Color(0xFF181A28), thickness = 1.dp)

        // Main Editor Area with Floating AI FAB
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.showLivePreview) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxWidth()
                    ) {
                        CodeEditorCore(
                            textFieldValue = uiState.editorValue,
                            onValueChange = { viewModel.onEditorChange(it) },
                            language = uiState.activeFile.language,
                            diagnostics = diagState.issues,
                            fontSize = 12.sp
                        )
                    }
                    HorizontalDivider(color = Color(0xFF181A28), thickness = 2.dp)
                    Box(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxWidth()
                    ) {
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

            // Signature watermark: "BLACK CAT IDE • J.COMPE"
            com.ide.mobile.ui.components.BlackCatEditorWatermark(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 12.dp)
            )

            // Floating Glowing AI FAB (bottom right of editor, matching Image 1)
            if (!uiState.showAiPanel) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 18.dp, bottom = 18.dp)
                        .size(54.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8))
                            ),
                            shape = CircleShape
                        )
                        .clickable { viewModel.toggleAiPanel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Abrir Asistente IA",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Docked AI Assistant Bottom Panel (exact replica from Image 1)
        AnimatedVisibility(
            visible = uiState.showAiPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // AI Response Banner if available
                if (uiState.isAiLoading || !uiState.aiResponse.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFF141624),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .border(1.dp, Color(0xFF2E3250), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.isAiLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF8B5CF6),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pensando respuesta...", color = Color(0xFF8B5CF6), fontSize = 12.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Respuesta del Asistente", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row {
                                    TextButton(
                                        onClick = {
                                            viewModel.insertAiGeneratedCode()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF7B61FF), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Insertar", fontSize = 11.sp, color = Color(0xFF7B61FF))
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissAiAssistant() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            uiState.aiResponse?.let { resp ->
                                Text(
                                    text = resp,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }

                AiAssistantPanel(
                    onActionClick = { actionId ->
                        when (actionId) {
                            "EXPLAIN" -> viewModel.askAiAssistant("Explica detalladamente la estructura y componentes de este código")
                            "GENERATE" -> viewModel.askAiAssistant("Genera un widget o componente Flutter reutilizable en Dart para este archivo")
                            "FIND_BUGS" -> viewModel.askAiAssistant("Revisa el código en busca de posibles fallos o advertencias de sintaxis")
                            "REFACTOR" -> viewModel.askAiAssistant("Refactoriza y optimiza esta función para mejorar rendimiento y legibilidad")
                        }
                    },
                    onSendMessage = { userPrompt ->
                        viewModel.askAiAssistant(userPrompt)
                    },
                    onClose = { viewModel.toggleAiPanel() },
                    selectedProvider = uiState.selectedAiProvider,
                    onSelectProvider = { provider -> viewModel.selectAiProvider(provider) },
                    selectedLocalModel = uiState.selectedLocalModel,
                    onSelectLocalModel = { model -> viewModel.selectLocalModel(model) },
                    onOpenHubPage = { hubId ->
                        when (hubId) {
                            "MODELS" -> viewModel.openAiSubPage(AiHubSubPage.MODEL_MANAGEMENT)
                            "RUNTIME" -> viewModel.openAiSubPage(AiHubSubPage.RUNTIME_ENGINE)
                            "ROUTER" -> viewModel.openAiSubPage(AiHubSubPage.PLUGINS_ROUTER)
                            "SILO" -> viewModel.openAiSubPage(AiHubSubPage.SILO_LIBRARY)
                        }
                    }
                )
            }
        }

        // Accessory Bar (undo, redo, brackets, quick fix)
        Column(modifier = Modifier.imePadding()) {
            diagState.focusedIssue?.let { issue ->
                Surface(
                    color = Color(0xFF1E1F30),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Línea ${issue.line}: ${issue.message}",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        if (issue.quickFix != null) {
                            TextButton(
                                onClick = { viewModel.applyQuickFix(issue.quickFix) },
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Corregir", fontSize = 10.sp, color = Color(0xFFC084FC), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            MobileCodeKeyboardBar(
                currentValue = uiState.editorValue,
                onValueChange = { viewModel.onEditorChange(it) },
                onUndo = { viewModel.handleUndo() },
                onRedo = { viewModel.handleRedo() },
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                focusedIssue = diagState.focusedIssue,
                onApplyQuickFix = { viewModel.applyQuickFix(it) }
            )
        }
    }
}

/**
 * 5-Tab Bottom Navigation Bar matching Image 1 & Image 2:
 * 1. Editor (< >)
 * 2. Archivos (Folder)
 * 3. Buscar (Search)
 * 4. Git (AccountTree)
 * 5. Ajustes (Settings)
 *
 * Plus Android home pill indicator at bottom.
 */
@Composable
private fun IdeBottomNavigationBar(
    selectedTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit
) {
    val items = listOf(
        NavigationItem(MainNavTab.EDITOR, "Editor", Icons.Default.Code),
        NavigationItem(MainNavTab.FILES, "Archivos", Icons.Default.Folder),
        NavigationItem(MainNavTab.SEARCH, "Buscar", Icons.Default.Search),
        NavigationItem(MainNavTab.GIT, "Git", Icons.Default.AccountTree),
        NavigationItem(MainNavTab.SETTINGS, "Ajustes", Icons.Default.Settings)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0D15))
            .border(0.5.dp, Color(0xFF181A28), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedTab == item.tab
                val tint = if (isSelected) Color(0xFF7B61FF) else Color(0xFF64748B)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(item.tab) }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = tint
                    )
                }
            }
        }

        // Android bottom home indicator pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(134.dp)
                    .height(4.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
            )
        }
    }
}

private data class NavigationItem(
    val tab: MainNavTab,
    val label: String,
    val icon: ImageVector
)
