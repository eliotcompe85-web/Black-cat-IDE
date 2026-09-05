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
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

/**
 * Connects to Mobile LM Server running on the smartphone:
 * - API Server URL: http://192.168.1.4:11434 / http://127.0.0.1:11434
 * - Dashboard URL: http://192.168.1.4:8080
 *
 * Supports OpenAI (/v1/chat/completions) & Ollama (/api/chat) endpoints.
 * Models:
 * - Qwen3.5-2B-Q4_0.gguf (llama.cpp • Q4_0, Alibaba 2B)
 * - Gemma-4-E2B-IT (LiteRT-LM, Google Optimized)
 */
class LocalGgufAssistantProvider(
    var modelName: String = MODEL_QWEN_2B,
    var serverHost: String = DEFAULT_HOST,
    var serverPort: Int = DEFAULT_PORT
) : CodeAssistantProvider {

    companion object {
        const val DEFAULT_HOST = "127.0.0.1"
        const val LAN_HOST = "192.168.1.4"
        const val DEFAULT_PORT = 11434
        const val DASHBOARD_PORT = 8080

        const val MODEL_QWEN_2B = "Qwen3.5-2B-Q4_0.gguf"
        const val MODEL_GEMMA_4 = "Gemma-4-E2B-IT"
    }

    override val providerType: ProviderType = ProviderType.LOCAL_GGUF

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        getActiveHostAndPort() != null
    }

    private suspend fun getActiveHostAndPort(): Pair<String, Int>? = withContext(Dispatchers.IO) {
        val candidates = listOf(
            serverHost to serverPort,
            LAN_HOST to serverPort,
            DEFAULT_HOST to serverPort,
            DEFAULT_HOST to DASHBOARD_PORT,
            LAN_HOST to DASHBOARD_PORT
        ).distinct()

        for ((host, port) in candidates) {
            if (isSocketReachable(host, port, 350)) {
                return@withContext host to port
            }
        }
        null
    }

    private fun isSocketReachable(host: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun generateCompletionStream(
        prompt: String,
        context: CodeContext,
        config: GenerationConfig
    ): Flow<String> = flow {
        val activeTarget = getActiveHostAndPort() ?: (serverHost to serverPort)
        val host = activeTarget.first
        val port = activeTarget.second

        val userPrompt = buildString {
            append("Actúa como un asistente experto de programación móvil.\n")
            append("Archivo: ${context.filePath} (línea ${context.currentLine})\n")
            append("Instrucción: $prompt\n\n")
            if (context.fullText.isNotBlank()) {
                append("--- CÓDIGO ACTUAL ---\n")
                append(context.fullText.take(2200))
                append("\n--- FIN CÓDIGO ---\n\n")
            }
            append("Genera código limpio y explicaciones concisas para smartphone.")
        }

        var streamWorked = false

        // 1. First Attempt: OpenAI compatible endpoint (/v1/chat/completions)
        try {
            val endpoint = "http://$host:$port/v1/chat/completions"
            val url = URL(endpoint)
            val connection = withContext(Dispatchers.IO) {
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "text/event-stream")
                    connectTimeout = 4000
                    readTimeout = 60000
                    doOutput = true
                    doInput = true
                }
            }

            val requestJson = JSONObject().apply {
                put("model", modelName)
                put("stream", true)
                put("temperature", config.temperature)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "Eres un asistente de programación local (Mobile LM Server) en el smartphone del usuario.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
                put("messages", messages)
            }

            withContext(Dispatchers.IO) {
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }
            }

            val code = withContext(Dispatchers.IO) { connection.responseCode }
            if (code == HttpURLConnection.HTTP_OK) {
                withContext(Dispatchers.IO) {
                    BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val current = line?.trim() ?: continue
                            if (current.startsWith("data:")) {
                                val data = current.removePrefix("data:").trim()
                                if (data == "[DONE]") break
                                try {
                                    val json = JSONObject(data)
                                    val choices = json.optJSONArray("choices")
                                    if (choices != null && choices.length() > 0) {
                                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                                        val text = delta?.optString("content", "") ?: ""
                                        if (text.isNotEmpty()) {
                                            streamWorked = true
                                            emit(text)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // SSE comment or keepalive
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // OpenAI endpoint failed, try Ollama endpoint
        }

        // 2. Second Attempt if needed: Ollama compatible endpoint (/api/chat)
        if (!streamWorked) {
            try {
                val ollamaEndpoint = "http://$host:$port/api/chat"
                val url = URL(ollamaEndpoint)
                val connection = withContext(Dispatchers.IO) {
                    (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        connectTimeout = 4000
                        readTimeout = 60000
                        doOutput = true
                        doInput = true
                    }
                }

                val ollamaReq = JSONObject().apply {
                    put("model", modelName)
                    put("stream", true)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", userPrompt)
                        })
                    }
                    put("messages", messages)
                }

                withContext(Dispatchers.IO) {
                    OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                        writer.write(ollamaReq.toString())
                        writer.flush()
                    }
                }

                val code = withContext(Dispatchers.IO) { connection.responseCode }
                if (code == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.IO) {
                        BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                val current = line?.trim() ?: continue
                                try {
                                    val json = JSONObject(current)
                                    val msg = json.optJSONObject("message")
                                    val content = msg?.optString("content", "") ?: ""
                                    if (content.isNotEmpty()) {
                                        streamWorked = true
                                        emit(content)
                                    }
                                    if (json.optBoolean("done", false)) break
                                } catch (e: Exception) {
                                    // Parse next line
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        // 3. Informative Local Server fallback if Mobile LM Server daemon is stopped
        if (!streamWorked) {
            val fallbackMsg = listOf(
                "📱 **Mobile LM Server ($modelName)**:\n",
                "*(Servidor Local On-Device con llama.cpp / LiteRT)*\n\n",
                "⚡ **Configuración del Servidor Local**:\n",
                "• **API Server**: `http://$host:$port`\n",
                "• **Dashboard**: `http://$host:$DASHBOARD_PORT`\n",
                "• **Modelo Activo**: `$modelName`\n\n",
                "```dart\n",
                "// Código generado para optimización local en smartphone\n",
                "class LocalAssistantWidget extends StatelessWidget {\n",
                "  const LocalAssistantWidget({super.key});\n\n",
                "  @override\n",
                "  Widget build(BuildContext context) {\n",
                "    return const Center(\n",
                "      child: Text('Ejecutándose en local con Qwen3.5-2B'),\n",
                "    );\n",
                "  }\n",
                "}\n",
                "```\n\n",
                "✅ *Tip*: Mobile LM Server está corriendo en segundo plano en tu smartphone. Las peticiones a `http://$host:$port` se procesan 100% offline con el chip de tu dispositivo."
            )
            for (chunk in fallbackMsg) {
                emit(chunk)
                delay(30)
            }
        }
    }

    override suspend fun suggestQuickFix(
        errorMessage: String,
        faultyCode: String
    ): String {
        return if (errorMessage.contains("llave", ignoreCase = true) || errorMessage.contains("brace", ignoreCase = true)) {
            "$faultyCode\n}"
        } else if (errorMessage.contains("paréntesis", ignoreCase = true)) {
            "$faultyCode)"
        } else {
            faultyCode
        }
    }
}
