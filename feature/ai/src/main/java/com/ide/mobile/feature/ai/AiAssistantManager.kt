package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.ApiKeysConfig
import com.ide.mobile.core.model.DownloadableAgent
import com.ide.mobile.core.model.LocalAgentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Gestor unificado de Asistentes IA de Black Cat IDE.
 * Orquesta proveedores Cloud (Gemini Free Tier, ChatGPT, Claude, Perplexity), Local GGUF y Agentes Antigravity.
 */
class AiAssistantManager(
    initialProvider: ProviderType = ProviderType.GEMINI_API
) {
    var geminiProvider = GeminiAssistantProvider()
    var openAiProvider = OpenAiAssistantProvider()
    var claudeProvider = ClaudeAssistantProvider()
    var perplexityProvider = PerplexityAssistantProvider()
    val localLmProvider = LocalGgufAssistantProvider()
    val ollamaProvider = OllamaAssistantProvider()
    val agentEngine = LocalAgentEngine()

    private var activeLocalAgent: LocalAgentEntity = LocalAgentEngine.BUILT_IN_AGENTS.first()

    private val providers = mutableMapOf<ProviderType, CodeAssistantProvider>(
        ProviderType.GEMINI_API to geminiProvider,
        ProviderType.OPENAI_API to openAiProvider,
        ProviderType.CLAUDE_API to claudeProvider,
        ProviderType.PERPLEXITY_API to perplexityProvider,
        ProviderType.LOCAL_GGUF to localLmProvider,
        ProviderType.LITERT_LM to localLmProvider,
        ProviderType.OLLAMA to ollamaProvider
    )

    private val _currentProviderType = MutableStateFlow(initialProvider)
    val currentProviderType: StateFlow<ProviderType> = _currentProviderType.asStateFlow()

    fun updateApiKeys(config: ApiKeysConfig) {
        geminiProvider = GeminiAssistantProvider(
            apiKey = config.geminiApiKey,
            projectName = config.geminiProject
        )
        openAiProvider = OpenAiAssistantProvider(
            apiKey = config.openAiApiKey,
            modelName = config.openAiModel
        )
        claudeProvider = ClaudeAssistantProvider(
            apiKey = config.claudeApiKey,
            modelName = config.claudeModel
        )
        perplexityProvider = PerplexityAssistantProvider(
            apiKey = config.perplexityApiKey,
            modelName = config.perplexityModel
        )

        providers[ProviderType.GEMINI_API] = geminiProvider
        providers[ProviderType.OPENAI_API] = openAiProvider
        providers[ProviderType.CLAUDE_API] = claudeProvider
        providers[ProviderType.PERPLEXITY_API] = perplexityProvider
    }

    fun selectProvider(type: ProviderType) {
        _currentProviderType.value = type
    }

    fun setActiveAgent(agent: LocalAgentEntity) {
        activeLocalAgent = agent
    }

    fun getActiveAgent(): LocalAgentEntity = activeLocalAgent

    fun getDownloadableAgents(): List<DownloadableAgent> = agentEngine.getDownloadableCatalog()

    suspend fun downloadAndActivateAgent(downloadable: DownloadableAgent, storageDir: File?): LocalAgentEntity {
        val activated = agentEngine.downloadAndActivateAgent(downloadable, storageDir)
        setActiveAgent(activated)
        selectProvider(ProviderType.LOCAL_AGENT)
        return activated
    }

    suspend fun executeHttpRequest(urlString: String, method: String = "GET", body: String? = null): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 8000
                readTimeout = 10000
                if (body != null && (method == "POST" || method == "PUT")) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    OutputStreamWriter(outputStream, "UTF-8").use { it.write(body) }
                }
            }
            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
            val text = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
            "HTTP $responseCode: $text"
        } catch (e: Exception) {
            "Error HTTP: ${e.localizedMessage ?: e.message}"
        }
    }

    fun setLocalModel(modelName: String) {
        localLmProvider.modelName = modelName
    }

    fun setLocalPort(port: Int) {
        localLmProvider.serverPort = port
    }

    fun getActiveProvider(): CodeAssistantProvider {
        return providers[_currentProviderType.value] ?: geminiProvider
    }

    fun generateCompletionStream(
        prompt: String,
        context: CodeContext,
        config: GenerationConfig = GenerationConfig()
    ): Flow<String> {
        return if (_currentProviderType.value == ProviderType.LOCAL_AGENT) {
            agentEngine.runAgent(activeLocalAgent, prompt, context)
        } else {
            getActiveProvider().generateCompletionStream(prompt, context, config)
        }
    }

    suspend fun suggestQuickFix(
        errorMessage: String,
        faultyCode: String
    ): String {
        return getActiveProvider().suggestQuickFix(errorMessage, faultyCode)
    }
}
