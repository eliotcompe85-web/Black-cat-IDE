package com.ide.mobile.feature.ai.downloader

import com.ide.mobile.core.model.ModelFormat
import com.ide.mobile.core.model.ModelItem
import com.ide.mobile.core.model.ModelStatus
import com.ide.mobile.core.model.ModelType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Model Management & Asynchronous Downloader for Black Cat IDE.
 * Supports Hugging Face GGUF models and Google LiteRT-LM models.
 */
class ModelDownloadManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    // Models installed or registered in device storage
    private val _installedModels = MutableStateFlow<List<ModelItem>>(
        listOf(
            ModelItem(
                id = "qwen-2b",
                name = "Qwen3.5-2B-Q4_0.gguf",
                sizeBytes = 1181116000L,
                sizeDisplay = "1.10 GB",
                format = ModelFormat.LLAMA_CPP_GGUF,
                type = ModelType.TEXT,
                status = ModelStatus.RUNNING,
                path = "/data/user/0/com.ide.mobile/files/models/Qwen3.5-2B-Q4_0.gguf",
                quantization = "Q4_0",
                parameterCount = "2B",
                description = "Optimizado para inferencia en smartphone (llama.cpp ARM NEON).",
                memoryRequiredMb = 1150
            ),
            ModelItem(
                id = "gemma-4-e2b",
                name = "Gemma-4-E2B-IT.litertlm",
                sizeBytes = 1482000000L,
                sizeDisplay = "1.38 GB",
                format = ModelFormat.LITERT_LM,
                type = ModelType.VISION,
                status = ModelStatus.READY,
                path = "/data/user/0/com.ide.mobile/files/models/Gemma-4-E2B-IT.litertlm",
                quantization = "INT4",
                parameterCount = "2B",
                description = "Motor LiteRT-LM acelerado por GPU/NPU de Google.",
                memoryRequiredMb = 1400
            ),
            ModelItem(
                id = "deepseek-1.5b",
                name = "DeepSeek-R1-Distill-1.5B.gguf",
                sizeBytes = 980000000L,
                sizeDisplay = "0.93 GB",
                format = ModelFormat.LLAMA_CPP_GGUF,
                type = ModelType.CODE,
                status = ModelStatus.STOPPED,
                path = "/data/user/0/com.ide.mobile/files/models/DeepSeek-R1-Distill-1.5B.gguf",
                quantization = "Q4_K_M",
                parameterCount = "1.5B",
                description = "Excelente razonamiento lógico y código en bajo consumo.",
                memoryRequiredMb = 980
            )
        )
    )
    val installedModels: StateFlow<List<ModelItem>> = _installedModels.asStateFlow()

    // Public Catalogue available for 1-tap download (Hugging Face Mobile hub)
    private val _hubCatalog = MutableStateFlow<List<ModelItem>>(
        listOf(
            ModelItem(
                id = "llama-3.2-1b",
                name = "Llama-3.2-1B-Instruct-Q4_K.gguf",
                sizeBytes = 820000000L,
                sizeDisplay = "782 MB",
                format = ModelFormat.LLAMA_CPP_GGUF,
                type = ModelType.TEXT,
                status = ModelStatus.READY,
                path = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF",
                quantization = "Q4_K_S",
                parameterCount = "1B",
                description = "Meta Llama ultra-ligero de alta velocidad para móviles.",
                memoryRequiredMb = 820
            ),
            ModelItem(
                id = "phi-3.5-mini",
                name = "Phi-3.5-mini-instruct-4k.litertlm",
                sizeBytes = 1950000000L,
                sizeDisplay = "1.82 GB",
                format = ModelFormat.LITERT_LM,
                type = ModelType.CODE,
                status = ModelStatus.READY,
                path = "https://huggingface.co/litert/phi-3.5-mini-litertlm",
                quantization = "INT4",
                parameterCount = "3.8B",
                description = "Microsoft Phi-3.5 optimizado para razonamiento y código.",
                memoryRequiredMb = 1950
            ),
            ModelItem(
                id = "smollm2-1.7b",
                name = "SmolLM2-1.7B-Instruct-Q4_0.gguf",
                sizeBytes = 1040000000L,
                sizeDisplay = "0.97 GB",
                format = ModelFormat.LLAMA_CPP_GGUF,
                type = ModelType.TEXT,
                status = ModelStatus.READY,
                path = "https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF",
                quantization = "Q4_0",
                parameterCount = "1.7B",
                description = "Hugging Face modelo ultra eficiente para tareas cotidianas.",
                memoryRequiredMb = 1050
            )
        )
    )
    val hubCatalog: StateFlow<List<ModelItem>> = _hubCatalog.asStateFlow()

    fun runModel(modelId: String) {
        _installedModels.value = _installedModels.value.map { item ->
            if (item.id == modelId) {
                item.copy(status = ModelStatus.RUNNING)
            } else if (item.status == ModelStatus.RUNNING) {
                // Stop other running model if active
                item.copy(status = ModelStatus.READY)
            } else {
                item
            }
        }
    }

    fun stopModel(modelId: String) {
        _installedModels.value = _installedModels.value.map { item ->
            if (item.id == modelId) item.copy(status = ModelStatus.STOPPED) else item
        }
    }

    fun deleteModel(modelId: String) {
        _installedModels.value = _installedModels.value.filter { it.id != modelId }
    }

    fun importLocalFile(fileName: String, filePath: String, sizeBytes: Long) {
        val format = if (fileName.endsWith(".litertlm", ignoreCase = true)) {
            ModelFormat.LITERT_LM
        } else {
            ModelFormat.LLAMA_CPP_GGUF
        }
        val sizeMb = sizeBytes / (1024 * 1024)
        val sizeDisplay = if (sizeMb > 1024) String.format("%.2f GB", sizeMb / 1024f) else "$sizeMb MB"

        val imported = ModelItem(
            id = "imported-${System.currentTimeMillis()}",
            name = fileName,
            sizeBytes = sizeBytes,
            sizeDisplay = sizeDisplay,
            format = format,
            type = ModelType.TEXT,
            status = ModelStatus.READY,
            path = filePath,
            quantization = "Custom / User SAF",
            parameterCount = "Importado",
            description = "Importado desde el almacenamiento interno del dispositivo."
        )

        _installedModels.value = listOf(imported) + _installedModels.value
    }

    fun startDownload(catalogItem: ModelItem) {
        // Check if already in installed
        val exists = _installedModels.value.any { it.name == catalogItem.name }
        if (exists) return

        val downloadingItem = catalogItem.copy(
            status = ModelStatus.DOWNLOADING,
            downloadProgress = 0.05f,
            downloadSpeed = "12.4 MB/s"
        )
        _installedModels.value = listOf(downloadingItem) + _installedModels.value

        // Simulate asynchronous download progress
        scope.launch {
            for (step in 1..10) {
                delay(400)
                val currentProgress = (step * 0.1f).coerceAtMost(1.0f)
                val speed = "${(10 + (step % 5) * 1.5).toInt()}.${step % 9} MB/s"

                _installedModels.value = _installedModels.value.map {
                    if (it.id == catalogItem.id) {
                        if (step == 10) {
                            it.copy(
                                status = ModelStatus.READY,
                                downloadProgress = 1.0f,
                                downloadSpeed = "",
                                path = "/data/user/0/com.ide.mobile/files/models/${catalogItem.name}"
                            )
                        } else {
                            it.copy(downloadProgress = currentProgress, downloadSpeed = speed)
                        }
                    } else it
                }
            }
        }
    }
}
