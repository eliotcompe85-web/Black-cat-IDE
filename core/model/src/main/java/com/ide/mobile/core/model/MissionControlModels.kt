package com.ide.mobile.core.model

import androidx.compose.runtime.Immutable

/**
 * Definición de los estados de ejecución de la máquina de estados del Agente (Mission Control).
 */
sealed interface AgentState {
    data object Idle : AgentState

    data class Planning(val currentThought: String) : AgentState

    data class Executing(
        val taskDescription: String,
        val progress: Float // 0.0 a 1.0
    ) : AgentState

    data class Verifying(val verificationStep: String) : AgentState

    data class WaitingForUser(
        val actionRequired: String,
        val canResume: Boolean = true
    ) : AgentState
}

/**
 * Modelo de UI para el Mission Control.
 */
@Immutable
data class ActiveAgentContext(
    val agentId: String,
    val agentName: String,
    val isLocal: Boolean,
    val currentState: AgentState
)
