package com.ide.mobile.core.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Contrato del repositorio para la administración del espacio de trabajo y persistencia
 * de proyectos recientes en Black Cat IDE.
 */
interface WorkspaceRepository {
    val workspaceState: StateFlow<WorkspaceState>
    val recentProjects: kotlinx.coroutines.flow.Flow<List<ProjectEntity>>

    suspend fun openOrCreateLocalProject(name: String, path: String)
    suspend fun importProjectFromDirectory(directoryUri: android.net.Uri, projectName: String)
    suspend fun getRecentProjects(): List<Project>
    suspend fun saveProject(project: Project)
    suspend fun deleteProject(projectId: String)
    suspend fun setCurrentProject(project: Project)
    suspend fun toggleFavorite(projectId: String)
    suspend fun loadInitialWorkspace()
}

/**
 * Entidad y esquema relacional de base de datos estilo Room para persistencia
 * en almacenamiento local de Android.
 */
object ProjectContract {
    const val DATABASE_NAME = "blackcat_workspace.db"
    const val DATABASE_VERSION = 1
    const val TABLE_PROJECTS = "projects"

    const val COLUMN_ID = "id"
    const val COLUMN_NAME = "name"
    const val COLUMN_PATH = "path"
    const val COLUMN_LAST_OPENED = "last_opened_timestamp"
    const val COLUMN_PROJECT_TYPE = "project_type"
    const val COLUMN_GIT_BRANCH = "git_branch"
    const val COLUMN_IS_FAVORITE = "is_favorite"

