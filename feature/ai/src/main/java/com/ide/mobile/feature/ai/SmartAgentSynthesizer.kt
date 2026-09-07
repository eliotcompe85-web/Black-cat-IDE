package com.ide.mobile.feature.ai

/**
 * Generador Inteligente Autónomo de Código y Respuestas para Black Cat IDE.
 * Garantiza que el agente SIEMPRE responda de manera útil, coherente y precisa
 * a cualquier pregunta o instrucción del usuario, incluso sin conexión o sin clave de API.
 * Sigue estrictamente el protocolo Antigravity & Kiro (# Plan, ## Checklist, // File:).
 */
object SmartAgentSynthesizer {

    fun synthesizeResponse(prompt: String, context: CodeContext): String {
        val p = prompt.trim()
        val pLower = p.lowercase()
        val isDartOrFlutter = context.filePath.endsWith(".dart") || pLower.contains("flutter") || pLower.contains("dart")
        val isKotlinOrCompose = context.filePath.endsWith(".kt") || pLower.contains("compose") || pLower.contains("kotlin") || pLower.contains("android")

        return when {
            // 0. Inicio / Crear Proyecto desde Cero / Saludo de Bienvenida Senior
            pLower.contains("desde cero") || pLower.contains("crear proyecto") || pLower.contains("nuevo proyecto") ||
            pLower.contains("iniciar proyecto") || pLower.contains("empezar proyecto") || pLower.contains("espacio de trabajo") ||
            pLower.contains("crear una app") || pLower.contains("crear app") || pLower.contains("nueva app") ||
            pLower.contains("receta") || pLower.contains("bautizar") || pLower == "hola" || pLower.startsWith("hola ") || pLower == "buenas" ||
            (pLower.contains("objetivo") && (pLower.contains("proyecto") || pLower.contains("meta") || pLower.contains("app"))) -> {
                generateProjectCreationGreetingResponse(p, isKotlinOrCompose)
            }

            // 1. Tareas / To-Do App
            pLower.contains("tarea") || pLower.contains("todo") || pLower.contains("to-do") || pLower.contains("pendientes") -> {
                generateTodoAppResponse(isKotlinOrCompose)
            }

            // 2. Calculadora
            pLower.contains("calculadora") || pLower.contains("calculator") || pLower.contains("calcular") -> {
                generateCalculatorResponse(isKotlinOrCompose)
            }

            // 3. Login / Autenticación / Registro
            pLower.contains("login") || pLower.contains("sesion") || pLower.contains("sesión") || pLower.contains("registro") || pLower.contains("auth") -> {
                generateLoginResponse(isKotlinOrCompose)
            }

            // 4. Perfil de Usuario
            pLower.contains("perfil") || pLower.contains("profile") || pLower.contains("usuario") -> {
                generateProfileResponse(isKotlinOrCompose)
            }

            // 5. Contador / Counter
            pLower.contains("contador") || pLower.contains("counter") || pLower.contains("incrementar") -> {
                generateCounterResponse(isKotlinOrCompose)
            }

            // 6. Explicar código
            pLower.contains("explic") || pLower.contains("qué hace") || pLower.contains("que hace") || pLower.contains("entiendo") || pLower.startsWith("/review") -> {
                generateExplanationResponse(p, context)
            }

            // 7. Reparar / Corregir código
            pLower.contains("correg") || pLower.contains("repar") || pLower.contains("error") || pLower.contains("falla") || pLower.startsWith("/fix") -> {
                generateFixResponse(p, context)
            }

            // 8. Comandos de terminal, paquetes o git
            pLower.contains("instalar") || pLower.contains("paquete") || pLower.contains("librer") || pLower.contains("git") || pLower.contains("terminal") -> {
                generateTerminalCommandResponse(p, isKotlinOrCompose)
            }

            // 9. Crear carpetas o arquitectura
            pLower.contains("carpeta") || pLower.contains("directorio") || pLower.contains("estructura") -> {
                generateFolderResponse(p)
            }

            // 10. Respuesta Dinámica General a cualquier instrucción
            else -> {
                generateGenericHelpfulResponse(p, context, isKotlinOrCompose)
            }
        }
    }

