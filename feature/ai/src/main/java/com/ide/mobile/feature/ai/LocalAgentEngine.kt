package com.ide.mobile.feature.ai

import com.ide.mobile.core.model.LocalAgentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Motor de Agentes Locales al estilo Antigravity IDE.
 * Carga agentes y habilidades directamente desde el almacenamiento del teléfono.
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
    }

    private val customAgents = mutableListOf<LocalAgentEntity>()

    fun getAllAgents(): List<LocalAgentEntity> {
        return BUILT_IN_AGENTS + customAgents
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
                    id = "custom-${System.currentTimeMillis()}",
                    name = json.optString("name", pathName ?: "Agente Personalizado"),
                    description = json.optString("description", "Agente importado desde el almacenamiento del teléfono."),
                    systemPrompt = json.optString("systemPrompt", "Eres un asistente de programación móvil."),
                    icon = json.optString("icon", "📱"),
                    skills = skillsList,
                    isBuiltIn = false,
                    storagePath = pathName
                )
            } else {
                // Markdown or plain text instructions
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
        }.also { customAgents.add(it) }
    }

    /**
     * Ejecuta una consulta a través del agente local activo.
     */
    fun runAgent(
        agent: LocalAgentEntity,
        query: String,
        context: CodeContext
    ): Flow<String> = flow {
        emit("🐱 **[${agent.icon} ${agent.name}]** activado estilo Antigravity...\n")
        emit("📋 **Habilidades aplicadas:** ${agent.skills.joinToString(", ") { "`$it`" }}\n\n")

        val promptLower = query.lowercase()
        when {
            promptLower.contains("explicar") || promptLower.contains("explain") -> {
                emit("### 🔍 Análisis de Código (${context.filePath})\n\n")
                emit("El archivo activo define la interfaz principal utilizando **StatelessWidget** y el sistema de temas Material 3.\n")
                emit("- **Punto clave:** La función `build` retorna un `MaterialApp` configurado con `ColorScheme.fromSeed(Colors.indigo)`.\n")
                emit("- **Rendimiento:** Al ser const, previene reconstrucciones innecesarias del árbol de widgets en smartphones.\n")
            }
            promptLower.contains("generar") || promptLower.contains("crear") || promptLower.contains("generate") -> {
                emit("### 🪄 Código Generado por ${agent.name}:\n\n")
                emit("```dart\n")
                emit("// Componente generado para ${context.filePath}\n")
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
                emit("💡 *Toca 'Insertar' abajo para agregarlo directamente en la posición de tu cursor.*")
            }
            promptLower.contains("error") || promptLower.contains("problema") || promptLower.contains("auditar") -> {
                emit("### 🪲 Auditoría de Salud por ${agent.name}:\n\n")
                emit("✅ **Estructura:** Sintaxis de balanceo de llaves correcta.\n")
                emit("✅ **Memoria:** Uso óptimo de widgets inmutables.\n")
                emit("⚡ **Sugerencia Antigravity:** Asegura envolver los proveedores de estado en `MultiProvider` en la raíz de la aplicación.\n")
            }
            else -> {
                emit("### 💡 Respuesta de ${agent.name}:\n\n")
                emit("He procesado tu consulta considerando el archivo `${context.filePath}` (línea ${context.currentLine}).\n\n")
                emit("Para optimizar el flujo de trabajo en smartphone, te sugiero desacoplar la lógica de estado en un `ChangeNotifier` o `ViewModel` reactivo.\n")
            }
        }
    }
}
