package com.ide.mobile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ide.mobile.core.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel Unificado de Black Cat IDE.
 * Orquesta la persistencia en Room (WorkspaceRepository), el estado reactivo
 * de Mission Control (ActiveAgentContext / AgentState) y el chat multimodal con Artefactos.
 */
class BlackCatViewModel(
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    // 1. Estado de Proyectos (Vinculado a Room a través del Repositorio)
    val recentProjects: StateFlow<List<ProjectEntity>> = workspaceRepository.recentProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Estado de Agentes Activos (Mission Control)
    private val _activeAgents = MutableStateFlow<List<ActiveAgentContext>>(
        listOf(
            ActiveAgentContext(
                agentId = "1",
                agentName = "Local Copilot (Default)",
                isLocal = true,
                currentState = AgentState.Idle
            )
        )
    )
    val activeAgents: StateFlow<List<ActiveAgentContext>> = _activeAgents.asStateFlow()

    // 3. Estado del Chat y Mensajes Actuales
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // --- ACCIONES DE WORKSPACE ---
    fun createProject(name: String, path: String) {
        viewModelScope.launch {
            workspaceRepository.openOrCreateLocalProject(name, path)
        }
    }

    fun importProject(uri: Uri, name: String) {
        viewModelScope.launch {
            workspaceRepository.importProjectFromDirectory(uri, name)
        }
    }

    // --- ACCIONES DE AGENTES & MISSION CONTROL ---
    fun updateAgentState(agentId: String, newState: AgentState) {
        _activeAgents.update { list ->
            list.map { agent ->
                if (agent.agentId == agentId) agent.copy(currentState = newState) else agent
            }
        }
    }

    // --- ACCIONES DE CHAT & MULTIMODAL ---
    fun sendMessage(text: String, attachments: List<Uri> = emptyList()) {
        viewModelScope.launch {
            // 1. Crear el mensaje del usuario (incluyendo posibles artefactos visuales/multimodales)
            val userMessage = ChatMessage(
                senderName = "Desarrollador",
                isFromUser = true,
                contents = listOf(ChatContent.Prose(text)) // Aquí se adjuntarían las URIs si el modelo lo procesa
            )

            _chatMessages.update { it + userMessage }

            // 2. Simular respuesta del Agente (Cambio a estado Planning en Mission Control)
            updateAgentState("1", AgentState.Planning("Analizando requerimiento..."))

            // Simulación de delay e inserción de respuesta con Artefactos
            delay(1500)

            updateAgentState("1", AgentState.Executing("Generando código modular...", 0.5f))

            delay(1500)

            val agentResponse = ChatMessage(
                senderName = "Local Copilot",
                isFromUser = false,
                contents = listOf(
                    ChatContent.Prose("He generado la estructura solicitada. Puedes aplicarla directamente al workspace."),
                    ChatContent.CodeBlock(
                        code = "fun main() {\n    println(\"Hello Black Cat IDE!\")\n}",
                        language = "kotlin",
                        targetFilePath = "app/src/main/java/Main.kt"
                    )
                )
            )

            _chatMessages.update { it + agentResponse }
            updateAgentState("1", AgentState.Idle)
        }
    }
}
