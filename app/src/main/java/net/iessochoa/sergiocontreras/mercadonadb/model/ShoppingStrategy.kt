package net.iessochoa.sergiocontreras.mercadonadb.model

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.model
 * Created by: Contr
 * On: 03/12/2025 at 15:28
 * Creado en Settings -> Editor -> File and Code Templates
 */
enum class ShoppingStrategy(val label: String) {
    CHEAPEST("El más barato 🤑"),
    MOST_EXPENSIVE("El más caro 💎"),
    BEST_VALUE("Mejor Calidad/Precio ⚖️"),
    RANDOM("¡Sorpréndeme! 🎲");
}