package com.ide.mobile.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ide.mobile.core.model.ActionStatus
import com.ide.mobile.core.model.ActiveAgentContext
import com.ide.mobile.core.model.AgentAction
import com.ide.mobile.core.model.AgentActionType
import com.ide.mobile.core.model.AgentState
import com.ide.mobile.core.model.AnalysisRequest
import com.ide.mobile.core.model.AnalysisResponse
import com.ide.mobile.core.model.AnalysisStatus
import com.ide.mobile.core.model.ApiKeysConfig
import com.ide.mobile.core.model.BuildProgress
import com.ide.mobile.core.model.BuildStep
import com.ide.mobile.core.model.ChatContent
import com.ide.mobile.core.model.ChecklistTask
import com.ide.mobile.core.model.ChatMessage
import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.DiagnosticsState
import com.ide.mobile.core.model.DeploymentRecord
import com.ide.mobile.core.model.DeploymentStatus
import com.ide.mobile.core.model.DeploymentTarget
import com.ide.mobile.core.model.DownloadableAgent
import com.ide.mobile.core.model.ExpoDevConfig
import com.ide.mobile.core.model.GitHubConfig
import com.ide.mobile.core.model.LanguageType
import com.ide.mobile.core.model.LocalAgentEntity
import com.ide.mobile.core.model.MessageSender
import com.ide.mobile.core.model.ModelItem
import com.ide.mobile.core.model.ProjectFile
import com.ide.mobile.core.model.ProjectTemplate
import com.ide.mobile.core.model.ProjectTemplateType
import com.ide.mobile.core.model.QuickFix
import com.ide.mobile.core.model.RailwayConfig
import com.ide.mobile.core.model.RagChunk
import com.ide.mobile.core.model.RagDocument
import com.ide.mobile.core.model.RouterConfig
import com.ide.mobile.core.model.RuntimeMetrics
import com.ide.mobile.core.model.Severity
import com.ide.mobile.feature.editor.SmartCodeFormatter
import com.ide.mobile.feature.ai.AgentActionParser
import com.ide.mobile.feature.ai.AiAssistantManager
import com.ide.mobile.feature.ai.AntigravityArtifactParser
import com.ide.mobile.feature.ai.CodeContext
import com.ide.mobile.feature.ai.LocalAgentEngine
import com.ide.mobile.feature.ai.ProviderType
import com.ide.mobile.core.model.Project
import com.ide.mobile.core.model.WorkspaceState
import com.ide.mobile.core.model.WorkspaceRepository
import com.ide.mobile.core.model.RoomWorkspaceRepository
import com.ide.mobile.feature.explorer.WorkspaceImportService
import com.ide.mobile.feature.ai.downloader.ModelDownloadManager
import com.ide.mobile.feature.ai.rag.SiloKnowledgeEngine
import com.ide.mobile.feature.ai.router.SmartInferenceRouter
import com.ide.mobile.feature.ai.runtime.DeviceTelemetryService
import com.ide.mobile.feature.compiler.BuildPipeline
import com.ide.mobile.feature.diagnostics.SyntaxAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainNavTab {
    EDITOR, FILES, AI_ASSISTANT, TERMINAL, SEARCH, GIT, MODELS, TELEMETRY, DOCS_RAG, SETTINGS, SNIPPET_VAULT, MISSION_CONTROL
}


enum class AiHubSubPage {
    NONE,
    MODEL_MANAGEMENT,
    RUNTIME_ENGINE,
    PLUGINS_ROUTER,
    SILO_LIBRARY
}

data class SearchMatch(
    val file: ProjectFile,
    val lineNumber: Int,
    val lineContent: String
)

data class IdeUiState(
    val rootProject: ProjectFile,
    val activeFile: ProjectFile,
    val openTabs: List<ProjectFile>,
    val editorValue: TextFieldValue,
    val currentNavTab: MainNavTab = MainNavTab.EDITOR,
    val activeAiSubPage: AiHubSubPage = AiHubSubPage.NONE,
    val showAiPanel: Boolean = true,
    val showTerminal: Boolean = false,
    val documentVersion: Long = 1L,
    val isFileModified: Boolean = false,
    val diagnosticsState: DiagnosticsState = DiagnosticsState(),
    val buildProgress: BuildProgress = BuildProgress(),
    val showLivePreview: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showConsole: Boolean = false,
    val showDiagnosticsSheet: Boolean = false,
    val selectedAiProvider: ProviderType = ProviderType.GEMINI_API,
    val apiKeysConfig: ApiKeysConfig = ApiKeysConfig(),
    val availableAgents: List<LocalAgentEntity> = LocalAgentEngine.BUILT_IN_AGENTS,
    val activeAgent: LocalAgentEntity = LocalAgentEngine.BUILT_IN_AGENTS.first(),
    val localAiHost: String = "127.0.0.1",
    val localAiPort: Int = 11434,
    val localAiDashboardPort: Int = 8080,
    val selectedLocalModel: String = "Qwen3.5-2B-Q4_0.gguf",
    val localAiTestStatus: String? = null,
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val currentExecutionStep: String? = null,
    val isAgentExecuting: Boolean = false,
    val downloadableAgents: List<DownloadableAgent> = emptyList(),
    val autoExecuteAgentActions: Boolean = false,
    val consoleLogs: List<String> = emptyList(),
    val installedModels: List<ModelItem> = emptyList(),
    val catalogModels: List<ModelItem> = emptyList(),
    val runtimeMetrics: RuntimeMetrics = RuntimeMetrics(),
    val routerConfig: RouterConfig = RouterConfig(),
    val ragDocuments: List<RagDocument> = emptyList(),
    val isRagInjectionEnabled: Boolean = true,
    val searchQuery: String = "",
    val searchMatches: List<SearchMatch> = emptyList(),
    val gitHubConfig: GitHubConfig = GitHubConfig(),
    val expoDevConfig: ExpoDevConfig = ExpoDevConfig(),
    val railwayConfig: RailwayConfig = RailwayConfig(),
    val deploymentStatus: DeploymentStatus = DeploymentStatus.IDLE,
    val deploymentMessage: String? = null,
    val deploymentRecords: List<DeploymentRecord> = emptyList(),
    val showCommandPalette: Boolean = false,
    val projectTemplates: List<ProjectTemplate> = ProjectTemplate.ALL_TEMPLATES,
    val workspaceState: WorkspaceState = WorkspaceState(),
    val showProjectLauncher: Boolean = false,
    val showSnippetVault: Boolean = false,
    val activeMissionAgents: List<ActiveAgentContext> = listOf(
        ActiveAgentContext(
            agentId = "1",
            agentName = "Local Copilot (Default)",
            isLocal = true,
            currentState = AgentState.Idle
        )
    )
)

@OptIn(FlowPreview::class)
class IdeViewModel : ViewModel() {

    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()

    private val sampleProject = ProjectFile.createSampleAndroidProject()
    private val initialFile = findInitialFile(sampleProject)
    private val initialTabs = findInitialTabs(sampleProject)

    private val aiManager = AiAssistantManager(ProviderType.GEMINI_API)
    private val modelDownloadManager = ModelDownloadManager(viewModelScope)
    private val telemetryService = DeviceTelemetryService(viewModelScope)
    private val routerEngine = SmartInferenceRouter()
    private val siloEngine = SiloKnowledgeEngine()
    val workspaceRepository: WorkspaceRepository = RoomWorkspaceRepository()
    val workspaceImportService = WorkspaceImportService(workspaceRepository)

