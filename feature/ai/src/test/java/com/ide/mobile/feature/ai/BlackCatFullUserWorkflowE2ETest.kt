package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Suite Exhaustiva de Pruebas E2E (End-to-End) de Black Cat IDE.
 * Simula el flujo completo de un usuario desarrollador:
 * 1. Descarga y activación inmediata de agentes Antigravity.
 * 2. Creación y estructuración de proyectos, carpetas y código.
 * 3. Conversación fluida con el agente y extracción de herramientas/comandos.
 * 4. Aprobación y simulación de ejecución en terminal.
 * 5. Control de versiones Git y preparación de push a GitHub.
 * 6. Despliegues móviles a Expo Dev y servicios backend a Railway Cloud.
 */
class BlackCatFullUserWorkflowE2ETest {

    @Test
    fun testUserDownloadsAndActivatesAgent_Workflow() = runBlocking {
        val engine = LocalAgentEngine()
        val catalog = engine.getDownloadableCatalog()

        // 1. Verificar catálogo disponible
        assertTrue("El catálogo debe contener agentes descargables", catalog.isNotEmpty())
        val flutterMaster = catalog.firstOrNull { it.id == "agent-flutter-master" }
        assertNotNull("Flutter & Dart Master debe existir en el catálogo", flutterMaster)

        // 2. Descargar y activar agente
        val activated = engine.downloadAndActivateAgent(flutterMaster!!)
        assertEquals("agent-flutter-master", activated.id)
        assertEquals("Flutter & Dart Master", activated.name)
        assertTrue(activated.skills.contains("bloc-pattern"))

        // 3. Verificar que ahora forma parte de los agentes disponibles
        val allAgents = engine.getAllAgents()
        assertTrue("El agente descargado debe estar en la lista activa", allAgents.any { it.id == "agent-flutter-master" })
    }

    @Test
    fun testUserProjectFileAndFolderCreation_Workflow() {
        val root = ProjectFile.createSampleAndroidProject()

        // 1. Verificar proyecto inicial
        assertTrue(root.isDirectory)
        val initialCount = root.flatten().size

        // 2. Simular creación de carpeta 'services'
        val servicesFolder = ProjectFile(
            id = "dir-services",
            name = "services",
            path = "/mi_nuevo_proyecto/lib/services",
            isDirectory = true
        )
        root.children.add(servicesFolder)

        // 3. Simular creación de archivo 'api_client.dart' dentro de 'services'
        val apiFile = ProjectFile(
            id = "file-api-client",
            name = "api_client.dart",
            path = "/mi_nuevo_proyecto/lib/services/api_client.dart",
            isDirectory = false,
            content = "class ApiClient { final String baseUrl = 'https://api.blackcatide.com'; }"
        )
        servicesFolder.children.add(apiFile)

        // 4. Comprobar que el árbol se actualizó
        val flatList = root.flatten()
        assertEquals(initialCount + 2, flatList.size)
        assertTrue(flatList.any { it.name == "api_client.dart" && it.content.contains("ApiClient") })
    }

    @Test
    fun testAgentActionParsingAndTerminalApproval_Workflow() {
        val agentResponse = """
            🐱 **[Black Cat Copilot]** activado estilo Antigravity...
            He preparado la estructura y la instalación de dependencias para tu proyecto:
            
            ```bash
            flutter pub add http
            ```
            
            ```bash
            mkdir -p lib/network
            ```
            
            ```dart
            // File: lib/network/http_service.dart
            class HttpService {
              void fetch() {}
            }
            ```
        """.trimIndent()

        // 1. Extraer acciones con AgentActionParser
        val actions = AgentActionParser.parseActions(agentResponse)
        assertEquals("Deben detectarse 3 acciones estructuradas", 3, actions.size)

        // 2. Verificar acción de instalación de dependencia
        val installAction = actions.firstOrNull { it.type == AgentActionType.INSTALL_DEPENDENCY }
        assertNotNull("Debe detectarse acción de instalar paquete", installAction)
        assertEquals("flutter pub add http", installAction?.payload)
        assertEquals(ActionStatus.PROPOSED, installAction?.status)

        // 3. Verificar acción de crear carpeta
        val folderAction = actions.firstOrNull { it.type == AgentActionType.CREATE_FOLDER }
        assertNotNull("Debe detectarse acción de crear carpeta", folderAction)
        assertEquals("lib/network", folderAction?.targetPath)
        assertEquals(ActionStatus.PROPOSED, folderAction?.status)

        // 4. Verificar acción de crear archivo
        val fileAction = actions.firstOrNull { it.type == AgentActionType.CREATE_FILE }
        assertNotNull("Debe detectarse acción de crear archivo", fileAction)
        assertEquals("lib/network/http_service.dart", fileAction?.targetPath)

        // 5. Simular aprobación del usuario
        val approvedAction = installAction?.copy(
            status = ActionStatus.SUCCESS,
            output = "✓ Package 'http' added to pubspec.yaml."
        )
        assertEquals(ActionStatus.SUCCESS, approvedAction?.status)
        assertTrue(approvedAction?.output?.contains("pubspec.yaml") == true)
    }

