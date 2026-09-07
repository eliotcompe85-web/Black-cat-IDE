# 🐾 Manual de Usuario Completo: Black Cat IDE Mobile

Bienvenido a **Black Cat IDE**, el entorno de desarrollo móvil de última generación diseñado para teléfonos y tabletas Android, equipado con un **motor agéntico autónomo** basado en los protocolos de **Google Antigravity** y **AWS Kiro**.

---

## 📑 Tabla de Contenidos
1. [Instalación y Primer Inicio](#1-instalación-y-primer-inicio)
2. [Estructura de la Interfaz y Navegación](#2-estructura-de-la-interfaz-y-navegación)
3. [El Editor de Código y Teclado de Programación](#3-el-editor-de-código-y-teclado-de-programación)
4. [Explorador de Archivos y Gestión de Proyectos](#4-explorador-de-archivos-y-gestión-de-proyectos)
5. [Terminal Integrada y Ejecución](#5-terminal-integrada-y-ejecución)
6. [Asistente de Inteligencia Artificial (Motor Antigravity)](#6-asistente-de-inteligencia-artificial-motor-antigravity)
   - [Modos: Planning vs Fast](#modos-planning-mode-vs-fast-mode)
   - [Comandos Rápidos Slash](#comandos-rápidos-slash)
   - [Cómo hace la IA para escribir código en tus archivos](#cómo-la-ia-escribe-código-en-tus-archivos)
   - [Entrada Multimodal y Visión](#adjuntar-imágenes-y-capturas-visión)
7. [Mission Control (Control de Misión y Agentes)](#7-mission-control-control-de-misión)
8. [AI Hub y Selección de Modelos (Locales y en la Nube)](#8-ai-hub-y-gestión-de-modelos)
9. [Control de Versiones con Git](#9-control-de-versiones-con-git)
10. [Vista Previa en Vivo (Live Preview)](#10-vista-previa-en-vivo-live-preview)
11. [Solución de Dudas Frecuentes (FAQ)](#11-solución-de-dudas-frecuentes-faq)

---

## 1. Instalación y Primer Inicio

### ¿Dónde está el archivo de instalación?
El archivo de instalación es **`Black-Cat-IDE.apk`** (~18.5 MB) y se encuentra en:
- El **Escritorio** de tu computadora: `C:\Users\jimmy\Desktop\Black-Cat-IDE.apk`
- La raíz del proyecto: `c:\Users\jimmy\Desktop\Black-cat-IDE\Black-cat-IDE\Black-Cat-IDE.apk`

### Pasos de instalación en Android:
1. Pasa el archivo `Black-Cat-IDE.apk` a tu teléfono (vía cable USB, WhatsApp, Telegram o Google Drive).
2. Toca el archivo descargado para abrir el instalador de Android.
3. Si el sistema te lo pide, activa el permiso **"Permitir desde esta fuente"**.
4. Pulsa **Instalar** y luego **Abrir**.
5. Al abrir, Black Cat IDE inicializará tu espacio de trabajo local con persistencia offline automática (SQLite / Room Database).

---

## 2. Estructura de la Interfaz y Navegación

La aplicación cuenta con una **barra de navegación ergonómica** situada en la zona accesible al pulgar:

| Pestaña | Icono | Función |
| :--- | :---: | :--- |
| **EDITOR** | `📝` | Área de edición con sintaxis coloreada y atajos. |
| **EXPLORADOR** | `📁` | Árbol jerárquico de carpetas, creación de archivos y plantillas. |
| **TERMINAL** | `💻` | Consola interactiva para ejecutar scripts y comandos. |
| **AI ASISTENTE** | `✨` | Chat inteligente con el protocolo Antigravity / Kiro. |
| **MISSION CONTROL** | `🚀` | Panel de orquestación y monitoreo del estado de los agentes en vivo. |
| **GIT** | `🌿` | Control de ramas, diffs, commits y sincronización con GitHub. |
| **AI HUB** | `🧠` | Descarga de modelos GGUF locales y configuración de claves API. |

---

## 3. El Editor de Código y Teclado de Programación

El editor está optimizado para escribir código sin frustraciones en pantallas táctiles:

- **Resaltado de Sintaxis Dinámico**: Colorea automáticamente palabras clave, tipos, cadenas, números y comentarios para Kotlin, Dart, Java, Python, JavaScript, HTML, CSS, JSON, SQL y C++.
- **Numeración de Líneas**: Indicador lateral con soporte para documentos extensos.
- **Barra de Herramientas de Programación (Fila de Teclas Rápidas)**:
  - Justo encima del teclado virtual encontrarás símbolos de uso continuo que normalmente están escondidos en teclados móviles: `{`, `}`, `(`, `)`, `[`, `]`, `;`, `=`, `<`, `>`, `!`, `&`, `|`, `"`, `'`, `/`, `\`, `Tab` e `Indent`.
- **Botón Superior de Ejecución ▶**: Ejecuta el proyecto directamente abriendo la consola de salida.

---

## 4. Explorador de Archivos y Gestión de Proyectos

- **Crear Archivos**: Pulsa el botón flotante `+` o el icono de nuevo archivo en la esquina superior. Especifica el nombre (ej. `HomeScreen.kt` o `estilos.css`).
- **Crear Carpetas**: Pulsa el icono de carpeta `📁+` para crear directorios organizados.
- **Plantillas de Proyecto**: Pulsa **Plantillas** para generar estructuras preconfiguradas (Flutter, Android Kotlin, React Native, Web App).
- **Importar Espacio de Trabajo**: Puedes importar proyectos existentes desde el almacenamiento interno de tu dispositivo.

---

## 5. Terminal Integrada y Ejecución

La terminal permite ejecutar comandos en un entorno de sandbox protegido:
- **Comandos comunes**: `ls`, `cat <archivo>`, `run`, `test`, `clear`, `build`.
- **Salida del Asistente**: Cuando la IA genera comandos de terminal, puedes verlos y ejecutarlos aquí pulsando **"Ver en Terminal"**.
- **Registro de Consola**: Muestra el historial completo de salidas de compilación y logs de depuración.

---

## 6. Asistente de Inteligencia Artificial (Motor Antigravity)

El asistente no es un simple chatbot: es un **agente de desarrollo autónomo** que interactúa con tu proyecto.

### Modos: Planning Mode vs Fast Mode
En la parte inferior del chat encontrarás el chip de modo:
- **`🛠️ Planning Mode` (Modo Recomendado / Protocolo Antigravity)**:
  - El agente primero crea un **Plan de Arquitectura** y una **Lista de Tareas (Checklist)**.
  - No altera tus archivos hasta que apruebes el plan con el botón **"Proceder"**.
- **`⚡ Fast Mode`**:
  - Para preguntas directas, correcciones rápidas de una sola línea o explicaciones teóricas sin ceremonia de planificación.

### Comandos Rápidos Slash
En la barra de chips puedes tocar los comandos directos:
- **`📋 /plan <tarea>`**: Genera el plan estructurado `# Plan: <Título>` y la lista de tareas.
- **`🎙️ /grill-me <idea>`**: El agente te entrevista haciéndote preguntas técnicas y ofreciéndote alternativas para definir bien tu app antes de programar.
- **`🔍 /review`**: Realiza una auditoría exhaustiva del archivo abierto en el editor buscando vulnerabilidades o malas prácticas.
- **`🛠️ /fix`**: Detecta errores en el archivo actual y genera la versión reparada.
- **`📑 /diff`**: Muestra las diferencias entre la versión actual y la propuesta con colores verde/rojo.
- **`🎯 /goal <meta>`**: Establece una meta de alto nivel y la desglosa en hitos de desarrollo.
- **`🧠 /learn <regla>`**: Enseña una regla de estilo o patrón específico al agente para que la recuerde.
- **`🧪 /test`**: Dispara la ejecución de pruebas del proyecto.
- **`▶️ /run`**: Lanza la ejecución de la app en la terminal.
- **`🧹 /clear`**: Limpia el historial de la conversación actual.

### ¿Cómo la IA escribe código en tus archivos?
Hay tres maneras automáticas:

1. **Botón "✓ Aplicar a [archivo]"**:
   - Cuando la IA genera un bloque con `// File: ruta/al/archivo.kt`, verás un botón azul en el encabezado.
   - **Tócalo una sola vez**: el agente creará o sobreescribirá el archivo en el almacenamiento del proyecto, actualizará el editor y te llevará directamente a verlo.
2. **Botón "Proceder y Ejecutar Plan ▶"**:
   - En las tarjetas de plan `# Plan:`, al pulsar el botón violeta, el agente ejecuta todas las acciones propuestas en lote y marca las tareas como completadas.
3. **Botón "Aplicar"**:
   - En cualquier bloque de código estándar, toca **"Aplicar"** en la esquina derecha para pegar el código en la posición actual del cursor de tu editor.

### Adjuntar Imágenes y Capturas (Visión)
- Toca el icono de clip `📎` a la izquierda de la barra de texto.
- Puedes adjuntar **diseños UI, mockups, diagramas de arquitectura o fotos de errores** de pantalla.
- La IA analizará la imagen y te entregará el código exacto para implementarlo.

---

## 7. Mission Control (Control de Misión)

En esta pestaña verás el estado en vivo de los agentes colaborativos que trabajan en tu proyecto:

- **`Planning` (Planificando)**: El agente analiza dependencias y estructura las tareas.
- **`Executing` (Ejecutando)**: Generación activa de código o ejecución de scripts.
- **`WaitingForUser` (Esperando al Usuario)**: Pausa de seguridad para que apruebes el plan o resuelvas una tarea con la etiqueta `[HUMANO]`.
- **`Verifying` (Verificando)**: Comprobación de consistencia y sintaxis de los archivos modificados.
- **`Idle` (En Espera)**: Agente listo para recibir una nueva instrucción.

---

## 8. AI Hub y Gestión de Modelos

Puedes utilizar Black Cat IDE con el proveedor de IA que prefieras:

1. **Modelos en la Nube**:
   - **Google Gemini API** (Configura tu API Key gratuita o Pro).
   - **Anthropic Claude API** (Claude 3.5 Sonnet).
   - **OpenAI API** (GPT-4o / GPT-4 Turbo).
2. **Modelos Locales GGUF (100% Offline)**:
   - Descarga modelos compactos optimizados (como Qwen 2.5 Coder o Gemma 2) para programar en aviones, zonas sin cobertura o con privacidad total sin enviar código a servidores externos.

---

## 9. Control de Versiones con Git

- **Visualizar Cambios**: Revisa archivos modificados, nuevos o eliminados.
- **Hacer Commit**: Escribe un mensaje descriptivo y confirma tus cambios localmente.
- **Sincronización**: Conecta tu repositorio remoto de GitHub para hacer `git push` o `git pull` directamente desde el móvil.

---

## 10. Vista Previa en Vivo (Live Preview)

Si estás desarrollando interfaces Web o prototipos móviles:
- Activa el botón de **Live Preview** en la barra superior.
- Se abrirá una pantalla dividida con el renderizado en tiempo real de tu HTML/CSS/JS conforme vas editando.

---

## 11. Solución de Dudas Frecuentes (FAQ)

#### ¿Puedo usar Black Cat IDE sin conexión a internet?
**Sí.** El editor de código, el explorador de archivos, la base de datos Room y la ejecución de terminal funcionan 100% offline. Si además descargas un modelo local desde el AI Hub, el asistente de IA también funcionará sin conexión.

#### ¿Dónde se guardan mis archivos creados por la IA?
Se guardan en el árbol de directorios del proyecto activo y se reflejan inmediatamente en la pestaña **EXPLORADOR**.

#### ¿Cómo ejecuto mis proyectos?
Pulsando el botón ▶ en el editor o escribiendo `/run` en el chat del asistente de IA.

---

*Manual generado para Black Cat IDE Mobile - Diseñado para máxima productividad móvil con Inteligencia Artificial Agéntica.*