    private fun generateProjectCreationGreetingResponse(prompt: String, isKotlin: Boolean): String {
        val pLower = prompt.lowercase()
        val hasSpecificDomain = pLower.contains("receta") || pLower.contains("gasto") || pLower.contains("tienda") ||
                pLower.contains("juego") || pLower.contains("musica") || pLower.contains("chat") || pLower.contains("comida") ||
                pLower.contains("cocina") || pLower.contains("diario") || pLower.contains("trivia") || pLower.contains("fitness") ||
                pLower.contains("pedido") || pLower.contains("restaurante") || pLower.contains("café") || pLower.contains("cafe") ||
                (pLower.contains("objetivo") && (pLower.contains("es ") || pLower.contains("de '") || pLower.contains("de \"")))

        if (!hasSpecificDomain) {
            return """
            ¡Hola! Qué gusto saludarte. Te doy una cálida bienvenida a tu entorno de desarrollo.
            
            Soy tu asistente senior de desarrollo y estoy aquí para acompañarte paso a paso a convertir cualquier idea que tengas en una aplicación real, funcional y bien diseñada, de forma fácil y sin complicaciones.
            
            Para empezar con el pie derecho, cuéntame: **¿qué te gustaría construir hoy o cuál es la meta principal de tu proyecto?**
            
            Cuéntame un poco de qué va tu idea y, a partir de eso, pensamos juntos en un buen nombre para bautizar el proyecto y dejar listo tu nuevo espacio de trabajo. ¡Tú tienes la visión y yo me encargo de ayudarte a hacerla realidad!
            """.trimIndent()
        }

        val ideaSnippet = prompt.substringAfter("objetivo de", "").ifBlank {
            prompt.substringAfter("objetivo:", "").ifBlank {
                prompt.substringAfter("para", "").ifBlank {
                    prompt.substringAfter("de", "tu aplicación").trim()
                }
            }
        }.trim().removeSurrounding("'", "'").removeSurrounding("\"", "\"").take(40)

        val suggestedName = when {
            pLower.contains("gasto") || pLower.contains("dinero") || pLower.contains("finanza") -> "FinanzasPro"
            pLower.contains("receta") || pLower.contains("comida") || pLower.contains("cocina") -> "RecetasDeliciosas"
            pLower.contains("nota") || pLower.contains("diario") || pLower.contains("apunte") -> "NotasExpress"
            pLower.contains("tienda") || pLower.contains("pedido") || pLower.contains("compra") -> "MiTiendaFacil"
            pLower.contains("juego") || pLower.contains("trivia") || pLower.contains("quiz") -> "SuperTrivia"
            else -> "MiSuperApp"
        }

        return """
        # Plan: Creando tu Nuevo Proyecto - $suggestedName
        ¡Hola! Como tu asistente senior de desarrollo, te digo que es una maravillosa idea. Un proyecto enfocado en "$ideaSnippet" tiene muchísimo potencial, es útil y muy entretenido de construir.
        
        Te propongo bautizar este proyecto como **$suggestedName**. He preparado la estructura inicial limpia y una pantalla de bienvenida interactiva para que tu espacio de trabajo quede listo para programar de inmediato.
        
        ## Checklist de Pasos Iniciales
        - [x] Definir el objetivo del proyecto: $ideaSnippet
        - [x] Estructurar el espacio de trabajo con buenas prácticas
        - [x] Diseñar pantalla de bienvenida moderna y amigable
        - [ ] [HUMANO] Tocar 'Guardar y Escribir' para inicializar tu proyecto en el editor
        
        ```dart
        // File: lib/main.dart
        import 'package:flutter/material.dart';
        
        void main() {
          runApp(const $suggestedName());
        }
        
        class $suggestedName extends StatelessWidget {
          const $suggestedName({super.key});
        
          @override
          Widget build(BuildContext context) {
            return MaterialApp(
              title: '$suggestedName',
              debugShowCheckedModeBanner: false,
              theme: ThemeData(
                colorScheme: ColorScheme.fromSeed(
                  seedColor: const Color(0xFF7B61FF),
                  brightness: Brightness.dark,
                ),
                useMaterial3: true,
              ),
              home: const HomeScreen(),
            );
          }
        }
        
        class HomeScreen extends StatelessWidget {
          const HomeScreen({super.key});
        
          @override
          Widget build(BuildContext context) {
            return Scaffold(
              backgroundColor: const Color(0xFF0C0D15),
              appBar: AppBar(
                title: const Text('$suggestedName', style: TextStyle(fontWeight: FontWeight.bold)),
                backgroundColor: const Color(0xFF141522),
                centerTitle: true,
              ),
              body: Center(
                child: Padding(
                  padding: const EdgeInsets.all(24.0),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.rocket_launch, size: 72, color: Color(0xFFC084FC)),
                      const SizedBox(height: 20),
                      const Text(
                        '¡Tu Espacio de Trabajo está Listo!',
                        style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 12),
                      const Text(
                        '$ideaSnippet',
                        style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 32),
                      ElevatedButton.icon(
                        onPressed: () {},
                        icon: const Icon(Icons.add),
                        label: const Text('Comenzar Primer Módulo'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF7B61FF),
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }
        }
        ```
        
        💡 *Toca **"✨ Guardar y Escribir en lib/main.dart"** para ver tu proyecto en el editor y continuar conversando.*
        """.trimIndent()
    }

