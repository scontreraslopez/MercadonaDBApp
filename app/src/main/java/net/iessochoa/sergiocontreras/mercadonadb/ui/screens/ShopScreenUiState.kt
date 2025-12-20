package net.iessochoa.sergiocontreras.mercadonadb.ui.screens

import net.iessochoa.sergiocontreras.mercadonadb.data.Product
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy

data class ShopScreenUiState(
    val selectedProduct: ProductDetails? = null,
    val selectedCategory: String? = null,
    val selectedStrategy: ShoppingStrategy = ShoppingStrategy.entries.first(),
    val categories: List<String> = emptyList(),
    val status: ShopScreenStatus = ShopScreenStatus.Idle
)

sealed class ShopScreenStatus {
    object Idle : ShopScreenStatus()
    object Loading : ShopScreenStatus()
    object Error : ShopScreenStatus()
    object Success : ShopScreenStatus() //podría enganchar las categorías pero de momento me sirve así
}

//UI Class sin ruido
data class ProductDetails(
    val category: String,
    val name: String,
    val price: Double,
    val referencePrice: Double,
    val referenceUnit: String,
    val foundWithStrategy: ShoppingStrategy
)

//Funcion de extensión mapper
fun Product.toProductDetails(strategy: ShoppingStrategy) = ProductDetails(
    category = category,
    name = name,
    price = price,
    referencePrice = referencePrice,
    referenceUnit = referenceUnit,
    foundWithStrategy = strategy
)

