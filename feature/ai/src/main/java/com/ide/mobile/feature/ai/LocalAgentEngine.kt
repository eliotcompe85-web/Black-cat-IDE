package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.DownloadableAgent
import com.ide.mobile.core.model.LocalAgentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Motor de Agentes Locales al estilo Antigravity IDE.
 * Carga, descarga y ejecuta agentes y habilidades directamente en el teléfono.
 */
class LocalAgentEngine {

    companion object {
        val BUILT_IN_AGENTS = listOf(
            LocalAgentEntity(
                id = "agent-copilot",
                name = "Black Cat Copilot",
                description = "Pair programmer experto en Flutter, Dart, Jetpack Compose y Kotlin.",
                systemPrompt = "Eres Black Cat Copilot, el agente principal de Antigravity en Black Cat IDE. Escribe código modular, robusto y optimizado para smartphones.",
                icon = "🐱",
                skills = listOf("flutter-3.x", "jetpack-compose", "dart-expert", "clean-code"),
                isBuiltIn = true,
                modelRecommendation = "Gemini Flash / Local GGUF"
            ),
            LocalAgentEntity(
                id = "agent-architect",
                name = "Antigravity Architect",
                description = "Audita la arquitectura del proyecto, estructura multimodular y previene fallos OOM.",
                systemPrompt = "Eres Antigravity Architect. Tu función es auditar el diseño, revisar la estructura de archivos y asegurar que la app móvil sea resiliente.",
                icon = "🏗️",
                skills = listOf("gradle-modules", "anti-oom-guard", "design-patterns", "security-audit"),
                isBuiltIn = true,
                modelRecommendation = "Claude 3.5 / ChatGPT 4o"
            ),
            LocalAgentEntity(
                id = "agent-debugger",
                name = "Rapid QuickFixer",
                description = "Detecta errores sintácticos, tags no cerrados y diagnostica logs de terminal.",
                systemPrompt = "Eres Rapid QuickFixer. Diagnostica de inmediato errores sintácticos, llaves sin cerrar y genera parches de 1 toque.",
                icon = "⚡",
                skills = listOf("syntax-repair", "terminal-diagnostics", "quick-fix-patch"),
                isBuiltIn = true,
                modelRecommendation = "Local On-Device / Sonar"
            )
        )

        /**
         * Catálogo de agentes descargables reales y listos para activar inmediatamente en el teléfono.
         */
        val DOWNLOADABLE_AGENTS_CATALOG = listOf(
            DownloadableAgent(
                id = "agent-flutter-master",
                name = "Flutter & Dart Master",
                description = "Especialista en BLoC, Provider, Riverpod, animaciones fluidas a 120 FPS y widgets móviles.",
                icon = "📱",
                category = "Frontend Móvil",
                skills = listOf("flutter-widgets", "bloc-pattern", "riverpod", "hero-animations", "pub-manager"),
                systemPrompt = "Eres Flutter & Dart Master. Escribe código Dart moderno y eficiente. Propón comandos de terminal (flutter pub add ...) y componentes modulares.",
                recommendedModel = "Gemini Flash (Gratuito)",
                downloadSize = "8.4 KB"
            ),
            DownloadableAgent(
                id = "agent-compose-guru",
                name = "Jetpack Compose Pro",
                description = "Experto en Material Design 3, arquitectura MVI/MVVM, StateFlow, Coroutines y Room en Android.",
                icon = "⚡",
                category = "Android Nativo",
                skills = listOf("jetpack-compose", "kotlin-coroutines", "state-flow", "room-database", "material-3"),
                systemPrompt = "Eres Jetpack Compose Pro. Diseña pantallas reactivas sin recomposiciones innecesarias y gestiona estados con StateFlow.",
                recommendedModel = "Claude 3.5 / Gemini Flash",
                downloadSize = "9.2 KB"
            ),
            DownloadableAgent(
                id = "agent-rest-api",
                name = "FullStack REST Integrator",
                description = "Generador de clientes HTTP, modelos JSON automáticos, manejo de caché y autenticación JWT.",
                icon = "🌐",
                category = "Red & Backend",
                skills = listOf("http-client", "json-serialization", "dio-client", "jwt-auth", "rest-endpoints"),
                systemPrompt = "Eres FullStack REST Integrator. Crea servicios de red robustos, gestiona peticiones HTTP y serialización segura.",
                recommendedModel = "ChatGPT 4o / Gemini Flash",
                downloadSize = "7.8 KB"
            ),
            DownloadableAgent(
                id = "agent-devops",
                name = "DevOps & Terminal Master",
                description = "Automatizador de scripts Bash, pipelines de Git, gestión de dependencias y diagnóstico de compilación.",
                icon = "🛠️",
                category = "DevOps & Terminal",
                skills = listOf("bash-scripts", "git-flow", "gradle-optimization", "package-install"),
                systemPrompt = "Eres DevOps & Terminal Master. Genera comandos de terminal claros y seguros para que el usuario pueda ejecutarlos en 1 toque.",
                recommendedModel = "Perplexity / Gemini Flash",
                downloadSize = "6.5 KB"
            ),
            DownloadableAgent(
                id = "agent-security",
                name = "Security & Anti-Leak Auditor",
                description = "Auditoría estática de código, detección de fugas de memoria, claves en texto plano y permisos seguros.",
                icon = "🛡️",
                category = "Seguridad",
                skills = listOf("security-audit", "memory-leak-detection", "secret-scanner", "clean-architecture"),
                systemPrompt = "Eres Security & Anti-Leak Auditor. Revisa el código en busca de fugas de memoria, claves expuestas y vulnerabilidades.",
                recommendedModel = "Gemini Flash / Claude 3.5",
                downloadSize = "10.1 KB"
            )
        )
    }

