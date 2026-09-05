package com.ide.mobile.core.model

import java.util.UUID

/**
 * Rol del emisor del mensaje en la conversación.
 */
enum class MessageSender {
    USER,
    AGENT,
    SYSTEM
}

/**
 * Tipo de acción o herramienta ejecutable por el agente.
 */
enum class AgentActionType {
    RUN_COMMAND,          // Comando en terminal Bash (ej. git, flutter, ls)
    CREATE_FOLDER,        // Crear directorio en el proyecto
    CREATE_FILE,          // Crear nuevo archivo con contenido
    MODIFY_FILE,          // Modificar archivo existente
    INSTALL_DEPENDENCY,   // Instalar dependencia/paquete (flutter pub add, npm i)
    HTTP_REQUEST          // Realizar solicitud/petición web HTTP
}

/**
 * Estado del ciclo de vida de una acción del agente.
 */
enum class ActionStatus {
    PROPOSED,   // Propuesto por el agente, esperando decisión del usuario
    RUNNING,    // Ejecutándose en vivo
    SUCCESS,    // Ejecutado exitosamente
    FAILED,     // Falló con error
    REJECTED    // Descartado por el usuario
}

/**
 * Acción estructurada propuesta o ejecutada por el agente.
 */
data class AgentAction(
    val id: String = UUID.randomUUID().toString(),
    val type: AgentActionType,
    val title: String,
    val description: String? = null,
    val payload: String,                 // Comando bash, contenido de archivo o URL
    val targetPath: String? = null,      // Ruta destino de archivo o carpeta
    val status: ActionStatus = ActionStatus.PROPOSED,
    val output: String? = null           // Salida de la consola o respuesta HTTP
)

/**
 * Mensaje individual dentro del historial de conversación fluida.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val agentName: String? = null,
    val agentIcon: String? = null,
    val actions: List<AgentAction> = emptyList(),
    val isStreaming: Boolean = false
)

/**
 * Agente Antigravity descargable con especificación completa y lista para activar.
 */
data class DownloadableAgent(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,
    val skills: List<String>,
    val systemPrompt: String,
    val recommendedModel: String,
    val downloadSize: String = "12 KB",
    val isDownloaded: Boolean = false
)
