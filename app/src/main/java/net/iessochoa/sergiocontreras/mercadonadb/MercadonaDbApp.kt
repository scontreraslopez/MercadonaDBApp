package net.iessochoa.sergiocontreras.mercadonadb

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.ui.theme
 * Created by: Contr
 * On: 03/12/2025 at 15:00
 * Creado en Settings -> Editor -> File and Code Templates
 */

@Composable
fun MercadonaDbApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding)
        )
    }
}