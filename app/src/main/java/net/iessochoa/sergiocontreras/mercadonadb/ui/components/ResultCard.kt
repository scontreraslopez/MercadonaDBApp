package net.iessochoa.sergiocontreras.mercadonadb.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy

// TODO hay que desacoplar de ShoppingStrategy y de Product haciendo strings

// TODO quitar esto Modelo de datos simple para la UI (o usa tu Entity directamente)
data class Product(
    val category: String,
    val name: String,
    val price: Double,
    val referencePrice: Double,
    val referenceUnit: String
)

@Composable
fun ResultCard(
    product: Product?,
    strategy: ShoppingStrategy,
    modifier: Modifier = Modifier
) {
    // Si no hay producto, mostramos un estado vacío o nada
    if (product == null) return

    // 1. Lógica de Colores según la estrategia
    val (primaryColor, labelText) = when (strategy) {
        ShoppingStrategy.CHEAPEST -> Pair(Color(0xFF4CAF50), "¡Ahorro Total!") // Verde
        ShoppingStrategy.MOST_EXPENSIVE -> Pair(Color(0xFFE91E63), "Capricho de Lujo") // Rosa/Rojo
        ShoppingStrategy.BEST_VALUE -> Pair(Color(0xFF2196F3), "Compra Inteligente") // Azul
        ShoppingStrategy.RANDOM -> Pair(Color(0xFF9C27B0), "La Suerte decide") // Morado
    }

    // 2. La Tarjeta
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            // Un borde sutil del color de la estrategia
            .border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Etiqueta de Estrategia (Chip superior)
            SuggestionChip(
                onClick = { },
                label = { Text(labelText) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = primaryColor.copy(alpha = 0.1f),
                    labelColor = primaryColor
                ),
                border = null
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Categoría (Pequeño y gris)
            Text(
                text = product.category.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                letterSpacing = 1.5.sp
            )

            // Nombre del Producto (Grande y Negrita)
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Divider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(60.dp),
                color = Color.LightGray
            )

            // EL PRECIO (Gigante)
            Text(
                text = "${product.price} €",
                style = MaterialTheme.typography.displayMedium, // Tipografía muy grande
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor // El precio toma el color de la estrategia
            )

            // Precio de referencia (El dato educativo)
            Text(
                text = "Sale a ${product.referencePrice} € / ${product.referenceUnit}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}

// --- PREVIEW PARA VER CÓMO QUEDA SIN EJECUTAR LA APP ---

@Preview(showBackground = true)
@Composable
fun PreviewResultCardCheapest() {
    val sampleProduct = Product(
        category = "Fruta",
        name = "Sandía",
        price = 3.75,
        referencePrice = 0.55,
        referenceUnit = "kg"
    )

    Column {
        ResultCard(product = sampleProduct, strategy = ShoppingStrategy.CHEAPEST)
        ResultCard(product = sampleProduct.copy(name="Jamón Ibérico", category="Carnicería", price = 55.0, referencePrice = 115.0), strategy = ShoppingStrategy.MOST_EXPENSIVE)
    }
}