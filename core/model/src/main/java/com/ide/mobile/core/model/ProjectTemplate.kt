package com.ide.mobile.core.model

/**
 * Plantillas preconfiguradas para creación instantánea de proyectos listos para producción.
 */
enum class ProjectTemplateType {
    FLUTTER_MOBILE,
    EXPO_REACT_NATIVE,
    RAILWAY_NODE_API,
    PYTHON_AI_SERVICE
}

data class ProjectTemplate(
    val type: ProjectTemplateType,
    val title: String,
    val description: String,
    val iconName: String,
    val tags: List<String>
) {
    companion object {
        val ALL_TEMPLATES = listOf(
            ProjectTemplate(
                type = ProjectTemplateType.FLUTTER_MOBILE,
                title = "Flutter Mobile App",
                description = "Aplicación móvil multiplataforma completa con Provider y Material 3.",
                iconName = "phone_android",
                tags = listOf("Dart", "Mobile", "Material 3")
            ),
            ProjectTemplate(
                type = ProjectTemplateType.EXPO_REACT_NATIVE,
                title = "Expo Dev React Native",
                description = "Proyecto móvil universal listo para compilar con EAS Build a Android.",
                iconName = "stay_current_portrait",
                tags = listOf("React Native", "Expo", "JavaScript")
            ),
            ProjectTemplate(
                type = ProjectTemplateType.RAILWAY_NODE_API,
                title = "Railway Express Backend",
                description = "Microservicio REST con Express.js y Dockerfile optimizado para Railway Cloud.",
                iconName = "cloud_upload",
                tags = listOf("Node.js", "Express", "Railway")
            ),
            ProjectTemplate(
                type = ProjectTemplateType.PYTHON_AI_SERVICE,
                title = "Python AI & LLM Service",
                description = "Servicio con FastAPI y scripts de orquestación de agentes locales y Gemini.",
                iconName = "psychology",
                tags = listOf("Python", "FastAPI", "AI Agents")
            )
        )

        fun createProjectFromTemplate(templateType: ProjectTemplateType): ProjectFile {
            return when (templateType) {
                ProjectTemplateType.FLUTTER_MOBILE -> createFlutterProject()
                ProjectTemplateType.EXPO_REACT_NATIVE -> createExpoProject()
                ProjectTemplateType.RAILWAY_NODE_API -> createRailwayProject()
                ProjectTemplateType.PYTHON_AI_SERVICE -> createPythonAiProject()
            }
        }

        fun createNewCustomProject(
            projectName: String,
            projectGoal: String = "",
            templateType: ProjectTemplateType = ProjectTemplateType.FLUTTER_MOBILE
        ): ProjectFile {
            val safeName = projectName.trim().replace(" ", "_").ifBlank { "mi_proyecto" }
            val cleanTitle = projectName.trim().ifBlank { "Mi Proyecto" }
            val goalText = projectGoal.ifBlank { "Construir una gran aplicación con Black Cat IDE" }

            val root = ProjectFile(
                id = "proj_${'$'}safeName",
                name = safeName,
                path = "/$safeName",
                isDirectory = true
            )

            val readme = ProjectFile(
                id = "file_readme_${'$'}safeName",
                name = "README.md",
                path = "/$safeName/README.md",
                isDirectory = false,
                content = """
                # 🚀 $cleanTitle
                
                > **Objetivo del proyecto:** $goalText
                
                ---
                
                ### ✨ Espacio de Trabajo Creado con Éxito
                Este proyecto ha sido configurado especialmente para ti con una estructura limpia y moderna.
                
                ### 🧙‍♂️ Tu Asistente de Desarrollo Senior
                Tu asistente IA está activo en la pestaña **IA Copilot**. Puedes preguntarle:
                - *"¿Cómo agregamos la primera pantalla?"*
                - *"Diseñemos los botones e interactividad"*
                - *"Explícame cómo funciona este código"*
                
                ¡Mucho éxito con tu nueva app!
                """.trimIndent()
            )

            val lib = ProjectFile(
                id = "dir_lib_${'$'}safeName",
                name = "lib",
                path = "/$safeName/lib",
                isDirectory = true
            )

            val mainDart = ProjectFile(
                id = "file_main_${'$'}safeName",
                name = "main.dart",
                path = "/$safeName/lib/main.dart",
                isDirectory = false,
                content = """
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '$cleanTitle',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF7B61FF),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      home: const WelcomeScreen(),
    );
  }
}

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0C0D15),
      appBar: AppBar(
        title: const Text('$cleanTitle', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF141522),
        elevation: 0,
        centerTitle: true,
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: const BoxDecoration(
                  color: Color(0x267B61FF),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.rocket_launch, size: 64, color: Color(0xFFC084FC)),
              ),
              const SizedBox(height: 24),
              Text(
                '¡Bienvenido a $cleanTitle!',
                style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Text(
                '$goalText',
                style: const TextStyle(fontSize: 14, color: Color(0xFF94A3B8)),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 32),
              ElevatedButton.icon(
                onPressed: () {},
                icon: const Icon(Icons.star),
                label: const Text('¡Comenzar!'),
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
""".trimIndent()
            )

            lib.children.add(mainDart)
            root.children.add(readme)
            root.children.add(lib)
            return root
        }

        private fun createFlutterProject(): ProjectFile {
            val root = ProjectFile(
                id = "proj_flutter",
                name = "flutter_blackcat_app",
                path = "/flutter_blackcat_app",
                isDirectory = true
            )
            val lib = ProjectFile(
                id = "dir_lib",
                name = "lib",
                path = "/flutter_blackcat_app/lib",
                isDirectory = true
            )
            val mainDart = ProjectFile(
                id = "file_main_dart",
                name = "main.dart",
                path = "/flutter_blackcat_app/lib/main.dart",
                isDirectory = false,
                content = """
import 'package:flutter/material.dart';

void main() {
  runApp(const BlackCatApp());
}

class BlackCatApp extends StatelessWidget {
  const BlackCatApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Black Cat App',
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
      appBar: AppBar(
        title: const Text('🐾 Black Cat Mobile'),
        centerTitle: true,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.code, size: 64, color: Color(0xFF7B61FF)),
            const SizedBox(height: 16),
            const Text(
              '¡Proyecto Flutter Creado!',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            const Text('Listo para compilar y ejecutar en Black Cat IDE.'),
          ],
        ),
      ),
    );
  }
}
                """.trimIndent()
            )
            val pubspec = ProjectFile(
                id = "file_pubspec",
                name = "pubspec.yaml",
                path = "/flutter_blackcat_app/pubspec.yaml",
                isDirectory = false,
                content = """
name: flutter_blackcat_app
description: "Aplicación móvil creada en Black Cat IDE"
version: 1.0.0+1
environment:
  sdk: '>=3.0.0 <4.0.0'
dependencies:
  flutter:
    sdk: flutter
  cupertino_icons: ^1.0.6
dev_dependencies:
  flutter_test:
    sdk: flutter
flutter:
  uses-material-design: true
                """.trimIndent()
            )
            val readme = ProjectFile(
                id = "file_readme",
                name = "README.md",
                path = "/flutter_blackcat_app/README.md",
                isDirectory = false,
                content = """
# 🐾 Flutter Black Cat App

Proyecto móvil inicializado directamente desde **Black Cat IDE**.

## 🚀 Pasos Rápidos
1. Edita `lib/main.dart`.
2. Presiona **▶ Run** para ver la ejecución.
3. Dirígete a la pestaña **Git** para subir a GitHub.
                """.trimIndent()
            )
            lib.children.add(mainDart)
            root.children.addAll(listOf(lib, pubspec, readme))
            return root
        }

        private fun createExpoProject(): ProjectFile {
            val root = ProjectFile(
                id = "proj_expo",
                name = "expo_blackcat_app",
                path = "/expo_blackcat_app",
                isDirectory = true
            )
            val appJs = ProjectFile(
                id = "file_app_js",
                name = "App.js",
                path = "/expo_blackcat_app/App.js",
                isDirectory = false,
                content = """
import React from 'react';
import { StyleSheet, Text, View, StatusBar } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>🐾 Black Cat Mobile App</Text>
      <Text style={styles.subtitle}>Desarrollado en Black Cat IDE</Text>
      <Text style={styles.badge}>Listo para EAS Build en Expo Dev</Text>
      <StatusBar barStyle="light-content" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0C0D15',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#94A3B8',
    marginBottom: 20,
  },
  badge: {
    fontSize: 12,
    color: '#7B61FF',
    backgroundColor: '#1E1B4B',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
  },
});
                """.trimIndent()
            )
            val pkgJson = ProjectFile(
                id = "file_pkg_json",
                name = "package.json",
                path = "/expo_blackcat_app/package.json",
                isDirectory = false,
                content = """
{
  "name": "expo-blackcat-app",
  "version": "1.0.0",
  "scripts": {
    "start": "expo start",
    "android": "expo start --android",
    "build:android": "eas build -p android --profile preview"
  },
  "dependencies": {
    "expo": "~51.0.0",
    "react": "18.2.0",
    "react-native": "0.74.1"
  }
}
                """.trimIndent()
            )
            val appJson = ProjectFile(
                id = "file_app_json",
                name = "app.json",
                path = "/expo_blackcat_app/app.json",
                isDirectory = false,
                content = """
{
  "expo": {
    "name": "BlackCatApp",
    "slug": "blackcat-app",
    "version": "1.0.0",
    "orientation": "portrait",
    "userInterfaceStyle": "dark",
    "android": {
      "package": "com.blackcat.app"
    }
  }
}
                """.trimIndent()
            )
            root.children.addAll(listOf(appJs, pkgJson, appJson))
            return root
        }

        private fun createRailwayProject(): ProjectFile {
            val root = ProjectFile(
                id = "proj_railway",
                name = "railway_backend_service",
                path = "/railway_backend_service",
                isDirectory = true
            )
            val serverJs = ProjectFile(
                id = "file_server_js",
                name = "server.js",
                path = "/railway_backend_service/server.js",
                isDirectory = false,
                content = """
const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

app.get('/health', (req, res) => {
  res.json({
    status: 'online',
    service: 'Black Cat Backend Service',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
});

app.get('/api/info', (req, res) => {
  res.json({
    name: 'Black Cat API',
    deployTarget: 'Railway Cloud',
    author: '⭐ CREATED BY J.COMPE'
  });
});

app.listen(PORT, () => {
  console.log(`✓ Servidor corriendo en puerto ${'$'}{PORT} en Railway`);
});
                """.trimIndent()
            )
            val pkgJson = ProjectFile(
                id = "file_railway_pkg",
                name = "package.json",
                path = "/railway_backend_service/package.json",
                isDirectory = false,
                content = """
{
  "name": "railway-backend-service",
  "version": "1.0.0",
  "main": "server.js",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.19.2"
  }
}
                """.trimIndent()
            )
            val railwayJson = ProjectFile(
                id = "file_railway_json",
                name = "railway.json",
                path = "/railway_backend_service/railway.json",
                isDirectory = false,
                content = """
{
  "${'$'}schema": "https://railway.app/railway.schema.json",
  "build": {
    "builder": "NIXPACKS"
  },
  "deploy": {
    "startCommand": "npm start",
    "restartPolicyType": "ON_FAILURE"
  }
}
                """.trimIndent()
            )
            root.children.addAll(listOf(serverJs, pkgJson, railwayJson))
            return root
        }

        private fun createPythonAiProject(): ProjectFile {
            val root = ProjectFile(
                id = "proj_python_ai",
                name = "python_ai_agent_service",
                path = "/python_ai_agent_service",
                isDirectory = true
            )
            val mainPy = ProjectFile(
                id = "file_main_py",
                name = "main.py",
                path = "/python_ai_agent_service/main.py",
                isDirectory = false,
                content = """
# 🐾 Black Cat Python AI Service
import sys
import json
from datetime import datetime

def analyze_code_structure():
    report = {
        "status": "ready",
        "engine": "Black Cat Local AI Assistant",
        "checked_at": datetime.now().isoformat(),
        "modules_detected": ["flutter", "compose", "express", "fastapi"]
    }
    print("--- 🐾 INFORME DE ANÁLISIS DE CÓDIGO ---")
    print(json.dumps(report, indent=2))
    return report

if __name__ == "__main__":
    print("Iniciando servicio de análisis inteligente...")
    analyze_code_structure()
    print("✓ Ejecución de script Python completada exitosamente.")
                """.trimIndent()
            )
            val reqs = ProjectFile(
                id = "file_reqs",
                name = "requirements.txt",
                path = "/python_ai_agent_service/requirements.txt",
                isDirectory = false,
                content = """
fastapi>=0.111.0
uvicorn>=0.30.0
pydantic>=2.7.0
requests>=2.32.0
                """.trimIndent()
            )
            root.children.addAll(listOf(mainPy, reqs))
            return root
        }
    }
}
