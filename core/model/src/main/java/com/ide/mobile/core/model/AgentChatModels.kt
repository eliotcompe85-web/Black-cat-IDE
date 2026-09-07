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
 * Elementos individuales de un Checklist interactivo en el chat del agente.
 */
data class ChecklistTask(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val isCompleted: Boolean,
    val requiresHuman: Boolean // True si el agente está bloqueado esperando que el usuario lo haga
)

/**
 * Contenido estructurado de un mensaje en el chat del asistente IA (Artefactos y Texto).
 */
sealed interface ChatContent {
    data class Text(val text: String) : ChatContent
    data class Prose(val text: String) : ChatContent // Texto normal renderizado en Markdown

    data class PlanArtifact(
        val title: String,
        val summary: String
    ) : ChatContent

    data class ActionChecklist(
        val title: String,
        val tasks: List<ChecklistTask>
    ) : ChatContent

    data class CodeBlock(
        val code: String,
        val language: String = "kotlin",
        val targetFilePath: String? = null // Si existe, permite inyectarlo directamente al espacio de trabajo
    ) : ChatContent

    data class ActionCard(val action: AgentAction) : ChatContent
}

/**
 * Mensaje individual dentro del historial de conversación fluida.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender = MessageSender.USER,
    val text: String = "",
    val contents: List<ChatContent> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val agentName: String? = null,
    val agentIcon: String? = null,
    val actions: List<AgentAction> = emptyList(),
    val isStreaming: Boolean = false,
    val senderName: String = agentName ?: if (sender == MessageSender.USER) "Usuario" else "Agente",
    val isFromUser: Boolean = (sender == MessageSender.USER)
) {
    /**
     * Obtiene la lista de contenidos estructurados. Si [contents] está vacío pero [text]
     * contiene texto o bloques de código markdown (```lenguaje ... ```),
     * los analiza dinámicamente y los combina con [actions].
     */
    val resolvedContents: List<ChatContent>
        get() {
            if (contents.isNotEmpty()) return contents
            val result = mutableListOf<ChatContent>()
            if (text.isNotBlank()) {
                val codeBlockRegex = "```([a-zA-Z0-9_\\-]*)\\n([\\s\\S]*?)```".toRegex()
                var lastIndex = 0
                for (match in codeBlockRegex.findAll(text)) {
                    val preText = text.substring(lastIndex, match.range.first).trim()
                    if (preText.isNotEmpty()) {
                        result.add(ChatContent.Text(preText))
                    }
                    val lang = match.groupValues[1].ifBlank { "kotlin" }
                    val code = match.groupValues[2].trimEnd()
                    result.add(ChatContent.CodeBlock(code = code, language = lang))
                    lastIndex = match.range.last + 1
                }
                if (lastIndex < text.length) {
                    val remaining = text.substring(lastIndex).trim()
                    if (remaining.isNotEmpty()) {
                        result.add(ChatContent.Text(remaining))
                    }
                }
            }
            actions.forEach { action ->
                result.add(ChatContent.ActionCard(action))
            }
            return result
        }
}

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