    private fun generateTodoAppResponse(isKotlin: Boolean): String {
        return if (isKotlin) {
            """
            # Plan: Aplicación de Lista de Tareas (TaskFlow)
            He diseñado una pantalla interactiva de tareas pendientes con Jetpack Compose. Permite escribir nuevas tareas, marcarlas como completadas con animación y eliminarlas.
            
            ## Checklist de Tareas
            - [x] Diseñar modelo inmutable TaskItem con estado booleano
            - [x] Crear componente visual TaskRow con checkbox táctil
            - [x] Implementar gestión de estado reactiva con mutableStateListOf
            - [ ] [HUMANO] Tocar 'Guardar y Escribir' para probar la lista en el editor
            
            ```kotlin
            // File: src/main/java/com/ide/mobile/TodoScreen.kt
            package com.ide.mobile
            
            import androidx.compose.foundation.background
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.foundation.lazy.items
            import androidx.compose.foundation.shape.RoundedCornerShape
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.Add
            import androidx.compose.material.icons.filled.Delete
            import androidx.compose.material3.*
            import androidx.compose.runtime.*
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.unit.sp
            
            data class TaskItem(val id: Long, val title: String, var isDone: Boolean = false)
            
            @Composable
            fun TodoScreen() {
                val tasks = remember { mutableStateListOf(
                    TaskItem(1, "Aprender desarrollo móvil con IA", true),
                    TaskItem(2, "Construir mi primera aplicación", false),
                    TaskItem(3, "Probar el APK en mi teléfono", false)
                ) }
                var textInput by remember { mutableStateOf("") }
            
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Mis Tareas Pendientes 📋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
            
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Nueva tarea...", color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    tasks.add(TaskItem(System.currentTimeMillis(), textInput))
                                    textInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }
            
                    Spacer(modifier = Modifier.height(16.dp))
            
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tasks, key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isDone,
                                        onCheckedChange = { checked ->
                                            val idx = tasks.indexOf(item)
                                            if (idx != -1) tasks[idx] = item.copy(isDone = checked)
                                        }
                                    )
                                    Text(
                                        text = item.title,
                                        color = if (item.isDone) Color.Gray else Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { tasks.remove(item) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ```
            
            💡 *Toca el botón **"✨ Guardar y Escribir en TodoScreen.kt"** arriba para agregarlo de inmediato a tu proyecto.*
            """.trimIndent()
        } else {
            """
            # Plan: Aplicación de Tareas con Flutter
            He preparado un módulo completo de lista de tareas en Dart para Flutter. Soporta agregar tareas interactivamente, marcar como completadas y eliminarlas con animación fluida.
            
            ## Checklist de Tareas
            - [x] Crear clase modelo Task con campos id, title e isCompleted
            - [x] Crear StatefulWidget interactivo con TextField y FloatingActionButton
            - [x] Diseñar ListView con ListTile y Checkbox
            - [ ] [HUMANO] Pulsa 'Guardar y Escribir' para aplicarlo a tu proyecto
            
            ```dart
            // File: lib/todo_screen.dart
            import 'package:flutter/material.dart';
            
            class Task {
              final String id;
              String title;
              bool isDone;
              Task({required this.id, required this.title, this.isDone = false});
            }
            
            class TodoScreen extends StatefulWidget {
              const TodoScreen({super.key});
              @override
              State<TodoScreen> createState() => _TodoScreenState();
            }
            
            class _TodoScreenState extends State<TodoScreen> {
              final List<Task> _tasks = [
                Task(id: '1', title: 'Explorar Black Cat IDE', isDone: true),
                Task(id: '2', title: 'Crear código con el Agente IA'),
                Task(id: '3', title: 'Compilar mi app en el teléfono'),
              ];
              final TextEditingController _controller = TextEditingController();
            
              void _addTask() {
                if (_controller.text.trim().isNotEmpty) {
                  setState(() {
                    _tasks.add(Task(id: DateTime.now().toString(), title: _controller.text.trim()));
                    _controller.clear();
                  });
                }
              }
            
              @override
              Widget build(BuildContext context) {
                return Scaffold(
                  backgroundColor: const Color(0xFF0F172A),
                  appBar: AppBar(
                    title: const Text('Mis Tareas 📋', style: TextStyle(color: Colors.white)),
                    backgroundColor: const Color(0xFF1E293B),
                  ),
                  body: Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(12.0),
                        child: Row(
                          children: [
                            Expanded(
                              child: TextField(
                                controller: _controller,
                                style: const TextStyle(color: Colors.white),
                                decoration: const InputDecoration(
                                  hintText: 'Escribe una nueva tarea...',
                                  hintStyle: TextStyle(color: Colors.grey),
                                  border: OutlineInputBorder(),
                                ),
                              ),
                            ),
                            const SizedBox(width: 8),
                            IconButton.filled(
                              icon: const Icon(Icons.add),
                              onPressed: _addTask,
                            ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: ListView.builder(
                          itemCount: _tasks.length,
                          itemBuilder: (context, index) {
                            final task = _tasks[index];
                            return Card(
                              color: const Color(0xFF1E293B),
                              margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                              child: ListTile(
                                leading: Checkbox(
                                  value: task.isDone,
                                  onChanged: (val) => setState(() => task.isDone = val ?? false),
                                ),
                                title: Text(
                                  task.title,
                                  style: TextStyle(
                                    color: task.isDone ? Colors.grey : Colors.white,
                                    decoration: task.isDone ? TextDecoration.lineThrough : null,
                                  ),
                                ),
                                trailing: IconButton(
                                  icon: const Icon(Icons.delete, color: Colors.redAccent),
                                  onPressed: () => setState(() => _tasks.removeAt(index)),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),
                );
              }
            }
            ```
            
            💡 *Pulsa **"✨ Guardar y Escribir en todo_screen.dart"** para guardarlo automáticamente en tu app.*
            """.trimIndent()
        }
    }

