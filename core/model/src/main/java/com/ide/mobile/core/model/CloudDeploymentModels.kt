package com.ide.mobile.core.model

/**
 * Configuración del repositorio remoto de GitHub y credenciales de acceso.
 */
data class GitHubConfig(
    val remoteUrl: String = "https://github.com/eliotcompe85-web/Black-cat-IDE.git",
    val personalAccessToken: String = "",
    val defaultBranch: String = "main",
    val authorName: String = "J.Compe",
    val authorEmail: String = "developer@blackcatide.com",
    val isConfigured: Boolean = true
)

/**
 * Configuración de despliegue para Expo Dev (React Native / Mobile).
 */
data class ExpoDevConfig(
    val expoToken: String = "",
    val projectSlug: String = "black-cat-mobile-app",
    val releaseChannel: String = "preview",
    val buildProfile: String = "android-apk",
    val isConfigured: Boolean = false
)

/**
 * Configuración de despliegue para Railway (Cloud Backend & Databases).
 */
data class RailwayConfig(
    val railwayToken: String = "",
    val projectId: String = "black-cat-backend",
    val environment: String = "production",
    val serviceName: String = "api-service",
    val isConfigured: Boolean = false
)

/**
 * Plataformas de despliegue soportadas.
 */
enum class DeploymentTarget {
    GITHUB,
    EXPO_DEV,
    RAILWAY
}

/**
 * Estados del ciclo de vida de un despliegue en la nube.
 */
enum class DeploymentStatus {
    IDLE,
    DEPLOYING,
    SUCCESS,
    FAILED
}

/**
 * Resultado o registro de un despliegue en la nube.
 */
data class DeploymentRecord(
    val id: String = System.currentTimeMillis().toString(),
    val target: DeploymentTarget,
    val status: DeploymentStatus,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val logs: List<String> = emptyList()
)
