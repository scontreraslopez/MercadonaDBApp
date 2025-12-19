package net.iessochoa.sergiocontreras.mercadonadb.ui.screens

import net.iessochoa.sergiocontreras.mercadonadb.data.Product
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy

data class ShopScreenUiState(
    val selectedProduct: ProductDetails,
    val selectedStrategy: ShoppingStrategy
)

//UI Class sin ruido
data class ProductDetails(
    val category: String,
    val name: String,
    val price: Double,
    val referencePrice: Double,
    val referenceUnit: String
)

//Funcion de extensión mapper
fun Product.toProductDetails() = ProductDetails(
    category = category,
    name = name,
    price = price,
    referencePrice = referencePrice,
    referenceUnit = referenceUnit
)