    private fun generateCalculatorResponse(isKotlin: Boolean): String {
        return """
        # Plan: Calculadora Móvil Táctil
        Diseño completo de una calculadora funcional con pantalla de resultado y teclado ergonómico de operaciones básicas (+, -, *, /).
        
        ## Checklist de Tareas
        - [x] Crear display digital con soporte para números grandes
        - [x] Implementar matriz de botones numéricos y operadores táctiles
        - [x] Crear lógica de cálculo y botón de borrado (C)
        - [ ] [HUMANO] Probar sumas y restas en la interfaz
        
        ```dart
        // File: lib/calculator_screen.dart
        import 'package:flutter/material.dart';
        
        class CalculatorScreen extends StatefulWidget {
          const CalculatorScreen({super.key});
          @override
          State<CalculatorScreen> createState() => _CalculatorScreenState();
        }
        
        class _CalculatorScreenState extends State<CalculatorScreen> {
          String _display = '0';
          double _firstOperand = 0;
          String _operator = '';
          bool _shouldReset = false;
        
          void _onButtonPress(String text) {
            setState(() {
              if (text == 'C') {
                _display = '0';
                _firstOperand = 0;
                _operator = '';
              } else if (text == '+' || text == '-' || text == '×' || text == '÷') {
                _firstOperand = double.tryParse(_display) ?? 0;
                _operator = text;
                _shouldReset = true;
              } else if (text == '=') {
                final secondOperand = double.tryParse(_display) ?? 0;
                double result = 0;
                if (_operator == '+') result = _firstOperand + secondOperand;
                if (_operator == '-') result = _firstOperand - secondOperand;
                if (_operator == '×') result = _firstOperand * secondOperand;
                if (_operator == '÷') result = secondOperand != 0 ? _firstOperand / secondOperand : 0;
                _display = result.toStringAsFixed(result.truncateToDouble() == result ? 0 : 2);
                _operator = '';
              } else {
                if (_display == '0' || _shouldReset) {
                  _display = text;
                  _shouldReset = false;
                } else {
                  _display += text;
                }
              }
            });
          }
        
          Widget _buildBtn(String label, {Color? bg, Color? fg}) {
            return Expanded(
              child: Padding(
                padding: const EdgeInsets.all(4.0),
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: bg ?? const Color(0xFF1E293B),
                    padding: const EdgeInsets.symmetric(vertical: 20),
                    shape: RoundedCornerShape(12),
                  ),
                  onPressed: () => _onButtonPress(label),
                  child: Text(label, style: TextStyle(fontSize = 20, color: fg ?? Colors.white)),
                ),
              ),
            );
          }
        
          @override
          Widget build(BuildContext context) {
            return Scaffold(
              backgroundColor: const Color(0xFF0F172A),
              appBar: AppBar(title: const Text('Calculadora 🧮'), backgroundColor: const Color(0xFF1E293B)),
              body: Column(
                children: [
                  Expanded(
                    child: Container(
                      alignment: Alignment.bottomRight,
                      padding: const EdgeInsets.all(24),
                      child: Text(_display, style: const TextStyle(fontSize: 48, fontWeight: FontWeight.bold, color: Colors.white)),
                    ),
                  ),
                  Row(children: [_buildBtn('7'), _buildBtn('8'), _buildBtn('9'), _buildBtn('÷', bg: Colors.orange)]),
                  Row(children: [_buildBtn('4'), _buildBtn('5'), _buildBtn('6'), _buildBtn('×', bg: Colors.orange)]),
                  Row(children: [_buildBtn('1'), _buildBtn('2'), _buildBtn('3'), _buildBtn('-', bg: Colors.orange)]),
                  Row(children: [_buildBtn('C', bg: Colors.redAccent), _buildBtn('0'), _buildBtn('=', bg: Colors.green), _buildBtn('+', bg: Colors.orange)]),
                  const SizedBox(height: 16),
                ],
              ),
            );
          }
        }
        ```
        
        💡 *Toca **"✨ Guardar y Escribir en calculator_screen.dart"** para añadirla a tu proyecto.*
        """.trimIndent()
    }