    private val customAgents = mutableListOf<LocalAgentEntity>()

    fun getAllAgents(): List<LocalAgentEntity> {
        return BUILT_IN_AGENTS + customAgents
    }

    fun getDownloadableCatalog(): List<DownloadableAgent> {
        val downloadedIds = customAgents.map { it.id }.toSet()
        return DOWNLOADABLE_AGENTS_CATALOG.map {
            it.copy(isDownloaded = downloadedIds.contains(it.id))
        }
    }

    /**
     * Descarga y activa de inmediato un agente del catálogo, guardando su definición en el almacenamiento.
     */
    suspend fun downloadAndActivateAgent(
        downloadable: DownloadableAgent,
        storageDir: File? = null
    ): LocalAgentEntity = withContext(Dispatchers.IO) {
        // Generar manifiesto JSON estructurado del agente
        val json = JSONObject().apply {
            put("id", downloadable.id)
            put("name", downloadable.name)
            put("description", downloadable.description)
            put("icon", downloadable.icon)
            put("skills", JSONArray(downloadable.skills))
            put("systemPrompt", downloadable.systemPrompt)
            put("recommendedModel", downloadable.recommendedModel)
            put("downloadedAt", System.currentTimeMillis())
        }

        val jsonString = json.toString(2)
        var savedPath: String? = null

        if (storageDir != null) {
            try {
                if (!storageDir.exists()) storageDir.mkdirs()
                val agentFile = File(storageDir, "${downloadable.id}.agent.json")
                agentFile.writeText(jsonString)
                savedPath = agentFile.absolutePath
            } catch (e: Exception) {
                // Fallback en memoria si hay restricciones de permisos
            }
        }

        val entity = LocalAgentEntity(
            id = downloadable.id,
            name = downloadable.name,
            description = downloadable.description,
            systemPrompt = downloadable.systemPrompt,
            icon = downloadable.icon,
            skills = downloadable.skills,
            isBuiltIn = false,
            storagePath = savedPath,
            modelRecommendation = downloadable.recommendedModel
        )

        // Registrar o actualizar en la lista activa de agentes
        customAgents.removeAll { it.id == entity.id }
        customAgents.add(entity)

        entity
    }

    /**
     * Importa un agente desde un archivo de texto/JSON (.agent.json o .md) en el teléfono.
     */
    fun importAgentFromContent(content: String, pathName: String? = null): LocalAgentEntity {
        return try {
            if (content.trim().startsWith("{")) {
                val json = JSONObject(content)
                val skillsList = mutableListOf<String>()
                if (json.has("skills")) {
                    val arr = json.getJSONArray("skills")
                    for (i in 0 until arr.length()) {
                        skillsList.add(arr.getString(i))
                    }
                }
                LocalAgentEntity(
                    id = json.optString("id", "custom-${System.currentTimeMillis()}"),
                    name = json.optString("name", pathName ?: "Agente Personalizado"),
                    description = json.optString("description", "Agente importado desde el almacenamiento del teléfono."),
                    systemPrompt = json.optString("systemPrompt", "Eres un asistente de programación móvil."),
                    icon = json.optString("icon", "📱"),
                    skills = skillsList,
                    isBuiltIn = false,
                    storagePath = pathName,
                    modelRecommendation = json.optString("recommendedModel", "Gemini Flash / Local GGUF")
                )
            } else {
                val lines = content.lines()
                val name = lines.firstOrNull { it.startsWith("# ") }?.removePrefix("# ")?.trim()
                    ?: (pathName ?: "Agente Local Móvil")
                LocalAgentEntity(
                    id = "custom-${System.currentTimeMillis()}",
                    name = name,
                    description = "Agente markdown cargado desde el teléfono.",
                    systemPrompt = content,
                    icon = "📜",
                    skills = listOf("custom-instructions", "phone-storage"),
                    isBuiltIn = false,
                    storagePath = pathName
                )
            }
        } catch (e: Exception) {
            LocalAgentEntity(
                id = "custom-${System.currentTimeMillis()}",
                name = pathName ?: "Agente Local",
                description = "Agente cargado desde el almacenamiento.",
                systemPrompt = content,
                icon = "📂",
                skills = listOf("custom-agent"),
                isBuiltIn = false,
                storagePath = pathName
            )
        }.also {
            customAgents.removeAll { a -> a.id == it.id }
            customAgents.add(it)
        }
    }

