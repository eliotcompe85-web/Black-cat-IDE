package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.*
import com.ide.mobile.feature.ai.downloader.ModelDownloadManager
import com.ide.mobile.feature.ai.rag.SiloKnowledgeEngine
import com.ide.mobile.feature.ai.router.SmartInferenceRouter
import com.ide.mobile.feature.ai.runtime.DeviceTelemetryService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * End-to-End (E2E) System and Integration Test Suite for Black Cat IDE.
 * Validates syntax, logical congruency, anti-OOM routing, RAG pipeline, and hardware telemetry.
 */
class BlackCatSystemE2ETest {

    @Test
    fun testSmartInferenceRouter_UnderThreshold_RoutesToLocal() {
        val router = SmartInferenceRouter(
            RouterConfig(isRouterEnabled = true, localTokenThreshold = 1536)
        )
        val shortPrompt = "Explica la diferencia entre StatefulWidget y StatelessWidget en Flutter."
        val result = router.evaluateRoute(shortPrompt)

        assertEquals(RouteDestination.LOCAL_DEVICE, result.destination)
        assertTrue("Tokens should be under threshold", result.estimatedTokens < 1536)
        assertTrue(result.reason.contains("Seguro para ejecución local"))
    }

    @Test
    fun testSmartInferenceRouter_OverThreshold_RoutesToCloudGemini() {
        val router = SmartInferenceRouter(
            RouterConfig(isRouterEnabled = true, localTokenThreshold = 1536)
        )
        // Simulate massive 2500+ token codebase prompt
        val hugeCodePrompt = buildString {
            appendLine("Analiza y refactoriza esta arquitectura completa de Flutter:")
            repeat(120) {
                appendLine("class HeavyDataModel$it { final String field$it; HeavyDataModel$it(this.field$it); }")
            }
        }
        val result = router.evaluateRoute(hugeCodePrompt)

        assertEquals(RouteDestination.CLOUD_GEMINI, result.destination)
        assertTrue("Tokens should exceed threshold", result.estimatedTokens > 1536)
        assertTrue("Reason should indicate anti-OOM protection", result.reason.contains("OOM"))
    }

    @Test
    fun testPromptCompression_ReducesTokenFootprint() {
        val router = SmartInferenceRouter(
            RouterConfig(isRouterEnabled = true, promptCompressionEnabled = true, compressionRatio = 0.5f)
        )
        val longChat = buildString {
            appendLine("// HEADER: Configuración de la app")
            appendLine("// User: Hola")
            repeat(50) { appendLine("// Assistant: Línea de conversación previa número $it con datos extensos...") }
            appendLine("// FINAL: Última consulta actual del usuario")
        }
        val compressed = router.compressPrompt(longChat, 0.5f)

        assertTrue(compressed.length < longChat.length)
        assertTrue(compressed.contains("Compresión Black Cat AI"))
        assertTrue(compressed.contains("HEADER"))
        assertTrue(compressed.contains("FINAL"))
    }

    @Test
    fun testSiloLibrary_ChunkingAndSemanticMatching() {
        val silo = SiloKnowledgeEngine()
        silo.isRagInjectionEnabled = true

        // Ingest new document
        silo.addDocument(
            name = "network_client.dart",
            content = "class NetworkClient { Future<Response> get(String url) async { return http.get(Uri.parse(url)); } }"
        )

        val docs = silo.documents.value
        val added = docs.find { it.name == "network_client.dart" }
        assertNotNull("Document should be registered in SiloLibrary", added)
        assertTrue("Should have calculated tokens", (added?.tokenCount ?: 0) > 0)

        // Semantic similarity test
        val chunks = silo.searchSimilarChunks("NetworkClient http get url")
        assertTrue("Should find matching chunk", chunks.isNotEmpty())
        val topChunk = chunks.first()
        assertTrue("Similarity score should be high", topChunk.similarityScore >= 0.70f)

        // Formatted prompt injection test
        val formattedContext = silo.formatRagContextForPrompt("NetworkClient get")
        assertTrue(formattedContext.contains("CONTEXTO SILOLIBRARY"))
        assertTrue(formattedContext.contains("network_client.dart"))
    }

    @Test
    fun testModelDownloadManager_LifecycleAndSafImport() = runBlocking {
        val manager = ModelDownloadManager()
        val initialList = manager.installedModels.value
        assertTrue("Initial models should contain Qwen and Gemma", initialList.isNotEmpty())

        // Test SAF local import
        manager.importLocalFile("custom_model_v1.gguf", "/sdcard/Download/custom_model_v1.gguf", 1400000000L)
        val afterImport = manager.installedModels.value
        val imported = afterImport.find { it.name == "custom_model_v1.gguf" }
        assertNotNull("Imported model should be present", imported)
        assertEquals(ModelFormat.LLAMA_CPP_GGUF, imported?.format)
        assertEquals(ModelStatus.READY, imported?.status)

        // Test running and stopping
        manager.runModel(imported!!.id)
        val runningState = manager.installedModels.value.find { it.id == imported.id }
        assertEquals(ModelStatus.RUNNING, runningState?.status)

        manager.stopModel(imported.id)
        val stoppedState = manager.installedModels.value.find { it.id == imported.id }
        assertEquals(ModelStatus.STOPPED, stoppedState?.status)
    }

    @Test
    fun testDeviceTelemetry_HardwareMetricsBounds() = runBlocking {
        val telemetry = DeviceTelemetryService()
        val metrics = telemetry.metrics.first()

        assertTrue("RAM used must be positive", metrics.ramUsedMb > 0f)
        assertTrue("RAM used must not exceed total", metrics.ramUsedMb <= metrics.ramTotalMb)
        assertTrue("CPU percent must be between 0 and 100", metrics.cpuPercent in 0..100)
        assertTrue("Tokens/sec must be positive", metrics.tokensPerSecond > 0f)
        assertTrue("Temperature should be in realistic phone range", metrics.temperatureCelsius in 20f..85f)
    }
}
