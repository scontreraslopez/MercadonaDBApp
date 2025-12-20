package net.iessochoa.sergiocontreras.mercadonadb.ui.screens

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.iessochoa.sergiocontreras.mercadonadb.data.ProductsRepository

class ShopScreenViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(ShopScreenUiState())
    val uiState: StateFlow<ShopScreenUiState> = _uiState.asStateFlow()

    // Mejorable acoplando el fakeRepository-singleton
    private val productsRepository = ProductsRepository

    // Al arrancar pillamos la lista de categorias
    init {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    categories = productsRepository.getCategories()
                )
            }
            Log.d("ShopScreenViewModel", "Categories: ${_uiState.value.categories}")
        }

    }




}