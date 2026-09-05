# Black Cat IDE 🐱⚡
> **CÓDIGO • IA • PRODUCTIVIDAD**  
> ⭐ **CREATED BY J.COMPE**

Blackcat es un entorno de desarrollo móvil avanzado para Android diseñado desde cero con una **arquitectura multi-modular en Gradle y Jetpack Compose**, optimizado para la ergonomía y desarrollo ágil en smartphones. Permite crear y editar código en pantalla táctil y ejecutar modelos de IA de forma local (`.gguf` con `llama.cpp` y `.litertlm` con `LiteRT-LM`), con soporte híbrido para la API de Google Gemini Flash, análisis de sintaxis autónomo y base de conocimiento local RAG (SiloLibrary).

---

## 🏛 Arquitectura Multi-Modular (8 Módulos)

```
mobile-android-ide/
├── settings.gradle.kts           # Configuración multimodular de Gradle
├── build.gradle.kts              # Plugins raíz (Application & Library)
├── gradle.properties
│
├── core/
│   └── model/                    # Modelos de dominio (ProjectFile, AiModelEntity, RouterConfig, DiagnosticIssue)
│
├── feature/
│   ├── editor/                   # Motor táctil del editor (CodeEditorCore, DartLexer, KotlinLexer, SyntaxTheme)
│   ├── keyboard/                 # Barra flotante de símbolos, micro-cursor y salto sobre corchetes (Jump Over)
│   ├── explorer/                 # Árbol de archivos y cajón deslizable (ProjectExplorerDrawer)
│   ├── diagnostics/              # Validador sintáctico en segundo plano con version guard y QuickFixes
│   ├── compiler/                 # BuildPipeline, daemon aislado y LiveComposePreviewHost
│   └── ai/                       # Gemini Flash, llama.cpp, LiteRT-LM, SiloLibrary RAG y SmartInferenceRouter
│
└── app/                          # Orquestación Scaffold, IdeViewModel, 15 vistas y Centro de IA (AI Hub)
```

---

## ✨ Características Destacadas

### 1. Centro de IA y Modelos Locales (AI Hub)
- **Gestor de Modelos (*Model Management*)**: Lista y descarga de modelos de Hugging Face Mobile (`Qwen3.5-2B`, `Gemma-4-E2B`, `Llama-3.2-1B`) e importación local vía Storage Access Framework (SAF).
- **Motor de Inferencia & Telemetría**: Selector de runtime (`llama.cpp` vs `LiteRT-LM`), monitoreo en tiempo real de RAM, velocidad (`tok/s`), CPU/NPU, y temperatura.
- **Smart Inference Router (Anti-OOM)**: Enrutamiento inteligente que evalúa la longitud del prompt; si excede el umbral seguro de RAM del teléfono, lo desvía automáticamente a Gemini Cloud para prevenir cuelgues.
- **SiloLibrary RAG**: Base de conocimiento local con fragmentación (*chunking*), similitud vectorial e inyección automática de contexto en el chat de IA.

### 2. Editor de Código Táctil y Ergonomía Móvil
- **Línea 13 activa**: Destacado con acento púrpura neón (`#7B61FF`) y numeración en gutter idéntico a la APK de referencia.
- **Auto-cierre y Salto sobre Cierre (*Jump Over Closing Bracket*)**: Al teclear `}`, `)` o `"`, el cursor salta hacia adelante sin duplicar el símbolo.
- **Envolver selección**: Seleccionar texto y pulsar un símbolo lo envuelve automáticamente (e.g. `{texto}`).
- **Barra de accesorios de teclado**: Acceso directo a símbolos frecuentes, indentación Tab (4 espacios), micro-flechas de cursor y botones `Undo` / `Redo`.

### 3. Diagnóstico Sintáctico en Tiempo Real
- **Análisis reactivo no bloqueante** con corrutinas en segundo plano y protección anti-condiciones de carrera (*Version Guard*).
- Detección de llaves desbalanceadas, cadenas sin cerrar y etiquetas XML inválidas con botones de **Auto-corregir (QuickFix)** en 1 toque.

### 4. Seguridad de Red y Blindaje de Procesos
- **Network Security Config**: Cifrado obligatorio HTTPS/TLS para todo el tráfico web, restringiendo HTTP en texto plano exclusivamente a `127.0.0.1` y la red local `192.168.1.4`.
- **Compilador Aislado**: El daemon de compilación se ejecuta en un proceso Android separado (`:build_daemon`), protegiendo la UI de la app de cualquier fallo de memoria (`OOM`).

---

## 🚀 Compilación del APK

Para generar el archivo binario instalable (`.apk`):
```powershell
./gradlew :app:assembleDebug
```
El APK se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

---
⭐ **Black Cat IDE** — Creado y diseñado por **J.COMPE**.
