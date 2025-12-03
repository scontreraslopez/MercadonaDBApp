package net.iessochoa.sergiocontreras.mercadonadb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores aproximados de la marca
val MercadonaGreen = Color(0xFF007A5E)
val MercadonaLightGreen = Color(0xFF009D7A)
val MercadonaYellow = Color(0xFFF39200)

@Composable
fun SupermarketBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp) // Altura del banner
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MercadonaGreen, MercadonaLightGreen)
                )
            )
    ) {
        // 1. Elemento Decorativo de Fondo (Cesta Gigante y transparente)
        Icon(
            imageVector = Icons.Rounded.ShoppingCart,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 20.dp) // Lo movemos un poco fuera
                .graphicsLayer { rotationZ = -20f } // Lo rotamos
        )

        // 2. Elemento Decorativo (Círculo amarillo tipo "Oferta")
        Box(
            modifier = Modifier
                .offset(x = (-20).dp, y = (-20).dp)
                .size(100.dp)
                .clip(RoundedCornerShape(50))
                .background(MercadonaYellow.copy(alpha = 0.2f))
                .align(Alignment.TopStart)
        )

        // 3. Contenido de Texto
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp, end = 16.dp)
        ) {
            Text(
                text = "MERCADONA",
                color = MercadonaYellow,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Analytics",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium, // Texto muy grande
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu oráculo del ahorro",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview
@Composable
fun PreviewBanner() {
    SupermarketBanner()
}