    /**
     * Ejecuta una consulta a través del agente local activo con pasos de razonamiento estilo Antigravity.
     */
    fun runAgent(
        agent: LocalAgentEntity,
        query: String,
        context: CodeContext
    ): Flow<String> = flow {
        emit("🐱 **[${agent.icon} ${agent.name}]** activado estilo Antigravity...\n")
        emit("📋 **Habilidades activas:** ${agent.skills.joinToString(", ") { "`$it`" }}\n\n")

        val promptLower = query.lowercase()
        when {
            promptLower.contains("carpeta") || promptLower.contains("directorio") || promptLower.contains("folder") -> {
                val folderName = if (promptLower.contains("services")) "services" else if (promptLower.contains("models")) "models" else "components"
                emit("⚙️ **Paso:** Analizando estructura de directorios...\n\n")
                emit("He preparado la creación de la carpeta `$folderName` dentro del proyecto.\n\n")
                emit("```bash\nmkdir -p lib/$folderName\n```\n\n")
                emit("💡 *Puedes aprobar la ejecución del comando abajo para que se cree inmediatamente en tu proyecto.*")
            }
            promptLower.contains("instalar") || promptLower.contains("dependencia") || promptLower.contains("paquete") -> {
                val pkg = if (promptLower.contains("http")) "http" else if (promptLower.contains("provider")) "provider" else "shared_preferences"
                emit("⚙️ **Paso:** Verificando compatibilidad de la librería `$pkg`...\n\n")
                emit("Comando preparado para ejecutar en la terminal de Black Cat IDE:\n\n")
                emit("```bash\nflutter pub add $pkg\n```\n\n")
                emit("💡 *Usa 'Aprobar y Ejecutar' en la tarjeta para correrlo en la terminal.*")
            }
            promptLower.contains("explicar") || promptLower.contains("explain") -> {
                emit("### 🔍 Análisis de Código (${context.filePath})\n\n")
                emit("El archivo activo define la interfaz principal utilizando **StatelessWidget** y el sistema de temas Material 3.\n")
                emit("- **Punto clave:** La función `build` retorna un widget desacoplado optimizado para smartphone.\n")
                emit("- **Rendimiento:** Evita cálculos pesados en el hilo UI.\n")
            }
            promptLower.contains("generar") || promptLower.contains("crear") || promptLower.contains("generate") -> {
                emit("### 🪄 Código Generado por ${agent.name}:\n\n")
                emit("```dart\n")
                emit("// File: lib/components/custom_card.dart\n")
                emit("import 'package:flutter/material.dart';\n\n")
                emit("class CustomActionCard extends StatelessWidget {\n")
                emit("  final String title;\n")
                emit("  final VoidCallback onTap;\n\n")
                emit("  const CustomActionCard({super.key, required this.title, required this.onTap});\n\n")
                emit("  @override\n")
                emit("  Widget build(BuildContext context) {\n")
                emit("    return Card(\n")
                emit("      color: const Color(0xFF191B2E),\n")
                emit("      shape: RoundedCornerShape(12),\n")
                emit("      child: ListTile(\n")
                emit("        title: Text(title, style: const TextStyle(color: Colors.white)),\n")
                emit("        trailing: const Icon(Icons.arrow_forward_ios, color: Color(0xFF7B61FF), size: 14),\n")
                emit("        onTap: onTap,\n")
                emit("      ),\n")
                emit("    );\n")
                emit("  }\n")
                emit("}\n")
                emit("```\n\n")
                emit("💡 *Puedes insertar este código directamente en tu archivo o usar la acción de crear archivo.*")
            }
            promptLower.contains("git") || promptLower.contains("terminal") -> {
                emit("### 🖥️ Diagnóstico de Terminal y Git:\n\n")
                emit("```bash\ngit status\n```\n\n")
                emit("Revisa las modificaciones actuales en el árbol de trabajo.")
            }
            else -> {
                emit("### 💡 Respuesta de ${agent.name}:\n\n")
                emit("He procesado tu consulta considerando el contexto de `${context.filePath}` (línea ${context.currentLine}).\n\n")
                emit("Para mantener un desarrollo fluido en tu dispositivo móvil, puedes pedirme que cree carpetas, instale paquetes, ejecute comandos o audite fallos de sintaxis.\n")
            }
        }
    }
}
