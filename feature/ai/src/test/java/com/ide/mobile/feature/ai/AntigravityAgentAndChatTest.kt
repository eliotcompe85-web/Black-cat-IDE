package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas automatizadas para la arquitectura Antigravity:
 * Jerarquía de Agentes (LocalAgent, ApiAgent), contenidos estructurados ChatContent
 * y resolución automática de bloques de código y acciones.
 */
class AntigravityAgentAndChatTest {

    @Test
    fun testLocalAgentInstantiationAndProperties() {
        val localAgent = LocalAgent(
            id = "agent-llama-cpp",
            name = "Llama-CLI Local",
            description = "Ejecutable local para inferencia offline",
            icon = "⚡",
            executablePath = "/data/local/tmp/llama-cli",
            workingDirectory = "/storage/emulated/0",
            arguments = listOf("-m", "model.gguf", "-c", "2048"),
            memoryLimitMb = 2048
        )

        assertEquals("agent-llama-cpp", localAgent.id)
        assertEquals("Llama-CLI Local", localAgent.name)
        assertEquals("Local Engine", localAgent.category)
        assertTrue(localAgent.isAvailable)
        assertEquals("/data/local/tmp/llama-cli", localAgent.executablePath)
        assertEquals(4, localAgent.arguments.size)
        assertTrue(localAgent.isReady)
        assertTrue(localAgent is Agent)
    }

    @Test
    fun testApiAgentInstantiationAndProperties() {
        val apiAgent = ApiAgent(
            id = "agent-gemini-pro",
            name = "Gemini Cloud Copilot",
            description = "Modelo multimodal con cuota gratuita",
            icon = "🌐",
            endpoint = "https://generativelanguage.googleapis.com",
            apiKey = "AIzaSyFakeKey123",
            modelName = "gemini-1.5-flash",
            maxTokens = 8192,
            temperature = 0.5f
        )

        assertEquals("agent-gemini-pro", apiAgent.id)
        assertEquals("Gemini Cloud Copilot", apiAgent.name)
        assertEquals("Cloud API", apiAgent.category)
        assertEquals("gemini-1.5-flash", apiAgent.modelName)
        assertEquals(8192, apiAgent.maxTokens)
        assertTrue(apiAgent.isReady)
        assertTrue(apiAgent is Agent)

        val unreadyAgent = Agent.ApiAgent(
            name = "Incompleto",
            endpoint = "",
            apiKey = "",
            modelName = "gpt-4"
        )
        assertFalse(unreadyAgent.isReady)
    }

    @Test
    fun testChatContentResolutionFromPlainText() {
        val message = ChatMessage(
            sender = MessageSender.USER,
            text = "Hola agente, necesito ayuda con mi proyecto."
        )

        val contents = message.resolvedContents
        assertEquals(1, contents.size)
        assertTrue(contents[0] is ChatContent.Text)
        assertEquals("Hola agente, necesito ayuda con mi proyecto.", (contents[0] as ChatContent.Text).text)
    }

    @Test
    fun testChatContentResolutionWithMarkdownCodeBlocks() {
        val rawResponse = """
            Aquí tienes la función solicitada en Kotlin:
            ```kotlin
            fun calcularTotal(items: List<Item>): Double {
                return items.sumOf { it.precio }
            }
            ```
            Puedes aplicarla directamente a tu archivo.
        """.trimIndent()

        val message = ChatMessage(
            sender = MessageSender.AGENT,
            text = rawResponse
        )

        val contents = message.resolvedContents
        assertTrue("Debe tener al menos 3 partes (Texto, Código, Texto)", contents.size >= 3)
        assertTrue("La primera parte es texto", contents[0] is ChatContent.Text)
        assertTrue("La segunda parte es bloque de código", contents[1] is ChatContent.CodeBlock)

        val codeBlock = contents[1] as ChatContent.CodeBlock
        assertEquals("kotlin", codeBlock.language)
        assertTrue(codeBlock.code.contains("fun calcularTotal"))

        val trailingText = contents[2] as ChatContent.Text
        assertTrue(trailingText.text.contains("Puedes aplicarla directamente"))
    }

    @Test
    fun testChatContentWithActionCards() {
        val action = AgentAction(
            type = AgentActionType.RUN_COMMAND,
            title = "Verificar estado de Git",
            payload = "git status --short",
            status = ActionStatus.PROPOSED
        )

        val message = ChatMessage(
            sender = MessageSender.AGENT,
            text = "He preparado un comando para verificar el repositorio.",
            actions = listOf(action)
        )

        val contents = message.resolvedContents
        assertEquals(2, contents.size)
        assertTrue(contents[0] is ChatContent.Text)
        assertTrue(contents[1] is ChatContent.ActionCard)

        val card = contents[1] as ChatContent.ActionCard
        assertEquals("Verificar estado de Git", card.action.title)
        assertEquals("git status --short", card.action.payload)
        assertEquals(ActionStatus.PROPOSED, card.action.status)
    }
}
