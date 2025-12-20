package net.iessochoa.sergiocontreras.mercadonadb.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.data
 * Created by: Contr
 * On: 19/12/2025 at 15:35
 * Creado en Settings -> Editor -> File and Code Templates
 */
@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProduct(id: Long?): Flow<Product>
    // Definición clasica mejor Flow: suspend fun getProduct(id: Long?): Product

    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<Product>>

    @Query("SELECT DISTINCT category FROM products")
    suspend fun getCategories(): List<String>
}