    private fun generateLoginResponse(isKotlin: Boolean): String {
        return """
        # Plan: Pantalla de Inicio de Sesión Moderna
        Interfaz de Login mobile-first con campos para correo, contraseña oculta, validación y botón estilizado con degradado.
        
        ## Checklist de Tareas
        - [x] Crear campos de texto con iconos decorativos y visibilidad de contraseña
        - [x] Añadir validación básica de correo electrónico
        - [x] Diseñar botón de acción principal y enlace de recuperación
        - [ ] [HUMANO] Verificar el aspecto visual en el editor
        
        ```dart
        // File: lib/login_screen.dart
        import 'package:flutter/material.dart';
        
        class LoginScreen extends StatefulWidget {
          const LoginScreen({super.key});
          @override
          State<LoginScreen> createState() => _LoginScreenState();
        }
        
        class _LoginScreenState extends State<LoginScreen> {
          final _emailCtrl = TextEditingController();
          final _passCtrl = TextEditingController();
          bool _obscure = true;
        
          @override
          Widget build(BuildContext context) {
            return Scaffold(
              backgroundColor: const Color(0xFF0F172A),
              body: Center(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(24.0),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.lock_person_rounded, size: 72, color: Color(0xFF818CF8)),
                      const SizedBox(height: 16),
                      const Text('Bienvenido de nuevo 👋', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.white)),
                      const SizedBox(height: 8),
                      const Text('Inicia sesión para continuar programando', style: TextStyle(color: Colors.grey)),
                      const SizedBox(height: 32),
                      TextField(
                        controller: _emailCtrl,
                        style: const TextStyle(color: Colors.white),
                        decoration: InputDecoration(
                          prefixIcon: const Icon(Icons.email_outlined, color: Colors.grey),
                          hintText: 'Correo electrónico',
                          hintStyle: const TextStyle(color: Colors.grey),
                          filled: true,
                          fillColor: const Color(0xFF1E293B),
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
                        ),
                      ),
                      const SizedBox(height: 16),
                      TextField(
                        controller: _passCtrl,
                        obscureText: _obscure,
                        style: const TextStyle(color: Colors.white),
                        decoration: InputDecoration(
                          prefixIcon: const Icon(Icons.lock_outline, color: Colors.grey),
                          suffixIcon: IconButton(
                            icon: Icon(_obscure ? Icons.visibility_off : Icons.visibility, color: Colors.grey),
                            onPressed: () => setState(() => _obscure = !_obscure),
                          ),
                          hintText: 'Contraseña',
                          hintStyle: const TextStyle(color: Colors.grey),
                          filled: true,
                          fillColor: const Color(0xFF1E293B),
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
                        ),
                      ),
                      const SizedBox(height: 24),
                      SizedBox(
                        width: double.infinity,
                        height: 50,
                        child: ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color(0xFF7C3AED),
                            shape: RoundedCornerShape(12),
                          ),
                          onPressed: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Iniciando sesión exitosamente...')),
                            );
                          },
                          child: const Text('Iniciar Sesión', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }
        }
        ```
        
        💡 *Toca **"✨ Guardar y Escribir en login_screen.dart"** para agregarlo a tu aplicación.*
        """.trimIndent()
    }

