package net.iessochoa.sergiocontreras.mercadonadb.ui

import androidx.compose.ui.graphics.Color
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy

fun ShoppingStrategy.getColor(): Color {
    return when (this) {
        ShoppingStrategy.CHEAPEST -> Color(0xFF4CAF50) // Verde
        ShoppingStrategy.MOST_EXPENSIVE -> Color(0xFFE91E63) // Rojo
        ShoppingStrategy.BEST_VALUE -> Color(0xFF2196F3) // Azul
        ShoppingStrategy.RANDOM -> Color(0xFF9C27B0) // Morado
    }
}

fun ShoppingStrategy.getLabel(): String {
    return when (this) {
        ShoppingStrategy.CHEAPEST -> "¡Ahorro Total! 🤑"
        ShoppingStrategy.MOST_EXPENSIVE -> "Capricho de Lujo 💎"
        ShoppingStrategy.BEST_VALUE -> "Compra Inteligente ⚖️"
        ShoppingStrategy.RANDOM -> "La Suerte decide 🎲"
    }
}