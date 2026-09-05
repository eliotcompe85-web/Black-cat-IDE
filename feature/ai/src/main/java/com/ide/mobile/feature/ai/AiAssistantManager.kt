package com.ide.mobile.feature.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiAssistantManager(
    initialProvider: ProviderType = ProviderType.GEMINI_API
) {
    val geminiProvider = GeminiAssistantProvider()
    val localLmProvider = LocalGgufAssistantProvider()

    private val providers = mapOf<ProviderType, CodeAssistantProvider>(
        ProviderType.GEMINI_API to geminiProvider,
        ProviderType.LOCAL_GGUF to localLmProvider
    )

    private val _currentProviderType = MutableStateFlow(initialProvider)
    val currentProviderType: StateFlow<ProviderType> = _currentProviderType.asStateFlow()

    fun selectProvider(type: ProviderType) {
        if (providers.containsKey(type)) {
            _currentProviderType.value = type
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
        return getActiveProvider().generateCompletionStream(prompt, context, config)
    }

    suspend fun suggestQuickFix(
        errorMessage: String,
        faultyCode: String
    ): String {
        return getActiveProvider().suggestQuickFix(errorMessage, faultyCode)
    }
}
