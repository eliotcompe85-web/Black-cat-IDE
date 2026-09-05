package com.ide.mobile.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders the official Black Cat IDE Logo:
 * - Loads from assets/logo_black_cat.jpg
 * - Graceful fallback to stylish neon vector if asset stream isn't available
 */
@Composable
fun BlackCatLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    val context = LocalContext.current
    val bitmap = remember(context) {
        try {
            context.assets.open("logo_black_cat.jpg").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Black Cat IDE Logo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
                .border(1.5.dp, Color(0xFF7B61FF), shape)
        )
    } else {
        // High-fidelity fallback styling matching the logo
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF26193E), Color(0xFF0C0D15))
                    )
                )
                .border(1.5.dp, Color(0xFF7B61FF), shape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🐱", fontSize = (size.value * 0.4).sp)
                Text(
                    text = "</>",
                    color = Color(0xFFC084FC),
                    fontSize = (size.value * 0.18).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Signature watermark pill: "⭐ CREATED BY J.COMPE"
 */
@Composable
fun BlackCatWatermarkBadge(
    modifier: Modifier = Modifier,
    text: String = "CREATED BY J.COMPE"
) {
    Surface(
        color = Color(0xFF141224),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E3266)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFC084FC),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = Color(0xFFE2D9F3),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Hero card displaying the Black Cat IDE branding and watermark
 * Used in empty states (SearchScreen, Drawer header, Settings About card)
 */
@Composable
fun BlackCatHeroCard(
    modifier: Modifier = Modifier,
    logoSize: Dp = 100.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF181528), Color(0xFF0F0E1A))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.5.dp, Color(0xFF332958), RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlackCatLogo(
            size = logoSize,
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "BLACK CAT IDE",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "CÓDIGO • IA • PRODUCTIVIDAD",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8B5CF6),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        BlackCatWatermarkBadge()
    }
}

/**
 * Background watermark for the code editor:
 * Very subtle (low opacity) glowing watermark placed in background corners
 */
@Composable
fun BlackCatEditorWatermark(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .alpha(0.35f)
            .background(Color(0xFF131122), RoundedCornerShape(8.dp))
            .border(0.8.dp, Color(0xFF2F2752), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BlackCatLogo(size = 18.dp, shape = RoundedCornerShape(4.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "BLACK CAT IDE • J.COMPE",
            color = Color(0xFF8B5CF6),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}
