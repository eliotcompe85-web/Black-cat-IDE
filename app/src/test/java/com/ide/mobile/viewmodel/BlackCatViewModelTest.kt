package com.ide.mobile.viewmodel

import com.ide.mobile.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlackCatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RoomWorkspaceRepository
    private lateinit var viewModel: BlackCatViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = RoomWorkspaceRepository()
        viewModel = BlackCatViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertNotNull(viewModel.recentProjects.value)
        assertEquals(1, viewModel.activeAgents.value.size)
        assertEquals("1", viewModel.activeAgents.value[0].agentId)
        assertEquals(AgentState.Idle, viewModel.activeAgents.value[0].currentState)
        assertTrue(viewModel.chatMessages.value.isEmpty())
    }

    @Test
    fun testUpdateAgentState() {
        viewModel.updateAgentState("1", AgentState.Planning("Creando arquitectura..."))
        val agent = viewModel.activeAgents.value.first { it.agentId == "1" }
        assertTrue(agent.currentState is AgentState.Planning)
        assertEquals("Creando arquitectura...", (agent.currentState as AgentState.Planning).currentThought)
    }

    @Test
    fun testCreateProject() = runTest {
        viewModel.createProject("MyNewTestProject", "/storage/emulated/0/MyNewTestProject")
        advanceUntilIdle()

        val recent = repository.getRecentProjects()
        assertTrue(recent.any { it.name == "MyNewTestProject" })
    }

    @Test
    fun testSendMessageSimulatesAgentResponse() = runTest {
        viewModel.sendMessage("Hola Black Cat, crea un archivo main")
        runCurrent()

        assertEquals(1, viewModel.chatMessages.value.size)
        assertEquals("Desarrollador", viewModel.chatMessages.value[0].senderName)
        assertTrue(viewModel.chatMessages.value[0].isFromUser)

        val agentPlanning = viewModel.activeAgents.value.first { it.agentId == "1" }
        assertTrue(agentPlanning.currentState is AgentState.Planning)

        advanceTimeBy(1600)
        runCurrent()
        val agentExecuting = viewModel.activeAgents.value.first { it.agentId == "1" }
        assertTrue(agentExecuting.currentState is AgentState.Executing)

        advanceUntilIdle()
        val agentIdle = viewModel.activeAgents.value.first { it.agentId == "1" }
        assertEquals(AgentState.Idle, agentIdle.currentState)

        assertEquals(2, viewModel.chatMessages.value.size)
        val agentMsg = viewModel.chatMessages.value[1]
        assertEquals("Local Copilot", agentMsg.senderName)
        assertFalse(agentMsg.isFromUser)
        assertTrue(agentMsg.contents.any { it is ChatContent.CodeBlock })
    }
}
