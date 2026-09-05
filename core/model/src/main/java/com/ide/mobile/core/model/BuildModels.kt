package com.ide.mobile.core.model

enum class BuildStep(val displayName: String, val basePercentage: Float) {
    INITIALIZING("Inicializando entorno y toolchain", 0.05f),
    AAPT2_RESOURCES("Procesando recursos XML con AAPT2", 0.25f),
    COMPILE_SOURCES("Compilando código Kotlin / Java", 0.50f),
    D8_DEXING("Convirtiendo bytecode a Dalvik DEX (D8)", 0.75f),
    PACKAGE_APK("Empaquetando archivo APK final", 0.85f),
    SIGN_APK("Firmando APK con debug.keystore (v2/v3)", 0.95f),
    READY_TO_INSTALL("APK generado listo para instalar", 1.0f),
    SUCCESS("Compilación finalizada exitosamente", 1.0f),
    FAILED("Error durante la compilación", 0.0f)
}

data class BuildProgress(
    val step: BuildStep = BuildStep.INITIALIZING,
    val percentage: Float = 0.0f,
    val currentLog: String = "",
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)

data class BuildResult(
    val success: Boolean,
    val apkName: String = "",
    val apkPath: String = "",
    val apkSizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val logs: List<String> = emptyList(),
    val error: String? = null
)

data class DeviceToolchainStatus(
    val cpuAbi: String,
    val totalMemoryMb: Long,
    val availableMemoryMb: Long,
    val isArm64: Boolean,
    val hasStoragePermission: Boolean,
    val hasInstallPermission: Boolean,
    val isLowRamDevice: Boolean,
    val recommendation: String
)
