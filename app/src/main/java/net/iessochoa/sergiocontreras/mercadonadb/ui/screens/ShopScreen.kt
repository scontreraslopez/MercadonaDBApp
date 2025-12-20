package net.iessochoa.sergiocontreras.mercadonadb.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.DynamicSelectTextField
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.ResultCard
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.SupermarketBanner
import net.iessochoa.sergiocontreras.mercadonadb.ui.theme.MercadonaDBTheme

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.ui.theme
 * Created by: Contr
 * On: 03/12/2025 at 15:00
 * Creado en Settings -> Editor -> File and Code Templates
 */

@Composable
fun ShopScreen(
    uiState: ShopScreenUiState,
    onReloadDatabaseClick: () -> Unit,
    onSelectedCategoryChange: (String) -> Unit,
    onSelectedStrategyChange: (ShoppingStrategy) -> Unit,
    onSearchProduct: (String, ShoppingStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedStrategy = uiState.selectedStrategy
    val selectedProduct = uiState.selectedProduct
    val selectedCategory = uiState.selectedCategory
    val categories = uiState.categories

    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        SupermarketBanner()
        /* TODO debemos añadir el selector de categoria para eso hay que recuperar todas las categorías */

        if (categories.isNotEmpty()) {
            DynamicSelectTextField(
                selectedValue = selectedCategory ?: categories.first(),
                options = categories,
                label = "Categoría",
                onValueChangedEvent = {
                    onSelectedCategoryChange(it)
                },
                modifier = Modifier.padding(top = 8.dp)
            )


            DynamicSelectTextField(
                selectedValue = selectedStrategy.toString(),
                options = ShoppingStrategy.entries.map {
                    it.toString()
                },
                label = "Estrategia de Compra",
                onValueChangedEvent = {
                    val strategyEnum = enumValueOf<ShoppingStrategy>(it)
                    onSelectedStrategyChange(strategyEnum)},
                modifier = Modifier.padding(top=8.dp)
            )

            Button(onClick = { onSearchProduct(
                selectedCategory ?: categories.first(),
                selectedStrategy
            ) }) {
                Text("Voy a tener suerte")
            }
            ResultCard(
                product = selectedProduct
            )
        } else {
            Button(onClick = onReloadDatabaseClick ) {
                Text("Reload Database")
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun PreviewShopScreen() {


    val sampleProduct = ProductDetails(
        category = "Fruta",
        name = "Sandía",
        price = 3.75,
        referencePrice = 0.55,
        referenceUnit = "kg",
        foundWithStrategy = ShoppingStrategy.CHEAPEST
    )

    val sampleCategories = listOf("Fruta", "Verdura", "Carne", "Pescado")

    val uiState = ShopScreenUiState(
        selectedProduct = sampleProduct,
        selectedStrategy = ShoppingStrategy.CHEAPEST,
        categories = sampleCategories
    )

    MercadonaDBTheme {
        ShopScreen(
            uiState = uiState,
            onSelectedCategoryChange = { },
            onReloadDatabaseClick = { },
            onSelectedStrategyChange = { },
            onSearchProduct = { _, _ -> }
        )
    }

}