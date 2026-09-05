package com.ide.mobile.feature.diagnostics

import com.ide.mobile.core.model.LanguageType
import com.ide.mobile.core.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntaxAnalyzerTest {

    @Test
    fun testValidKotlinCodeProducesNoErrors() {
        val code = """
            fun main() {
                val greeting = "Hello Kotlin"
                println(greeting)
            }
        """.trimIndent()

        val issues = SyntaxAnalyzer.analyze(code, LanguageType.KOTLIN)
        val errors = issues.filter { it.severity == Severity.ERROR }
        assertTrue("Expected 0 errors for valid code, found: $errors", errors.isEmpty())
    }

    @Test
    fun testDetectUnclosedBraceWithQuickFix() {
        val code = """
            fun broken() {
                val x = 10
        """.trimIndent()

        val issues = SyntaxAnalyzer.analyze(code, LanguageType.KOTLIN)
        val errors = issues.filter { it.severity == Severity.ERROR }
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("llave"))
        assertTrue(errors[0].quickFix != null)
        assertEquals("\n}", errors[0].quickFix?.replacementText)
    }

    @Test
    fun testDetectMismatchedXmlTag() {
        val xml = "<LinearLayout><TextView></LinearLayout>"
        val issues = SyntaxAnalyzer.analyze(xml, LanguageType.XML)
        val errors = issues.filter { it.severity == Severity.ERROR }
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun testAnalyzeRequestWithVersionPreservation() {
        val request = com.ide.mobile.core.model.AnalysisRequest(
            fileId = "test.kt",
            language = LanguageType.KOTLIN,
            code = "fun test() {",
            documentVersion = 42L
        )
        val response = SyntaxAnalyzer.analyzeRequest(request)
        assertEquals(42L, response.documentVersion)
        assertEquals("test.kt", response.fileId)
        assertTrue(response.issues.isNotEmpty())
        assertTrue(response.executionTimeMs >= 0)
    }
}
