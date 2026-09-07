package com.ide.mobile.feature.explorer

import com.ide.mobile.core.model.Project
import com.ide.mobile.core.model.ProjectTemplateType
import com.ide.mobile.core.model.WorkspaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Servicio de validación, auto-detección e importación de proyectos locales
 * al repositorio de Workspace en Black Cat IDE.
 */
class WorkspaceImportService(
    private val workspaceRepository: WorkspaceRepository
) {

    /**
     * Valida que la ruta exista en el sistema de archivos y sea un directorio accesible.
     */
    fun validateDirectory(path: String): Result<File> {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("La ruta del directorio no puede estar vacía."))
        }
        val file = File(trimmed)
        if (!file.exists()) {
            return Result.failure(IllegalArgumentException("El directorio especificado no existe: $trimmed"))
        }
        if (!file.isDirectory) {
            return Result.failure(IllegalArgumentException("La ruta especificada no es un directorio: $trimmed"))
        }
        return Result.success(file)
    }

    /**
     * Detecta automáticamente la tecnología del proyecto inspeccionando archivos marcadores
     * como build.gradle.kts, pubspec.yaml, package.json o requirements.txt.
     */
    fun detectProjectType(directory: File): ProjectTemplateType {
        val markerFiles = directory.listFiles()?.map { it.name.lowercase() }?.toSet() ?: emptySet()

        return when {
            "pubspec.yaml" in markerFiles -> ProjectTemplateType.FLUTTER_MOBILE
            "app.json" in markerFiles || "eas.json" in markerFiles -> ProjectTemplateType.EXPO_REACT_NATIVE
            "railway.json" in markerFiles || "dockerfile" in markerFiles || "procfile" in markerFiles -> ProjectTemplateType.RAILWAY_NODE_API
            "requirements.txt" in markerFiles || "main.py" in markerFiles -> ProjectTemplateType.PYTHON_AI_SERVICE
            "build.gradle.kts" in markerFiles || "build.gradle" in markerFiles -> ProjectTemplateType.FLUTTER_MOBILE
            "package.json" in markerFiles -> {
                // Verificar si package.json menciona react-native o expo
                val pkgJson = File(directory, "package.json")
                if (pkgJson.exists() && runCatching { pkgJson.readText() }.getOrDefault("").contains("expo")) {
                    ProjectTemplateType.EXPO_REACT_NATIVE
                } else {
                    ProjectTemplateType.RAILWAY_NODE_API
                }
            }
            else -> ProjectTemplateType.FLUTTER_MOBILE
        }
    }

    /**
     * Intenta detectar la rama Git activa leyendo el archivo .git/HEAD.
     */
    fun detectGitBranch(directory: File): String? {
        val gitHead = File(directory, ".git/HEAD")
        if (gitHead.exists() && gitHead.isFile) {
            val content = runCatching { gitHead.readText().trim() }.getOrNull() ?: return null
            if (content.startsWith("ref: refs/heads/")) {
                return content.removePrefix("ref: refs/heads/")
            }
        }
        return null
    }

    /**
     * Ejecuta el flujo completo de validación, detección e inserción del proyecto
     * en el repositorio de Workspace.
     */
    suspend fun importProject(
        path: String,
        customName: String? = null
    ): Result<Project> = withContext(Dispatchers.IO) {
        val validation = validateDirectory(path)
        if (validation.isFailure) {
            return@withContext Result.failure(validation.exceptionOrNull()!!)
        }

        val dir = validation.getOrThrow()
        val projectName = customName?.takeIf { it.isNotBlank() } ?: dir.name.ifBlank { "Nuevo Proyecto" }
        val projectType = detectProjectType(dir)
        val branch = detectGitBranch(dir)

        val project = Project(
            id = UUID.randomUUID().toString(),
            name = projectName,
            path = dir.absolutePath,
            lastOpenedTimestamp = System.currentTimeMillis(),
            projectType = projectType,
            gitBranch = branch,
            isFavorite = false
        )

        try {
            workspaceRepository.saveProject(project)
            workspaceRepository.setCurrentProject(project)
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