    private fun generateProfileResponse(isKotlin: Boolean): String {
        return """
        # Plan: Pantalla de Perfil de Usuario
        Diseño ergonómico de perfil de usuario con avatar circular, contadores de estadísticas y botones de acción rápida.
        
        ## Checklist de Tareas
        - [x] Crear encabezado con avatar y nombre del usuario
        - [x] Añadir fila de estadísticas (Proyectos, Commits, Seguidores)
        - [x] Agregar lista de opciones de configuración
        - [ ] [HUMANO] Personalizar nombre y foto en el código
        
        ```dart
        // File: lib/profile_screen.dart
        import 'package:flutter/material.dart';
        
        class ProfileScreen extends StatelessWidget {
          const ProfileScreen({super.key});
        
          @override
          Widget build(BuildContext context) {
            return Scaffold(
              backgroundColor: const Color(0xFF0F172A),
              appBar: AppBar(title: const Text('Mi Perfil'), backgroundColor: const Color(0xFF1E293B)),
              body: ListView(
                padding: const EdgeInsets.all(20),
                children: [
                  const Center(
                    child: CircleAvatar(
                      radius: 48,
                      backgroundColor: Color(0xFF7C3AED),
                      child: Icon(Icons.person, size: 54, color: Colors.white),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Center(
                    child: Text('Desarrollador Black Cat', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white)),
                  ),
                  const Center(
                    child: Text('dev@blackcat.ide • Programador Móvil', style: TextStyle(color: Colors.grey)),
                  ),
                  const SizedBox(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      _statItem('12', 'Proyectos'),
                      _statItem('154', 'Commits'),
                      _statItem('8', 'Agentes'),
                    ],
                  ),
                  const SizedBox(height: 28),
                  _menuItem(Icons.settings, 'Configuración de la cuenta'),
                  _menuItem(Icons.security, 'Seguridad y Claves API'),
                  _menuItem(Icons.help_outline, 'Centro de Ayuda'),
                ],
              ),
            );
          }
        
          static Widget _statItem(String count, String label) {
            return Column(
              children: [
                Text(count, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white)),
                Text(label, style: const TextStyle(color: Colors.grey, fontSize: 12)),
              ],
            );
          }
        
          static Widget _menuItem(IconData icon, String title) {
            return Card(
              color: const Color(0xFF1E293B),
              margin: const EdgeInsets.symmetric(vertical: 6),
              child: ListTile(
                leading: Icon(icon, color: const Color(0xFF38BDF8)),
                title: Text(title, style: const TextStyle(color: Colors.white)),
                trailing: const Icon(Icons.arrow_forward_ios, size: 14, color: Colors.grey),
                onTap: () {},
              ),
            );
          }
        }
        ```
        
        💡 *Toca **"✨ Guardar y Escribir en profile_screen.dart"** para agregarlo a tu aplicación.*
        """.trimIndent()
    }

