package com.ide.mobile.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// ─── Modelo de datos ──────────────────────────────────────────────────────────

data class CodeSnippet(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val language: String,
    val code: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Datos de Ejemplo ──────────────────────────────────────────────────────────

val DEFAULT_SNIPPETS = listOf(
    CodeSnippet(
        title = "Flutter Stateful Widget",
        description = "Widget con estado boilerplate listo para usar",
        language = "Dart",
        code = "class MyWidget extends StatefulWidget {\n  const MyWidget({super.key});\n\n  @override\n  State<MyWidget> createState() => _MyWidgetState();\n}\n\nclass _MyWidgetState extends State<MyWidget> {\n  @override\n  Widget build(BuildContext context) {\n    return Container();\n  }\n}",
        tags = listOf("flutter", "stateful", "widget")
    ),
    CodeSnippet(
        title = "viewModelScope coroutine",
        description = "Lanzar coroutine desde un ViewModel con manejo de errores",
        language = "Kotlin",
        code = "viewModelScope.launch {\n    try {\n        val result = repository.fetchData()\n        _uiState.update { it.copy(data = result) }\n    } catch (e: Exception) {\n        _uiState.update { it.copy(error = e.message) }\n    }\n}",
        tags = listOf("kotlin", "coroutine", "viewmodel")
    ),
    CodeSnippet(
        title = "Python FastAPI Endpoint",
        description = "Endpoint REST basico con Pydantic",
        language = "Python",
        code = "from fastapi import FastAPI\nfrom pydantic import BaseModel\n\napp = FastAPI()\n\nclass Item(BaseModel):\n    name: str\n    price: float\n\n@app.post(\"/items/\")\nasync def create_item(item: Item):\n    return {\"id\": 1, **item.dict()}",
        tags = listOf("python", "fastapi", "rest")
    ),
    CodeSnippet(
        title = "Jetpack Compose LaunchedEffect",
        description = "Efecto de lado que se ejecuta al componer el componente",
        language = "Kotlin",
        code = "LaunchedEffect(Unit) {\n    viewModel.loadData()\n    snapshotFlow { listState.firstVisibleItemIndex }\n        .distinctUntilChanged()\n        .collect { index ->\n            // Reaccionar al scroll\n        }\n}",
        tags = listOf("compose", "launchedeffect", "side-effect")
    )
)

// ─── Color por Lenguaje ────────────────────────────────────────────────────────

fun languageAccentColor(lang: String): Color = when (lang.lowercase()) {
    "dart" -> Color(0xFF38BDF8)
    "kotlin" -> Color(0xFFA78BFA)
    "python" -> Color(0xFFFBBF24)
    "javascript", "typescript" -> Color(0xFFF59E0B)
    "xml" -> Color(0xFF34D399)
    "json" -> Color(0xFF94A3B8)
    else -> Color(0xFFC084FC)
}

// ─── Pantalla Principal ────────────────────────────────────────────────────────

@Composable
fun SnippetVaultScreen(
    onInsertSnippet: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var snippets by remember { mutableStateOf(DEFAULT_SNIPPETS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf("Todos") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var expandedSnippetId by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current

    val languages = listOf("Todos") + snippets.map { it.language }.distinct().sorted()

    val filtered = snippets.filter { s ->
        val matchQuery = searchQuery.isBlank() ||
                s.title.contains(searchQuery, true) ||
                s.tags.any { it.contains(searchQuery, true) }
        val matchLang = selectedLang == "Todos" || s.language == selectedLang
        matchQuery && matchLang
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
    ) {
        // Cabecera
        Surface(
            color = Color(0xFF141522),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFF24263A))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver",
                                tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("📦 Snippet Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${snippets.size} fragmentos guardados", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Busqueda
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0C0D15), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            cursorBrush = SolidColor(Color(0xFF7B61FF)),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Buscar snippets, tags...", color = Color(0xFF475569), fontSize = 12.sp)
                                inner()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Filtro por lenguaje
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    languages.forEach { lang ->
                        val selected = lang == selectedLang
                        val color = if (lang == "Todos") Color(0xFF7B61FF) else languageAccentColor(lang)
                        Surface(
                            color = if (selected) color.copy(alpha = 0.2f) else Color(0xFF0C0D15),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selected) color else Color(0xFF26283C)),
                            modifier = Modifier.clickable { selectedLang = lang }
                        ) {
                            Text(lang, fontSize = 10.sp, color = if (selected) color else Color(0xFF64748B),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // Lista
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (searchQuery.isBlank()) "Sin snippets guardados" else "Sin resultados para \"$searchQuery\"",
                        color = Color(0xFF64748B), fontSize = 13.sp)
                    if (searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showCreateDialog = true }) {
                            Text("+ Crear primer snippet", color = Color(0xFF7B61FF), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { snippet ->
                    val isExpanded = expandedSnippetId == snippet.id
                    val accentColor = languageAccentColor(snippet.language)

                    Surface(
                        color = Color(0xFF141522),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(if (isExpanded) 1.2.dp else 0.8.dp,
                            if (isExpanded) accentColor.copy(alpha = 0.7f) else Color(0xFF24263A)),
                        modifier = Modifier.fillMaxWidth().clickable {
                            expandedSnippetId = if (isExpanded) null else snippet.id
                        }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(color = accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                        Text(snippet.language, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Column {
                                        Text(snippet.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        if (snippet.description.isNotBlank())
                                            Text(snippet.description, color = Color(0xFF64748B), fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                                Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                            }

                            if (snippet.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(5.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    snippet.tags.forEach { tag ->
                                        Surface(color = Color(0xFF1C1E30), shape = RoundedCornerShape(4.dp)) {
                                            Text("#$tag", color = Color(0xFF7B61FF), fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(color = Color(0xFF0C0D15), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(snippet.code, color = Color(0xFFE2E8F0), fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace, modifier = Modifier.padding(8.dp), lineHeight = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onInsertSnippet(snippet.code) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Insertar", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { clipboard.setText(AnnotatedString(snippet.code)) },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp),
                                        border = BorderStroke(0.8.dp, Color(0xFF38BDF8))
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Copiar", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                    }
                                    TextButton(
                                        onClick = { snippets = snippets.filter { it.id != snippet.id }; expandedSnippetId = null },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(11.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSnippetDialog(
            onConfirm = { snippets = listOf(it) + snippets; showCreateDialog = false },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun CreateSnippetDialog(onConfirm: (CodeSnippet) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Dart") }
    var code by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    val langOptions = listOf("Dart", "Kotlin", "Python", "JavaScript", "TypeScript", "XML", "JSON", "Otro")

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color(0xFF0F111A), shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF2E324E)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("✨ Nuevo Snippet", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF94A3B8))
                    }
                }
                HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

                // Titulo
                Text("Titulo *", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF141522), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)) {
                    BasicTextField(value = title, onValueChange = { title = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        cursorBrush = SolidColor(Color(0xFF7B61FF)), modifier = Modifier.fillMaxWidth(), singleLine = true,
                        decorationBox = { inner -> if (title.isEmpty()) Text("Ej: Flutter StatefulWidget", color = Color(0xFF475569), fontSize = 12.sp); inner() })
                }

                // Lenguaje
                Text("Lenguaje", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    langOptions.forEach { lang ->
                        val sel = lang == language
                        val col = languageAccentColor(lang)
                        Surface(color = if (sel) col.copy(alpha = 0.2f) else Color(0xFF141522),
                            shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (sel) col else Color(0xFF26283C)),
                            modifier = Modifier.clickable { language = lang }) {
                            Text(lang, color = if (sel) col else Color(0xFF64748B), fontSize = 10.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                // Codigo
                Text("Codigo *", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFF0C0D15), RoundedCornerShape(6.dp)).padding(8.dp)) {
                    BasicTextField(value = code, onValueChange = { code = it },
                        textStyle = TextStyle(color = Color(0xFFE2E8F0), fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                        cursorBrush = SolidColor(Color(0xFF7B61FF)), modifier = Modifier.fillMaxSize(),
                        decorationBox = { inner -> if (code.isEmpty()) Text("Pega tu codigo aqui...", color = Color(0xFF475569), fontSize = 11.sp); inner() })
                }

                // Tags
                Text("Tags (separados por coma)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF141522), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)) {
                    BasicTextField(value = tags, onValueChange = { tags = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        cursorBrush = SolidColor(Color(0xFF7B61FF)), modifier = Modifier.fillMaxWidth(), singleLine = true,
                        decorationBox = { inner -> if (tags.isEmpty()) Text("flutter, widget, estado", color = Color(0xFF475569), fontSize = 12.sp); inner() })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF26283C))) {
                        Text("Cancelar", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && code.isNotBlank()) {
                                onConfirm(CodeSnippet(
                                    title = title.trim(), description = description.trim(),
                                    language = language, code = code.trim(),
                                    tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                ))
                            }
                        },
                        enabled = title.isNotBlank() && code.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