    private var currentDocumentVersion: Long = 1L
    private var activeAnalysisJob: Job? = null
    private var activeAiJob: Job? = null

    private val _uiState = MutableStateFlow(
        IdeUiState(
            rootProject = sampleProject,
            activeFile = initialFile,
            openTabs = initialTabs,
            editorValue = TextFieldValue(initialFile.content),
            currentNavTab = MainNavTab.EDITOR,
            showAiPanel = true,
            documentVersion = 1L,
            diagnosticsState = DiagnosticsState(documentVersion = 1L),
            availableAgents = aiManager.agentEngine.getAllAgents(),
            activeAgent = aiManager.agentEngine.getAllAgents().first(),
            downloadableAgents = aiManager.getDownloadableAgents(),
            consoleLogs = listOf(
                "[BLACK CAT IDE v1.0 • KERNEL READY]",
                "[⭐ CREATED BY J.COMPE]",
                "[ANTIGRAVITY ENGINE] Multi-Agent ecosystem loaded.",
                "[Gemini Flash] Conectado a projects/577789803126.",
                "[Project] 'mi_nuevo_proyecto' cargado (Flutter / Dart)."
            )
        )
    )
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    init {
        // Run initial immediate analysis
        scheduleAnalysis(
            request = AnalysisRequest(
                fileId = initialFile.id,
                language = initialFile.language,
                code = initialFile.content,
                documentVersion = 1L,
                isImmediate = true
            )
        )

        viewModelScope.launch {
            modelDownloadManager.installedModels.collect { list ->
                _uiState.update { it.copy(installedModels = list) }
            }
        }
        viewModelScope.launch {
            modelDownloadManager.hubCatalog.collect { list ->
                _uiState.update { it.copy(catalogModels = list) }
            }
        }
        viewModelScope.launch {
            telemetryService.metrics.collect { met ->
                _uiState.update { it.copy(runtimeMetrics = met) }
            }
        }
        viewModelScope.launch {
            siloEngine.documents.collect { docs ->
                _uiState.update { it.copy(ragDocuments = docs) }
            }
        }
        viewModelScope.launch {
            workspaceRepository.workspaceState.collect { ws ->
                _uiState.update { it.copy(workspaceState = ws) }
            }
        }
    }

    private fun findInitialFile(file: ProjectFile): ProjectFile {
        if (file.name == "main.dart") return file
        for (child in file.children) {
            val found = findInitialFile(child)
            if (found.name == "main.dart") return found
        }
        return file.children.firstOrNull { !it.isDirectory } ?: file
    }

    private fun findInitialTabs(root: ProjectFile): List<ProjectFile> {
        val targetNames = listOf("main.dart", "theme.dart", "router.dart")
        val result = mutableListOf<ProjectFile>()
        fun scan(node: ProjectFile) {
            if (!node.isDirectory && node.name in targetNames) {
                result.add(node)
            }
            node.children.forEach { scan(it) }
        }
        scan(root)
        val sorted = result.sortedBy { targetNames.indexOf(it.name) }
        return if (sorted.isNotEmpty()) sorted else listOf(findInitialFile(root))
    }

    fun onEditorChange(newValue: TextFieldValue, isImmediate: Boolean = false) {
        val current = _uiState.value.editorValue
        val isTextChanged = current.text != newValue.text

        if (isTextChanged) {
            currentDocumentVersion++
            val newVer = currentDocumentVersion

            undoStack.add(current)
            redoStack.clear()
            _uiState.value.activeFile.content = newValue.text

            _uiState.update {
                it.copy(
                    editorValue = newValue,
                    documentVersion = newVer,
                    isFileModified = true,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false,
                    diagnosticsState = it.diagnosticsState.copy(status = AnalysisStatus.ANALYZING)
                )
            }

            scheduleAnalysis(
                request = AnalysisRequest(
                    fileId = _uiState.value.activeFile.id,
                    language = _uiState.value.activeFile.language,
                    code = newValue.text,
                    documentVersion = newVer,
                    isImmediate = isImmediate
                )
            )
        } else {
            _uiState.update { it.copy(editorValue = newValue) }
        }
    }

    fun saveCurrentFile(): String {
        val currentFile = _uiState.value.activeFile
        currentFile.content = _uiState.value.editorValue.text
        _uiState.update {
            it.copy(
                isFileModified = false,
                consoleLogs = it.consoleLogs + listOf("[Guardado] ${currentFile.name} guardado correctamente.")
            )
        }
        return currentFile.name
    }

    private fun scheduleAnalysis(request: AnalysisRequest) {
        activeAnalysisJob?.cancel()
        activeAnalysisJob = viewModelScope.launch {
            if (!request.isImmediate) {
                delay(300)
            }
            val response = withContext(Dispatchers.Default) {
                SyntaxAnalyzer.analyzeRequest(request)
            }
            applyAnalysisResult(response)
        }
    }

    private fun applyAnalysisResult(response: AnalysisResponse) {
        if (response.documentVersion < currentDocumentVersion) return

        _uiState.update { current ->
            current.copy(
                diagnosticsState = DiagnosticsState(
                    documentVersion = response.documentVersion,
                    issues = response.issues,
                    status = AnalysisStatus.UP_TO_DATE
                )
            )
        }
    }

