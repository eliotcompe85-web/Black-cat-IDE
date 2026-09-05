package com.ide.mobile.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ide.mobile.core.model.AnalysisRequest
import com.ide.mobile.core.model.AnalysisResponse
import com.ide.mobile.core.model.AnalysisStatus
import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.DiagnosticsState
import com.ide.mobile.core.model.ProjectFile
import com.ide.mobile.core.model.QuickFix
import com.ide.mobile.core.model.Severity
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

import com.ide.mobile.core.model.BuildProgress
import com.ide.mobile.core.model.BuildStep
import com.ide.mobile.feature.compiler.BuildPipeline
import com.ide.mobile.feature.ai.AiAssistantManager
import com.ide.mobile.feature.ai.CodeContext
import com.ide.mobile.feature.ai.ProviderType

enum class MainNavTab {
    EDITOR, FILES, SEARCH, GIT, SETTINGS
}

enum class AiHubSubPage {
    NONE,
    MODEL_MANAGEMENT,
    RUNTIME_ENGINE,
    PLUGINS_ROUTER,
    SILO_LIBRARY
}

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
    val diagnosticsState: DiagnosticsState = DiagnosticsState(),
    val buildProgress: BuildProgress = BuildProgress(),
    val showLivePreview: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showConsole: Boolean = false,
    val showDiagnosticsSheet: Boolean = false,
    val showAiAssistant: Boolean = false,
    val selectedAiProvider: ProviderType = ProviderType.LOCAL_GGUF,
    val localAiHost: String = "127.0.0.1",
    val localAiPort: Int = 11434,
    val localAiDashboardPort: Int = 8080,
    val selectedLocalModel: String = "Qwen3.5-2B-Q4_0.gguf",
    val localAiTestStatus: String? = null,
    val aiResponse: String? = null,
    val isAiLoading: Boolean = false,
    val consoleLogs: List<String> = emptyList(),
    val installedModels: List<com.ide.mobile.core.model.ModelItem> = emptyList(),
    val catalogModels: List<com.ide.mobile.core.model.ModelItem> = emptyList(),
    val runtimeMetrics: com.ide.mobile.core.model.RuntimeMetrics = com.ide.mobile.core.model.RuntimeMetrics(),
    val routerConfig: com.ide.mobile.core.model.RouterConfig = com.ide.mobile.core.model.RouterConfig(),
    val ragDocuments: List<com.ide.mobile.core.model.RagDocument> = emptyList(),
    val isRagInjectionEnabled: Boolean = true
)

@OptIn(FlowPreview::class)
class IdeViewModel : ViewModel() {

    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()

    private val sampleProject = ProjectFile.createSampleAndroidProject()
    private val initialFile = findInitialFile(sampleProject)
    private val initialTabs = findInitialTabs(sampleProject)

    private val aiManager = AiAssistantManager(ProviderType.GEMINI_API)
    private val modelDownloadManager = com.ide.mobile.feature.ai.downloader.ModelDownloadManager(viewModelScope)
    private val telemetryService = com.ide.mobile.feature.ai.runtime.DeviceTelemetryService(viewModelScope)
    private val routerEngine = com.ide.mobile.feature.ai.router.SmartInferenceRouter()
    private val siloEngine = com.ide.mobile.feature.ai.rag.SiloKnowledgeEngine()

