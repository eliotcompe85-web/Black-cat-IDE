package com.ide.mobile.core.model

data class ProjectFile(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val language: LanguageType = when {
        name.endsWith(".dart") -> LanguageType.DART
        name.endsWith(".kt") || name.endsWith(".kts") -> LanguageType.KOTLIN
        name.endsWith(".xml") -> LanguageType.XML
        name.endsWith(".py") -> LanguageType.PYTHON
        else -> LanguageType.OTHER
    },
    var content: String = "",
    val children: MutableList<ProjectFile> = mutableListOf(),
    var isExpanded: Boolean = true
) {
    companion object {
        fun createSampleAndroidProject(): ProjectFile {
            val root = ProjectFile(
                id = "root",
                name = "mi_nuevo_proyecto",
                path = "/mi_nuevo_proyecto",
                isDirectory = true
            )

            val lib = ProjectFile(
                id = "lib",
                name = "lib",
                path = "/mi_nuevo_proyecto/lib",
                isDirectory = true
            )

            // Exact content from Image 1 reference screenshot
            val mainDart = ProjectFile(
                id = "main_dart",
                name = "main.dart",
                path = "/mi_nuevo_proyecto/lib/main.dart",
                isDirectory = false,
                content = """
                    import 'package:flutter/material.dart';
                    import 'package:provider/provider.dart';

                    void main() {
                      runApp(const MyApp());
                    }

                    class MyApp extends StatelessWidget {
                      const MyApp({super.key});

                      @override
                      Widget build(BuildContext context) {
                        return MaterialApp(
                          title: 'AI Code Editor',
                          theme: ThemeData(
                            colorScheme: ColorScheme.fromSeed(
                              seedColor: Colors.indigo,
                            ),
                            useMaterial3: true,
                          ),
                          home: const HomeScreen(),
                        );
                      }
                    }
                """.trimIndent()
            )

            val themeDart = ProjectFile(
                id = "theme_dart",
                name = "theme.dart",
                path = "/mi_nuevo_proyecto/lib/theme.dart",
                isDirectory = false,
                content = """
                    import 'package:flutter/material.dart';

                    final appTheme = ThemeData.dark().copyWith(
                      primaryColor: const Color(0xFF7B61FF),
                      scaffoldBackgroundColor: const Color(0xFF0C0D15),
                    );
                """.trimIndent()
            )

            val routerDart = ProjectFile(
                id = "router_dart",
                name = "router.dart",
                path = "/mi_nuevo_proyecto/lib/router.dart",
                isDirectory = false,
                content = """
                    import 'package:flutter/material.dart';

                    class AppRouter {
                      static Route<dynamic> generateRoute(RouteSettings settings) {
                        return MaterialPageRoute(builder: (_) => const Scaffold());
                      }
                    }
                """.trimIndent()
            )

            val pubspec = ProjectFile(
                id = "pubspec",
                name = "pubspec.yaml",
                path = "/mi_nuevo_proyecto/pubspec.yaml",
                isDirectory = false,
                content = """
                    name: mi_nuevo_proyecto
                    description: AI Code Editor App
                    version: 1.0.0+1

                    environment:
                      sdk: '>=3.0.0 <4.0.0'

                    dependencies:
                      flutter:
                        sdk: flutter
                      provider: ^6.1.1
                """.trimIndent()
            )

            val androidDir = ProjectFile(
                id = "android_dir",
                name = "android",
                path = "/mi_nuevo_proyecto/android",
                isDirectory = true
            )

            val mainActivity = ProjectFile(
                id = "main_activity",
                name = "MainActivity.kt",
                path = "/mi_nuevo_proyecto/android/MainActivity.kt",
                isDirectory = false,
                content = """
                    package com.example.app

                    import io.flutter.embedding.android.FlutterActivity

                    class MainActivity: FlutterActivity() {
                    }
                """.trimIndent()
            )

            val dartTool = ProjectFile(id = "dart_tool", name = ".dart_tool", path = "/mi_nuevo_proyecto/.dart_tool", isDirectory = true)
            val ideaDir = ProjectFile(id = "idea", name = ".idea", path = "/mi_nuevo_proyecto/.idea", isDirectory = true)
            val assetsDir = ProjectFile(id = "assets", name = "assets", path = "/mi_nuevo_proyecto/assets", isDirectory = true)
            val iosDir = ProjectFile(id = "ios", name = "ios", path = "/mi_nuevo_proyecto/ios", isDirectory = true)

            val widgetsDir = ProjectFile(id = "widgets", name = "widgets", path = "/mi_nuevo_proyecto/lib/widgets", isDirectory = true)
            val homeScreenDart = ProjectFile(
                id = "home_screen_dart",
                name = "home_screen.dart",
                path = "/mi_nuevo_proyecto/lib/home_screen.dart",
                isDirectory = false,
                content = """
                    import 'package:flutter/material.dart';

                    class HomeScreen extends StatelessWidget {
                      const HomeScreen({super.key});

                      @override
                      Widget build(BuildContext context) {
                        return Scaffold(
                          appBar: AppBar(title: const Text('Inicio')),
                          body: const Center(
                            child: Text('¡Bienvenido al editor móvil!'),
                          ),
                        );
                      }
                    }
                """.trimIndent()
            )

            val gitignore = ProjectFile(
                id = "gitignore",
                name = ".gitignore",
                path = "/mi_nuevo_proyecto/.gitignore",
                isDirectory = false,
                content = ".dart_tool/\nbuild/\n.idea/\n"
            )

            val readme = ProjectFile(
                id = "readme",
                name = "README.md",
                path = "/mi_nuevo_proyecto/README.md",
                isDirectory = false,
                content = "# mi_nuevo_proyecto\n\nAI Powered Flutter & Android App.\n"
            )

            androidDir.children.add(mainActivity)
            lib.children.addAll(listOf(mainDart, homeScreenDart, themeDart, routerDart, widgetsDir))
            root.children.addAll(listOf(dartTool, ideaDir, androidDir, assetsDir, iosDir, lib, gitignore, pubspec, readme))

            return root
        }
    }
}
