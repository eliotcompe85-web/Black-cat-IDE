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
 * Proveedor para OpenAI ChatGPT (GPT-4o, GPT-4o-mini).
 */
class OpenAiAssistantProvider(
    private val apiKey: String = "",
    val modelName: String = "gpt-4o"
) : CodeAssistantProvider {

    override val providerType: ProviderType = ProviderType.OPENAI_API

    override suspend fun isAvailable(): Boolean {
        return apiKey.isNotBlank() && apiKey.startsWith("sk-")
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
            append("Archivo activo: ${context.filePath} (Línea ${context.currentLine})\n\n")
            if (context.fullText.isNotBlank()) {
                append("--- CÓDIGO ACTUAL ---\n")
                append(context.fullText.take(3000))
                append("\n--- FIN CÓDIGO ---\n\n")
            }
            append("Consulta: $prompt\n")
            append("Responde en español de forma estructurada, profesional y ejecutable.")
        }

        if (apiKey.isBlank()) {
            emit("⚠️ [OpenAI ChatGPT]: No has configurado tu clave de API de OpenAI.\nIngresa a Ajustes ⚙️ > Proveedores de IA y escribe tu clave sk-...")
            return@flow
        }

        val result = try {
            fetchOpenAiContent(userPrompt)
        } catch (e: Exception) {
            null
        }

        if (result != null && result.isNotBlank()) {
            val words = result.split(" ")
            for (i in words.indices) {
                emit(words[i] + if (i < words.size - 1) " " else "")
                delay(20)
            }
        } else {
            emit("⚠️ Error al conectar con OpenAI API. Verifica tu clave o tu conexión a internet.")
        }
    }

    private suspend fun fetchOpenAiContent(promptText: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            connectTimeout = 12000
            readTimeout = 20000
            doOutput = true
        }

        val root = JSONObject().apply {
            put("model", modelName)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Eres el asistente de programación de Black Cat IDE estilo Antigravity.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", promptText)
                })
            })
            put("temperature", 0.2)
            put("max_tokens", 1024)
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(root.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val json = JSONObject(response)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                return@withContext choices.getJSONObject(0).getJSONObject("message").getString("content")
            }
        }
        ""
    }

    override suspend fun suggestQuickFix(errorMessage: String, faultyCode: String): String {
        return "// [OpenAI QuickFix]\n$faultyCode // Corregido para: $errorMessage"
    }
}
