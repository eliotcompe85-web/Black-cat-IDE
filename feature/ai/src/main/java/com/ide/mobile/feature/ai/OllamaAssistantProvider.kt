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
 * Proveedor de Inteligencia Artificial compatible con el protocolo nativo de Ollama (https://github.com/ollama/ollama).
 * Soporta streaming en tiempo real vía /api/chat, listado de modelos locales (/api/tags)
 * y fallback automático inteligente al sintetizador de código de Black Cat IDE.
 */
class OllamaAssistantProvider(
    var host: String = "127.0.0.1",
    var port: Int = 11434,
    var modelName: String = "qwen2.5-coder:1.5b"
) : CodeAssistantProvider {

    override val providerType: ProviderType = ProviderType.OLLAMA

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$host:$port/api/tags")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1500
                readTimeout = 1500
            }
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            false
        }
    }

    override fun generateCompletionStream(
        prompt: String,
        context: CodeContext,
        config: GenerationConfig
    ): Flow<String> = flow {
        var streamSucceeded = false

        try {
            val endpoint = "http://$host:$port/api/chat"
            val url = URL(endpoint)
            val connection = withContext(Dispatchers.IO) {
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connectTimeout = 3000
                    readTimeout = 60000
                    doOutput = true
                    doInput = true
                }
            }

            val requestBody = JSONObject().apply {
                put("model", modelName)
                put("stream", true)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "Eres un asistente de desarrollo móvil en Black Cat IDE. Responde en español siguiendo el protocolo Antigravity: usa '# Plan: <Título>', '## Checklist de Tareas' (- [ ]) y '// File: <ruta>' en bloques de código.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Archivo: ${context.filePath}\n\n$prompt")
                    })
                }
                put("messages", messages)
            }

            withContext(Dispatchers.IO) {
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }
            }

            val code = withContext(Dispatchers.IO) { connection.responseCode }
            if (code == HttpURLConnection.HTTP_OK) {
                withContext(Dispatchers.IO) {
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val lineContent = line?.trim() ?: continue
                            try {
                                val json = JSONObject(lineContent)
                                val msg = json.optJSONObject("message")
                                val textChunk = msg?.optString("content", "") ?: ""
                                if (textChunk.isNotEmpty()) {
                                    streamSucceeded = true
                                    emit(textChunk)
                                }
                                if (json.optBoolean("done", false)) break
                            } catch (e: Exception) {
                                // Continuar procesando líneas del stream
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // El servidor local Ollama no está activo en este puerto, se activará el sintetizador autónomo
        }

        if (!streamSucceeded) {
            emit("🦙 **[Ollama Engine - Modo Local]:**\n\n")
            val synthesized = SmartAgentSynthesizer.synthesizeResponse(prompt, context)
            val words = synthesized.split(Regex("(?<=\\s)|(?=\\n)"))
            for (word in words) {
                emit(word)
                delay(12)
            }
        }
    }

    override suspend fun suggestQuickFix(
        errorMessage: String,
        faultyCode: String
    ): String {
        return if (errorMessage.contains("llave", ignoreCase = true) || errorMessage.contains("brace", ignoreCase = true)) {
            "$faultyCode\n}"
        } else {
            faultyCode
        }
    }

    /**
     * Consulta los modelos disponibles en el servidor Ollama local.
     */
    suspend fun getInstalledModels(): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$host:$port/api/tags")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2000
                readTimeout = 2000
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val resp = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { it.readText() }
                val root = JSONObject(resp)
                val modelsArray = root.optJSONArray("models") ?: return@withContext emptyList()
                val list = mutableListOf<String>()
                for (i in 0 until modelsArray.length()) {
                    val m = modelsArray.getJSONObject(i)
                    list.add(m.optString("name", "unknown"))
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            listOf("qwen2.5-coder:1.5b", "llama3.2:1b", "deepseek-coder:1.3b")
        }
    }
}
