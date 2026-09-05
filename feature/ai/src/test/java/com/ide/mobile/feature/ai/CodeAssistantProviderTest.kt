package com.ide.mobile.feature.ai

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeAssistantProviderTest {

    @Test
    fun testGeminiAssistantProviderStreaming() = runBlocking {
        val provider = GeminiAssistantProvider()
        assertTrue(provider.isAvailable())

        val context = CodeContext(
            fullText = "fun main() {}",
            cursorOffset = 10,
            currentLine = 1,
            filePath = "/app/MainActivity.kt"
        )

        val flow = provider.generateCompletionStream("EXPLAIN", context)
        val chunks = flow.toList()
        assertTrue(chunks.isNotEmpty())
        val combined = chunks.joinToString("")
        assertTrue(combined.isNotBlank())
    }

    @Test
    fun testAiAssistantManagerSwitching() = runBlocking {
        val manager = AiAssistantManager(ProviderType.GEMINI_API)
        assertEquals(ProviderType.GEMINI_API, manager.currentProviderType.value)

        manager.selectProvider(ProviderType.LOCAL_GGUF)
        assertEquals(ProviderType.LOCAL_GGUF, manager.currentProviderType.value)
        assertEquals(ProviderType.LOCAL_GGUF, manager.getActiveProvider().providerType)
    }

    @Test
    fun testLocalGgufAssistantProviderFallbackStreaming() = runBlocking {
        val local = LocalGgufAssistantProvider(modelName = "Qwen3.5-2B-Q4_0.gguf")
        val context = CodeContext(
            fullText = "class MyApp {}",
            cursorOffset = 5,
            currentLine = 1,
            filePath = "/lib/main.dart"
        )
        val flow = local.generateCompletionStream("EXPLAIN", context)
        val chunks = flow.toList()
        assertTrue(chunks.isNotEmpty())
        val text = chunks.joinToString("")
        assertTrue(text.isNotBlank())
    }
}
