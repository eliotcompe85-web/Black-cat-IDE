package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.ActionStatus
import com.ide.mobile.core.model.AgentAction
import com.ide.mobile.core.model.AgentActionType
import java.util.UUID

/**
 * Analizador y extractor inteligente de acciones y herramientas para el Agente Antigravity.
 * Detecta comandos de terminal, creación/modificación de carpetas y ficheros, dependencias y peticiones web.
 */
object AgentActionParser {

    /**
     * Extrae acciones estructuradas de un texto (respuesta del agente o prompt del usuario).
     */
    fun parseActions(text: String): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()

        // 1. Detección de comandos en bloques de código bash / sh
        val bashBlockRegex = Regex("```(?:bash|sh|shell|console|terminal)\\s*\\n([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        for (match in bashBlockRegex.findAll(text)) {
            val commandLines = match.groupValues[1].lines()
                .map { it.trim().removePrefix("$").trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }

            for (cmd in commandLines) {
                if (cmd.startsWith("flutter pub add ") || cmd.startsWith("npm install ") || cmd.startsWith("npm i ")) {
                    val pkgName = cmd.substringAfterLast(" ")
                    actions.add(
                        AgentAction(
                            id = UUID.randomUUID().toString(),
                            type = AgentActionType.INSTALL_DEPENDENCY,
                            title = "Instalar paquete `$pkgName`",
                            description = "Instala la librería en el proyecto mediante el gestor de paquetes.",
                            payload = cmd,
                            status = ActionStatus.PROPOSED
                        )
                    )
                } else if (cmd.startsWith("mkdir ")) {
                    val folderPath = cmd.removePrefix("mkdir ").removePrefix("-p ").trim()
                    actions.add(
                        AgentAction(
                            id = UUID.randomUUID().toString(),
                            type = AgentActionType.CREATE_FOLDER,
                            title = "Crear carpeta `$folderPath`",
                            description = "Crea el directorio dentro de la estructura del proyecto.",
                            payload = folderPath,
                            targetPath = folderPath,
                            status = ActionStatus.PROPOSED
                        )
                    )
                } else {
                    actions.add(
                        AgentAction(
                            id = UUID.randomUUID().toString(),
                            type = AgentActionType.RUN_COMMAND,
                            title = "Ejecutar comando `$cmd`",
                            description = "Ejecutar en la terminal interactiva de Black Cat IDE.",
                            payload = cmd,
                            status = ActionStatus.PROPOSED
                        )
                    )
                }
            }
        }

        // 2. Detección de archivos con nombre en comentarios de código
        val fileBlockRegex = Regex("```(?:dart|kotlin|java|json|yaml|html|css|javascript|typescript)?\\s*\\n(?://|#|/\\*)\\s*(?:File|Archivo):?\\s*([a-zA-Z0-9_./-]+)\\s*\\n([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        for (match in fileBlockRegex.findAll(text)) {
            val filePath = match.groupValues[1].trim()
            val codeContent = match.groupValues[2].trim()
            actions.add(
                AgentAction(
                    id = UUID.randomUUID().toString(),
                    type = AgentActionType.CREATE_FILE,
                    title = "Crear archivo `$filePath`",
                    description = "Genera el nuevo archivo en el proyecto con el código propuesto.",
                    payload = codeContent,
                    targetPath = filePath,
                    status = ActionStatus.PROPOSED
                )
            )
        }

        // 3. Detección heurística en lenguaje natural (ej. "crea la carpeta...", "instala...")
        val lowerText = text.lowercase()
        if (actions.isEmpty()) {
            if (lowerText.contains("crea la carpeta ") || lowerText.contains("crear carpeta ")) {
                val folder = text.substringAfter("carpeta ").trim().split(" ", "\n", ",", ".").firstOrNull()?.trim('`', '\'', '"')
                if (!folder.isNullOrBlank()) {
                    actions.add(
                        AgentAction(
                            id = UUID.randomUUID().toString(),
                            type = AgentActionType.CREATE_FOLDER,
                            title = "Crear carpeta `$folder`",
                            description = "Crea la carpeta en la raíz del proyecto.",
                            payload = folder,
                            targetPath = folder,
                            status = ActionStatus.PROPOSED
                        )
                    )
                }
            }

            if (lowerText.contains("instala ") || lowerText.contains("instalar paquete ")) {
                val pkg = text.substringAfter("instala ").substringAfter("paquete ").trim().split(" ", "\n", ",", ".").firstOrNull()?.trim('`', '\'', '"')
                if (!pkg.isNullOrBlank()) {
                    val cmd = "flutter pub add $pkg"
                    actions.add(
                        AgentAction(
                            id = UUID.randomUUID().toString(),
                            type = AgentActionType.INSTALL_DEPENDENCY,
                            title = "Instalar paquete `$pkg`",
                            description = "Ejecuta `$cmd` en la consola de Black Cat IDE.",
                            payload = cmd,
                            status = ActionStatus.PROPOSED
                        )
                    )
                }
            }

            if (lowerText.contains("git status") || lowerText.contains("estado de git")) {
                actions.add(
                    AgentAction(
                        id = UUID.randomUUID().toString(),
                        type = AgentActionType.RUN_COMMAND,
                        title = "Ejecutar `git status`",
                        description = "Consulta las modificaciones en el árbol de trabajo.",
                        payload = "git status",
                        status = ActionStatus.PROPOSED
                    )
                )
            }
        }

        return actions
    }
}
