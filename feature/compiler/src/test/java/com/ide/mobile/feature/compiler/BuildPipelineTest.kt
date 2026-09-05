package com.ide.mobile.feature.compiler

import com.ide.mobile.core.model.BuildStep
import com.ide.mobile.core.model.ProjectFile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildPipelineTest {

    @Test
    fun testLiveComposeParserExtractsInfo() {
        val code = """
            @Composable
            fun GreetingScreen() {
                Text(text = "Hello World")
                Button(onClick = {}) { Text(text = "Click me") }
            }
        """.trimIndent()

        val parsed = LiveComposeParser.parse(code)
        assertEquals("GreetingScreen", parsed.composableName)
        assertEquals("Hello World", parsed.titleText)
        assertTrue(parsed.hasButton)
    }

    @Test
    fun testBuildPipelineSuccessFlow() = runBlocking {
        val project = ProjectFile.createSampleAndroidProject()
        val flow = BuildPipeline.execute(
            project = project,
            activeCode = "fun main() {}",
            hasSyntaxErrors = false
        )

        val steps = flow.toList()
        assertTrue(steps.isNotEmpty())
        val last = steps.last()
        assertEquals(BuildStep.SUCCESS, last.step)
        assertEquals(1.0f, last.percentage, 0.01f)
    }

    @Test
    fun testBuildPipelineFailsOnSyntaxErrors() = runBlocking {
        val project = ProjectFile.createSampleAndroidProject()
        val flow = BuildPipeline.execute(
            project = project,
            activeCode = "fun broken( {",
            hasSyntaxErrors = true
        )

        val steps = flow.toList()
        val last = steps.last()
        assertEquals(BuildStep.FAILED, last.step)
    }
}
