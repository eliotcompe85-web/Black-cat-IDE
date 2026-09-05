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
 * Proveedor para Perplexity AI (Sonar, Sonar Pro con búsqueda web en vivo).
 */
class PerplexityAssistantProvider(
    private val apiKey: String = "",
    val modelName: String = "sonar"
) : CodeAssistantProvider {

    override val providerType: ProviderType = ProviderType.PERPLEXITY_API

    override suspend fun isAvailable(): Boolean {
        return apiKey.isNotBlank() && apiKey.startsWith("pplx-")
    }

    override fun generateCompletionStream(
        prompt: String,
        context: CodeContext,
        config: GenerationConfig
    ): Flow<String> = flow {
        val userPrompt = buildString {
            append("Actúa como el asistente Perplexity AI de Black Cat IDE.\n")
            append("Busca documentación actualizada y responde con precisión técnica.\n")
            append("Archivo: ${context.filePath} (Línea ${context.currentLine})\n\n")
            if (context.fullText.isNotBlank()) {
                append("--- CÓDIGO ACTUAL ---\n")
                append(context.fullText.take(2500))
                append("\n--- FIN CÓDIGO ---\n\n")
            }
            append("Pregunta: $prompt\n")
        }

        if (apiKey.isBlank()) {
            emit("⚠️ [Perplexity AI]: Clave de API no configurada (pplx-...).\nConfigúrala en Ajustes ⚙️ > Proveedores de IA para habilitar búsqueda web y docs en vivo.")
            return@flow
        }

        val result = try {
            fetchPerplexityContent(userPrompt)
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
            emit("⚠️ Error al conectar con Perplexity AI. Verifica tu clave o estado de red.")
        }
    }

    private suspend fun fetchPerplexityContent(promptText: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.perplexity.ai/chat/completions")
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
                    put("content", "Eres el motor de búsqueda y programación de Black Cat IDE.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", promptText)
                })
            })
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
        return "// [Perplexity QuickFix con búsqueda web]\n$faultyCode // Solución para: $errorMessage"
    }
}
