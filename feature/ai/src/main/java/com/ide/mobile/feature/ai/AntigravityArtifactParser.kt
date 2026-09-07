package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.*
import java.util.UUID

/**
 * Analizador de respuestas estructuradas bajo el protocolo de Agente Antigravity / Kiro.
 * Convierte el flujo de texto crudo de la IA en artefactos interactivos de primer nivel:
 * - [ChatContent.PlanArtifact]: Resumen de arquitectura y planificación.
 * - [ChatContent.ActionChecklist]: Tareas secuenciales con detección de pasos que requieren intervención humana.
 * - [ChatContent.CodeBlock]: Bloques de código asociados a rutas de archivo para inyección directa al espacio de trabajo.
 * - [ChatContent.ActionCard]: Comandos ejecutables en la terminal interactiva.
 * - [ChatContent.Prose]: Explicaciones y texto de acompañamiento.
 */
object AntigravityArtifactParser {

    /**
     * Parsea una respuesta completa de la IA en una lista ordenada de artefactos de [ChatContent].
     */
    fun parseResponse(
        rawText: String,
        existingActions: List<AgentAction> = emptyList()
    ): List<ChatContent> {
        if (rawText.isBlank()) return emptyList()

        val contents = mutableListOf<ChatContent>()
        var textToProcess = rawText

        // 1. Extraer Plan de Implementación (# Plan: ... o ## Plan ...)
        val planRegex = Regex("(?:^|\\n)(?:#|##)\\s*Plan:?\\s*([^\\n]+)\\n([\\s\\S]*?)(?=(?:\\n(?:#|##)|```|\\n-\\s*\\[|$))", RegexOption.IGNORE_CASE)
        val planMatch = planRegex.find(textToProcess)
        if (planMatch != null) {
            val title = planMatch.groupValues[1].trim()
            val summary = planMatch.groupValues[2].trim()
            if (title.isNotBlank() || summary.isNotBlank()) {
                contents.add(
                    ChatContent.PlanArtifact(
                        title = title.ifBlank { "Plan de Implementación Agéntico" },
                        summary = summary
                    )
                )
                textToProcess = textToProcess.replace(planMatch.value, "\n")
            }
        }

        // 2. Extraer Checklist de Tareas (- [ ] Tarea, - [x] Tarea)
        val checklistRegex = Regex("(?:^|\\n)(?:##\\s*Checklist|##\\s*Tareas|Tareas:?)?[\\s\\S]*?((?:\\n?\\s*[-*]\\s*\\[[ xX]\\][\\s\\S]*?)+)(?=(?:\\n(?:#|##)|```|$))", RegexOption.IGNORE_CASE)
        val taskItemRegex = Regex("[-*]\\s*\\[([ xX])\\]\\s*([^\\n]+)")
        val checklistMatch = checklistRegex.find(textToProcess)
        if (checklistMatch != null) {
            val tasksText = checklistMatch.groupValues[1]
            val tasks = mutableListOf<ChecklistTask>()
            for (itemMatch in taskItemRegex.findAll(tasksText)) {
                val isChecked = itemMatch.groupValues[1].equals("x", ignoreCase = true)
                val taskDesc = itemMatch.groupValues[2].trim()
                val requiresHuman = taskDesc.contains("[HUMANO]", ignoreCase = true) ||
                        taskDesc.contains("[HUMAN]", ignoreCase = true) ||
                        taskDesc.contains("(requiere confirmación)", ignoreCase = true) ||
                        taskDesc.contains("(manual)", ignoreCase = true)
                val cleanDesc = taskDesc
                    .replace("\\[HUMANO\\]".toRegex(RegexOption.IGNORE_CASE), "")
                    .replace("\\[HUMAN\\]".toRegex(RegexOption.IGNORE_CASE), "")
                    .trim()

                tasks.add(
                    ChecklistTask(
                        id = UUID.randomUUID().toString(),
                        description = cleanDesc,
                        isCompleted = isChecked,
                        requiresHuman = requiresHuman
                    )
                )
            }
            if (tasks.isNotEmpty()) {
                contents.add(
                    ChatContent.ActionChecklist(
                        title = "Checklist de Tareas del Agente",
                        tasks = tasks
                    )
                )
                textToProcess = textToProcess.replace(checklistMatch.value, "\n")
            }
        }

        // 3. Extraer Bloques de Código con Rutas de Archivo (// File: ruta, # File: ruta)
        val codeBlockRegex = Regex("```([a-zA-Z0-9_\\-]*)\\n([\\s\\S]*?)```")
        val fileHeaderRegex = Regex("^(?:\\/\\/|#|\\/\\*)\\s*(?:File|Archivo|Ruta):?\\s*([a-zA-Z0-9_./-]+)", RegexOption.IGNORE_CASE)

        var lastIndex = 0
        val remainingTextPieces = mutableListOf<String>()

        for (match in codeBlockRegex.findAll(textToProcess)) {
            val preCode = textToProcess.substring(lastIndex, match.range.first).trim()
            if (preCode.isNotBlank()) {
                remainingTextPieces.add(preCode)
            }

            val lang = match.groupValues[1].ifBlank { "kotlin" }
            val codeBody = match.groupValues[2]

            // Buscar si la primera línea contiene la ruta del archivo
            val firstLine = codeBody.lines().firstOrNull()?.trim() ?: ""
            val fileMatch = fileHeaderRegex.find(firstLine)
            val targetPath = fileMatch?.groupValues?.getOrNull(1)?.trim()

            // Si es un comando de bash, lo procesamos como acción ejecutable
            if (lang.equals("bash", ignoreCase = true) || lang.equals("sh", ignoreCase = true)) {
                val cmdLines = codeBody.lines()
                    .map { it.trim().removePrefix("$").trim() }
                    .filter { it.isNotBlank() && !it.startsWith("#") }

                cmdLines.forEach { cmd ->
                    contents.add(
                        ChatContent.ActionCard(
                            AgentAction(
                                id = UUID.randomUUID().toString(),
                                type = AgentActionType.RUN_COMMAND,
                                title = "Ejecutar: $cmd",
                                description = "Comando en la terminal interactiva de Black Cat IDE",
                                payload = cmd,
                                status = ActionStatus.PROPOSED
                            )
                        )
                    )
                }
            } else {
                contents.add(
                    ChatContent.CodeBlock(
                        code = codeBody.trimEnd(),
                        language = lang,
                        targetFilePath = targetPath
                    )
                )
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < textToProcess.length) {
            val remaining = textToProcess.substring(lastIndex).trim()
            if (remaining.isNotBlank()) {
                remainingTextPieces.add(remaining)
            }
        }

        // Agregar texto explicativo o de prosa
        val combinedProse = remainingTextPieces.joinToString("\n\n").trim()
        if (combinedProse.isNotBlank()) {
            contents.add(0, ChatContent.Prose(combinedProse))
        }

        // Agregar acciones adicionales que no hayan sido agregadas aún
        existingActions.forEach { action ->
            if (contents.none { it is ChatContent.ActionCard && it.action.id == action.id }) {
                contents.add(ChatContent.ActionCard(action))
            }
        }

        return contents
    }
}
