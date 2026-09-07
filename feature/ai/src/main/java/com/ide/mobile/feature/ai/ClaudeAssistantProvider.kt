package com.ide.mobile.feature.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Proveedor para Anthropic Claude (Claude 3.5 Sonnet, Claude 3.5 Haiku).
 */
class ClaudeAssistantProvider(
    private val apiKey: String = "",
    val modelName: String = "claude-3-5-sonnet-20241022"
) : CodeAssistantProvider {

    override val providerType: ProviderType = ProviderType.CLAUDE_API

    override suspend fun isAvailable(): Boolean {
        return apiKey.isNotBlank() && apiKey.startsWith("sk-ant-")
    }

    override fun generateCompletionStream(
        prompt: String,
        context: CodeContext,
        config: GenerationConfig
    ): Flow<String> = flow {
        val userPrompt = buildString {
            append("Actúa como un asistente de desarrollo senior, empático y amigable en Black Cat IDE (arquitectura Antigravity y Kiro).\n")
            append("Tu objetivo es ayudar al usuario a crear código con éxito, explicando cada concepto técnico de forma sencilla, cercana y amigable.\n\n")
            append("DIRECTRICES DE COMUNICACIÓN Y FORMATO:\n")
            append("- INICIO Y NUEVOS PROYECTOS: Al iniciar la conversación o al crear un proyecto desde cero, saluda con calidez y pregunta sobre la meta del proyecto. No menciones detalles técnicos complejos, carpetas o comandos de inmediato; mantén una charla fluida para definir el nombre y preparar el espacio de trabajo.\n")
            append("- Tono amigable, didáctico y alentador: explica qué hace cada parte del código en lenguaje claro y accesible.\n")
            append("1. PLAN: Comienza con '# Plan: <Título descriptivo>' y un breve resumen explicativo del enfoque.\n")
            append("2. CHECKLIST: Incluye '## Checklist de Tareas' con viñetas:\n")
            append("   - [ ] <Paso técnico explicado con claridad>\n")
            append("   - [ ] [HUMANO] <Paso donde el usuario prueba o interactúa>\n")
            append("3. ARCHIVOS: En cada bloque de código pon la ruta en la primera línea: // File: ruta/nombre_del_archivo.kt\n")
            append("4. COMANDOS: Pon comandos en bloques ```bash ... ```.\n\n")
            append("Archivo: ${context.filePath} (Línea ${context.currentLine})\n\n")
            if (context.fullText.isNotBlank()) {
                append("```\n")
                append(context.fullText.take(3000))
                append("\n```\n\n")
            }
            append("Instrucción: $prompt\n")
        }

        if (apiKey.isBlank()) {
            emit("⚠️ [Anthropic Claude]: No has configurado tu clave sk-ant-...\nIngresa en Ajustes ⚙️ > Proveedores de IA para activarlo.")
            return@flow
        }

        val result = try {
            fetchClaudeContent(listOf(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            }))
        } catch (e: Exception) {
            null
        }

        if (result != null && result.isNotBlank()) {
            val words = result.split("(?<=\\s)".toRegex())
            for (word in words) {
                emit(word)
                delay(if (word.any { it in ".!?,;:" }) 50L else 15L)
            }
        } else {
            emit("⚠️ Error de conexión: Claude no pudo procesar la solicitud. Verifica tu conexión o intenta de nuevo más tarde.")
        }
    }

    private suspend fun fetchClaudeContent(messages: List<JSONObject>): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.anthropic.com/v1/messages")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", "2023-06-01")
            connectTimeout = 15000
            readTimeout = 30000
            doOutput = true
        }

        val root = JSONObject().apply {
            put("model", modelName)
            put("max_tokens", 1024)
            put("messages", JSONArray(messages))
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(root.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val json = JSONObject(response)
            val contentArray = json.getJSONArray("content")
            if (contentArray.length() > 0) {
                return@withContext contentArray.getJSONObject(0).getString("text")
            }
        } else {
            val error = conn.errorStream?.bufferedReader()?.readText() ?: "Sin detalles"
            throw Exception("HTTP $responseCode: $error")
        }
        ""
    }

    override suspend fun suggestQuickFix(errorMessage: String, faultyCode: String): String {
        val prompt = "Corrige este código. Error: $errorMessage. Código:\n$faultyCode\nDevuelve solo el código corregido."
        return fetchClaudeContent(listOf(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        })).ifBlank { "// [Claude QuickFix] No se pudo obtener la corrección.\n$faultyCode" }
    }
}
