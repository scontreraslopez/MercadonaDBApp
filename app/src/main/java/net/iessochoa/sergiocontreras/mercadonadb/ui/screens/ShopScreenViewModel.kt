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
import net.iessochoa.sergiocontreras.mercadonadb.model.ShoppingStrategy

class ShopScreenViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(ShopScreenUiState())
    val uiState: StateFlow<ShopScreenUiState> = _uiState.asStateFlow()

    // Mejorable acoplando el fakeRepository-singleton
    private val productsRepository = ProductsRepository

    // Al arrancar pillamos la lista de categorias
    init {
        viewModelScope.launch {
            productsRepository.getCategories().collect { categories ->
                _uiState.update { currentState ->
                    currentState.copy(
                        status = ShopScreenStatus.Success,
                        categories = categories
                    )
                }
            }

            //Log.d("ShopScreenViewModel", "Categories: ${_uiState.value.categories}")
        }

    }

    fun reloadDatabase() {
        viewModelScope.launch {
            productsRepository.reloadDatabase()
        }
    }


    fun onSelectedStrategyChange(shoppingStrategy: ShoppingStrategy){
        _uiState.update { currentState ->
            currentState.copy(
                selectedStrategy = shoppingStrategy
            )
        }
    }

    fun onSelectedCategoryChange(selectedCategory: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = selectedCategory
            )
        }

    }

    fun onSearchProduct(category: String, strategy: ShoppingStrategy) {
        viewModelScope.launch {
            val productsInCategory = productsRepository.getProductsByCategory(category)
            val selectedProduct = when (strategy) {
                ShoppingStrategy.CHEAPEST -> productsInCategory.minByOrNull { it.price }
                ShoppingStrategy.MOST_EXPENSIVE -> productsInCategory.maxByOrNull { it.price }
                ShoppingStrategy.BEST_VALUE -> productsInCategory.minByOrNull { it.referencePrice }
                ShoppingStrategy.RANDOM -> productsInCategory.random()
            }
            _uiState.update { currentState ->
                currentState.copy(
                    selectedProduct = selectedProduct?.toProductDetails(currentState.selectedStrategy)
                )
            }

        }
        //Log.d("ShopScreenViewModel", "Selected Product: ${_uiState.value.selectedProduct}")
    }

}
