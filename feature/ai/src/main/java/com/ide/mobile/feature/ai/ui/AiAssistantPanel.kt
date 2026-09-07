package com.ide.mobile.feature.ai.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ide.mobile.core.model.*
import com.ide.mobile.feature.ai.ProviderType

@Composable
fun AiAssistantPanel(
    selectedProvider: ProviderType = ProviderType.GEMINI_API,
    onSelectProvider: (ProviderType) -> Unit = {},
    activeAgent: LocalAgentEntity? = null,
    availableAgents: List<LocalAgentEntity> = emptyList(),
    onSelectAgent: (LocalAgentEntity) -> Unit = {},
    downloadableAgents: List<DownloadableAgent> = emptyList(),
    onDownloadAndActivateAgent: (DownloadableAgent) -> Unit = {},
    chatMessages: List<ChatMessage> = emptyList(),
    currentExecutionStep: String? = null,
    isAiLoading: Boolean = false,
    onSendMessage: (String) -> Unit = {},
    onExecuteAction: (AgentAction) -> Unit = {},
    onViewInTerminal: (AgentAction) -> Unit = {},
    onRejectAction: (AgentAction) -> Unit = {},
    onClearChat: () -> Unit = {},
    onInsertCode: (String) -> Unit = {},
    onApplyCodeWithTarget: ((String, String?) -> Unit)? = null,
    onToggleTask: ((String, String) -> Unit)? = null,
    onProceedPlan: (() -> Unit)? = null,
    onClose: () -> Unit = {},
    onOpenMissionControl: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }
    var showDownloadCatalog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var customAgents by remember { mutableStateOf(listOf<Agent>()) }

    // Construir lista unificada de agentes (Locales y Cloud)
    val allAntigravityAgents = remember(availableAgents, selectedProvider, activeAgent, customAgents) {
        val list = mutableListOf<Agent>()
        availableAgents.forEach { local ->
            list.add(
                LocalAgent(
                    id = local.id,
                    name = local.name,
                    description = local.description,
                    icon = local.icon,
                    isAvailable = local.isBuiltIn || (local.storagePath != null),
                    executablePath = "/data/local/tmp/agent_${local.id}",
                    workingDirectory = "."
                )
            )
        }
        list.add(ApiAgent(id = "gemini-free", name = "Gemini Free Tier", endpoint = "https://generativelanguage.googleapis.com", modelName = "gemini-1.5-flash", icon = "⚡"))
        list.add(ApiAgent(id = "chatgpt-4o", name = "ChatGPT (4o)", endpoint = "https://api.openai.com/v1", modelName = "gpt-4o", icon = "🧠"))
        list.add(ApiAgent(id = "claude-35", name = "Claude (3.5 Sonnet)", endpoint = "https://api.anthropic.com/v1", modelName = "claude-3-5-sonnet", icon = "🎭"))
        list.add(ApiAgent(id = "perplexity", name = "Perplexity Online", endpoint = "https://api.perplexity.ai", modelName = "sonar", icon = "🔍"))
        list.add(LocalAgent(id = "local-gguf", name = "Local GGUF Runtime", executablePath = "llama-cli", workingDirectory = ".", icon = "📱"))
        list.addAll(customAgents)
        list
    }

    var selectedAgentState by remember(selectedProvider, activeAgent) {
        mutableStateOf(
            if (selectedProvider == ProviderType.LOCAL_AGENT && activeAgent != null) {
                allAntigravityAgents.find { it.id == activeAgent.id } ?: allAntigravityAgents.first()
            } else {
                when (selectedProvider) {
                    ProviderType.GEMINI_API -> allAntigravityAgents.find { it.id == "gemini-free" }
                    ProviderType.OPENAI_API -> allAntigravityAgents.find { it.id == "chatgpt-4o" }
                    ProviderType.CLAUDE_API -> allAntigravityAgents.find { it.id == "claude-35" }
                    ProviderType.PERPLEXITY_API -> allAntigravityAgents.find { it.id == "perplexity" }
                    ProviderType.LOCAL_GGUF -> allAntigravityAgents.find { it.id == "local-gguf" }
                    else -> allAntigravityAgents.firstOrNull()
                } ?: allAntigravityAgents.first()
            }
        )
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(chatMessages.size, isAiLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
    ) {
        // 1. Barra de Encabezado Superior
        Surface(
            color = Color(0xFF141522),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFF24263A))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✨ Antigravity Studio",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Selector de Agentes interactivo con dropdown
                        AgentSelector(
                            selectedAgent = selectedAgentState,
                            availableAgents = allAntigravityAgents,
                            onSelectAgent = { agent ->
                                selectedAgentState = agent
                                when (agent) {
                                    is LocalAgent -> {
                                        onSelectProvider(ProviderType.LOCAL_AGENT)
                                        val matching = availableAgents.find { it.id == agent.id }
                                        if (matching != null) onSelectAgent(matching)
                                    }
                                    is ApiAgent -> {
                                        when (agent.id) {
                                            "gemini-free" -> onSelectProvider(ProviderType.GEMINI_API)
                                            "chatgpt-4o" -> onSelectProvider(ProviderType.OPENAI_API)
                                            "claude-35" -> onSelectProvider(ProviderType.CLAUDE_API)
                                            "perplexity" -> onSelectProvider(ProviderType.PERPLEXITY_API)
                                            else -> onSelectProvider(ProviderType.GEMINI_API)
                                        }
                                    }
                                }
                            },
                            onAddNewApiAgent = { newAgent ->
                                customAgents = customAgents + newAgent
                                selectedAgentState = newAgent
                            }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showDownloadCatalog = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar Agentes",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (onOpenMissionControl != null) {
                            IconButton(
                                onClick = onOpenMissionControl,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = "Mission Control",
                                    tint = Color(0xFFE879F9),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpiar Chat",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selector Horizontal de Proveedores de IA
                val providerList = listOf(
                    ProviderType.LOCAL_AGENT to "🤖 Agentes Antigravity",
                    ProviderType.GEMINI_API to "⚡ Gemini Free",
                    ProviderType.OPENAI_API to "🧠 ChatGPT (4o)",
                    ProviderType.CLAUDE_API to "🎭 Claude (3.5)",
                    ProviderType.PERPLEXITY_API to "🔍 Perplexity",
                    ProviderType.LOCAL_GGUF to "📱 Local LM"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    providerList.forEach { (type, label) ->
                        val isSelected = selectedProvider == type
                        Surface(
                            color = if (isSelected) Color(0xFF7B61FF).copy(alpha = 0.25f) else Color(0xFF1B1D2D),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF7B61FF) else Color(0xFF26283C)),
                            modifier = Modifier.clickable { onSelectProvider(type) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Chips de selección de agente si el proveedor es LOCAL_AGENT
                if (selectedProvider == ProviderType.LOCAL_AGENT && availableAgents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        availableAgents.forEach { agent ->
                            val isAgentActive = agent.id == activeAgent?.id
                            Surface(
                                color = if (isAgentActive) Color(0xFF7B61FF) else Color(0xFF1A1C2C),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, if (isAgentActive) Color(0xFFA78BFA) else Color(0xFF26283C)),
                                modifier = Modifier.clickable { onSelectAgent(agent) }
                            ) {
                                Text(
                                    text = "${agent.icon} ${agent.name}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = if (isAgentActive) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Botón de acceso rápido a descarga de agentes
                        Surface(
                            color = Color(0xFF102A36),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { showDownloadCatalog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Descargar Agente", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Banner de Ejecución en Vivo ("Muestra lo que está ejecutando")
        if (currentExecutionStep != null || isAiLoading) {
            Surface(
                color = Color(0xFF1A1733),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFF7B61FF).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        color = Color(0xFF38BDF8),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = currentExecutionStep ?: "Procesando respuesta en vivo...",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 3. Conversación Fluida (Historial de Mensajes)
        Box(modifier = Modifier.weight(1f)) {
            if (chatMessages.isEmpty()) {
                // Pantalla de Bienvenida y Sugerencias de Herramientas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeAgent?.icon ?: "🐱",
                        fontSize = 42.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeAgent?.name ?: "Black Cat Antigravity Copilot",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeAgent?.description ?: "Listo para crear carpetas, instalar paquetes y compilar.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✨ ¿Qué deseas crear hoy?",
                        color = Color(0xFFC084FC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toca una opción o escribe abajo con tus propias palabras:",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val starterCards = listOf(
                        Triple("📱", "Crear una App de Tareas", "Crea una app completa de lista de tareas con opciones para agregar, marcar y eliminar tareas"),
                        Triple("🎨", "Diseñar Pantalla de Login", "Diseña una pantalla de inicio de sesión moderna con correo, contraseña y botón animado"),
                        Triple("🧮", "Crear una Calculadora Táctil", "Crea el código de una calculadora funcional con diseño limpio y botones interactivos"),
                        Triple("👤", "Diseñar Perfil de Usuario", "Crea una pantalla de perfil de usuario con foto circular, biografía y botones"),
                        Triple("🔍", "Explicar mi Código en Español", "Explícame con palabras sencillas y paso a paso qué hace el código de mi archivo actual")
                    )

                    starterCards.forEach { (emoji, title, promptText) ->
                        Surface(
                            color = Color(0xFF141522),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF262A42)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onSendMessage(promptText) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = promptText,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages, key = { it.id }) { message ->
                        ChatMessageBubble(
                            message = message,
                            onApproveAction = onExecuteAction,
                            onRejectAction = onRejectAction,
                            onViewInTerminal = onViewInTerminal,
                            onApplyCode = onInsertCode,
                            onApplyCodeWithTarget = onApplyCodeWithTarget,
                            onToggleTask = onToggleTask,
                            onProceedPlan = onProceedPlan
                        )
                    }
                }
            }
        }

        // 4. Barra de Entrada Expandible Dinámica Multimodal (con soporte de visión y fotos adjuntas)
        ExpandableMultimodalInputBar(
            onSendMessage = { text, _ ->
                onSendMessage(text)
            },
            isLoading = isAiLoading
        )
    }

    // Modal de Catálogo de Descarga de Agentes
    if (showDownloadCatalog) {
        DownloadAgentsDialog(
            agents = downloadableAgents,
            activeAgentId = activeAgent?.id,
            onDownloadAndActivate = { agent ->
                onDownloadAndActivateAgent(agent)
                showDownloadCatalog = false
            },
            onDismiss = { showDownloadCatalog = false }
        )
    }
}

/**
 * Renderizador de mensaje individual con burbuja, bloques de código y tarjetas de terminal ejecutables.
 */
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onExecuteAction: (AgentAction) -> Unit,
    onViewInTerminal: (AgentAction) -> Unit,
    onRejectAction: (AgentAction) -> Unit,
    onInsertCode: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Cabecera del Mensaje
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isUser) "Tú" else "${message.agentIcon ?: "🤖"} ${message.agentName ?: "Agente Antigravity"}",
                color = if (isUser) Color(0xFFA78BFA) else Color(0xFF38BDF8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Burbuja del Mensaje
        Surface(
            color = if (isUser) Color(0xFF261D47) else Color(0xFF141522),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            border = BorderStroke(0.8.dp, if (isUser) Color(0xFF7B61FF).copy(alpha = 0.4f) else Color(0xFF24263A)),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.text,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // Si el mensaje contiene bloques de código, mostrar acciones de código
                if (!isUser && message.text.contains("```")) {
                    val codeBlocks = extractCodeBlocks(message.text)
                    codeBlocks.forEach { code ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onCopyText(code) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp),
                                border = BorderStroke(0.8.dp, Color(0xFF64748B))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copiar", color = Color.White, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { onInsertCode(code) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Insertar en Editor", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Tarjetas Interactivas de Acción de Terminal / Archivos / Paquetes
                if (message.actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ Herramientas y Acciones Propuestas:",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    message.actions.forEach { action ->
                        AgentActionCard(
                            action = action,
                            onExecute = { onExecuteAction(action) },
                            onViewTerminal = { onViewInTerminal(action) },
                            onReject = { onRejectAction(action) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta interactiva para la terminal y herramientas que el usuario puede revisar y aprobar.
 */
@Composable
fun AgentActionCard(
    action: AgentAction,
    onExecute: () -> Unit,
    onViewTerminal: () -> Unit,
    onReject: () -> Unit
) {
    val icon = when (action.type) {
        AgentActionType.RUN_COMMAND -> Icons.Default.Terminal
        AgentActionType.INSTALL_DEPENDENCY -> Icons.Default.Extension
        AgentActionType.CREATE_FOLDER -> Icons.Default.Folder
        AgentActionType.CREATE_FILE -> Icons.Default.Code
        AgentActionType.MODIFY_FILE -> Icons.Default.Edit
        AgentActionType.HTTP_REQUEST -> Icons.Default.Language
    }

    val accentColor = when (action.status) {
        ActionStatus.PROPOSED -> Color(0xFF38BDF8)
        ActionStatus.RUNNING -> Color(0xFFFBBF24)
        ActionStatus.SUCCESS -> Color(0xFF34D399)
        ActionStatus.FAILED -> Color(0xFFEF4444)
        ActionStatus.REJECTED -> Color(0xFF64748B)
    }

    Surface(
        color = Color(0xFF0C0D15),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = action.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when (action.status) {
                            ActionStatus.PROPOSED -> "Pendiente"
                            ActionStatus.RUNNING -> "Ejecutando..."
                            ActionStatus.SUCCESS -> "Completado ✓"
                            ActionStatus.FAILED -> "Error ✗"
                            ActionStatus.REJECTED -> "Descartado"
                        },
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Vista previa del comando / código
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0xFF141522),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = action.payload.trim().take(120),
                    color = Color(0xFFA5B4FC),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(6.dp)
                )
            }

            // Salida de consola si ya se ejecutó
            val outputText = action.output
            if (!outputText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = outputText,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Botones de Acción (Si está pendiente)
            if (action.status == ActionStatus.PROPOSED) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onExecute,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Aprobar y Ejecutar", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewTerminal,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Ver en Terminal", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }

                    TextButton(
                        onClick = onReject,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Descartar", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

/**
 * Diálogo interactivo del catálogo para descargar y activar agentes en el teléfono.
 */
@Composable
fun DownloadAgentsDialog(
    agents: List<DownloadableAgent>,
    activeAgentId: String?,
    onDownloadAndActivate: (DownloadableAgent) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF0F111A),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF2E324E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📥 Catálogo de Agentes Antigravity",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Descarga agentes especializados y actívalos al instante para desarrollo móvil óptimo:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(agents, key = { it.id }) { agent ->
                        val isCurrent = agent.id == activeAgentId
                        Surface(
                            color = Color(0xFF141522),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isCurrent) Color(0xFF34D399) else Color(0xFF24263A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(agent.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(agent.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(agent.category, color = Color(0xFF38BDF8), fontSize = 9.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { onDownloadAndActivate(agent) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCurrent) Color(0xFF1E3A2F) else Color(0xFF7B61FF)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = if (isCurrent) "✓ Activo" else "Descargar y Activar",
                                            color = if (isCurrent) Color(0xFF34D399) else Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(agent.description, color = Color(0xFFCBD5E1), fontSize = 10.sp)

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    agent.skills.forEach { skill ->
                                        Surface(
                                            color = Color(0xFF1C1E30),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = skill,
                                                color = Color(0xFFA5B4FC),
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun extractCodeBlocks(text: String): List<String> {
    val list = mutableListOf<String>()
    val regex = Regex("```(?:[a-zA-Z0-9_-]+)?\\s*\\n([\\s\\S]*?)```")
    for (match in regex.findAll(text)) {
        list.add(match.groupValues[1].trim())
    }
    return list
}
