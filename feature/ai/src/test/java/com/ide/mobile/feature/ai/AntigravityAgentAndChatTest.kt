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

        val actionCard = contents[1] as ChatContent.ActionCard
        assertEquals(AgentActionType.RUN_COMMAND, actionCard.action.type)
        assertEquals("git status --short", actionCard.action.payload)
    }

    @Test
    fun testMissionControlAgentStates() {
        val idleAgent = ActiveAgentContext(
            agentId = "local-1",
            agentName = "Python Fixer",
            isLocal = true,
            currentState = AgentState.Idle
        )
        assertEquals(AgentState.Idle, idleAgent.currentState)

        val executingAgent = idleAgent.copy(
            currentState = AgentState.Executing(
                taskDescription = "Analizando dependencias",
                progress = 0.65f
            )
        )
        assertTrue(executingAgent.currentState is AgentState.Executing)
        assertEquals(0.65f, (executingAgent.currentState as AgentState.Executing).progress, 0.001f)

        val waitingAgent = executingAgent.copy(
            currentState = AgentState.WaitingForUser(
                actionRequired = "Ingrese API Key para Claude",
                canResume = true
            )
        )
        assertTrue(waitingAgent.currentState is AgentState.WaitingForUser)
        val waitingState = waitingAgent.currentState as AgentState.WaitingForUser
        assertTrue(waitingState.canResume)
        assertEquals("Ingrese API Key para Claude", waitingState.actionRequired)
    }

    @Test
    fun testArtifactChatContents() {
        val checklist = ChatContent.ActionChecklist(
            title = "Plan de Refactorización",
            tasks = listOf(
                ChecklistTask(description = "Crear modelo de dominio", isCompleted = true, requiresHuman = false),
                ChecklistTask(description = "Configurar clave en .env", isCompleted = false, requiresHuman = true)
            )
        )
        assertEquals("Plan de Refactorización", checklist.title)
        assertEquals(2, checklist.tasks.size)
        assertTrue(checklist.tasks[0].isCompleted)
        assertFalse(checklist.tasks[0].requiresHuman)
        assertTrue(checklist.tasks[1].requiresHuman)

        val codeBlockWithTarget = ChatContent.CodeBlock(
            code = "fun hello() = println()",
            language = "kotlin",
            targetFilePath = "app/src/main/MainActivity.kt"
        )
        assertEquals("app/src/main/MainActivity.kt", codeBlockWithTarget.targetFilePath)

        val planArtifact = ChatContent.PlanArtifact(
            title = "Arquitectura de IA",
            summary = "Estructurar agentes y checklists interactivos"
        )
        assertEquals("Arquitectura de IA", planArtifact.title)

        val message = ChatMessage(
            senderName = "Gemini Flash Copilot",
            isFromUser = false,
            contents = listOf(planArtifact, checklist, codeBlockWithTarget)
        )
        assertEquals(3, message.contents.size)
        assertFalse(message.isFromUser)
        assertEquals("Gemini Flash Copilot", message.senderName)
    }

    @Test
    fun testAntigravityArtifactParserFullLoop() {
        val rawAiResponse = """
            # Plan: Sistema de Autenticación Firebase
            Diseñar e implementar el repositorio de login y la pantalla de inicio de sesión.
            
            ## Checklist de Tareas
            - [x] Crear repositorio de auth
            - [ ] [HUMANO] Configurar credenciales google-services.json
            - [ ] Inyectar dependencias en ViewModel
            
            ```kotlin
            // File: lib/auth_service.dart
            class AuthService {
                void login() => print("Login");
            }
            ```
            
            ```bash
            flutter pub add firebase_auth
            ```
        """.trimIndent()

        val parsed = AntigravityArtifactParser.parseResponse(rawAiResponse)
        assertTrue(parsed.any { it is ChatContent.PlanArtifact })
        val plan = parsed.filterIsInstance<ChatContent.PlanArtifact>().first()
        assertEquals("Sistema de Autenticación Firebase", plan.title)

        assertTrue(parsed.any { it is ChatContent.ActionChecklist })
        val checklist = parsed.filterIsInstance<ChatContent.ActionChecklist>().first()
        assertEquals(3, checklist.tasks.size)
        assertTrue(checklist.tasks[0].isCompleted)
        assertTrue(checklist.tasks[1].requiresHuman)
        assertFalse(checklist.tasks[2].requiresHuman)

        assertTrue(parsed.any { it is ChatContent.CodeBlock })
        val codeBlock = parsed.filterIsInstance<ChatContent.CodeBlock>().first()
        assertEquals("lib/auth_service.dart", codeBlock.targetFilePath)

        assertTrue(parsed.any { it is ChatContent.ActionCard })
        val actionCard = parsed.filterIsInstance<ChatContent.ActionCard>().first()
        assertEquals(AgentActionType.RUN_COMMAND, actionCard.action.type)
        assertEquals("flutter pub add firebase_auth", actionCard.action.payload)
    }
}
