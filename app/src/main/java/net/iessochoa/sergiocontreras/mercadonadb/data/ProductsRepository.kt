package net.iessochoa.sergiocontreras.mercadonadb.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import androidx.room.Room// La interfaz sigue siendo una buena práctica para definir el "contrato"


interface IProductRepository {
    fun getProducts(): Flow<List<Product>>
    fun getProduct(id: Long): Flow<Product?> // Devuelve nulo si no lo encuentra
    fun getCategories(): Flow<List<String>>


    suspend fun deleteProduct(product: Product)
    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)

    suspend fun getProductsByCategory(category: String): List<Product>

    suspend fun reloadDatabase()
}

// Singleton que se inicializa automáticamente en el primer uso
object ProductsRepository: IProductRepository {

    // El DAO será nulo hasta que se inicialice la BBDD
    private lateinit var productDao: ProductDao

    //Variable para saber si ya hemos inicializado
    private var isInitialized = false

    //Propiedad para guardar el contexto de forma segura. No es lo mejor pasar Activity context hacia abajo.
    private lateinit var applicationContext: Context

    // Método de inicialización llamable desde fuera
    fun initialize(context: Context) {
        if (isInitialized) return

        this.applicationContext = context.applicationContext

        val database = MercadoniaDatabase.getDatabase(context)
        productDao = database.productDao()
        isInitialized = true
    }

    // Comprobación interna para asegurar la inicialización
    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("ProductsRepository debe ser inicializado. Llama a initialize() desde tu MainActivity.")
        }
    }

    override fun getProducts(): Flow<List<Product>> {
        checkInitialized()
        return productDao.getProducts()
    }

    override fun getProduct(id: Long): Flow<Product?> {
        checkInitialized()
        return productDao.getProduct(id)
    }

    override suspend fun deleteProduct(product: Product) {
        checkInitialized()
        return productDao.deleteProduct(product)
    }

    override suspend fun insertProduct(product: Product) {
        checkInitialized()
        return productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: Product) {
        checkInitialized()
        return productDao.updateProduct(product)
    }

    override suspend fun getProductsByCategory(category: String): List<Product> {
        checkInitialized()
        return productDao.getProductsByCategory(category)
    }

    override fun getCategories(): Flow<List<String>> {
        checkInitialized()
        return productDao.getCategories()
    }

    override suspend fun reloadDatabase() {
        checkInitialized()
        productDao.deleteAllProducts()
        MercadoniaDatabase.populateDatabase(applicationContext, productDao)
    }

}

