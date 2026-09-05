package com.ide.mobile.core.model

data class RouterConfig(
    val isRouterEnabled: Boolean = true,
    val localTokenThreshold: Int = 1536,
    val fallbackToCloudOnOom: Boolean = true,
    val promptCompressionEnabled: Boolean = true,
    val compressionRatio: Float = 0.5f,
    val selectedCloudProvider: String = "Google Gemini Flash",
    val totalRoutedLocal: Int = 42,
    val totalRoutedCloud: Int = 18
)

enum class RouteDestination(val label: String, val badge: String, val colorHex: Long) {
    LOCAL_DEVICE("Inferencia Local On-Device", "🔒 100% Privado • 0 Datos", 0xFF34D399),
    CLOUD_GEMINI("Google Gemini Flash (Cloud)", "⚡ Ultra Rápido • Sin Límite OOM", 0xFF7B61FF),
    CLOUD_OPENAI("OpenAI API (Cloud)", "☁️ Respaldo Cloud", 0xFF38BDF8)
}

data class RouteEvaluationResult(
    val estimatedTokens: Int,
    val destination: RouteDestination,
    val reason: String,
    val compressedTokens: Int? = null
)
