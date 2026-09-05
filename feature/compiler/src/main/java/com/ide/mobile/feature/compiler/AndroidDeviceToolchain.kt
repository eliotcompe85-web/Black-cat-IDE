package com.ide.mobile.feature.compiler

import android.os.Build
import com.ide.mobile.core.model.DeviceToolchainStatus

object AndroidDeviceToolchain {

    fun inspectDevice(): DeviceToolchainStatus {
        val runtime = Runtime.getRuntime()
        val totalMemoryMb = runtime.maxMemory() / (1024 * 1024)
        val freeMemoryMb = runtime.freeMemory() / (1024 * 1024)
        val allocatedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val availableMemoryMb = totalMemoryMb - allocatedMb

        val abis = Build.SUPPORTED_ABIS
        val primaryAbi = if (abis.isNotEmpty()) abis[0] else "unknown"
        val isArm64 = primaryAbi.contains("arm64") || primaryAbi.contains("aarch64")
        val isLowRam = totalMemoryMb < 256

        val recommendation = when {
            isLowRam -> "Memoria RAM restringida. Se recomienda usar Live Preview y compilar APKs en segundo plano."
            !isArm64 -> "Arquitectura $primaryAbi detectada. Se recomienda verificar compatibilidad de binarios AAPT2."
            else -> "Dispositivo ARM64 óptimo para compilación on-device y previsualización en tiempo real."
        }

        return DeviceToolchainStatus(
            cpuAbi = primaryAbi,
            totalMemoryMb = totalMemoryMb,
            availableMemoryMb = availableMemoryMb,
            isArm64 = isArm64,
            hasStoragePermission = true,
            hasInstallPermission = true,
            isLowRamDevice = isLowRam,
            recommendation = recommendation
        )
    }
}