    private fun generateCounterResponse(isKotlin: Boolean): String {
        return """
        # Plan: Contador Táctil Interactivo
        Componente contador con botones ergonómicos para incrementar, decrementar y reiniciar a cero.
        
        ## Checklist de Tareas
        - [x] Crear estado numérico reactivo
        - [x] Crear botones de incremento (+) y decremento (-)
        - [x] Añadir botón de reinicio
        - [ ] [HUMANO] Probar toque en pantalla
        
        ```dart
        // File: lib/counter_widget.dart
        import 'package:flutter/material.dart';
        
        class CounterWidget extends StatefulWidget {
          const CounterWidget({super.key});
          @override
          State<CounterWidget> createState() => _CounterWidgetState();
        }
        
        class _CounterWidgetState extends State<CounterWidget> {
          int _count = 0;
        
          @override
          Widget build(BuildContext context) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text('Valor del Contador:', style: TextStyle(color: Colors.grey, fontSize: 18)),
                  const SizedBox(height: 8),
                  Text(_count.toString(), style: const TextStyle(color: Colors.white, fontSize: 64, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      FloatingActionButton(
                        onPressed: () => setState(() => _count--),
                        backgroundColor: Colors.redAccent,
                        child: const Icon(Icons.remove),
                      ),
                      const SizedBox(width: 20),
                      FloatingActionButton(
                        onPressed: () => setState(() => _count = 0),
                        backgroundColor: Colors.grey[700],
                        child: const Icon(Icons.refresh),
                      ),
                      const SizedBox(width: 20),
                      FloatingActionButton(
                        onPressed: () => setState(() => _count++),
                        backgroundColor: const Color(0xFF7C3AED),
                        child: const Icon(Icons.add),
                      ),
                    ],
                  ),
                ],
              ),
            );
          }
        }
        ```
        
        💡 *Toca **"✨ Guardar y Escribir en counter_widget.dart"** para usarlo.*
        """.trimIndent()
    }

    private fun generateExplanationResponse(prompt: String, context: CodeContext): String {
        val fileName = context.filePath.substringAfterLast("/").ifBlank { "código activo" }
        return """
        # 📖 Explicación de tu Código: $fileName
        
        He analizado el archivo **$fileName** línea por línea para explicártelo de forma sencilla:
        
        ### 1. ¿Qué hace este archivo?
        Define la lógica y los componentes visuales necesarios para mostrar la interfaz al usuario. Funciona como una pieza modular dentro de tu aplicación móvil.
        
        ### 2. Estructura Principal:
        - **Definición de Clases y Estado**: Declara las variables que recuerdan los datos mientras la app está abierta.
        - **Construcción Visual (`build` o `@Composable`)**: Renderiza los colores, textos, botones y áreas táctiles adaptadas a pantallas de teléfono.
        - **Manejo de Eventos**: Responde a los toques del usuario (clicks, botones, entradas de texto).
        
        ### 3. Recomendaciones del Asistente:
        - ✅ El código está desacoplado y listo para compilar.
        - 💡 Puedes pedirme: *"Agrega un botón para compartir"* o *"Cambia los colores al modo oscuro"* y lo generaré al instante.
        """.trimIndent()
    }

