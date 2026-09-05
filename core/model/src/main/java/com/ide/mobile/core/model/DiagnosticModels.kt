package com.ide.mobile.core.model

enum class Severity {
    ERROR,
    WARNING,
    INFO
}

data class QuickFix(
    val id: String,
    val title: String,
    val replacementRange: IntRange,
    val replacementText: String
)

data class DiagnosticIssue(
    val id: String,
    val line: Int,           // 1-indexed
    val column: Int,         // 0-indexed column within the line
    val length: Int = 1,
    val message: String,
    val severity: Severity = Severity.ERROR,
    val quickFix: QuickFix? = null
)

enum class AnalysisStatus {
    IDLE,          // Inactive / waiting
    ANALYZING,     // Parsing in background Dispatchers.Default
    UP_TO_DATE,    // Analysis completed for current version
    FAILED         // Parser exception / failure
}

data class DiagnosticsState(
    val documentVersion: Long = 0L,
    val issues: List<DiagnosticIssue> = emptyList(),
    val issuesByLine: Map<Int, List<DiagnosticIssue>> = emptyMap(),
    val status: AnalysisStatus = AnalysisStatus.IDLE,
    val focusedIssue: DiagnosticIssue? = null,
    val executionTimeMs: Long = 0L
)

data class AnalysisRequest(
    val fileId: String,
    val language: LanguageType,
    val code: String,
    val documentVersion: Long,
    val cursorOffset: Int = 0,
    val isImmediate: Boolean = false
)

data class AnalysisResponse(
    val fileId: String,
    val documentVersion: Long,
    val issues: List<DiagnosticIssue>,
    val executionTimeMs: Long
)