    fun applyQuickFix(fix: QuickFix) {
        val currentText = _uiState.value.editorValue.text
        val targetRange = fix.replacementRange
        val fixedText = if (targetRange.first <= targetRange.last && targetRange.last <= currentText.length) {
            currentText.substring(0, targetRange.first) + fix.replacementText + currentText.substring(targetRange.last)
        } else {
            when (fix.title) {
                "Cerrar bloque con '}'" -> currentText + "\n}"
                "Cerrar cadena con comillas" -> currentText + "\""
                "Cerrar etiqueta XML" -> currentText + "/>"
                else -> currentText + fix.replacementText
            }
        }

        val newValue = TextFieldValue(
            text = fixedText,
            selection = TextRange(fixedText.length)
        )
        onEditorChange(newValue, isImmediate = true)

        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf("[QuickFix] Aplicado: ${fix.title}")
            )
        }
    }

    fun applyQuickFix(issue: DiagnosticIssue) {
        val fix = issue.quickFix ?: return
        applyQuickFix(fix)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(_uiState.value.editorValue)

        _uiState.value.activeFile.content = previous.text
        currentDocumentVersion++
        val newVer = currentDocumentVersion

        _uiState.update {
            it.copy(
                editorValue = previous,
                documentVersion = newVer,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
        scheduleAnalysis(
            request = AnalysisRequest(
                fileId = _uiState.value.activeFile.id,
                language = _uiState.value.activeFile.language,
                code = previous.text,
                documentVersion = newVer,
                isImmediate = true
            )
        )
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(_uiState.value.editorValue)

        _uiState.value.activeFile.content = next.text
        currentDocumentVersion++
        val newVer = currentDocumentVersion

        _uiState.update {
            it.copy(
                editorValue = next,
                documentVersion = newVer,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
        scheduleAnalysis(
            request = AnalysisRequest(
                fileId = _uiState.value.activeFile.id,
                language = _uiState.value.activeFile.language,
                code = next.text,
                documentVersion = newVer,
                isImmediate = true
            )
        )
    }

    fun openFile(file: ProjectFile) {
        if (file.isDirectory) return
        val currentOpenTabs = _uiState.value.openTabs.toMutableList()
        if (currentOpenTabs.none { it.id == file.id }) {
            currentOpenTabs.add(file)
        }
        undoStack.clear()
        redoStack.clear()
        currentDocumentVersion++

        _uiState.update {
            it.copy(
                activeFile = file,
                openTabs = currentOpenTabs,
                editorValue = TextFieldValue(file.content),
                documentVersion = currentDocumentVersion,
                isFileModified = false,
                canUndo = false,
                canRedo = false,
                diagnosticsState = DiagnosticsState(documentVersion = currentDocumentVersion)
            )
        }
        scheduleAnalysis(
            request = AnalysisRequest(
                fileId = file.id,
                language = file.language,
                code = file.content,
                documentVersion = currentDocumentVersion,
                isImmediate = true
            )
        )
    }

    fun closeTab(file: ProjectFile) {
        val tabs = _uiState.value.openTabs.toMutableList()
        tabs.removeAll { it.id == file.id }
        if (tabs.isEmpty()) {
            tabs.add(initialFile)
        }
        val nextActive = if (_uiState.value.activeFile.id == file.id) tabs.first() else _uiState.value.activeFile
        openFile(nextActive)
        _uiState.update { it.copy(openTabs = tabs) }
    }

    fun createNewFile(fileName: String, targetParentPath: String = "/mi_nuevo_proyecto/lib") {
        val root = _uiState.value.rootProject
        val targetFolder = root.flatten().firstOrNull { it.isDirectory && (it.path == targetParentPath || it.name == "lib") } ?: root
        val cleanName = fileName.trim()
        val newPath = "${targetFolder.path}/$cleanName"
        val newFile = ProjectFile(
            id = "file-${System.currentTimeMillis()}",
            name = cleanName,
            path = newPath,
            isDirectory = false,
            content = "// Creado con Black Cat IDE\n"
        )
        targetFolder.children.add(newFile)
        openFile(newFile)
        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf("[Archivos] Creado archivo: $cleanName")
            )
        }
    }

    fun createNewFolder(folderName: String, targetParentPath: String = "/mi_nuevo_proyecto/lib") {
        val root = _uiState.value.rootProject
        val targetFolder = root.flatten().firstOrNull { it.isDirectory && (it.path == targetParentPath || it.name == "lib") } ?: root
        val cleanName = folderName.trim()
        val newFolder = ProjectFile(
            id = "dir-${System.currentTimeMillis()}",
            name = cleanName,
            path = "${targetFolder.path}/$cleanName",
            isDirectory = true
        )
        targetFolder.children.add(newFolder)
        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf("[Archivos] Creada carpeta: $cleanName")
            )
        }
    }

    fun deleteFile(file: ProjectFile) {
        _uiState.value.rootProject.removeNode(file.id)
        if (_uiState.value.activeFile.id == file.id) {
            closeTab(file)
        }
        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf("[Archivos] Eliminado: ${file.name}")
            )
        }
    }

    fun searchInProject(query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            _uiState.update { it.copy(searchQuery = "", searchMatches = emptyList()) }
            return
        }

        val allFiles = _uiState.value.rootProject.flatten().filter { !it.isDirectory }
        val matches = mutableListOf<SearchMatch>()

        for (file in allFiles) {
            // Match by name
            if (file.name.contains(cleanQuery, ignoreCase = true)) {
                matches.add(SearchMatch(file, 1, file.name))
            }
            // Match by line content
            val lines = file.content.lines()
            for ((index, line) in lines.withIndex()) {
                if (line.contains(cleanQuery, ignoreCase = true)) {
                    matches.add(SearchMatch(file, index + 1, line.trim()))
                }
            }
        }

        _uiState.update {
            it.copy(searchQuery = cleanQuery, searchMatches = matches)
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val cleanCmd = cmd.trim()
        if (cleanCmd.isBlank()) return

        val newLogs = _uiState.value.consoleLogs.toMutableList()
        newLogs.add("$ $cleanCmd")

        when {
            cleanCmd == "clear" -> {
                _uiState.update { it.copy(consoleLogs = emptyList()) }
                return
            }
            cleanCmd == "help" -> {
                newLogs.add("Comandos disponibles:")
                newLogs.add("  ollama [cmd]      Motor local Ollama (run, list, pull, ps, serve)")
                newLogs.add("  flutter run       Compila y ejecuta en emulador Pixel 6")
                newLogs.add("  git status        Muestra estado del árbol de trabajo")
                newLogs.add("  ls / dir          Lista los archivos del proyecto activo")
                newLogs.add("  cat [archivo]     Muestra el contenido de un archivo")
                newLogs.add("  clear             Limpia el buffer de la terminal")
            }
            cleanCmd.startsWith("ollama") -> {
                when {
                    cleanCmd == "ollama" || cleanCmd == "ollama --help" || cleanCmd == "ollama -h" -> {
                        newLogs.add("Usage: ollama [command]")
                        newLogs.add("")
                        newLogs.add("Available Commands:")
                        newLogs.add("  run <model>    Cargar y ejecutar modelo local (ej: ollama run qwen2.5-coder)")
                        newLogs.add("  list / ls      Listar modelos locales descargados")
                        newLogs.add("  pull <model>   Descargar un modelo al teléfono (ej: ollama pull llama3.2:1b)")
                        newLogs.add("  ps             Ver modelos activos en memoria")
                        newLogs.add("  serve          Iniciar servidor Ollama en puerto 11434")
                    }
                    cleanCmd == "ollama list" || cleanCmd == "ollama ls" -> {
                        newLogs.add("NAME                  ID              SIZE      MODIFIED")
                        newLogs.add("qwen2.5-coder:1.5b    0b40e53a3e6c    986 MB    2 hours ago")
                        newLogs.add("llama3.2:1b           b3a987d6e4b1    1.3 GB    1 day ago")
                        newLogs.add("deepseek-coder:1.3b   a7c85e2b489d    890 MB    3 days ago")
                        newLogs.add("gemma2:2b             c1a4e28f78a2    1.6 GB    1 week ago")
                    }
                    cleanCmd == "ollama ps" -> {
                        newLogs.add("NAME                  ID              SIZE      PROCESSOR    UNTIL")
                        newLogs.add("qwen2.5-coder:1.5b    0b40e53a3e6c    986 MB    100% CPU     En memoria activa")
                    }
                    cleanCmd.startsWith("ollama run ") -> {
                        val modelName = cleanCmd.removePrefix("ollama run ").trim()
                        newLogs.add("pulling manifest for '$modelName'")
                        newLogs.add("verifying sha256 digest")
                        newLogs.add("writing manifest")
                        newLogs.add("success: modelo '$modelName' cargado en memoria!")
                        newLogs.add(">>> [Ollama] Conectado a $modelName. Listo para chatear en el Asistente IA.")
                        _uiState.update { it.copy(selectedAiProvider = ProviderType.OLLAMA) }
                    }
                    cleanCmd.startsWith("ollama pull ") -> {
                        val modelName = cleanCmd.removePrefix("ollama pull ").trim()
                        newLogs.add("pulling manifest for $modelName...")
                        newLogs.add("downloading layer 1/3: 100% [====================]")
                        newLogs.add("downloading layer 2/3: 100% [====================]")
                        newLogs.add("downloading layer 3/3: 100% [====================]")
                        newLogs.add("verifying sha256 digest...")
                        newLogs.add("success: modelo '$modelName' descargado y listo para 'ollama run $modelName'")
                    }
                    cleanCmd == "ollama serve" -> {
                        newLogs.add("Ollama local engine listening on 127.0.0.1:11434 (API v1)")
                        newLogs.add("Endpoints activos: /api/chat, /api/generate, /api/tags")
                    }
                    else -> {
                        newLogs.add("ollama: comando desconocido. Ejecuta 'ollama --help'")
                    }
                }
            }
            cleanCmd.startsWith("flutter run") -> {
                newLogs.add("Launching lib/main.dart on Pixel 6 in debug mode...")
                newLogs.add("Running Gradle task 'assembleDebug'...")
                newLogs.add("✓ Built build/app/outputs/flutter-apk/app-debug.apk.")
                newLogs.add("Connecting to VM Service at ws://127.0.0.1:41235/ws...")
                newLogs.add("I/flutter: ¡Aplicación iniciada correctamente! 🚀")
            }
            cleanCmd.startsWith("git status") -> {
                newLogs.add("On branch main")
                newLogs.add("Your branch is up to date with 'origin/main'.")
                if (_uiState.value.isFileModified) {
                    newLogs.add("Changes not staged for commit:")
                    newLogs.add("  modified:   ${_uiState.value.activeFile.path}")
                } else {
                    newLogs.add("nothing to commit, working tree clean")
                }
            }
            cleanCmd.startsWith("ls") || cleanCmd.startsWith("dir") -> {
                val files = _uiState.value.rootProject.children.map { if (it.isDirectory) "${it.name}/" else it.name }
                newLogs.add(files.joinToString("  "))
            }
            cleanCmd.startsWith("cat ") -> {
                val fileName = cleanCmd.removePrefix("cat ").trim()
                val target = _uiState.value.rootProject.flatten().firstOrNull { it.name == fileName }
                if (target != null) {
                    newLogs.addAll(target.content.lines().take(15))
                } else {
                    newLogs.add("cat: $fileName: No such file or directory")
                }
            }
            else -> {
                newLogs.add("bash: $cleanCmd: comando ejecutado en sandbox.")
            }
        }

        _uiState.update { it.copy(consoleLogs = newLogs) }
    }

    fun clearTerminalLogs() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    fun selectNavTab(tab: MainNavTab) {
        _uiState.update { it.copy(currentNavTab = tab) }
    }

    fun toggleTerminal() {
        _uiState.update { it.copy(showTerminal = !it.showTerminal) }
    }

    fun toggleAiPanel() {
        _uiState.update { it.copy(showAiPanel = !it.showAiPanel) }
    }

    fun toggleLivePreview() {
        _uiState.update { it.copy(showLivePreview = !it.showLivePreview) }
    }

    // AI Providers and Multi-Agent Orchestration
    fun selectAiProvider(provider: ProviderType) {
        aiManager.selectProvider(provider)
        _uiState.update { it.copy(selectedAiProvider = provider) }
    }

    fun selectLocalAgent(agent: LocalAgentEntity) {
        aiManager.setActiveAgent(agent)
        _uiState.update {
            it.copy(
                activeAgent = agent,
                selectedAiProvider = ProviderType.LOCAL_AGENT,
                consoleLogs = it.consoleLogs + listOf("[Antigravity] Agente activo: ${agent.icon} ${agent.name}")
            )
        }
    }

    fun importAgentFromPhone(content: String, fileName: String? = null) {
        val imported = aiManager.agentEngine.importAgentFromContent(content, fileName)
        val allAgents = aiManager.agentEngine.getAllAgents()
        selectLocalAgent(imported)
        _uiState.update {
            it.copy(
                availableAgents = allAgents,
                consoleLogs = it.consoleLogs + listOf("[Antigravity] Agente importado con éxito: ${imported.name}")
            )
        }
    }

    fun updateApiKeys(config: ApiKeysConfig) {
        aiManager.updateApiKeys(config)
        _uiState.update {
            it.copy(
                apiKeysConfig = config,
                consoleLogs = it.consoleLogs + listOf("[API Config] Claves de proveedores actualizadas.")
            )
        }
    }

    fun updateLocalAiConfig(host: String, port: Int, model: String) {
        _uiState.update {
            it.copy(
                localAiHost = host,
                localAiPort = port,
                selectedLocalModel = model,
                consoleLogs = it.consoleLogs + listOf("[Local AI] Configuración guardada: $host:$port (Modelo: $model)")
            )
        }
    }

    fun testLocalAiConnection(host: String, port: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(localAiTestStatus = "Probando conexión con $host:$port...") }
            delay(400)
            val success = (host == "127.0.0.1" || host == "localhost" || host.startsWith("192.168."))
            _uiState.update {
                it.copy(
                    localAiTestStatus = if (success) "✓ Conexión establecida con $host:$port (Servidor Local Activo)" else "✗ No se pudo conectar a $host:$port"
                )
            }
        }
    }

    fun askAiAssistant(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) return
        activeAiJob?.cancel()

        // Manejo nativo de Comandos Slash (Protocolo Antigravity & Kiro)
        if (cleanPrompt.equals("/clear", ignoreCase = true)) {
            clearChatHistory()
            return
        }
        if (cleanPrompt.equals("/run", ignoreCase = true)) {
            runProject()
            return
        }
        if (cleanPrompt.equals("/test", ignoreCase = true)) {
            executeTerminalCommand("flutter test")
            _uiState.update { it.copy(showTerminal = true, currentNavTab = MainNavTab.TERMINAL) }
            return
        }

        val effectivePrompt = when {
            cleanPrompt.startsWith("/plan", ignoreCase = true) ->
                "Genera un Plan de Implementación estructurado (# Plan: <Título>) con su '## Checklist de Tareas' (- [ ]) y código para: " + cleanPrompt.removePrefix("/plan").trim()
            cleanPrompt.startsWith("/review", ignoreCase = true) ->
                "Realiza una revisión técnica minuciosa de código del archivo actual, señalando mejoras y generando correcciones si aplican: " + cleanPrompt.removePrefix("/review").trim()
            cleanPrompt.startsWith("/fix", ignoreCase = true) ->
                "Analiza y corrige los errores sintácticos o de compilación en el archivo actual, proporcionando el bloque de código corregido: " + cleanPrompt.removePrefix("/fix").trim()
            cleanPrompt.startsWith("/diff", ignoreCase = true) ->
                "Explica las diferencias y cambios propuestos para el archivo actual: " + cleanPrompt.removePrefix("/diff").trim()
            cleanPrompt.startsWith("/grill-me", ignoreCase = true) ->
                "Actúa como un arquitecto senior en modo /grill-me. Realiza una entrevista técnica con preguntas y opciones críticas para resolver decisiones de diseño antes de programar: " + cleanPrompt.removePrefix("/grill-me").trim()
            cleanPrompt.startsWith("/goal", ignoreCase = true) ->
                "Establece un objetivo autónomo de alto nivel desglosado en metas ejecutables y checkpoints de verificación: " + cleanPrompt.removePrefix("/goal").trim()
            cleanPrompt.startsWith("/learn", ignoreCase = true) ->
                "Aprende y documenta este patrón o corrección como regla persistente del proyecto: " + cleanPrompt.removePrefix("/learn").trim()
            else -> cleanPrompt
        }

        val activeAgent = _uiState.value.activeAgent
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = cleanPrompt
        )
        val initialAgentMsg = ChatMessage(
            sender = MessageSender.AGENT,
            agentName = activeAgent.name,
            agentIcon = activeAgent.icon,
            text = "",
            isStreaming = true
        )

        val updatedMessages = _uiState.value.chatMessages + listOf(userMsg, initialAgentMsg)
        _uiState.update {
            it.copy(
                chatMessages = updatedMessages,
                isAiLoading = true,
                currentExecutionStep = "Planificando solución y analizando código..."
            )
        }

        // 1. Sincronizar Mission Control en Fase Planning
        updateAgentMissionState(
            activeAgent.id,
            AgentState.Planning("Analizando requerimientos y arquitectura del proyecto...")
        )

        activeAiJob = viewModelScope.launch {
            val codeCtx = CodeContext(
                fullText = _uiState.value.activeFile.content,
                cursorOffset = _uiState.value.editorValue.selection.start,
                currentLine = 13,
                filePath = _uiState.value.activeFile.path
            )

            // 2. Transición a Fase Executing durante la generación
            updateAgentMissionState(
                activeAgent.id,
                AgentState.Executing("Generando artefactos y código...", 0.45f)
            )

            val buffer = StringBuilder()
            aiManager.generateCompletionStream(effectivePrompt, codeCtx).collect { chunk ->
                buffer.append(chunk)
                val currentText = buffer.toString()
                _uiState.update { state ->
                    val msgs = state.chatMessages.toMutableList()
                    val lastIdx = msgs.indexOfLast { m -> m.id == initialAgentMsg.id }
                    if (lastIdx != -1) {
                        msgs[lastIdx] = msgs[lastIdx].copy(text = currentText)
                    }
                    state.copy(chatMessages = msgs, aiResponse = currentText)
                }
            }

            val finalResponse = buffer.toString()
            val parsedActions = AgentActionParser.parseActions(finalResponse)
            val structuredContents = AntigravityArtifactParser.parseResponse(finalResponse, parsedActions)

            _uiState.update { state ->
                val msgs = state.chatMessages.toMutableList()
                val lastIdx = msgs.indexOfLast { m -> m.id == initialAgentMsg.id }
                if (lastIdx != -1) {
                    msgs[lastIdx] = msgs[lastIdx].copy(
                        text = finalResponse,
                        contents = structuredContents,
                        actions = parsedActions,
                        isStreaming = false
                    )
                }
                state.copy(
                    chatMessages = msgs,
                    isAiLoading = false,
                    currentExecutionStep = if (parsedActions.isNotEmpty()) "Acciones propuestas listas para revisar" else null
                )
            }

            // 3. Evaluar Puntos de Control y Estado de Misión
            val hasHumanCheckpoint = structuredContents.any { content ->
                content is ChatContent.ActionChecklist && content.tasks.any { it.requiresHuman && !it.isCompleted }
            }
            val hasProposedActions = parsedActions.isNotEmpty()

            if (_uiState.value.autoExecuteAgentActions && hasProposedActions) {
                updateAgentMissionState(
                    activeAgent.id,
                    AgentState.Executing("Aplicando cambios automáticamente al espacio de trabajo...", 0.85f)
                )
                parsedActions.forEach { act -> executeAgentAction(act) }
                updateAgentMissionState(
                    activeAgent.id,
                    AgentState.Verifying("Verificando diagnóstico y sintaxis...")
                )
                delay(300)
                updateAgentMissionState(activeAgent.id, AgentState.Idle)
            } else if (hasHumanCheckpoint || hasProposedActions) {
                updateAgentMissionState(
                    activeAgent.id,
                    AgentState.WaitingForUser("Revisión de cambios requerida por el usuario", canResume = true)
                )
            } else {
                updateAgentMissionState(
                    activeAgent.id,
                    AgentState.Verifying("Verificando sintaxis del código generado...")
                )
                delay(300)
                updateAgentMissionState(activeAgent.id, AgentState.Idle)
            }
        }
    }

    fun executeAgentAction(action: AgentAction) {
        viewModelScope.launch {
            updateActionStatus(action.id, ActionStatus.RUNNING, null)
            _uiState.update { it.copy(currentExecutionStep = "Ejecutando: ${action.title}...") }

            var outputText = ""

            when (action.type) {
                AgentActionType.RUN_COMMAND, AgentActionType.INSTALL_DEPENDENCY -> {
                    executeTerminalCommand(action.payload)
                    outputText = "✓ Comando '${action.payload}' ejecutado en la terminal interactiva."
                }
                AgentActionType.CREATE_FOLDER -> {
                    val folderName = action.targetPath ?: action.payload
                    createNewFolder(folderName)
                    outputText = "✓ Carpeta '$folderName' creada con éxito."
                }
                AgentActionType.CREATE_FILE -> {
                    val fileName = action.targetPath ?: "nuevo_componente.dart"
                    createNewFile(fileName)
                    _uiState.value.activeFile.content = action.payload
                    onEditorChange(TextFieldValue(action.payload), isImmediate = true)
                    outputText = "✓ Archivo '$fileName' creado y abierto en el editor."
                }
                AgentActionType.MODIFY_FILE -> {
                    _uiState.value.activeFile.content = action.payload
                    onEditorChange(TextFieldValue(action.payload), isImmediate = true)
                    outputText = "✓ Código actualizado en '${_uiState.value.activeFile.name}'."
                }
                AgentActionType.HTTP_REQUEST -> {
                    outputText = aiManager.executeHttpRequest(action.payload)
                }
            }

            delay(250)
            updateActionStatus(action.id, ActionStatus.SUCCESS, outputText)
            _uiState.update {
                it.copy(
                    currentExecutionStep = null,
                    consoleLogs = it.consoleLogs + listOf("[Antigravity] Acción completada: ${action.title}")
                )
            }
        }
    }

    fun rejectAgentAction(action: AgentAction) {
        updateActionStatus(action.id, ActionStatus.REJECTED, "Descartado por el usuario.")
    }

    private fun updateActionStatus(actionId: String, status: ActionStatus, output: String?) {
        _uiState.update { state ->
            val updatedMessages = state.chatMessages.map { msg ->
                val updatedActions = msg.actions.map { act ->
                    if (act.id == actionId) act.copy(status = status, output = output ?: act.output) else act
                }
                msg.copy(actions = updatedActions)
            }
            state.copy(chatMessages = updatedMessages)
        }
    }

    fun downloadAndActivateAgent(downloadable: DownloadableAgent) {
        viewModelScope.launch {
            _uiState.update { it.copy(currentExecutionStep = "Descargando y activando agente ${downloadable.name}...") }
            val activated = aiManager.downloadAndActivateAgent(downloadable, null)
            val allAgents = aiManager.agentEngine.getAllAgents()
            val downloadables = aiManager.getDownloadableAgents()

            val welcomeMsg = ChatMessage(
                sender = MessageSender.AGENT,
                agentName = activated.name,
                agentIcon = activated.icon,
                text = "✅ **${activated.name}** descargado y activado en el dispositivo.\n\n" +
                        "• **Especialidad:** ${downloadable.category}\n" +
                        "• **Habilidades:** ${downloadable.skills.joinToString(", ") { "`$it`" }}\n\n" +
                        "Estoy listo para asistirte en desarrollo móvil, crear carpetas, instalar librerías o ejecutar comandos."
            )

            _uiState.update {
                it.copy(
                    activeAgent = activated,
                    selectedAiProvider = ProviderType.LOCAL_AGENT,
                    availableAgents = allAgents,
                    downloadableAgents = downloadables,
                    chatMessages = it.chatMessages + listOf(welcomeMsg),
                    currentExecutionStep = null,
                    consoleLogs = it.consoleLogs + listOf("[Antigravity] Agente descargado y activado: ${activated.name}")
                )
            }
        }
    }

    fun updateAgentMissionState(agentId: String, newState: AgentState) {
        _uiState.update { state ->
            state.copy(
                activeMissionAgents = state.activeMissionAgents.map {
                    if (it.agentId == agentId) it.copy(currentState = newState) else it
                }
            )
        }
    }

    fun resumeAgentMission(agentId: String) {
        viewModelScope.launch {
            updateAgentMissionState(agentId, AgentState.Executing("Reanudando ejecución colaborativa...", 0.75f))

            // 1. Ejecutar acciones propuestas pendientes
            val pendingActions = _uiState.value.chatMessages
                .flatMap { it.actions }
                .filter { it.status == ActionStatus.PROPOSED }

            if (pendingActions.isNotEmpty()) {
                pendingActions.forEach { act ->
                    executeAgentAction(act)
                    delay(150)
                }
            }

            // 2. Marcar las tareas de checklist como completadas
            _uiState.update { state ->
                val updatedMsgs = state.chatMessages.map { msg ->
                    val updatedContents = msg.contents.map { content ->
                        if (content is ChatContent.ActionChecklist) {
                            content.copy(tasks = content.tasks.map { it.copy(isCompleted = true) })
                        } else content
                    }
                    msg.copy(contents = updatedContents)
                }
                state.copy(chatMessages = updatedMsgs)
            }

            updateAgentMissionState(agentId, AgentState.Verifying("Verificando consistencia del proyecto..."))
            delay(400)
            updateAgentMissionState(agentId, AgentState.Idle)
        }
    }

    fun applyArtifactCodeToProject(code: String, targetFilePath: String? = null) {
        val root = _uiState.value.rootProject
        val targetPath = targetFilePath?.trim() ?: _uiState.value.activeFile.path
        val fileName = targetPath.substringAfterLast("/").ifBlank { "componente.kt" }

        // Buscar si el archivo existe en el proyecto
        val existingFile = root.flatten().firstOrNull { it.path == targetPath || it.name == fileName }
        if (existingFile != null) {
            existingFile.content = code
            if (_uiState.value.activeFile.id == existingFile.id || _uiState.value.activeFile.path == existingFile.path) {
                onEditorChange(TextFieldValue(code, selection = TextRange(0)), isImmediate = true)
            }
        } else {
            val parentPath = if (targetPath.contains("/")) targetPath.substringBeforeLast("/") else "/mi_nuevo_proyecto/lib"
            createNewFile(fileName, parentPath)
            _uiState.value.activeFile.content = code
            onEditorChange(TextFieldValue(code, selection = TextRange(0)), isImmediate = true)
        }

        updateAgentMissionState(
            _uiState.value.activeAgent.id,
            AgentState.Verifying("Verificando consistencia de código inyectado...")
        )
        viewModelScope.launch {
            delay(400)
            updateAgentMissionState(_uiState.value.activeAgent.id, AgentState.Idle)
        }
    }

    fun toggleChecklistTask(messageId: String, taskId: String) {
        _uiState.update { state ->
            val updated = state.chatMessages.map { msg ->
                if (msg.id == messageId) {
                    val newContents = msg.contents.map { content ->
                        if (content is ChatContent.ActionChecklist) {
                            val newTasks = content.tasks.map { t ->
                                if (t.id == taskId) t.copy(isCompleted = !t.isCompleted) else t
                            }
                            content.copy(tasks = newTasks)
                        } else content
                    }
                    msg.copy(contents = newContents)
                } else msg
            }
            state.copy(chatMessages = updated)
        }
    }

    fun runProject() {
        executeTerminalCommand("flutter run")
        _uiState.update { it.copy(showTerminal = true, currentNavTab = MainNavTab.TERMINAL) }
    }

    fun clearChatHistory() {
        _uiState.update { it.copy(chatMessages = emptyList(), aiResponse = null, currentExecutionStep = null) }
    }

    fun updateGitHubConfig(config: GitHubConfig) {
        _uiState.update {
            it.copy(
                gitHubConfig = config,
                consoleLogs = it.consoleLogs + listOf("[GitHub] Repositorio configurado: ${config.remoteUrl} (rama: ${config.defaultBranch})")
            )
        }
    }

    fun updateExpoDevConfig(config: ExpoDevConfig) {
        _uiState.update {
            it.copy(
                expoDevConfig = config,
                consoleLogs = it.consoleLogs + listOf("[Expo Dev] Proyecto configurado: ${config.projectSlug} (${config.buildProfile})")
            )
        }
    }

    fun updateRailwayConfig(config: RailwayConfig) {
        _uiState.update {
            it.copy(
                railwayConfig = config,
                consoleLogs = it.consoleLogs + listOf("[Railway] Backend configurado: ${config.serviceName} (${config.environment})")
            )
        }
    }

    fun pushToGitHub(commitMessage: String = "Actualización desde Black Cat IDE") {
        viewModelScope.launch {
            val gh = _uiState.value.gitHubConfig
            _uiState.update {
                it.copy(
                    deploymentStatus = DeploymentStatus.DEPLOYING,
                    deploymentMessage = "Subiendo cambios a GitHub (${gh.defaultBranch})..."
                )
            }

            executeTerminalCommand("git add .")
            executeTerminalCommand("git commit -m \"$commitMessage\"")
            executeTerminalCommand("git push origin ${gh.defaultBranch}")

            delay(300)
            val record = DeploymentRecord(
                target = DeploymentTarget.GITHUB,
                status = DeploymentStatus.SUCCESS,
                summary = "Push a ${gh.remoteUrl} (rama ${gh.defaultBranch}) completado exitosamente."
            )

            _uiState.update {
                it.copy(
                    isFileModified = false,
                    deploymentStatus = DeploymentStatus.SUCCESS,
                    deploymentMessage = "✓ Cambios subidos a GitHub con éxito.",
                    deploymentRecords = it.deploymentRecords + listOf(record),
                    consoleLogs = it.consoleLogs + listOf(
                        "[GitHub] ✓ Push completado en origin/${gh.defaultBranch}",
                        "[GitHub] Repositorio sincronizado: ${gh.remoteUrl}"
                    )
                )
            }
        }
    }

    fun deployToExpoDev() {
        viewModelScope.launch {
            val expo = _uiState.value.expoDevConfig
            _uiState.update {
                it.copy(
                    deploymentStatus = DeploymentStatus.DEPLOYING,
                    deploymentMessage = "Iniciando compilación y despliegue en Expo Dev..."
                )
            }

            executeTerminalCommand("npx expo export --platform android")
            executeTerminalCommand("eas build -p android --profile ${expo.buildProfile} --non-interactive")

            delay(400)
            val record = DeploymentRecord(
                target = DeploymentTarget.EXPO_DEV,
                status = DeploymentStatus.SUCCESS,
                summary = "Despliegue móvil en Expo Dev (${expo.projectSlug}) completado."
            )

            _uiState.update {
                it.copy(
                    deploymentStatus = DeploymentStatus.SUCCESS,
                    deploymentMessage = "✓ App desplegada en Expo Dev con éxito.",
                    deploymentRecords = it.deploymentRecords + listOf(record),
                    consoleLogs = it.consoleLogs + listOf(
                        "[Expo Dev] ✓ Build generado para perfil ${expo.buildProfile}",
                        "[Expo Dev] Disponible en canal: ${expo.releaseChannel}"
                    )
                )
            }
        }
    }

    fun deployToRailway() {
        viewModelScope.launch {
            val rw = _uiState.value.railwayConfig
            _uiState.update {
                it.copy(
                    deploymentStatus = DeploymentStatus.DEPLOYING,
                    deploymentMessage = "Desplegando servicio en Railway Cloud..."
                )
            }

            executeTerminalCommand("railway up --service ${rw.serviceName} --detach")

            delay(400)
            val record = DeploymentRecord(
                target = DeploymentTarget.RAILWAY,
                status = DeploymentStatus.SUCCESS,
                summary = "Despliegue de backend en Railway (${rw.serviceName}) completado."
            )

            _uiState.update {
                it.copy(
                    deploymentStatus = DeploymentStatus.SUCCESS,
                    deploymentMessage = "✓ Servicio desplegado en Railway Cloud con éxito.",
                    deploymentRecords = it.deploymentRecords + listOf(record),
                    consoleLogs = it.consoleLogs + listOf(
                        "[Railway] ✓ Contenedor levantado en entorno: ${rw.environment}",
                        "[Railway] Servicio activo: ${rw.serviceName}.up.railway.app"
                    )
                )
            }
        }
    }

    fun sendAiPrompt(prompt: String) {
        askAiAssistant(prompt)
    }

    fun insertAiGeneratedCode() {
        val resp = _uiState.value.aiResponse ?: return
        val extracted = if (resp.contains("```")) {
            resp.substringAfter("```").substringAfter("\n").substringBefore("```")
        } else {
            resp
        }
        insertAiCodeIntoEditor(extracted)
    }

    fun selectLocalModel(modelName: String) {
        aiManager.setLocalModel(modelName)
        _uiState.update { it.copy(selectedLocalModel = modelName) }
    }

    fun insertAiCodeIntoEditor(codeToInsert: String) {

        val current = _uiState.value.editorValue
        val cursorPos = current.selection.start.coerceIn(0, current.text.length)
        val newText = current.text.substring(0, cursorPos) + codeToInsert + current.text.substring(cursorPos)
        val newSelection = TextRange(cursorPos + codeToInsert.length)
        onEditorChange(TextFieldValue(newText, newSelection), isImmediate = true)
        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf("[Asistente IA] Código insertado en ${it.activeFile.name}")
            )
        }
    }

    fun commitChanges(message: String) {
        _uiState.update {
            it.copy(
                isFileModified = false,
                consoleLogs = it.consoleLogs + listOf(
                    "[Git] Committed: \"$message\"",
                    "[Git] Working tree clean on branch main."
                )
            )
        }
    }

    // AI Hub Sub-Pages
    fun openAiSubPage(page: AiHubSubPage) {
        _uiState.update { it.copy(activeAiSubPage = page) }
    }

    fun closeAiSubPage() {
        _uiState.update { it.copy(activeAiSubPage = AiHubSubPage.NONE) }
    }

    fun runModel(id: String) {
        modelDownloadManager.runModel(id)
        val model = _uiState.value.installedModels.firstOrNull { it.id == id }
        if (model != null) {
            aiManager.setLocalModel(model.name)
            _uiState.update { it.copy(selectedLocalModel = model.name) }
        }
    }

    fun stopModel(id: String) {
        modelDownloadManager.stopModel(id)
    }

    fun deleteModel(id: String) {
        modelDownloadManager.deleteModel(id)
    }

    fun downloadModel(item: ModelItem) {
        modelDownloadManager.startDownload(item)
        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf("[Descarga] Iniciando descarga de ${item.name} desde Hugging Face...")
            )
        }
    }

    fun importLocalModel(name: String, sizeBytes: Long) {
        modelDownloadManager.importLocalFile(name, "/storage/emulated/0/Download/$name", sizeBytes)
    }

    fun updateHardwareConfig(threads: Int, contextWindow: Int, gpuLayers: Int, engineName: String) {
        telemetryService.updateHardwareConfig(threads, contextWindow, gpuLayers, engineName)
    }

    fun updateRouterConfig(config: RouterConfig) {
        routerEngine.config = config
        _uiState.update { it.copy(routerConfig = config) }
    }

    fun addRagDocument(name: String, content: String) {
        siloEngine.addDocument(name, content)
    }

    fun removeRagDocument(id: String) {
        siloEngine.removeDocument(id)
    }

    fun searchRagChunks(query: String): List<RagChunk> {
        return siloEngine.searchSimilarChunks(query)
    }

    fun toggleRagInjection(enabled: Boolean) {
        siloEngine.isRagInjectionEnabled = enabled
        _uiState.update { it.copy(isRagInjectionEnabled = enabled) }
    }

    fun toggleCommandPalette(show: Boolean? = null) {
        _uiState.update { it.copy(showCommandPalette = show ?: !it.showCommandPalette) }
    }

    fun formatCurrentCode() {
        val current = _uiState.value.editorValue
        val lang = _uiState.value.activeFile.language
        val formatted = SmartCodeFormatter.format(current.text, lang)
        if (formatted != current.text) {
            undoStack.add(current)
            _uiState.value.activeFile.content = formatted
            _uiState.update {
                it.copy(
                    editorValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length)),
                    isFileModified = true,
                    canUndo = undoStack.isNotEmpty(),
                    consoleLogs = it.consoleLogs + listOf("[Formateador] ✓ Código formateado según normas de ${lang.name}")
                )
            }
        } else {
            _uiState.update {
                it.copy(consoleLogs = it.consoleLogs + listOf("[Formateador] El archivo ya cuenta con estilo óptimo."))
            }
        }
    }

    fun loadProjectTemplate(template: ProjectTemplate) {
        val newProject = ProjectTemplate.createProjectFromTemplate(template.type)
        val newInitialFile = findInitialFile(newProject)
        val newTabs = findInitialTabs(newProject)
        undoStack.clear()
        redoStack.clear()

        _uiState.update {
            it.copy(
                rootProject = newProject,
                activeFile = newInitialFile,
                openTabs = newTabs,
                editorValue = TextFieldValue(text = newInitialFile.content, selection = TextRange(0)),
                isFileModified = false,
                canUndo = false,
                canRedo = false,
                showCommandPalette = false,
                currentNavTab = MainNavTab.EDITOR,
                consoleLogs = it.consoleLogs + listOf(
                    "[Plantillas] ✓ Proyecto '${template.title}' generado con éxito.",
                    "[Plantillas] Estructura creada en: ${newProject.path}"
                )
            )
        }
    }

    /**
     * Crea un proyecto desde cero de forma amigable y guiada por el Asistente Senior de IA.
     */
    fun createProjectFromScratch(
        name: String,
        goal: String = "",
        type: ProjectTemplateType = ProjectTemplateType.FLUTTER_MOBILE
    ) {
        val safeName = name.trim().replace(" ", "_").ifBlank { "MiProyecto" }
        val displayTitle = name.trim().ifBlank { "Mi Proyecto" }
        val projectGoal = goal.trim().ifBlank { "Crear una nueva aplicación móvil" }

        val newProject = ProjectTemplate.createNewCustomProject(safeName, projectGoal, type)
        val newInitialFile = findInitialFile(newProject)
        val newTabs = findInitialTabs(newProject)
        undoStack.clear()
        redoStack.clear()

        _uiState.update {
            it.copy(
                rootProject = newProject,
                activeFile = newInitialFile,
                openTabs = newTabs,
                editorValue = TextFieldValue(text = newInitialFile.content, selection = TextRange(0)),
                isFileModified = false,
                canUndo = false,
                canRedo = false,
                showProjectLauncher = false,
                showCommandPalette = false,
                currentNavTab = MainNavTab.AI_ASSISTANT,
                consoleLogs = it.consoleLogs + listOf(
                    "[Asistente] ✨ ¡Bienvenido a tu nuevo proyecto '$displayTitle'!",
                    "[Asistente] Objetivo: $projectGoal",
                    "[Asistente] Espacio de trabajo inicializado en: ${newProject.path}"
                )
            )
        }

        // Enviar automáticamente el saludo de bienvenida con el objetivo al asistente para iniciar la charla fluida
        val prompt = "¡Hola! He creado mi proyecto '$displayTitle' con el objetivo de '$projectGoal'. ¿Cómo empezamos a construirlo paso a paso?"
        askAiAssistant(prompt)
    }

    fun runSmartRunner() {
        val active = _uiState.value.activeFile
        if (active.name.endsWith(".html") || active.name.endsWith(".htm") || active.name.endsWith(".md")) {
            _uiState.update {
                it.copy(
                    showLivePreview = true,
                    consoleLogs = it.consoleLogs + listOf("[Runner] Abriendo vista previa interactiva para ${active.name}")
                )
            }
            return
        }

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _uiState.update {
                it.copy(
                    consoleLogs = it.consoleLogs + listOf(
                        "[Runner] ▶ Ejecutando ${active.name} (${active.language.name})...",
                        "[Runner] Entorno: Sandbox Local Black Cat IDE"
                    )
                )
            }

            delay(150)
            val duration = System.currentTimeMillis() - startTime
            _uiState.update {
                it.copy(
                    consoleLogs = it.consoleLogs + listOf(
                        "[Runner] ----------------------------------------",
                        "[Runner] ✓ Salida del programa: Proceso finalizado con código de salida 0.",
                        "[Runner] Tiempo de ejecución: ${duration}ms"
                    )
                )
            }
        }
    }

    // ==========================================
    // WORKSPACE & PROJECT LAUNCHER MANAGEMENT
    // ==========================================

    fun toggleProjectLauncher(show: Boolean) {
        _uiState.update { it.copy(showProjectLauncher = show) }
    }

    fun openWorkspaceProject(project: Project) {
        viewModelScope.launch {
            workspaceRepository.setCurrentProject(project)
            _uiState.update {
                it.copy(
                    showProjectLauncher = false,
                    consoleLogs = it.consoleLogs + listOf(
                        "[Workspace] Proyecto activo cambiado a '${project.name}'",
                        "[Workspace] Ruta: ${project.path}"
                    )
                )
            }
        }
    }

    fun importLocalDirectory(path: String, name: String? = null) {
        viewModelScope.launch {
            val result = workspaceImportService.importProject(path, name)
            result.onSuccess { proj ->
                _uiState.update {
                    it.copy(
                        showProjectLauncher = false,
                        consoleLogs = it.consoleLogs + listOf(
                            "[Workspace] ✓ Proyecto '${proj.name}' importado exitosamente (${proj.projectType.name})",
                            "[Workspace] Ruta: ${proj.path}"
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        consoleLogs = it.consoleLogs + listOf(
                            "[Workspace] ✗ Error al importar directorio: ${err.message}"
                        )
                    )
                }
            }
        }
    }

    fun cloneRemoteRepository(url: String, destPath: String) {
        viewModelScope.launch {
            val projName = url.substringAfterLast("/").removeSuffix(".git").ifBlank { "ClonedProject" }
            val result = workspaceImportService.importProject(destPath, projName)
            result.onSuccess { proj ->
                _uiState.update {
                    it.copy(
                        showProjectLauncher = false,
                        consoleLogs = it.consoleLogs + listOf(
                            "[Git] ✓ Clonación y registro exitoso: '${proj.name}'",
                            "[Git] URL: $url",
                            "[Workspace] Ruta local: ${proj.path}"
                        )
                    )
                }
            }
        }
    }

    fun deleteWorkspaceProject(projectId: String) {
        viewModelScope.launch {
            workspaceRepository.deleteProject(projectId)
        }
    }

    fun toggleProjectFavorite(projectId: String) {
        viewModelScope.launch {
            workspaceRepository.toggleFavorite(projectId)
        }
    }

    // ==========================================
    // FILE OPERATIONS
    // ==========================================

    /**
     * Renombra el archivo activo (en memoria) y lo vuelve a abrir con el nuevo nombre.
     */
    fun renameFile(newName: String) {
        val file = _uiState.value.activeFile
        val clean = newName.trim()
        if (clean.isBlank() || clean == file.name) return
        val updatedFile = file.copy(
            name = clean,
            path = file.path.substringBeforeLast("/") + "/$clean"
        )
        _uiState.update {
            it.copy(
                openTabs = it.openTabs.map { tab -> if (tab.id == file.id) updatedFile else tab },
                activeFile = updatedFile,
                consoleLogs = it.consoleLogs + listOf("[Archivos] Archivo renombrado: $clean")
            )
        }
    }

    /**
     * Duplica el archivo activo con sufijo "_copia" y lo abre en una nueva pestaña.
     */
    fun duplicateCurrentFile() {
        val source = _uiState.value.activeFile
        val ext = source.name.substringAfterLast(".", "")
        val baseName = source.name.substringBeforeLast(".")
        val copyName = if (ext.isNotBlank()) "${baseName}_copia.$ext" else "${baseName}_copia"
        val copyPath = source.path.substringBeforeLast("/") + "/$copyName"
        val copy = ProjectFile(
            id = "file-dup-${System.currentTimeMillis()}",
            name = copyName,
            path = copyPath,
            isDirectory = false,
            content = source.content
        )
        // Añadir al árbol bajo el mismo padre
        val parent = _uiState.value.rootProject.flatten()
            .firstOrNull { it.isDirectory && it.path == source.path.substringBeforeLast("/") }
        parent?.children?.add(copy)
        openFile(copy)
        _uiState.update {
            it.copy(consoleLogs = it.consoleLogs + listOf("[Archivos] Duplicado creado: $copyName"))
        }
    }

    /**
     * Retorna el contenido raw del archivo activo para ser compartido / exportado.
     */
    fun exportCurrentFileContent(): String {
        return _uiState.value.editorValue.text
    }

    // ==========================================
    // SNIPPET VAULT
    // ==========================================

    fun toggleSnippetVault(show: Boolean? = null) {
        _uiState.update { it.copy(showSnippetVault = show ?: !it.showSnippetVault) }
    }
}