    private var currentDocumentVersion: Long = 1L
    private var activeAnalysisJob: Job? = null

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
            consoleLogs = listOf(
                "[IDE Daemon] Mobile runtime and Dart/Flutter engine ready.",
                "[Project] Loaded 'mi_nuevo_proyecto' (Flutter 3.x / Dart 3.x).",
                "[Live AI] AI Assistant online (Gemini & Local GGUF streaming)."
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
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false,
                    diagnosticsState = it.diagnosticsState.copy(status = AnalysisStatus.ANALYZING)
                )
            }

            // Schedule background analysis
            scheduleAnalysis(
                AnalysisRequest(
                    fileId = _uiState.value.activeFile.id,
                    language = _uiState.value.activeFile.language,
                    code = newValue.text,
                    documentVersion = newVer,
                    cursorOffset = newValue.selection.start,
                    isImmediate = isImmediate
                )
            )
        } else {
            // Only cursor or selection moved: update focused issue under cursor immediately
            val cursorOffset = newValue.selection.start
            val focused = calculateFocusedIssue(cursorOffset, _uiState.value.editorValue.text, _uiState.value.diagnosticsState.issues)
            _uiState.update {
                it.copy(
                    editorValue = newValue,
                    diagnosticsState = it.diagnosticsState.copy(focusedIssue = focused)
                )
            }
        }
    }

    private fun scheduleAnalysis(request: AnalysisRequest) {
        // Cancel any previous outdated pending job
        activeAnalysisJob?.cancel()

        activeAnalysisJob = viewModelScope.launch {
            if (!request.isImmediate) {
                // 300ms debounce for touch keyboard typing
                delay(300)
            }

            // Execute parsing in Dispatchers.Default
            val response: AnalysisResponse = withContext(Dispatchers.Default) {
                SyntaxAnalyzer.analyzeRequest(request)
            }

            // Document Version Guard: Discard results if the user typed newer code while analyzing!
            if (response.documentVersion < currentDocumentVersion) {
                return@launch
            }

            // Group issues by line for O(1) Gutter lookups in Compose
            val issuesByLine = response.issues.groupBy { it.line }

            // Find if cursor is currently pointing at a line with an issue
            val cursor = _uiState.value.editorValue.selection.start
            val focused = calculateFocusedIssue(cursor, _uiState.value.editorValue.text, response.issues)

            _uiState.update {
                it.copy(
                    diagnosticsState = DiagnosticsState(
                        documentVersion = response.documentVersion,
                        issues = response.issues,
                        issuesByLine = issuesByLine,
                        status = AnalysisStatus.UP_TO_DATE,
                        focusedIssue = focused,
                        executionTimeMs = response.executionTimeMs
                    )
                )
            }
        }
    }

    private fun calculateFocusedIssue(cursorOffset: Int, text: String, issues: List<DiagnosticIssue>): DiagnosticIssue? {
        if (issues.isEmpty()) return null
        val safeOffset = cursorOffset.coerceIn(0, text.length)
        val currentLine = text.substring(0, safeOffset).count { it == '\n' } + 1
        return issues.firstOrNull { it.line == currentLine }
    }

    fun openFile(file: ProjectFile) {
        if (file.isDirectory) return
        val currentTabs = _uiState.value.openTabs.toMutableList()
        if (!currentTabs.any { it.id == file.id }) {
            currentTabs.add(file)
        }

        // Save current changes
        _uiState.value.activeFile.content = _uiState.value.editorValue.text

        undoStack.clear()
        redoStack.clear()

        currentDocumentVersion++
        val newVer = currentDocumentVersion
        val newEditorValue = TextFieldValue(file.content)

        _uiState.update {
            it.copy(
                activeFile = file,
                openTabs = currentTabs,
                editorValue = newEditorValue,
                documentVersion = newVer,
                canUndo = false,
                canRedo = false,
                diagnosticsState = DiagnosticsState(
                    documentVersion = newVer,
                    status = AnalysisStatus.ANALYZING
                )
            )
        }

        scheduleAnalysis(
            AnalysisRequest(
                fileId = file.id,
                language = file.language,
                code = file.content,
                documentVersion = newVer,
                isImmediate = true
            )
        )
    }

    fun closeTab(file: ProjectFile) {
        val currentTabs = _uiState.value.openTabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.id == file.id }
        if (index != -1) {
            currentTabs.removeAt(index)
            val nextActive = if (_uiState.value.activeFile.id == file.id && currentTabs.isNotEmpty()) {
                currentTabs.getOrElse(index) { currentTabs.last() }
            } else {
                _uiState.value.activeFile
            }

            _uiState.update {
                it.copy(
                    openTabs = currentTabs,
                    activeFile = nextActive,
                    editorValue = if (nextActive.id != file.id) TextFieldValue(nextActive.content) else it.editorValue
                )
            }
        }
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_uiState.value.editorValue)
            _uiState.value.activeFile.content = prev.text

            currentDocumentVersion++
            val newVer = currentDocumentVersion

            _uiState.update {
                it.copy(
                    editorValue = prev,
                    documentVersion = newVer,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }

            scheduleAnalysis(
                AnalysisRequest(
                    fileId = _uiState.value.activeFile.id,
                    language = _uiState.value.activeFile.language,
                    code = prev.text,
                    documentVersion = newVer,
                    isImmediate = true
                )
            )
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
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
                AnalysisRequest(
                    fileId = _uiState.value.activeFile.id,
                    language = _uiState.value.activeFile.language,
                    code = next.text,
                    documentVersion = newVer,
                    isImmediate = true
                )
            )
        }
    }

    /**
     * Applies an automated quick fix to the code (triggers immediate re-analysis)
     */
    fun applyQuickFix(quickFix: QuickFix) {
        val current = _uiState.value.editorValue
        val text = current.text
        val range = quickFix.replacementRange

        val safeStart = range.first.coerceIn(0, text.length)
        val safeEnd = range.last.coerceIn(safeStart, text.length)

        val newText = text.replaceRange(safeStart, safeEnd, quickFix.replacementText)
        val newCursor = safeStart + quickFix.replacementText.length

        val newEditorValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )

        onEditorChange(newEditorValue, isImmediate = true)
    }

    fun toggleConsole() {
        _uiState.update { it.copy(showConsole = !it.showConsole) }
    }

    fun toggleDiagnosticsSheet() {
        _uiState.update { it.copy(showDiagnosticsSheet = !it.showDiagnosticsSheet) }
    }

    fun dismissDiagnosticsSheet() {
        _uiState.update { it.copy(showDiagnosticsSheet = false) }
    }

    fun toggleLivePreview() {
        _uiState.update { it.copy(showLivePreview = !it.showLivePreview) }
    }

    fun runProject() {
        val file = _uiState.value.activeFile
        file.content = _uiState.value.editorValue.text

        val hasErrors = _uiState.value.diagnosticsState.issues.any { it.severity == Severity.ERROR }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showConsole = true,
                    consoleLogs = it.consoleLogs + listOf(
                        "=========================================",
                        "🚀 INICIANDO PIPELINE DE COMPILACIÓN ON-DEVICE",
                        "Target: Android 14 (API 34) • Modo: ARM64-v8a",
                        "========================================="
                    )
                )
            }

            BuildPipeline.execute(
                project = _uiState.value.rootProject,
                activeCode = file.content,
                hasSyntaxErrors = hasErrors
            ).collect { progress ->
                _uiState.update { current ->
                    current.copy(
                        buildProgress = progress,
                        consoleLogs = current.consoleLogs + listOf(progress.currentLog)
                    )
                }
            }
        }
    }

    // LAMA AI Assistant Actions
    fun toggleAiAssistant() {
        _uiState.update { it.copy(showAiAssistant = !it.showAiAssistant) }
    }

    fun dismissAiAssistant() {
        _uiState.update { it.copy(showAiAssistant = false, aiResponse = null) }
    }

    fun selectAiProvider(type: ProviderType) {
        aiManager.selectProvider(type)
        _uiState.update { it.copy(selectedAiProvider = type) }
    }

    fun insertAiGeneratedCode(codeToInsert: String? = null) {
        val raw = codeToInsert ?: _uiState.value.aiResponse ?: return
        if (raw.isBlank()) return

        // Extract code block if enclosed in ```
        val codeBlockRegex = Regex("```(?:dart|kotlin|xml)?\\s*([\\s\\S]*?)```")
        val match = codeBlockRegex.find(raw)
        val extracted = match?.groups?.get(1)?.value?.trim() ?: raw.trim()

        val currentText = _uiState.value.editorValue.text
        val cursor = _uiState.value.editorValue.selection.start.coerceIn(0, currentText.length)

        val snippet = "\n\n$extracted\n"
        val newText = StringBuilder(currentText).insert(cursor, snippet).toString()
        val newCursor = cursor + snippet.length

        onEditorChange(
            TextFieldValue(text = newText, selection = TextRange(newCursor)),
            isImmediate = true
        )

        _uiState.update {
            it.copy(
                aiResponse = "🎉 Código insertado con éxito en ${it.activeFile.name}."
            )
        }
    }

    fun askAiAssistant(action: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiResponse = "") }

            val text = _uiState.value.editorValue.text
            val cursor = _uiState.value.editorValue.selection.start.coerceIn(0, text.length)
            val currentLine = text.substring(0, cursor).count { it == '\n' } + 1

            val context = CodeContext(
                fullText = text,
                cursorOffset = cursor,
                currentLine = currentLine,
                filePath = _uiState.value.activeFile.path
            )

            val ragContext = siloEngine.formatRagContextForPrompt(action)
            val effectivePrompt = if (ragContext.isNotBlank()) "$ragContext\n$action" else action

            // Evaluate route (Local vs Cloud fallback)
            val route = routerEngine.evaluateRoute(effectivePrompt)
            if (route.destination == com.ide.mobile.core.model.RouteDestination.CLOUD_GEMINI) {
                aiManager.selectProvider(ProviderType.GEMINI_API)
            }

            var accumulated = ""
            aiManager.generateCompletionStream(effectivePrompt, context).collect { chunk ->
                accumulated += chunk
                _uiState.update {
                    it.copy(
                        aiResponse = accumulated,
                        isAiLoading = false
                    )
                }
            }
        }
    }

    fun selectNavTab(tab: MainNavTab) {
        _uiState.update { it.copy(currentNavTab = tab) }
    }

    fun toggleAiPanel() {
        _uiState.update { it.copy(showAiPanel = !it.showAiPanel) }
    }

    fun setAiPanelVisible(visible: Boolean) {
        _uiState.update { it.copy(showAiPanel = visible) }
    }

    fun toggleTerminal() {
        _uiState.update { it.copy(showTerminal = !it.showTerminal) }
    }

    fun selectLocalModel(model: String) {
        aiManager.setLocalModel(model)
        _uiState.update { it.copy(selectedLocalModel = model) }
    }

    fun updateLocalAiConfig(host: String, port: Int, model: String) {
        aiManager.setLocalModel(model)
        aiManager.setLocalPort(port)
        aiManager.localLmProvider.serverHost = host
        _uiState.update {
            it.copy(
                localAiHost = host,
                localAiPort = port,
                selectedLocalModel = model,
                consoleLogs = it.consoleLogs + listOf("[Local LM Server] Configuración actualizada: $host:$port ($model)")
            )
        }
    }

    fun testLocalAiConnection(host: String, port: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(localAiTestStatus = "Comprobando conexión con $host:$port...") }
            val isReachable = withContext(Dispatchers.IO) {
                try {
                    java.net.Socket().use { socket ->
                        socket.connect(java.net.InetSocketAddress(host, port), 600)
                        true
                    }
                } catch (e: Exception) {
                    false
                }
            }

            val statusMsg = if (isReachable) {
                "🟢 Conectado con éxito a $host:$port (Mobile LM Server activo)"
            } else {
                "🔴 No responde en $host:$port (verifica que el interruptor 'Server Status' esté activo en Mobile LM Server)"
            }

            _uiState.update {
                it.copy(
                    localAiTestStatus = statusMsg,
                    consoleLogs = it.consoleLogs + listOf("[Local LM Server] Test: $statusMsg")
                )
            }
        }
    }

    fun commitChanges(message: String) {
        _uiState.update {
            it.copy(
                consoleLogs = it.consoleLogs + listOf(
                    "[Git] Committed: \"$message\"",
                    "[Git] Working tree clean on branch main."
                )
            )
        }
    }

    // AI Hub Navigation & Actions
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
            selectLocalModel(model.name)
        }
    }

    fun stopModel(id: String) {
        modelDownloadManager.stopModel(id)
    }

    fun deleteModel(id: String) {
        modelDownloadManager.deleteModel(id)
    }

    fun downloadModel(item: com.ide.mobile.core.model.ModelItem) {
        modelDownloadManager.startDownload(item)
    }

    fun importLocalModel(name: String, sizeBytes: Long) {
        modelDownloadManager.importLocalFile(name, "/storage/emulated/0/Download/$name", sizeBytes)
    }

    fun updateHardwareConfig(threads: Int, contextWindow: Int, gpuLayers: Int, engineName: String) {
        telemetryService.updateHardwareConfig(threads, contextWindow, gpuLayers, engineName)
    }

    fun updateRouterConfig(config: com.ide.mobile.core.model.RouterConfig) {
        routerEngine.config = config
        _uiState.update { it.copy(routerConfig = config) }
    }

    fun addRagDocument(name: String, content: String) {
        siloEngine.addDocument(name, content)
    }

    fun removeRagDocument(id: String) {
        siloEngine.removeDocument(id)
    }

    fun searchRagChunks(query: String): List<com.ide.mobile.core.model.RagChunk> {
        return siloEngine.searchSimilarChunks(query)
    }

    fun toggleRagInjection(enabled: Boolean) {
        siloEngine.isRagInjectionEnabled = enabled
        _uiState.update { it.copy(isRagInjectionEnabled = enabled) }
    }
}


