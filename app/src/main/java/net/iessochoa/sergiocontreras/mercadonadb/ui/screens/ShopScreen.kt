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
    modifier: Modifier = Modifier
) {
    val selectedStrategy = uiState.selectedStrategy
    val selectedProduct = uiState.selectedProduct
    val categories = uiState.categories

    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        SupermarketBanner()
        /* TODO debemos añadir el selector de categoria para eso hay que recuperar todas las categorías */

        if (categories.isNotEmpty()) {
            DynamicSelectTextField(
                selectedValue = categories.first(),
                options = categories,
                label = "Categoría",
                onValueChangedEvent = {},
                modifier = Modifier.padding(top = 8.dp)
            )


            DynamicSelectTextField(
                selectedValue = selectedStrategy.toString(),
                options = ShoppingStrategy.entries.map {
                    it.toString()
                },
                label = "Estrategia de Compra",
                onValueChangedEvent = {},
                modifier = Modifier.padding(top=8.dp)
            )

            Button(onClick = { /*TODO*/ }) {
                Text("Voy a tener suerte")
            }
            ResultCard(
                product = selectedProduct,
                strategy = ShoppingStrategy.CHEAPEST
            )
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
        referenceUnit = "kg"
    )

    val sampleCategories = listOf("Fruta", "Verdura", "Carne", "Pescado")

    val uiState = ShopScreenUiState(
        selectedProduct = sampleProduct,
        selectedStrategy = ShoppingStrategy.CHEAPEST
    )

    MercadonaDBTheme() {
        ShopScreen(uiState)
    }

}