    const val SQL_CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_PROJECTS (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_PATH TEXT NOT NULL UNIQUE,
            $COLUMN_LAST_OPENED INTEGER NOT NULL,
            $COLUMN_PROJECT_TYPE TEXT NOT NULL,
            $COLUMN_GIT_BRANCH TEXT,
            $COLUMN_IS_FAVORITE INTEGER NOT NULL DEFAULT 0
        )
    """

    const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_PROJECTS"
}

/**
 * Helper de SQLite con arquitectura Room para el ciclo de vida de la base de datos.
 */
class WorkspaceDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    ProjectContract.DATABASE_NAME,
    null,
    ProjectContract.DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ProjectContract.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(ProjectContract.SQL_DROP_TABLE)
        onCreate(db)
    }
}

/**
 * Implementación de repositorio respaldada por base de datos SQLite con patrón Room / DAO.
 */
class RoomWorkspaceRepository(
    private val dbHelper: WorkspaceDatabaseHelper? = null,
    private val projectDao: ProjectDao? = null
) : WorkspaceRepository {

    private val _workspaceState = MutableStateFlow(WorkspaceState())
    override val workspaceState: StateFlow<WorkspaceState> = _workspaceState.asStateFlow()

    override val recentProjects: kotlinx.coroutines.flow.Flow<List<ProjectEntity>> =
        projectDao?.getAllProjects() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun openOrCreateLocalProject(name: String, path: String) {
        val project = ProjectEntity(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            rootPath = path,
            lastOpenedTimestamp = System.currentTimeMillis(),
            isRemote = false
        )
        projectDao?.insertProject(project)
        saveProject(
            Project(
                id = project.id,
                name = project.name,
                path = project.rootPath,
                lastOpenedTimestamp = project.lastOpenedTimestamp,
                projectType = ProjectTemplateType.FLUTTER_MOBILE
            )
        )
    }

    override suspend fun importProjectFromDirectory(directoryUri: android.net.Uri, projectName: String) {
        val path = directoryUri.toString()
        openOrCreateLocalProject(projectName, path)
    }

    // Memoria caché de respaldo si el helper es nulo (pruebas unitarias puras)
    private val inMemoryProjects = mutableMapOf<String, Project>()

    init {
        // Inicializar proyectos por defecto si está vacío
        seedDefaultProjectsIfNeeded()
    }

    private fun seedDefaultProjectsIfNeeded() {
        val defaultProjects = listOf(
            Project(
                id = "default-blackcat-ide",
                name = "Black-cat-IDE",
                path = "/storage/emulated/0/Black-cat-IDE",
                projectType = ProjectTemplateType.FLUTTER_MOBILE,
                gitBranch = "main",
                isFavorite = true
            ),
            Project(
                id = "demo-expo-app",
                name = "MobileStore-Expo",
                path = "/storage/emulated/0/Projects/MobileStore-Expo",
                projectType = ProjectTemplateType.EXPO_REACT_NATIVE,
                gitBranch = "feature/checkout",
                isFavorite = false
            ),
            Project(
                id = "demo-railway-api",
                name = "CloudBackend-Railway",
                path = "/storage/emulated/0/Projects/CloudBackend-Railway",
                projectType = ProjectTemplateType.RAILWAY_NODE_API,
                gitBranch = "main",
                isFavorite = false
            )
        )
        defaultProjects.forEach { inMemoryProjects[it.id] = it }
        _workspaceState.update {
            it.copy(
                currentProject = defaultProjects.first(),
                recentProjects = defaultProjects
            )
        }
    }

    override suspend fun loadInitialWorkspace() = withContext(Dispatchers.IO) {
        _workspaceState.update { it.copy(isLoading = true) }
        try {
            val projects = getRecentProjects()
            _workspaceState.update {
                it.copy(
                    recentProjects = projects,
                    currentProject = it.currentProject ?: projects.firstOrNull(),
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _workspaceState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    override suspend fun getRecentProjects(): List<Project> = withContext(Dispatchers.IO) {
        val helper = dbHelper ?: return@withContext inMemoryProjects.values
            .sortedByDescending { it.lastOpenedTimestamp }

        val list = mutableListOf<Project>()
        try {
            val db = helper.readableDatabase
            val cursor = db.query(
                ProjectContract.TABLE_PROJECTS,
                null,
                null,
                null,
                null,
                null,
                "${ProjectContract.COLUMN_LAST_OPENED} DESC"
            )
            cursor.use { c ->
                while (c.moveToNext()) {
                    val id = c.getString(c.getColumnIndexOrThrow(ProjectContract.COLUMN_ID))
                    val name = c.getString(c.getColumnIndexOrThrow(ProjectContract.COLUMN_NAME))
                    val path = c.getString(c.getColumnIndexOrThrow(ProjectContract.COLUMN_PATH))
                    val timestamp = c.getLong(c.getColumnIndexOrThrow(ProjectContract.COLUMN_LAST_OPENED))
                    val typeStr = c.getString(c.getColumnIndexOrThrow(ProjectContract.COLUMN_PROJECT_TYPE))
                    val branch = c.getString(c.getColumnIndexOrThrow(ProjectContract.COLUMN_GIT_BRANCH))
                    val isFav = c.getInt(c.getColumnIndexOrThrow(ProjectContract.COLUMN_IS_FAVORITE)) == 1

                    val templateType = runCatching { ProjectTemplateType.valueOf(typeStr) }
                        .getOrDefault(ProjectTemplateType.FLUTTER_MOBILE)

                    list.add(
                        Project(
                            id = id,
                            name = name,
                            path = path,
                            lastOpenedTimestamp = timestamp,
                            projectType = templateType,
                            gitBranch = branch,
                            isFavorite = isFav
                        )
                    )
                }
            }
            if (list.isEmpty()) {
                // Copiar proyectos por defecto si la base está vacía
                inMemoryProjects.values.forEach { saveProject(it) }
                return@withContext inMemoryProjects.values.sortedByDescending { it.lastOpenedTimestamp }
            }
        } catch (e: Exception) {
            return@withContext inMemoryProjects.values.sortedByDescending { it.lastOpenedTimestamp }
        }
        list
    }

    override suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        inMemoryProjects[project.id] = project
        val helper = dbHelper
        if (helper != null) {
            try {
                val db = helper.writableDatabase
                val values = ContentValues().apply {
                    put(ProjectContract.COLUMN_ID, project.id)
                    put(ProjectContract.COLUMN_NAME, project.name)
                    put(ProjectContract.COLUMN_PATH, project.path)
                    put(ProjectContract.COLUMN_LAST_OPENED, project.lastOpenedTimestamp)
                    put(ProjectContract.COLUMN_PROJECT_TYPE, project.projectType.name)
                    put(ProjectContract.COLUMN_GIT_BRANCH, project.gitBranch)
                    put(ProjectContract.COLUMN_IS_FAVORITE, if (project.isFavorite) 1 else 0)
                }
                db.insertWithOnConflict(
                    ProjectContract.TABLE_PROJECTS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            } catch (_: Exception) { }
        }
        val updatedList = getRecentProjects()
        _workspaceState.update {
            it.copy(
                recentProjects = updatedList,
                currentProject = if (it.currentProject?.id == project.id) project else it.currentProject
            )
        }
    }

    override suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        inMemoryProjects.remove(projectId)
        dbHelper?.let { helper ->
            try {
                val db = helper.writableDatabase
                db.delete(
                    ProjectContract.TABLE_PROJECTS,
                    "${ProjectContract.COLUMN_ID} = ?",
                    arrayOf(projectId)
                )
            } catch (_: Exception) { }
        }
        val updatedList = getRecentProjects()
        _workspaceState.update {
            it.copy(
                recentProjects = updatedList,
                currentProject = if (it.currentProject?.id == projectId) updatedList.firstOrNull() else it.currentProject
            )
        }
    }

    override suspend fun setCurrentProject(project: Project) = withContext(Dispatchers.IO) {
        val updated = project.copy(lastOpenedTimestamp = System.currentTimeMillis())
        saveProject(updated)
        _workspaceState.update { it.copy(currentProject = updated) }
    }

    override suspend fun toggleFavorite(projectId: String) = withContext(Dispatchers.IO) {
        val existing = inMemoryProjects[projectId] ?: getRecentProjects().find { it.id == projectId }
        if (existing != null) {
            val updated = existing.copy(isFavorite = !existing.isFavorite)
            saveProject(updated)
        }
    }
}

/**
 * Repositorio de Workspace directo respaldado por Room ProjectDao.
 */
class DirectWorkspaceRepository(private val projectDao: ProjectDao) {

    val recentProjects: kotlinx.coroutines.flow.Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun openOrCreateLocalProject(name: String, path: String) {
        val project = ProjectEntity(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            rootPath = path,
            lastOpenedTimestamp = System.currentTimeMillis(),
            isRemote = false
        )
        projectDao.insertProject(project)
    }

    suspend fun importProjectFromDirectory(directoryUri: android.net.Uri, projectName: String) {
        val path = directoryUri.toString()
        openOrCreateLocalProject(projectName, path)
    }
}
