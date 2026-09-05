package com.ide.mobile.feature.diagnostics

import com.ide.mobile.core.model.DiagnosticIssue
import com.ide.mobile.core.model.LanguageType
import com.ide.mobile.core.model.QuickFix
import com.ide.mobile.core.model.Severity
import com.ide.mobile.core.model.AnalysisRequest
import com.ide.mobile.core.model.AnalysisResponse
import java.util.UUID

object SyntaxAnalyzer {

    fun analyzeRequest(request: AnalysisRequest): AnalysisResponse {
        val start = System.currentTimeMillis()
        val issues = analyze(request.code, request.language)
        val elapsed = System.currentTimeMillis() - start
        return AnalysisResponse(
            fileId = request.fileId,
            documentVersion = request.documentVersion,
            issues = issues,
            executionTimeMs = elapsed
        )
    }

    fun analyze(code: String, language: LanguageType): List<DiagnosticIssue> {
        return when (language) {
            LanguageType.KOTLIN -> analyzeKotlin(code)
            LanguageType.XML -> analyzeXml(code)
            else -> emptyList()
        }
    }

    private data class BracketItem(
        val char: Char,
        val offset: Int,
        val line: Int,
        val column: Int
    )

    private fun analyzeKotlin(code: String): List<DiagnosticIssue> {
        val issues = mutableListOf<DiagnosticIssue>()
        val lines = code.split("\n")
        val bracketStack = ArrayDeque<BracketItem>()

        var currentLine = 1
        var currentCol = 0
        var i = 0

        var inSingleQuote = false
        var inDoubleQuote = false
        var inTripleQuote = false
        var inSingleLineComment = false
        var inBlockComment = false
        var stringStartOffset = -1
        var stringStartLine = -1
        var stringStartCol = -1

        while (i < code.length) {
            val c = code[i]
            val nextC = if (i + 1 < code.length) code[i + 1] else null
            val prevC = if (i > 0) code[i - 1] else null

            // Newline handling
            if (c == '\n') {
                if (inSingleLineComment) {
                    inSingleLineComment = false
                }
                if (inDoubleQuote && !inTripleQuote) {
                    // Single-line string cannot span across newlines in Kotlin!
                    issues.add(
                        DiagnosticIssue(
                            id = UUID.randomUUID().toString(),
                            line = stringStartLine,
                            column = stringStartCol,
                            message = "String literal no cerrado antes del fin de línea",
                            severity = Severity.ERROR,
                            quickFix = QuickFix(
                                id = "close_string_${stringStartOffset}",
                                title = "Cerrar comilla (\") al final de la línea",
                                replacementRange = i..i,
                                replacementText = "\""
                            )
                        )
                    )
                    inDoubleQuote = false
                }
                currentLine++
                currentCol = 0
                i++
                continue
            }

            currentCol++

            // Comments handling
            if (!inDoubleQuote && !inSingleQuote) {
                if (!inBlockComment && !inSingleLineComment) {
                    if (c == '/' && nextC == '/') {
                        inSingleLineComment = true
                        i += 2
                        currentCol++
                        continue
                    }
                    if (c == '/' && nextC == '*') {
                        inBlockComment = true
                        i += 2
                        currentCol++
                        continue
                    }
                } else if (inBlockComment) {
                    if (c == '*' && nextC == '/') {
                        inBlockComment = false
                        i += 2
                        currentCol++
                        continue
                    }
                    i++
                    continue
                } else {
                    i++
                    continue
                }
            }

            if (inSingleLineComment || inBlockComment) {
                i++
                continue
            }

            // Triple quote strings
            if (c == '"' && nextC == '"' && i + 2 < code.length && code[i + 2] == '"') {
                inTripleQuote = !inTripleQuote
                i += 3
                currentCol += 2
                continue
            }

            // String literals
            if (c == '"' && !inTripleQuote && prevC != '\\') {
                inDoubleQuote = !inDoubleQuote
                if (inDoubleQuote) {
                    stringStartOffset = i
                    stringStartLine = currentLine
                    stringStartCol = currentCol
                }
                i++
                continue
            }

            if (inDoubleQuote || inTripleQuote) {
                i++
                continue
            }

            // Char literals
            if (c == '\'' && prevC != '\\') {
                inSingleQuote = !inSingleQuote
                i++
                continue
            }
            if (inSingleQuote) {
                i++
                continue
            }

            // Bracket validation: (), {}, []
            when (c) {
                '(', '{', '[' -> {
                    bracketStack.addLast(BracketItem(c, i, currentLine, currentCol))
                }
                ')' -> {
                    if (bracketStack.isEmpty() || bracketStack.last().char != '(') {
                        issues.add(
                            DiagnosticIssue(
                                id = UUID.randomUUID().toString(),
                                line = currentLine,
                                column = currentCol,
                                message = "Paréntesis de cierre ')' inesperado",
                                severity = Severity.ERROR,
                                quickFix = QuickFix(
                                    id = "remove_paren_$i",
                                    title = "Eliminar ')' sobrante",
                                    replacementRange = i..(i + 1),
                                    replacementText = ""
                                )
                            )
                        )
                    } else {
                        bracketStack.removeLast()
                    }
                }
                '}' -> {
                    if (bracketStack.isEmpty() || bracketStack.last().char != '{') {
                        issues.add(
                            DiagnosticIssue(
                                id = UUID.randomUUID().toString(),
                                line = currentLine,
                                column = currentCol,
                                message = "Llave de cierre '}' inesperada",
                                severity = Severity.ERROR,
                                quickFix = QuickFix(
                                    id = "remove_brace_$i",
                                    title = "Eliminar '}' sobrante",
                                    replacementRange = i..(i + 1),
                                    replacementText = ""
                                )
                            )
                        )
                    } else {
                        bracketStack.removeLast()
                    }
                }
                ']' -> {
                    if (bracketStack.isEmpty() || bracketStack.last().char != '[') {
                        issues.add(
                            DiagnosticIssue(
                                id = UUID.randomUUID().toString(),
                                line = currentLine,
                                column = currentCol,
                                message = "Corchete de cierre ']' inesperado",
                                severity = Severity.ERROR,
                                quickFix = QuickFix(
                                    id = "remove_bracket_$i",
                                    title = "Eliminar ']' sobrante",
                                    replacementRange = i..(i + 1),
                                    replacementText = ""
                                )
                            )
                        )
                    } else {
                        bracketStack.removeLast()
                    }
                }
            }

            i++
        }

        // Check remaining unclosed brackets in stack
        while (bracketStack.isNotEmpty()) {
            val item = bracketStack.removeLast()
            val (expectedChar, expectedName) = when (item.char) {
                '{' -> Pair("}", "llave")
                '(' -> Pair(")", "paréntesis")
                '[' -> Pair("]", "corchete")
                else -> Pair("", "")
            }
            issues.add(
                DiagnosticIssue(
                    id = UUID.randomUUID().toString(),
                    line = item.line,
                    column = item.column,
                    message = "Falta $expectedName de cierre '$expectedChar'",
                    severity = Severity.ERROR,
                    quickFix = QuickFix(
                        id = "close_${item.char}_${item.offset}",
                        title = "Insertar '$expectedChar' de cierre",
                        replacementRange = code.length..code.length,
                        replacementText = "\n$expectedChar"
                    )
                )
            )
        }

        // Additional checks for incomplete declarations
        lines.forEachIndexed { index, lineStr ->
            val trimmed = lineStr.trim()
            val lineNum = index + 1
            if (trimmed.startsWith("fun ") && !trimmed.contains("(") && !trimmed.contains("=")) {
                issues.add(
                    DiagnosticIssue(
                        id = UUID.randomUUID().toString(),
                        line = lineNum,
                        column = lineStr.indexOf("fun"),
                        message = "Declaración de función incompleta (faltan paréntesis)",
                        severity = Severity.WARNING,
                        quickFix = QuickFix(
                            id = "fix_fun_$lineNum",
                            title = "Añadir '()'",
                            replacementRange = (lineStr.length)..(lineStr.length),
                            replacementText = "() {}"
                        )
                    )
                )
            }
            if (trimmed == "val" || trimmed == "var") {
                issues.add(
                    DiagnosticIssue(
                        id = UUID.randomUUID().toString(),
                        line = lineNum,
                        column = lineStr.indexOf(trimmed),
                        message = "Declaración de variable incompleta",
                        severity = Severity.ERROR
                    )
                )
            }
        }

        return issues
    }

