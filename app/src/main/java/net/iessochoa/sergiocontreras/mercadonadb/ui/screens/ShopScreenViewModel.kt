package net.iessochoa.sergiocontreras.mercadonadb.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShopScreenViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(ShopScreenUiState())
    val uiState: StateFlow<ShopScreenUiState> = _uiState.asStateFlow()

    init {



    }


}