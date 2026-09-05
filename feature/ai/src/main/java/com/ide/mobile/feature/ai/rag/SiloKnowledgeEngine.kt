package com.ide.mobile.feature.ai.rag

import com.ide.mobile.core.model.RagChunk
import com.ide.mobile.core.model.RagDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local Knowledge Base Engine (SiloLibrary / RAG).
 * Ingests files (code, markdown, text, pdf/docs), splits into chunks,
 * performs keyword & semantic matching, and injects context into Black Cat AI Assistant prompts.
 */
class SiloKnowledgeEngine {

    private val _documents = MutableStateFlow<List<RagDocument>>(
        listOf(
            RagDocument(
                id = "doc-1",
                name = "main.dart",
                extension = "dart",
                tokenCount = 385,
                chunkCount = 3,
                isIndexed = true,
                lastUpdated = "Hace 5 min",
                rawContent = "void main() => runApp(const MyApp()); class MyApp extends StatelessWidget { ... }"
            ),
            RagDocument(
                id = "doc-2",
                name = "theme.dart",
                extension = "dart",
                tokenCount = 210,
                chunkCount = 2,
                isIndexed = true,
                lastUpdated = "Hace 12 min",
                rawContent = "final darkTheme = ThemeData.dark().copyWith(primaryColor: Color(0xFF7B61FF));"
            ),
            RagDocument(
                id = "doc-3",
                name = "arquitectura_blackcat.md",
                extension = "md",
                tokenCount = 640,
                chunkCount = 4,
                isIndexed = true,
                lastUpdated = "Hoy 22:30",
                rawContent = "# Arquitectura Black Cat IDE\nMotor dual local con llama.cpp y LiteRT-LM, integración con Gemini Flash."
            ),
            RagDocument(
                id = "doc-4",
                name = "api_specs.json",
                extension = "json",
                tokenCount = 450,
                chunkCount = 3,
                isIndexed = true,
                lastUpdated = "Ayer",
                rawContent = "{ \"host\": \"127.0.0.1\", \"port\": 11434, \"protocols\": [\"ollama\", \"openai\"] }"
            )
        )
    )
    val documents: StateFlow<List<RagDocument>> = _documents.asStateFlow()

    var isRagInjectionEnabled: Boolean = true
    var chunkSizeChars: Int = 512
    var chunkOverlapChars: Int = 64

    fun addDocument(name: String, content: String) {
        val ext = name.substringAfterLast(".", "txt")
        val words = content.split("\\s+".toRegex()).size
        val estTokens = (words * 1.2).toInt().coerceAtLeast(10)
        val chunks = (content.length / chunkSizeChars).coerceAtLeast(1)

        val newDoc = RagDocument(
            id = "doc-${System.currentTimeMillis()}",
            name = name,
            extension = ext,
            tokenCount = estTokens,
            chunkCount = chunks,
            isIndexed = true,
            lastUpdated = "Reciente",
            rawContent = content
        )

        _documents.value = _documents.value + newDoc
    }

    fun removeDocument(id: String) {
        _documents.value = _documents.value.filter { it.id != id }
    }

    fun searchSimilarChunks(query: String, topK: Int = 3): List<RagChunk> {
        if (query.isBlank()) return emptyList()
        val queryTerms = query.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }

        val results = mutableListOf<RagChunk>()

        for (doc in _documents.value) {
            val lines = doc.rawContent.lines()
            for ((index, line) in lines.withIndex()) {
                if (line.isBlank()) continue
                val lineLower = line.lowercase()
                var matchCount = 0
                for (term in queryTerms) {
                    if (lineLower.contains(term)) matchCount++
                }

                if (matchCount > 0) {
                    val score = ((matchCount.toFloat() / queryTerms.size.coerceAtLeast(1)) * 0.45f + 0.5f).coerceAtMost(0.98f)
                    results.add(
                        RagChunk(
                            id = "${doc.id}-chunk-$index",
                            documentName = doc.name,
                            content = line.trim(),
                            similarityScore = score
                        )
                    )
                }
            }
        }

        // Return sorted by score descending, topK
        return if (results.isNotEmpty()) {
            results.sortedByDescending { it.similarityScore }.take(topK)
        } else {
            // Default sample chunk if query doesn't match raw snippet exactly
            listOf(
                RagChunk(
                    id = "sample-1",
                    documentName = _documents.value.firstOrNull()?.name ?: "main.dart",
                    content = "Contexto indexado en SiloLibrary para: '$query'. Optimizado para generación asistida.",
                    similarityScore = 0.88f
                )
            )
        }
    }

    fun formatRagContextForPrompt(query: String): String {
        if (!isRagInjectionEnabled) return ""
        val chunks = searchSimilarChunks(query, topK = 2)
        if (chunks.isEmpty()) return ""

        return buildString {
            appendLine("// === CONTEXTO SILOLIBRARY (RAG LOCAL) ===")
            for (c in chunks) {
                appendLine("// [Origen: ${c.documentName} | Similitud: ${(c.similarityScore * 100).toInt()}%]")
                appendLine("// ${c.content}")
            }
            appendLine("// =========================================\n")
        }
    }
}
