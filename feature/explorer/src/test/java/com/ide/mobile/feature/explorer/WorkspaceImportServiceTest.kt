package com.ide.mobile.feature.explorer

import com.ide.mobile.core.model.ProjectTemplateType
import com.ide.mobile.core.model.RoomWorkspaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Pruebas unitarias para WorkspaceImportService:
 * Validación de rutas, auto-detección de marcadores (build.gradle.kts, pubspec.yaml, package.json, requirements.txt)
 * e importación reactiva al repositorio de Workspace.
 */
class WorkspaceImportServiceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testDetectFlutterProjectMarker() {
        val projectDir = tempFolder.newFolder("MyFlutterApp")
        File(projectDir, "pubspec.yaml").writeText("name: my_flutter_app\n")

        val repository = RoomWorkspaceRepository()
        val service = WorkspaceImportService(repository)

        val detectedType = service.detectProjectType(projectDir)
        assertEquals(ProjectTemplateType.FLUTTER_MOBILE, detectedType)
    }

    @Test
    fun testDetectExpoReactNativeProjectMarker() {
        val projectDir = tempFolder.newFolder("MyExpoApp")
        File(projectDir, "app.json").writeText("{\"expo\": {\"name\": \"MyExpoApp\"}}")

        val repository = RoomWorkspaceRepository()
        val service = WorkspaceImportService(repository)

        val detectedType = service.detectProjectType(projectDir)
        assertEquals(ProjectTemplateType.EXPO_REACT_NATIVE, detectedType)
    }

    @Test
    fun testDetectRailwayExpressProjectMarker() {
        val projectDir = tempFolder.newFolder("MyRailwayBackend")
        File(projectDir, "railway.json").writeText("{\"\$schema\": \"https://railway.app/railway.schema.json\"}")

        val repository = RoomWorkspaceRepository()
        val service = WorkspaceImportService(repository)

        val detectedType = service.detectProjectType(projectDir)
        assertEquals(ProjectTemplateType.RAILWAY_NODE_API, detectedType)
    }

    @Test
    fun testDetectPythonAiProjectMarker() {
        val projectDir = tempFolder.newFolder("MyPythonService")
        File(projectDir, "requirements.txt").writeText("fastapi==0.104.0\nuvicorn==0.24.0\n")

        val repository = RoomWorkspaceRepository()
        val service = WorkspaceImportService(repository)

        val detectedType = service.detectProjectType(projectDir)
        assertEquals(ProjectTemplateType.PYTHON_AI_SERVICE, detectedType)
    }

    @Test
    fun testImportProjectFlowUpdatesRepository() = runTest {
        val projectDir = tempFolder.newFolder("MobileBankingApp")
        File(projectDir, "pubspec.yaml").writeText("name: banking_app\n")

        val repository = RoomWorkspaceRepository()
        val service = WorkspaceImportService(repository)

        val result = service.importProject(projectDir.absolutePath, "Banking App Official")
        assertTrue(result.isSuccess)

        val project = result.getOrThrow()
        assertEquals("Banking App Official", project.name)
        assertEquals(projectDir.absolutePath, project.path)
        assertEquals(ProjectTemplateType.FLUTTER_MOBILE, project.projectType)

        // Verificar que el repositorio ahora lo tiene registrado
        val recentList = repository.getRecentProjects()
        assertTrue(recentList.any { it.id == project.id && it.name == "Banking App Official" })
    }

    @Test
    fun testValidateNonExistentDirectoryFails() {
        val repository = RoomWorkspaceRepository()
        val service = WorkspaceImportService(repository)

        val result = service.validateDirectory("/ruta/inexistente/blackcat_123456")
        assertTrue(result.isFailure)
    }
}
