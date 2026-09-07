package com.ide.mobile.feature.ai

import kotlinx.coroutines.flow.Flow

enum class ProviderType(val displayName: String, val badge: String) {
    LOCAL_AGENT("Agente Local Antigravity", "🤖 Local"),
    GEMINI_API("Google Gemini AI", "⚡ Ultra Rápido"),
    OPENAI_API("OpenAI ChatGPT", "🧠 Cloud"),
    CLAUDE_API("Anthropic Claude", "🎭 Cloud"),
    PERPLEXITY_API("Perplexity AI", "🔍 Web / Docs"),
    LOCAL_GGUF("Local LM (llama.cpp)", "📱 On-Device"),
    LITERT_LM("Google LiteRT-LM", "⚡ NPU/GPU"),
    OLLAMA("Ollama Local Engine", "🦙 Ollama")
}


data class GenerationConfig(
    val temperature: Float = 0.2f,
    val maxTokens: Int = 1024,
    val stopSequences: List<String> = emptyList()
)

data class CodeContext(
    val fullText: String,
    val cursorOffset: Int,
    val currentLine: Int,
    val filePath: String
)

interface CodeAssistantProvider {
    val providerType: ProviderType

    suspend fun isAvailable(): Boolean

    fun generateCompletionStream(
        prompt: String,
        context: CodeContext,
        config: GenerationConfig = GenerationConfig()
    ): Flow<String>

    suspend fun suggestQuickFix(
        errorMessage: String,
        faultyCode: String
    ): String
}
