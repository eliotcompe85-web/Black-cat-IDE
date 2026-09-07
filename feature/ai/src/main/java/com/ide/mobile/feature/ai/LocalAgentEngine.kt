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
                systemPrompt = "Eres Black Cat Copilot, tu compañero amigable de programación en Black Cat IDE. Explico cada concepto técnico con palabras sencillas y ejemplos cotidianos. Escribo código limpio, robusto y guiado paso a paso para que construir tu app móvil sea fácil y divertido.",
                icon = "🐱",
                skills = listOf("flutter-3.x", "jetpack-compose", "dart-expert", "clean-code"),
                isBuiltIn = true,
                modelRecommendation = "Gemini Flash / Local GGUF"
            ),
            LocalAgentEntity(
                id = "agent-architect",
                name = "Antigravity Architect",
                description = "Audita la arquitectura del proyecto, estructura multimodular y previene fallos OOM.",
                systemPrompt = "Eres Antigravity Architect, tu guía cercano de estructura y diseño de software. Te explico cómo se organizan las piezas de tu app con metáforas simples y claras, asegurando que tu proyecto crezca ordenado, rápido y sin errores.",
                icon = "🏗️",
                skills = listOf("gradle-modules", "anti-oom-guard", "design-patterns", "security-audit"),
                isBuiltIn = true,
                modelRecommendation = "Claude 3.5 / ChatGPT 4o"
            ),
            LocalAgentEntity(
                id = "agent-debugger",
                name = "Rapid QuickFixer",
                description = "Detecta errores sintácticos, tags no cerrados y diagnostica logs de terminal.",
                systemPrompt = "Eres Rapid QuickFixer, tu asistente rápido y amigable para resolver problemas. Te explico con calma y sencillez qué causó cada error y te entrego la solución exacta lista para aplicar en un solo toque.",
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
                systemPrompt = "Eres Flutter & Dart Master, tu mentor amigable para crear interfaces móviles hermosas. Te explico los conceptos de Flutter y Dart de manera didáctica y visual, creando componentes fáciles de entender y comandos listos para usar.",
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
                systemPrompt = "Eres Jetpack Compose Pro, tu compañero para diseñar interfaces Android modernas y fluidas. Te explico cómo funcionan el estado y los componentes de forma intuitiva y amigable, entregándote código claro y listo para probar.",
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
                systemPrompt = "Eres FullStack REST Integrator, tu facilitador para conectar tu app con internet y servidores. Te explico cómo viajan los datos (JSON y HTTP) de forma súper sencilla, como si enviaras una carta, y genero código seguro y funcional.",
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
                systemPrompt = "Eres DevOps & Terminal Master, tu guía confiable en la terminal y el control de versiones. Te explico para qué sirve cada comando con calma y sin tecnicismos complejos, permitiéndote ejecutar soluciones con total seguridad.",
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
                systemPrompt = "Eres Security & Anti-Leak Auditor, tu guardián amigable de seguridad. Te explico de forma clara y sin alarmas cómo proteger tu app, cuidar tus contraseñas y evitar fugas de memoria con las mejores prácticas.",
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
        emit("🐱 **[${agent.icon} ${agent.name}]** activado...\n\n")
        val synthesized = SmartAgentSynthesizer.synthesizeResponse(query, context)
        val words = synthesized.split(Regex("(?<=\\s)|(?=\\n)"))
        for (word in words) {
            emit(word)
            delay(10)
        }
    }
}
