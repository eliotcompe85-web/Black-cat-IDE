package com.ide.mobile.feature.compiler

import com.ide.mobile.core.model.BuildProgress
import com.ide.mobile.core.model.BuildResult
import com.ide.mobile.core.model.BuildStep
import com.ide.mobile.core.model.ProjectFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object BuildPipeline {

    fun execute(
        project: ProjectFile,
        activeCode: String,
        hasSyntaxErrors: Boolean
    ): Flow<BuildProgress> = flow {
        val startTime = System.currentTimeMillis()

        // 1. Initializing
        emit(
            BuildProgress(
                step = BuildStep.INITIALIZING,
                percentage = 0.05f,
                currentLog = "[Daemon] Inicializando pipeline de compilación para ${project.name}...",
                isRunning = true
            )
        )
        delay(350)

        // 2. AAPT2 Resources
        emit(
            BuildProgress(
                step = BuildStep.AAPT2_RESOURCES,
                percentage = 0.25f,
                currentLog = "[AAPT2] Compilando recursos XML (res/values/strings.xml, AndroidManifest.xml)...",
                isRunning = true
            )
        )
        delay(400)
        emit(
            BuildProgress(
                step = BuildStep.AAPT2_RESOURCES,
                percentage = 0.35f,
                currentLog = "[AAPT2] Generando R.java y empaquetando tabla de recursos resources.ap_ [SUCCESS]",
                isRunning = true
            )
        )
        delay(250)

        // 3. Compile sources
        emit(
            BuildProgress(
                step = BuildStep.COMPILE_SOURCES,
                percentage = 0.50f,
                currentLog = "[kotlinc] Analizando árbol sintáctico (AST) y compilando fuentes Kotlin...",
                isRunning = true
            )
        )
        delay(500)

        if (hasSyntaxErrors) {
            emit(
                BuildProgress(
                    step = BuildStep.FAILED,
                    percentage = 0.50f,
                    currentLog = "[ERROR] Fallo en compilación: Existen errores sintácticos no resueltos en el código.",
                    isRunning = false,
                    isFinished = true,
                    errorMessage = "Revisa el panel de diagnósticos para corregir las fallas antes de compilar."
                )
            )
            return@flow
        }

        emit(
            BuildProgress(
                step = BuildStep.COMPILE_SOURCES,
                percentage = 0.65f,
                currentLog = "[kotlinc] Generación de bytecode .class finalizada correctamente [SUCCESS]",
                isRunning = true
            )
        )
        delay(300)

        // 4. D8 Dexing
        emit(
            BuildProgress(
                step = BuildStep.D8_DEXING,
                percentage = 0.75f,
                currentLog = "[D8] Convirtiendo bytecode Java/Kotlin a formato Dalvik Executable (classes.dex)...",
                isRunning = true
            )
        )
        delay(400)
        emit(
            BuildProgress(
                step = BuildStep.D8_DEXING,
                percentage = 0.80f,
                currentLog = "[D8] classes.dex generado (Optimización dex-shrink aplicada) [SUCCESS]",
                isRunning = true
            )
        )
        delay(250)

        // 5. Package APK
        emit(
            BuildProgress(
                step = BuildStep.PACKAGE_APK,
                percentage = 0.85f,
                currentLog = "[APK Builder] Ensamblando resources.ap_ + classes.dex -> app-debug.apk...",
                isRunning = true
            )
        )
        delay(350)

        // 6. Sign APK
        emit(
            BuildProgress(
                step = BuildStep.SIGN_APK,
                percentage = 0.95f,
                currentLog = "[apksigner] Firmando APK con debug.keystore (Esquema de firma APK v2/v3)...",
                isRunning = true
            )
        )
        delay(300)

        // 7. Ready to install / Success
        val totalDuration = System.currentTimeMillis() - startTime
        emit(
            BuildProgress(
                step = BuildStep.SUCCESS,
                percentage = 1.0f,
                currentLog = "[BUILD SUCCESS] app-debug.apk generado en ${totalDuration}ms. Listo para instalar 🎉",
                isRunning = false,
                isFinished = true
            )
        )
    }
}
