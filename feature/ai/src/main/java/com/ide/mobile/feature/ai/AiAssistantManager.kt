package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.ApiKeysConfig
import com.ide.mobile.core.model.LocalAgentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor unificado de Asistentes IA de Black Cat IDE.
 * Orquesta proveedores Cloud (Gemini, ChatGPT, Claude, Perplexity), Local GGUF y Agentes Antigravity.
 */
class AiAssistantManager(
    initialProvider: ProviderType = ProviderType.GEMINI_API
) {
    var geminiProvider = GeminiAssistantProvider()
    var openAiProvider = OpenAiAssistantProvider()
    var claudeProvider = ClaudeAssistantProvider()
    var perplexityProvider = PerplexityAssistantProvider()
    val localLmProvider = LocalGgufAssistantProvider()
    val agentEngine = LocalAgentEngine()

    private var activeLocalAgent: LocalAgentEntity = LocalAgentEngine.BUILT_IN_AGENTS.first()

    private val providers = mutableMapOf<ProviderType, CodeAssistantProvider>(
        ProviderType.GEMINI_API to geminiProvider,
        ProviderType.OPENAI_API to openAiProvider,
        ProviderType.CLAUDE_API to claudeProvider,
        ProviderType.PERPLEXITY_API to perplexityProvider,
        ProviderType.LOCAL_GGUF to localLmProvider,
        ProviderType.LITERT_LM to localLmProvider
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
