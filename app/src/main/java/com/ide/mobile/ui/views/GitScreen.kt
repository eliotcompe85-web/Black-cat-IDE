package com.ide.mobile.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ide.mobile.core.model.*

enum class GitTabSection {
    LOCAL_CHANGES,
    GITHUB_SYNC,
    CLOUD_DEPLOYMENTS
}

@Composable
fun GitScreen(
    isFileModified: Boolean = false,
    activeFileName: String = "lib/main.dart",
    gitHubConfig: GitHubConfig = GitHubConfig(),
    expoDevConfig: ExpoDevConfig = ExpoDevConfig(),
    railwayConfig: RailwayConfig = RailwayConfig(),
    deploymentStatus: DeploymentStatus = DeploymentStatus.IDLE,
    deploymentMessage: String? = null,
    onCommit: (String) -> Unit = {},
    onPushToGitHub: (String) -> Unit = {},
    onDeployExpoDev: () -> Unit = {},
    onDeployRailway: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var commitMessage by remember { mutableStateOf("Actualización desde Black Cat IDE") }
    var currentSection by remember { mutableStateOf(GitTabSection.LOCAL_CHANGES) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D15))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera Principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Git & Despliegues Cloud",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "GitHub • Expo Dev • Railway",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selector de Pestañas de Git y Despliegues
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                GitTabSection.LOCAL_CHANGES to "📁 Cambios Locales",
                GitTabSection.GITHUB_SYNC to "⬆️ GitHub Push",
                GitTabSection.CLOUD_DEPLOYMENTS to "🚀 Expo & Railway"
            )

            tabs.forEach { (section, title) ->
                val isSelected = currentSection == section
                Surface(
                    color = if (isSelected) Color(0xFF7B61FF).copy(alpha = 0.25f) else Color(0xFF141522),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF7B61FF) else Color(0xFF24263A)),
                    modifier = Modifier.clickable { currentSection = section }
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Banner de Estado de Despliegue en Vivo
        if (deploymentStatus == DeploymentStatus.DEPLOYING || !deploymentMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = when (deploymentStatus) {
                    DeploymentStatus.DEPLOYING -> Color(0xFF261E47)
                    DeploymentStatus.SUCCESS -> Color(0xFF143026)
                    DeploymentStatus.FAILED -> Color(0xFF381519)
                    else -> Color(0xFF141522)
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    when (deploymentStatus) {
                        DeploymentStatus.DEPLOYING -> Color(0xFF7B61FF)
                        DeploymentStatus.SUCCESS -> Color(0xFF34D399)
                        DeploymentStatus.FAILED -> Color(0xFFEF4444)
                        else -> Color(0xFF2E324E)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (deploymentStatus == DeploymentStatus.DEPLOYING) {
                        CircularProgressIndicator(
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = deploymentMessage ?: "Operación completada.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido según la Pestaña Seleccionada
        when (currentSection) {
            GitTabSection.LOCAL_CHANGES -> {
                // Estado del Árbol de Trabajo
                Row(
                    modifier = Modifier
                        .background(Color(0xFF181926), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF26283C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = Color(0xFF7B61FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rama: ${gitHubConfig.defaultBranch}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Archivos Modificados en el Proyecto",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isFileModified) {
                    Surface(
                        color = Color(0xFF141522),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF362B15)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = activeFileName, color = Color.White, fontSize = 12.sp)
                            }
                            Surface(color = Color(0xFF362B15), shape = RoundedCornerShape(4.dp)) {
                                Text("Modificado", color = Color(0xFFFBBF24), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFF141522),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF24263A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Árbol de trabajo limpio. Sin cambios pendientes.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Mensaje de Commit", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(Color(0xFF141522), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF26283C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = commitMessage,
                        onValueChange = { commitMessage = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        cursorBrush = SolidColor(Color(0xFF7B61FF)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onCommit(commitMessage) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirmar Cambios (Git Commit)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            GitTabSection.GITHUB_SYNC -> {
                Surface(
                    color = Color(0xFF141522),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF24263A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐱", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Sincronización con GitHub", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Subir commits a repositorio remoto", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }

                        HorizontalDivider(color = Color(0xFF222436), thickness = 0.5.dp)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Repositorio Remoto:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(gitHubConfig.remoteUrl, color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Rama Destino:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("origin/${gitHubConfig.defaultBranch}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Autor:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("${gitHubConfig.authorName} (${gitHubConfig.authorEmail})", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { onPushToGitHub(commitMessage) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subir Cambios a GitHub (git push)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            GitTabSection.CLOUD_DEPLOYMENTS -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Tarjeta de Despliegue Expo Dev
                    Surface(
                        color = Color(0xFF141522),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🚀", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Expo Dev (Mobile Deployment)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Generar APK / Exportar bundle React Native", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Slug del Proyecto:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(expoDevConfig.projectSlug, color = Color.White, fontSize = 11.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Perfil de Build:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(expoDevConfig.buildProfile, color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDeployExpoDev,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Desplegar en Expo Dev (eas build)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    // Tarjeta de Despliegue Railway
                    Surface(
                        color = Color(0xFF141522),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFC084FC).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🚂", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Railway Cloud (Backend & DB)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Contenedores backend en producción", color = Color(0xFFC084FC), fontSize = 10.sp)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Servicio:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(railwayConfig.serviceName, color = Color.White, fontSize = 11.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Entorno:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text(railwayConfig.environment, color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onDeployRailway,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Desplegar en Railway (railway up)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
