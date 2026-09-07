package com.ide.mobile.core.model

import java.util.UUID

/**
 * Jerarquía sellada representativa de los Agentes de Inteligencia Artificial
 * en el entorno Antigravity de Black Cat IDE.
 */
sealed interface Agent {
    val id: String
    val name: String
    val description: String get() = ""
    val icon: String get() = "🤖"
    val isAvailable: Boolean get() = isReady
    val category: String get() = if (this is LocalAgent) "Local Engine" else "Cloud API"
    val isReady: Boolean // Indica si el agente tiene todo lo necesario para operar

    /**
     * Agente ejecutado localmente en el dispositivo o máquina host mediante
     * un binario ejecutable (ej. llama.cpp, llama-cli, python script, agente nativo).
     */
    data class LocalAgent(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        override val description: String = "Agente de ejecución local en hardware de dispositivo",
        override val icon: String = "⚡",
        override val isAvailable: Boolean = true,
        override val category: String = "Local Engine",
        val executablePath: String,
        val workingDirectory: String = ".",
        val arguments: List<String> = emptyList(),
        val memoryLimitMb: Int = 4096,
        val environmentVariables: Map<String, String> = emptyMap()
    ) : Agent {
        override val isReady: Boolean
            get() = executablePath.isNotBlank() && isAvailable
    }

    /**
     * Agente conectado a un servicio de inferencia en la nube mediante API HTTP/REST
     * (ej. Gemini Pro/Flash, OpenAI GPT-4o, Claude 3.5 Sonnet, Perplexity).
     */
    data class ApiAgent(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        override val description: String = "Agente en la nube de alta capacidad de razonamiento",
        override val icon: String = "🌐",
        override val isAvailable: Boolean = true,
        override val category: String = "Cloud API",
        val endpoint: String,
        val apiKey: String = "",
        val modelName: String,
        val maxTokens: Int = 4096,
        val temperature: Float = 0.7f,
        val systemPrompt: String? = null
    ) : Agent {
        // El agente solo está listo si la API Key y el endpoint están configurados
        override val isReady: Boolean
            get() = apiKey.isNotBlank() && endpoint.isNotBlank() && isAvailable
    }
}

// Typealiases for top-level access compatibility across all modules
typealias LocalAgent = Agent.LocalAgent
typealias ApiAgent = Agent.ApiAgent