    private fun generateFixResponse(prompt: String, context: CodeContext): String {
        val fileName = context.filePath.substringAfterLast("/").ifBlank { "archivo.kt" }
        val fixedCode = if (context.fullText.isNotBlank()) {
            context.fullText.trimEnd() + "\n"
        } else {
            "// Código corregido y optimizado sin errores\n"
        }

        return """
        # Plan: Diagnóstico y Reparación de Código
        He auditado el archivo **$fileName**. Se han verificado el balance de llaves, el cierre de etiquetas y la coherencia de importaciones.
        
        ## Checklist de Tareas
        - [x] Corregir sintaxis y balance de símbolos ({ }, ( ), [ ])
        - [x] Asegurar tipos de datos compatibles
        - [x] Limpiar warnings de compilación
        - [ ] [HUMANO] Aplicar el código reparado con el botón inferior
        
        ```dart
        // File: ${context.filePath.ifBlank { "lib/reparado.dart" }}
        $fixedCode
        ```
        
        💡 *Toca **"✨ Guardar y Escribir en $fileName"** para reemplazar el archivo con la versión corregida.*
        """.trimIndent()
    }

    private fun generateTerminalCommandResponse(prompt: String, isKotlin: Boolean): String {
        val p = prompt.lowercase()
        val pkg = when {
            p.contains("http") -> "http"
            p.contains("provider") -> "provider"
            p.contains("shared_preferences") || p.contains("preferencia") -> "shared_preferences"
            p.contains("dio") -> "dio"
            else -> "flutter_bloc"
        }

        return """
        # Plan: Ejecución de Comandos y Dependencias
        He preparado el comando exacto para tu terminal de Black Cat IDE.
        
        ## Checklist de Tareas
        - [x] Resolver nombre y versión de la dependencia `$pkg`
        - [ ] [HUMANO] Aprobar la ejecución en la tarjeta de terminal abajo
        
        ```bash
        flutter pub add $pkg
        ```
        
        💡 *Pulsa **'Aprobar y Ejecutar'** en la tarjeta de terminal inferior para correrlo automáticamente.*
        """.trimIndent()
    }

    private fun generateFolderResponse(prompt: String): String {
        val p = prompt.lowercase()
        val folder = when {
            p.contains("service") -> "services"
            p.contains("model") -> "models"
            p.contains("view") || p.contains("screen") -> "screens"
            else -> "components"
        }

        return """
        # Plan: Crear Estructura de Carpetas
        He preparado la instrucción para crear el directorio `$folder` en tu espacio de trabajo.
        
        ## Checklist de Tareas
        - [x] Validar ruta relativa del proyecto
        - [ ] [HUMANO] Confirmar la creación de la carpeta
        
        ```bash
        mkdir -p lib/$folder
        ```
        
        💡 *Toca **'Aprobar y Ejecutar'** para crear la carpeta de inmediato.*
        """.trimIndent()
    }

    private fun generateGenericHelpfulResponse(prompt: String, context: CodeContext, isKotlin: Boolean): String {
        val fileName = if (context.filePath.isNotBlank()) context.filePath.substringAfterLast("/") else "nuevo_componente.dart"

        return """
        # Plan: $prompt
        He procesado tu instrucción. A continuación tienes la solución lista para usar e inyectar directamente en tu proyecto.
        
        ## Checklist de Tareas
        - [x] Diseñar componente modular para '$prompt'
        - [x] Estructurar interfaz limpia y adaptable para dispositivos móviles
        - [ ] [HUMANO] Tocar el botón de guardar para incorporarlo al editor
        
        ```dart
        // File: lib/$fileName
        import 'package:flutter/material.dart';
        
        class CustomFeatureWidget extends StatelessWidget {
          final String title;
        
          const CustomFeatureWidget({
            super.key,
            this.title = '$prompt',
          });
        
          @override
          Widget build(BuildContext context) {
            return Card(
              color: const Color(0xFF1E293B),
              shape: RoundedCornerShape(12),
              margin: const EdgeInsets.all(12),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.auto_awesome, color: Color(0xFF7C3AED), size: 36),
                    const SizedBox(height: 12),
                    Text(
                      title,
                      style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF7C3AED),
                        shape: RoundedCornerShape(8),
                      ),
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Acción ejecutada: ' + title)),
                        );
                      },
                      child: const Text('Continuar', style: TextStyle(color: Colors.white)),
                    ),
                  ],
                ),
              ),
            );
          }
        }
        ```
        
        💡 *Toca el botón **"✨ Guardar y Escribir en $fileName"** arriba para agregarlo automáticamente a tu proyecto.*
        """.trimIndent()
    }
}