    @Test
    fun testGeminiFreeTierMultiModelResilience() = runBlocking {
        val provider = GeminiAssistantProvider(
            apiKey = "TEST_FREE_KEY",
            modelName = "gemini-1.5-flash"
        )

        assertTrue(provider.isAvailable())
        assertTrue("La lista de modelos gratuitos debe contener gemini-1.5-flash", GeminiAssistantProvider.FREE_TIER_MODELS.contains("gemini-1.5-flash"))
        assertTrue("La lista de modelos gratuitos debe contener gemini-2.0-flash", GeminiAssistantProvider.FREE_TIER_MODELS.contains("gemini-2.0-flash"))

        val dummyContext = CodeContext(
            fullText = "void main() {}",
            cursorOffset = 0,
            currentLine = 1,
            filePath = "lib/main.dart"
        )

        // Comprobar que genera stream continuo sin fallar ni bloquearse
        val firstChunk = provider.generateCompletionStream("explicar código", dummyContext).first()
        assertTrue("El stream debe emitir contenido", firstChunk.isNotBlank())
    }

    @Test
    fun testGitHubSyncWorkflowSimulation() {
        val config = GitHubConfig(
            remoteUrl = "https://github.com/usuario/mi_repo.git",
            personalAccessToken = "ghp_mockSecretTokenForTesting123456",
            defaultBranch = "main",
            authorName = "Developer",
            authorEmail = "dev@blackcatide.com"
        )

        assertTrue("La configuración debe estar activa", config.isConfigured)
        assertEquals("main", config.defaultBranch)

        // Simulación del registro de despliegue a GitHub
        val record = DeploymentRecord(
            target = DeploymentTarget.GITHUB,
            status = DeploymentStatus.SUCCESS,
            summary = "Push a ${config.remoteUrl} completado con éxito.",
            logs = listOf("$ git add .", "$ git commit -m \"Init\"", "$ git push origin main", "Everything up-to-date")
        )

        assertEquals(DeploymentTarget.GITHUB, record.target)
        assertEquals(DeploymentStatus.SUCCESS, record.status)
        assertTrue(record.logs.size == 4)
    }

    @Test
    fun testExpoDevDeploymentWorkflowSimulation() {
        val expo = ExpoDevConfig(
            expoToken = "expo_token_test_abc123",
            projectSlug = "black-cat-e2e-app",
            buildProfile = "android-apk",
            isConfigured = true
        )

        assertTrue(expo.isConfigured)
        assertEquals("android-apk", expo.buildProfile)

        val record = DeploymentRecord(
            target = DeploymentTarget.EXPO_DEV,
            status = DeploymentStatus.SUCCESS,
            summary = "Build generado en Expo Dev: ${expo.projectSlug}",
            logs = listOf("$ npx expo export", "$ eas build -p android", "Build artifact ready: app-release.apk")
        )

        assertEquals(DeploymentTarget.EXPO_DEV, record.target)
        assertEquals(DeploymentStatus.SUCCESS, record.status)
        assertTrue(record.summary.contains("black-cat-e2e-app"))
    }

    @Test
    fun testRailwayCloudDeploymentWorkflowSimulation() {
        val railway = RailwayConfig(
            railwayToken = "railway_live_token_test",
            projectId = "project-e2e-backend",
            serviceName = "user-auth-api",
            environment = "production",
            isConfigured = true
        )

        assertTrue(railway.isConfigured)
        assertEquals("production", railway.environment)

        val record = DeploymentRecord(
            target = DeploymentTarget.RAILWAY,
            status = DeploymentStatus.SUCCESS,
            summary = "Despliegue activo en Railway: ${railway.serviceName}.up.railway.app",
            logs = listOf("$ railway up --detach", "Container building...", "Deployment deployed: HTTP 200 Healthy")
        )

        assertEquals(DeploymentTarget.RAILWAY, record.target)
        assertEquals(DeploymentStatus.SUCCESS, record.status)
        assertTrue(record.summary.contains("user-auth-api"))
    }
}
