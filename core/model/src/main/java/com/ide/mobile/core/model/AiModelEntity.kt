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
