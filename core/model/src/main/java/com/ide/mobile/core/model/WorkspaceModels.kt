package com.ide.mobile.core.model

import java.util.UUID

/**
 * Representa un proyecto registrado en el espacio de trabajo (Workspace)
 * de Black Cat IDE.
 */
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val projectType: ProjectTemplateType = ProjectTemplateType.FLUTTER_MOBILE,
    val gitBranch: String? = null,
    val isFavorite: Boolean = false
) {
    val displayPath: String
        get() = if (path.length > 35) "..." + path.takeLast(32) else path

    val formattedDate: String
        get() {
            val seconds = (System.currentTimeMillis() - lastOpenedTimestamp) / 1000
            return when {
                seconds < 60 -> "Hace un momento"
                seconds < 3600 -> "Hace ${seconds / 60} min"
                seconds < 86400 -> "Hace ${seconds / 3600} h"
                else -> "Hace ${seconds / 86400} días"
            }
        }
}

/**
 * Estado inmutable y reactivo del espacio de trabajo.
 */
data class WorkspaceState(
    val currentProject: Project? = null,
    val recentProjects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
