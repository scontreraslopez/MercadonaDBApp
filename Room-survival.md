# ROOM Survival Guide

Guía práctica para implementar ROOM en Android con Kotlin.

---

## Tabla de Contenidos

1. [Dependencias](#1-dependencias)
2. [Entity (Product)](#2-entity-product)
3. [DAO (ProductDao)](#3-dao-productdao)
4. [Database](#4-database)
5. [Repository (Singleton simplificado)](#5-repository-singleton-simplificado)
6. [Uso en ViewModel](#6-uso-en-viewmodel)
7. [Extensión: Inicialización desde CSV](#7-extensión-inicialización-desde-csv)

---

## 1. Dependencias

En `build.gradle.kts` (app):

```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" // Ajustar versión
}

dependencies {
    // ROOM
    val roomVersion = "2.7.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
```

> **Nota:** KSP (Kotlin Symbol Processing) reemplaza a kapt. Es más rápido.

---

## 2. Entity (Product)

La **Entity** representa una tabla en la base de datos. Cada instancia = una fila.

```kotlin
// data/Product.kt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "product_name")
    val name: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "category")
    val category: String
)
```

### Puntos clave:

| Anotación | Propósito |
|-----------|-----------|
| `@Entity(tableName = "products")` | Define la tabla. Si no pones `tableName`, usa el nombre de la clase |
| `@PrimaryKey(autoGenerate = true)` | Clave primaria autoincremental |
| `@ColumnInfo(name = "...")` | Nombre de columna en BD (evita palabras reservadas como `name`) |

### Errores comunes:

- No poner valor por defecto en `id` cuando es autogenerado → Poner `= 0`
- Usar palabras reservadas de SQL como nombre de propiedad → Usar `@ColumnInfo`

---

## 3. DAO (ProductDao)

El **DAO** (Data Access Object) define las operaciones sobre la tabla.

```kotlin
// data/ProductDao.kt
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: Product)

    // READ - Un producto (reactivo)
    @Query("SELECT * FROM products WHERE id = :id")
    fun getProduct(id: Long): Flow<Product?>

    // READ - Todos los productos (reactivo)
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    // UPDATE
    @Update
    suspend fun update(product: Product)

    // DELETE
    @Delete
    suspend fun delete(product: Product)

    // DELETE ALL
    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
```

### Puntos clave:

| Concepto | Explicación |
|----------|-------------|
| `suspend fun` | Operación asíncrona (requiere coroutine). Úsalo para INSERT, UPDATE, DELETE |
| `Flow<T>` | Stream reactivo. La UI se actualiza automáticamente cuando cambian los datos |
| `OnConflictStrategy.IGNORE` | Si hay conflicto de clave, ignora la inserción |

### Cuándo usar cada retorno:

```kotlin
// Operaciones de escritura → suspend fun (sin retorno o retorna id)
suspend fun insert(product: Product): Long

// Lecturas puntuales → suspend fun
suspend fun getProductsByCategory(category: String): List<Product>

// Lecturas observables → Flow (la UI se actualiza sola)
fun getAllProducts(): Flow<List<Product>>
```

---

## 4. Database

La clase **Database** es abstracta y extiende `RoomDatabase`. Es el punto de acceso principal.

```kotlin
// data/MercadoniaDatabase.kt
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class MercadoniaDatabase : RoomDatabase() {

    // Expone el DAO
    abstract fun productDao(): ProductDao

    companion object {
        private const val DATABASE_NAME = "mercadonia.db"

        @Volatile
        private var Instance: MercadoniaDatabase? = null

        fun getDatabase(context: Context): MercadoniaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MercadoniaDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

### Anatomía del Singleton:

```kotlin
@Volatile  // Garantiza visibilidad entre threads
private var Instance: MercadoniaDatabase? = null

fun getDatabase(context: Context): MercadoniaDatabase {
    // 1. Si ya existe, retornarlo (fast path)
    return Instance ?: synchronized(this) {
        // 2. Dentro del bloque sincronizado, crear si no existe
        Room.databaseBuilder(...)
            .build()
            .also { Instance = it }  // 3. Guardar referencia
    }
}
```

### Puntos clave:

| Elemento | Propósito |
|----------|-----------|
| `@Volatile` | Los cambios son visibles inmediatamente entre threads |
| `synchronized(this)` | Solo un thread puede crear la instancia |
| `fallbackToDestructiveMigration(true)` | Si cambia la versión, destruye y recrea la BD |
| `context.applicationContext` | Evita memory leaks usando el contexto de aplicación |

---

## 5. Repository (Singleton simplificado)

El **Repository** abstrae la fuente de datos. Aquí usamos `object` de Kotlin para simplificar.

```kotlin
// data/ProductsRepository.kt
import android.content.Context
import kotlinx.coroutines.flow.Flow

object ProductsRepository {

    private lateinit var productDao: ProductDao
    private var isInitialized = false

    /**
     * Debe llamarse una vez desde MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        val database = MercadoniaDatabase.getDatabase(context)
        productDao = database.productDao()
        isInitialized = true
    }

    private fun checkInitialized() {
        check(isInitialized) {
            "ProductsRepository no inicializado. Llama initialize() en MainActivity"
        }
    }

    // READ
    fun getAllProducts(): Flow<List<Product>> {
        checkInitialized()
        return productDao.getAllProducts()
    }

    fun getProduct(id: Long): Flow<Product?> {
        checkInitialized()
        return productDao.getProduct(id)
    }

    // WRITE
    suspend fun insert(product: Product) {
        checkInitialized()
        productDao.insert(product)
    }

    suspend fun update(product: Product) {
        checkInitialized()
        productDao.update(product)
    }

    suspend fun delete(product: Product) {
        checkInitialized()
        productDao.delete(product)
    }
}
```

### Inicialización en MainActivity:

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el repositorio
        ProductsRepository.initialize(this)

        setContent {
            // Tu app...
        }
    }
}
```

### Por qué este patrón:

- **Simplicidad**: `object` = singleton automático en Kotlin
- **Sin inyección de dependencias**: No necesita Hilt/Dagger para proyectos pequeños
- **Inicialización explícita**: El `check()` avisa si olvidaste inicializar

---

## 6. Uso en ViewModel

El **ViewModel** consume el Repository y expone estado a la UI.

```kotlin
// ui/screens/ProductViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    // Referencia al repository (es un object, no hace falta inyectarlo)
    private val repository = ProductsRepository

    init {
        // Cargar productos al iniciar
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _uiState.update { currentState ->
                    currentState.copy(
                        products = products,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addProduct(name: String, price: Double, category: String) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                price = price,
                category = category
            )
            repository.insert(product)
            // No hace falta recargar, el Flow se actualiza solo
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }
}

// Estado de la UI
data class ProductUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### Flujo de datos:

```
BD (SQLite)
    ↓ Flow<List<Product>>
DAO
    ↓ Flow<List<Product>>
Repository
    ↓ Flow<List<Product>>
ViewModel (collect)
    ↓ StateFlow<UiState>
UI (Compose)
```

### Puntos clave:

| Patrón | Uso |
|--------|-----|
| `viewModelScope.launch` | Coroutine que se cancela automáticamente al destruir el ViewModel |
| `Flow.collect` | Escucha cambios reactivos de la BD |
| `MutableStateFlow` | Estado mutable interno |
| `StateFlow` | Estado inmutable expuesto a la UI |
| `_uiState.update { }` | Actualiza el estado de forma thread-safe |

---

## Resumen del Flujo Completo

```
┌─────────────────────────────────────────────────────────────┐
│                         ARQUITECTURA                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌──────────┐                                              │
│   │   UI     │  ← StateFlow<UiState>                        │
│   │ (Compose)│                                              │
│   └────┬─────┘                                              │
│        │                                                     │
│        ▼                                                     │
│   ┌──────────┐                                              │
│   │ViewModel │  ← viewModelScope.launch { }                 │
│   │          │  ← repository.getAllProducts().collect { }   │
│   └────┬─────┘                                              │
│        │                                                     │
│        ▼                                                     │
│   ┌──────────┐                                              │
│   │Repository│  ← object (Singleton)                        │
│   │          │  ← Abstrae acceso a datos                    │
│   └────┬─────┘                                              │
│        │                                                     │
│        ▼                                                     │
│   ┌──────────┐                                              │
│   │   DAO    │  ← @Insert, @Query, @Update, @Delete         │
│   │          │  ← suspend fun / Flow<T>                     │
│   └────┬─────┘                                              │
│        │                                                     │
│        ▼                                                     │
│   ┌──────────┐                                              │
│   │ Database │  ← @Database + Singleton                     │
│   │          │  ← Room.databaseBuilder()                    │
│   └────┬─────┘                                              │
│        │                                                     │
│        ▼                                                     │
│   ┌──────────┐                                              │
│   │  Entity  │  ← @Entity, @PrimaryKey, @ColumnInfo         │
│   │(Product) │  ← data class                                │
│   └──────────┘                                              │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Extensión: Inicialización desde CSV

> **Caso de uso**: Precargar datos desde un archivo CSV cuando se crea la BD por primera vez.

### 7.1 Ubicar el CSV

Coloca el archivo en:
```
app/src/main/assets/database/productos.csv
```

Formato ejemplo:
```csv
id,supermercado,categoria,nombre,precio,precio_ref,unidad_ref,fecha
1,Mercadona,Frutas,Plátano,1.25,1.25,kg,2024-01-15
2,Mercadona,Frutas,Manzana,2.10,2.10,kg,2024-01-15
```

### 7.2 Añadir Callback a la Database

```kotlin
@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class MercadoniaDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        private const val DATABASE_NAME = "mercadonia.db"
        private const val TAG = "MercadoniaDB"

        @Volatile
        private var Instance: MercadoniaDatabase? = null

        fun getDatabase(context: Context): MercadoniaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MercadoniaDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    // Añadir el callback
                    .addCallback(MercadoniaDatabaseCallback(context))
                    .build()
                    .also { Instance = it }
            }
        }

        // Función para poblar (accesible desde fuera)
        suspend fun populateDatabase(context: Context, productDao: ProductDao) {
            // Regex para parsear CSV con comas dentro de comillas
            val csvRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

            context.assets.open("database/productos.csv")
                .bufferedReader()
                .useLines { lines ->
                    lines
                        .drop(1) // Saltar cabecera
                        .map { line -> line.split(csvRegex) }
                        .filter { it.size >= 8 } // Validar columnas
                        .forEach { data ->
                            val product = Product(
                                name = data[3],
                                price = data[4].toDoubleOrNull() ?: 0.0,
                                category = data[2]
                            )
                            productDao.insert(product)
                        }
                }

            Log.d(TAG, "Base de datos poblada desde CSV")
        }
    }

    // Callback privado
    private class MercadoniaDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d(TAG, "Primera creación de la BD - Cargando CSV...")

            // Lanzar en coroutine (operación pesada)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = getDatabase(context)
                    populateDatabase(context, database.productDao())
                } catch (e: Exception) {
                    Log.e(TAG, "Error cargando CSV: ${e.message}", e)
                }
            }
        }
    }
}
```

### 7.3 Añadir método de recarga al Repository

```kotlin
object ProductsRepository {
    // ... código anterior ...

    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        if (isInitialized) return

        this.applicationContext = context.applicationContext  // Guardar referencia
        val database = MercadoniaDatabase.getDatabase(context)
        productDao = database.productDao()
        isInitialized = true
    }

    /**
     * Recarga todos los datos desde el CSV
     */
    suspend fun reloadFromCSV() {
        checkInitialized()
        productDao.deleteAll()
        MercadoniaDatabase.populateDatabase(applicationContext, productDao)
    }
}
```

### Puntos clave del CSV:

| Concepto | Explicación |
|----------|-------------|
| `RoomDatabase.Callback` | Se ejecuta en eventos del ciclo de vida de la BD |
| `onCreate()` | Solo se llama la **primera vez** que se crea la BD |
| `onOpen()` | Se llama cada vez que se abre la BD |
| `Dispatchers.IO` | Thread pool para operaciones de I/O |
| `useLines { }` | Lee línea a línea sin cargar todo en memoria |
| Regex CSV | Maneja comas dentro de campos entrecomillados |

---

## Checklist de Implementación

- [ ] Añadir dependencias de Room y KSP
- [ ] Crear la Entity (`@Entity`, `@PrimaryKey`, `@ColumnInfo`)
- [ ] Crear el DAO (`@Dao`, `@Insert`, `@Query`, etc.)
- [ ] Crear la Database (`@Database`, Singleton)
- [ ] Crear el Repository (object Singleton)
- [ ] Inicializar Repository en MainActivity
- [ ] Usar Repository desde ViewModel
- [ ] (Opcional) Añadir Callback para CSV

---

## Errores Frecuentes

| Error | Solución |
|-------|----------|
| `Cannot access database on the main thread` | Usar `suspend fun` o `Flow` |
| `No value passed for parameter 'id'` | Poner `id: Long = 0` en la Entity |
| `Cannot find implementation for...` | Verificar que KSP está configurado |
| `Database must provide abstract fun productDao()` | Añadir el método abstracto |
| `Repository not initialized` | Llamar `initialize()` en MainActivity |

---

## Referencias

- [Room Persistence Library (Android Developers)](https://developer.android.com/training/data-storage/room)
- [Kotlin Flows](https://developer.android.com/kotlin/flow)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
