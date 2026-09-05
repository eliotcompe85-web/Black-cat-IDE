package com.ide.mobile.feature.ai.router

import com.ide.mobile.core.model.RouteDestination
import com.ide.mobile.core.model.RouteEvaluationResult
import com.ide.mobile.core.model.RouterConfig

/**
 * Middleware that evaluates incoming prompts before execution:
 * - Estimates token count.
 * - Compresses prompt if enabled to fit local context.
 * - Routes to local model if <= threshold to save network & battery.
 * - Routes to Cloud (Google Gemini) if > threshold to prevent mobile Out-Of-Memory (OOM).
 */
class SmartInferenceRouter(
    var config: RouterConfig = RouterConfig()
) {

    fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        // Heuristic: On average in code & natural language, ~3.8 characters per token,
        // plus special tokens and symbol weights.
        val wordCount = text.split("\\s+".toRegex()).size
        val charBasedEstimate = (text.length / 3.8).toInt()
        return maxOf(wordCount, charBasedEstimate, 1)
    }

    fun compressPrompt(text: String, ratio: Float = 0.5f): String {
        if (!config.promptCompressionEnabled || text.length < 200) return text
        
        val lines = text.lines()
        if (lines.size <= 6) return text
        
        // Retain header lines, system directives, and compress repetitive context
        val keepStart = (lines.size * 0.35f).toInt().coerceAtLeast(2)
        val keepEnd = (lines.size * 0.35f).toInt().coerceAtLeast(2)
        
        val startLines = lines.take(keepStart)
        val endLines = lines.takeLast(keepEnd)
        
        return buildString {
            startLines.forEach { appendLine(it) }
            appendLine("// [... Compresión Black Cat AI: ${lines.size - (keepStart + keepEnd)} líneas omitidas para optimizar memoria local ...]")
            endLines.forEach { appendLine(it) }
        }
    }

    fun evaluateRoute(prompt: String): RouteEvaluationResult {
        if (!config.isRouterEnabled) {
            return RouteEvaluationResult(
                estimatedTokens = estimateTokenCount(prompt),
                destination = RouteDestination.LOCAL_DEVICE,
                reason = "Enrutador desactivado: Ejecutando en local por defecto."
            )
        }

        var effectivePrompt = prompt
        var compressedTokens: Int? = null

        if (config.promptCompressionEnabled && prompt.length > 500) {
            effectivePrompt = compressPrompt(prompt, config.compressionRatio)
            compressedTokens = estimateTokenCount(effectivePrompt)
        }

        val tokens = compressedTokens ?: estimateTokenCount(effectivePrompt)

        return if (tokens <= config.localTokenThreshold) {
            RouteEvaluationResult(
                estimatedTokens = tokens,
                destination = RouteDestination.LOCAL_DEVICE,
                reason = "Seguro para ejecución local ($tokens <= ${config.localTokenThreshold} tokens). Sin consumo de datos.",
                compressedTokens = compressedTokens
            )
        } else {
            RouteEvaluationResult(
                estimatedTokens = tokens,
                destination = RouteDestination.CLOUD_GEMINI,
                reason = "Supera umbral local ($tokens > ${config.localTokenThreshold} tokens). Desviado a Gemini Cloud para prevenir OOM.",
                compressedTokens = compressedTokens
            )
        }
    }
}
