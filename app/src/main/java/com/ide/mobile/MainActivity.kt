package com.ide.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.ide.mobile.ui.IdeMainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0C0D15),
                    surface = Color(0xFF11121C),
                    primary = Color(0xFF7B61FF),
                    onSurface = Color(0xFFE2E8F0)
                )
            ) {
                IdeMainScreen()
            }
        }
    }
}
