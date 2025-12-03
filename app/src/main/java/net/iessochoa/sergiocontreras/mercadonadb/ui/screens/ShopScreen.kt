package net.iessochoa.sergiocontreras.mercadonadb.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.DynamicSelectTextField
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.Product
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.ResultCard
import net.iessochoa.sergiocontreras.mercadonadb.ui.components.SupermarketBanner

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.ui.theme
 * Created by: Contr
 * On: 03/12/2025 at 15:00
 * Creado en Settings -> Editor -> File and Code Templates
 */

@Composable
fun ShopScreen(modifier: Modifier = Modifier) {
    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        var selectedStrategy = ShoppingStrategy.CHEAPEST
        val sampleProduct = Product(
            category = "Fruta",
            name = "Sandía",
            price = 3.75,
            referencePrice = 0.55,
            referenceUnit = "kg"
        )

        SupermarketBanner()
        DynamicSelectTextField(
            selectedValue = selectedStrategy.toString(),
            options = ShoppingStrategy.entries.map {
                it.toString()
            },
            label = "Estrategia de Compra",
            onValueChangedEvent = {}
        )

        Button(onClick = { /*TODO*/ }) {
            Text("Voy a tener suerte")
        }
        ResultCard(
            product = sampleProduct,
            strategy = ShoppingStrategy.CHEAPEST
        )

    }

}

@Preview(showBackground = true)
@Composable
fun PreviewShopScreen() {
    ShopScreen()
}