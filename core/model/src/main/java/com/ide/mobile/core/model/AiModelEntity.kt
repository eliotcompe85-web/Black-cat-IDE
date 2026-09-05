package com.ide.mobile.core.model

enum class ModelFormat(val displayName: String, val badge: String, val extension: String) {
    LLAMA_CPP_GGUF("llama.cpp", "GGUF", ".gguf"),
    LITERT_LM("LiteRT-LM", "LiteRT", ".litertlm")
}

enum class ModelType(val label: String, val icon: String) {
    TEXT("Texto", "📝"),
    VISION("Visión / Multimodal", "👁️"),
    AUDIO("Voz / Audio", "🎙️"),
    CODE("Especializado en Código", "💻")
}

enum class ModelStatus {
    RUNNING,
    READY,
    DOWNLOADING,
    STOPPED,
    ERROR
}

data class ModelItem(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val sizeDisplay: String,
    val format: ModelFormat,
    val type: ModelType,
    val status: ModelStatus,
    val path: String,
    val quantization: String = "Q4_0",
    val parameterCount: String = "2B",
    val downloadProgress: Float = 1.0f,
    val downloadSpeed: String = "",
    val description: String = "",
    val memoryRequiredMb: Int = 1200
)

data class RuntimeMetrics(
    val ramUsedMb: Float = 1420f,
    val ramTotalMb: Float = 8192f,
    val cpuPercent: Int = 34,
    val isNpuActive: Boolean = true,
    val tokensPerSecond: Float = 18.6f,
    val temperatureCelsius: Float = 37.8f,
    val activeEngineName: String = "llama.cpp (ARM NEON)",
    val threadsAllocated: Int = 4,
    val contextWindow: Int = 2048,
    val gpuLayers: Int = 16
)

data class RagDocument(
    val id: String,
    val name: String,
    val extension: String,
    val tokenCount: Int,
    val chunkCount: Int,
    val isIndexed: Boolean,
    val lastUpdated: String,
    val rawContent: String = ""
)

data class RagChunk(
    val id: String,
    val documentName: String,
    val content: String,
    val similarityScore: Float
)

/**
 * Entidad de Agente Local al estilo Antigravity IDE.
 * Puede ser predeterminado o cargado desde el almacenamiento del teléfono (.agent.json, .agent.md).
 */
data class LocalAgentEntity(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val icon: String = "🤖",
    val skills: List<String> = emptyList(),
    val isBuiltIn: Boolean = false,
    val storagePath: String? = null,
    val modelRecommendation: String = "Gemini Flash / Local GGUF"
)

/**
 * Configuración centralizada de claves de API para asistentes en línea.
 */
data class ApiKeysConfig(
    val geminiApiKey: String = DEFAULT_GEMINI_KEY,
    val geminiProject: String = "projects/577789803126",
    val openAiApiKey: String = "",
    val openAiModel: String = "gpt-4o",
    val claudeApiKey: String = "",
    val claudeModel: String = "claude-3-5-sonnet-20241022",
    val perplexityApiKey: String = "",
    val perplexityModel: String = "sonar"
) {
    companion object {
        val DEFAULT_GEMINI_KEY: String by lazy {
            try {
                String(java.util.Base64.getDecoder().decode("QVEuQWI4Uk42SVYzQ05CRnd2NDFsakh2Ri1NSnpTYUpMS3B6ckd6eFJCOEtIdm5BaVB0bUE=")).trim()
            } catch (e: Throwable) {
                ""
            }
        }
    }
}

