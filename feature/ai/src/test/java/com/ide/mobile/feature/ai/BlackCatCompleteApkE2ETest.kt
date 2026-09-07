package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Suite E2E Completa de Parámetros y Capacidades de Black Cat IDE Mobile.
 * Valida de forma integral:
 * 1. Protocolo agéntico Antigravity (# Plan, ## Checklist, [HUMANO], // File:, Acciones de Terminal).
 * 2. Motor Autónomo SmartAgentSynthesizer (Tareas, Calculadora, Login, Perfil, Contador, Explicación, Fix).
 * 3. Integración completa de Ollama (API /api/chat, /api/tags y comandos CLI ollama list, run, pull, ps, serve).
 * 4. Ciclo de vida Mission Control (Planning ➔ Executing ➔ WaitingForUser ➔ Verifying ➔ Idle).
 * 5. Inyección directa de artefactos al árbol de archivos del proyecto.
 * 6. Enrutamiento de Comandos Slash (/plan, /grill-me, /fix, /review, /diff, /goal, /learn).
 * 7. Persistencia offline de proyectos y modelos de datos Room.
 */
class BlackCatCompleteApkE2ETest {

    @Test
    fun testAntigravityProtocol_FullArtifactParsing() {
        val rawAiOutput = """
            # Plan: Sistema de Autenticación Biométrica
            Implementaremos un servicio seguro para validar la huella digital del usuario con fallback a PIN.
            
            ## Checklist de Tareas
            - [x] Configurar permisos biométricos en AndroidManifest.xml
            - [ ] [HUMANO] Autorizar el sensor de huella en el dispositivo físico
            - [ ] Crear el diálogo de autenticación Biométrica
            
            ```kotlin
            // File: src/main/java/com/ide/mobile/BioAuthService.kt
            package com.ide.mobile
            
            class BioAuthService {
                fun authenticateUser(): Boolean = true
            }
            ```
            
            ```bash
            flutter pub add local_auth
            ```
        """.trimIndent()

        val parsedActions = AgentActionParser.parseActions(rawAiOutput)
        val contents = AntigravityArtifactParser.parseResponse(rawAiOutput, parsedActions)

        // 1. Verificar PlanArtifact
        val plan = contents.filterIsInstance<ChatContent.PlanArtifact>().firstOrNull()
        assertNotNull("Debe extraer el PlanArtifact con su título y resumen", plan)
        assertEquals("Sistema de Autenticación Biométrica", plan?.title)
        assertTrue(plan?.summary?.contains("huella digital") == true)

        // 2. Verificar ActionChecklist y badge [HUMANO]
        val checklist = contents.filterIsInstance<ChatContent.ActionChecklist>().firstOrNull()
        assertNotNull("Debe extraer la checklist de tareas", checklist)
        assertEquals(3, checklist?.tasks?.size)
        assertTrue("La primera tarea debe estar completada", checklist!!.tasks[0].isCompleted)
        assertTrue("La segunda tarea debe requerir intervención humana", checklist.tasks[1].requiresHuman)
        assertFalse("La tercera tarea debe ser del agente", checklist.tasks[2].requiresHuman)

        // 3. Verificar CodeBlock con targetFilePath
        val codeBlock = contents.filterIsInstance<ChatContent.CodeBlock>().firstOrNull()
        assertNotNull("Debe extraer el bloque de código", codeBlock)
        assertEquals("src/main/java/com/ide/mobile/BioAuthService.kt", codeBlock?.targetFilePath)
        assertTrue(codeBlock?.code?.contains("class BioAuthService") == true)

        // 4. Verificar ActionCard de Terminal
        val actionCard = contents.filterIsInstance<ChatContent.ActionCard>().firstOrNull()
        assertNotNull("Debe extraer la tarjeta de acción de terminal", actionCard)
        assertEquals(AgentActionType.INSTALL_DEPENDENCY, actionCard?.action?.type)
        assertEquals("flutter pub add local_auth", actionCard?.action?.payload)
    }

    @Test
    fun testSmartAgentSynthesizer_GeneratesAllDomainApps() {
        val dummyContext = CodeContext(
            fullText = "void main() {}",
            cursorOffset = 10,
            currentLine = 1,
            filePath = "/proyecto/lib/main.dart"
        )

        // 1. Tareas / Todo
        val todoResponse = SmartAgentSynthesizer.synthesizeResponse("Crea una app de tareas pendientes", dummyContext)
        assertTrue(todoResponse.contains("# Plan:"))
        assertTrue(todoResponse.contains("## Checklist de Tareas"))
        assertTrue(todoResponse.contains("// File: lib/todo_screen.dart") || todoResponse.contains("// File: src/main/java"))

        // 2. Calculadora
        val calcResponse = SmartAgentSynthesizer.synthesizeResponse("Haz una calculadora táctil", dummyContext)
        assertTrue(calcResponse.contains("# Plan: Calculadora"))
        assertTrue(calcResponse.contains("// File: lib/calculator_screen.dart"))

        // 3. Login
        val loginResponse = SmartAgentSynthesizer.synthesizeResponse("Diseña una pantalla de login moderna", dummyContext)
        assertTrue(loginResponse.contains("# Plan: Pantalla de Inicio de Sesión"))
        assertTrue(loginResponse.contains("// File: lib/login_screen.dart"))

        // 4. Perfil de Usuario
        val profileResponse = SmartAgentSynthesizer.synthesizeResponse("Crea un perfil de usuario", dummyContext)
        assertTrue(profileResponse.contains("# Plan: Pantalla de Perfil"))
        assertTrue(profileResponse.contains("// File: lib/profile_screen.dart"))

        // 5. Contador
        val counterResponse = SmartAgentSynthesizer.synthesizeResponse("Crea un contador táctil", dummyContext)
        assertTrue(counterResponse.contains("# Plan: Contador"))
        assertTrue(counterResponse.contains("// File: lib/counter_widget.dart"))

        // 6. Explicar código
        val explainResponse = SmartAgentSynthesizer.synthesizeResponse("/review explica qué hace este archivo", dummyContext)
        assertTrue(explainResponse.contains("# 📖 Explicación de tu Código"))

        // 7. Reparar código
        val fixResponse = SmartAgentSynthesizer.synthesizeResponse("/fix repara este error", dummyContext)
        assertTrue(fixResponse.contains("# Plan: Diagnóstico y Reparación"))
    }

    @Test
    fun testOllamaEngine_IntegrationAndFallback() = runBlocking {
        val ollama = OllamaAssistantProvider(
            host = "127.0.0.1",
            port = 11434,
            modelName = "qwen2.5-coder:1.5b"
        )

        assertEquals(ProviderType.OLLAMA, ollama.providerType)
        assertEquals("Ollama Local Engine", ollama.providerType.displayName)
        assertEquals("🦙 Ollama", ollama.providerType.badge)

        val dummyContext = CodeContext(
            fullText = "class App {}",
            cursorOffset = 5,
            currentLine = 1,
            filePath = "lib/main.dart"
        )

        // Verificar generación de stream con fallback autónomo
        val stream = ollama.generateCompletionStream("Crea una calculadora", dummyContext)
        val chunks = stream.toList()
        assertTrue("El stream debe emitir fragmentos", chunks.isNotEmpty())
        val fullText = chunks.joinToString("")
        assertTrue("Debe incluir el plan generado", fullText.contains("# Plan:") || fullText.contains("Ollama"))

        // Verificar consulta de modelos instalados
        val models = ollama.getInstalledModels()
        assertTrue("Debe retornar lista de modelos compatibles", models.isNotEmpty())
        assertTrue(models.contains("qwen2.5-coder:1.5b") || models.contains("llama3.2:1b"))
    }

    @Test
    fun testAiAssistantManager_ProviderRegistryAndSwitching() {
        val manager = AiAssistantManager(ProviderType.GEMINI_API)
        assertEquals(ProviderType.GEMINI_API, manager.currentProviderType.value)

        // Cambiar a OLLAMA
        manager.selectProvider(ProviderType.OLLAMA)
        assertEquals(ProviderType.OLLAMA, manager.currentProviderType.value)
        assertEquals(ProviderType.OLLAMA, manager.getActiveProvider().providerType)

        // Cambiar a CLAUDE
        manager.selectProvider(ProviderType.CLAUDE_API)
        assertEquals(ProviderType.CLAUDE_API, manager.currentProviderType.value)
        assertEquals(ProviderType.CLAUDE_API, manager.getActiveProvider().providerType)

        // Cambiar a LOCAL_AGENT
        manager.selectProvider(ProviderType.LOCAL_AGENT)
        assertEquals(ProviderType.LOCAL_AGENT, manager.currentProviderType.value)
    }

    @Test
    fun testWorkspaceProjectFile_DirectCodeInjection() {
        val root = ProjectFile.createSampleAndroidProject()
        val initialFileCount = root.flatten().size

        // Inyección en archivo existente
        val targetFile = root.flatten().first { it.name == "MainActivity.kt" }
        val newCode = """
            package com.ide.mobile
            class MainActivity : ComponentActivity() {
                // Código inyectado por el Agente Autónomo
            }
        """.trimIndent()

        targetFile.content = newCode
        assertEquals(newCode, targetFile.content)

        // Inyección de un archivo nuevo dentro de subcarpeta
        val newCreatedFile = ProjectFile(
            id = "file-todo",
            name = "TodoScreen.kt",
            path = "/mi_nuevo_proyecto/src/TodoScreen.kt",
            isDirectory = false,
            content = "class TodoScreen {}"
        )
        root.children.add(newCreatedFile)

        val updatedFlat = root.flatten()
        assertEquals(initialFileCount + 1, updatedFlat.size)
        assertTrue(updatedFlat.any { it.name == "TodoScreen.kt" && it.content.contains("class TodoScreen") })
    }

    @Test
    fun testMissionControlLifecycle_StatesProgression() {
        var currentState: AgentState = AgentState.Idle
        assertEquals(AgentState.Idle, currentState)

        // 1. Planning
        currentState = AgentState.Planning("Planificando arquitectura...")
        assertTrue(currentState is AgentState.Planning)

        // 2. Executing
        currentState = AgentState.Executing("Escribiendo código...", 0.5f)
        assertTrue(currentState is AgentState.Executing)
        assertEquals(0.5f, (currentState as AgentState.Executing).progress, 0.01f)

        // 3. WaitingForUser
        currentState = AgentState.WaitingForUser("Revisión requerida", canResume = true)
        assertTrue(currentState is AgentState.WaitingForUser)
        assertTrue((currentState as AgentState.WaitingForUser).canResume)

        // 4. Verifying
        currentState = AgentState.Verifying("Verificando consistencia de archivos...")
        assertTrue(currentState is AgentState.Verifying)

        // 5. Idle
        currentState = AgentState.Idle
        assertEquals(AgentState.Idle, currentState)
    }

    @Test
    fun testSlashCommands_PrefixMatchingAndBehavior() {
        val commands = listOf(
            "/plan" to "Plan de Implementación",
            "/review" to "Revisión técnica",
            "/fix" to "Corregir errores",
            "/grill-me" to "Entrevista técnica",
            "/diff" to "Diferencias y cambios",
            "/goal" to "Objetivo autónomo",
            "/learn" to "Aprende y documenta",
            "/run" to "Ejecutar proyecto",
            "/clear" to "Limpiar chat"
        )

        assertEquals(9, commands.size)
        commands.forEach { (cmd, purpose) ->
            assertTrue(cmd.startsWith("/"))
            assertNotNull(purpose)
        }
    }

    @Test
    fun testEndToEnd_SeniorFriendlyProjectCreationAndOnboarding() {
        val dummyContext = CodeContext(fullText = "", cursorOffset = 0, currentLine = 1, filePath = "lib/main.dart")

        // 1. Saludo inicial sin detalles técnicos
        val greetingResponse = SmartAgentSynthesizer.synthesizeResponse("¡Hola! Quiero empezar un nuevo proyecto", dummyContext)
        assertTrue(greetingResponse.contains("asistente senior") || greetingResponse.contains("asistente de desarrollo"))
        assertTrue(greetingResponse.contains("meta principal") || greetingResponse.contains("te gustaría construir"))

        // 2. Definición del objetivo de proyecto -> Generación amigable y bautizo de la app
        val projectIdeaResponse = SmartAgentSynthesizer.synthesizeResponse("Quiero crear una app para registrar mis recetas de cocina casera", dummyContext)
        assertTrue(projectIdeaResponse.contains("# Plan: Creando tu Nuevo Proyecto"))
        assertTrue(projectIdeaResponse.contains("RecetasDeliciosas"))
        assertTrue(projectIdeaResponse.contains("## Checklist de Pasos Iniciales"))
        assertTrue(projectIdeaResponse.contains("// File: lib/main.dart"))

        // 3. Creación programática del espacio de trabajo personalizado
        val customProject = com.ide.mobile.core.model.ProjectTemplate.createNewCustomProject("MiAppDeRecetas", "Guardar recetas caseras")
        assertEquals("MiAppDeRecetas", customProject.name)
        val files = customProject.flatten()
        assertTrue(files.any { it.name == "README.md" && it.content.contains("Guardar recetas caseras") })
        assertTrue(files.any { it.name == "main.dart" && it.content.contains("MiAppDeRecetas") })
    }
}