    private fun analyzeXml(code: String): List<DiagnosticIssue> {
        val issues = mutableListOf<DiagnosticIssue>()
        val tagStack = ArrayDeque<Pair<String, Int>>() // tag name and line

        val tagRegex = Regex("<(/?[a-zA-Z0-9_\\-:]+)([^>]*)(/?)>")
        var lineNum = 1
        var lastIndex = 0

        tagRegex.findAll(code).forEach { match ->
            val rawTag = match.groups[1]?.value ?: return@forEach
            val selfClosing = match.groups[3]?.value == "/" || match.value.endsWith("/>")

            // Count lines up to match
            lineNum += code.substring(lastIndex, match.range.first).count { it == '\n' }
            lastIndex = match.range.first

            if (rawTag.startsWith("?xml") || rawTag.startsWith("!--")) {
                return@forEach
            }

            if (rawTag.startsWith("/")) {
                val closingName = rawTag.substring(1)
                if (tagStack.isEmpty()) {
                    issues.add(
                        DiagnosticIssue(
                            id = UUID.randomUUID().toString(),
                            line = lineNum,
                            column = 0,
                            message = "Etiqueta de cierre </$closingName> sin etiqueta de apertura",
                            severity = Severity.ERROR
                        )
                    )
                } else {
                    val last = tagStack.removeLast()
                    if (last.first != closingName) {
                        issues.add(
                            DiagnosticIssue(
                                id = UUID.randomUUID().toString(),
                                line = lineNum,
                                column = 0,
                                message = "Etiqueta incongruente: se esperaba </${last.first}> pero se encontró </$closingName>",
                                severity = Severity.ERROR
                            )
                        )
                    }
                }
            } else if (!selfClosing) {
                tagStack.addLast(Pair(rawTag, lineNum))
            }
        }

        while (tagStack.isNotEmpty()) {
            val unclosed = tagStack.removeLast()
            issues.add(
                DiagnosticIssue(
                    id = UUID.randomUUID().toString(),
                    line = unclosed.second,
                    column = 0,
                    message = "Etiqueta XML <${unclosed.first}> no fue cerrada",
                    severity = Severity.ERROR,
                    quickFix = QuickFix(
                        id = "close_tag_${unclosed.first}",
                        title = "Cerrar etiqueta </${unclosed.first}>",
                        replacementRange = code.length..code.length,
                        replacementText = "\n</${unclosed.first}>"
                    )
                )
            )
        }

        return issues
    }
}
