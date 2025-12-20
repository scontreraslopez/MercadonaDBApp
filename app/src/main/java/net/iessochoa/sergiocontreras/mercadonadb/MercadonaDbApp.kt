package net.iessochoa.sergiocontreras.mercadonadb

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import net.iessochoa.sergiocontreras.mercadonadb.ui.screens.ShopScreen
import net.iessochoa.sergiocontreras.mercadonadb.ui.screens.ShopScreenViewModel

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.ui.theme
 * Created by: Contr
 * On: 03/12/2025 at 15:00
 * Creado en Settings -> Editor -> File and Code Templates
 */

@Composable
fun MercadonaDbApp(
    modifier: Modifier = Modifier,
    viewModel: ShopScreenViewModel = viewModel(),
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    //Log.d("ShopScreenViewModel", "Categories: ${uiState.categories}")


    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        ShopScreen(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onSelectedCategoryChange = { viewModel.onSelectedCategoryChange(it) },
            onReloadDatabaseClick = { viewModel.reloadDatabase() },
            onSelectedStrategyChange = { viewModel.onSelectedStrategyChange(it) },
            onSearchProduct = { category, strategy -> viewModel.onSearchProduct(category, strategy) }
        )
    }
}