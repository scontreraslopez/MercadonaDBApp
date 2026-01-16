# ROOM_HELPER.md

Receta de cocina: implementar **Room** (inspirada en *MercadonaDB*)

> Objetivo: montar una base de datos local con Room siguiendo una arquitectura limpia y fácil de probar:
>
>- **Entity** (modelo persistente) → `Product.kt`
>- **DAO** (cuchillo: queries) → `ProductDao.kt`
>- **Database** (horno: RoomDatabase + singleton + prepopulate) → `MercadoniaDatabase.kt`
>- **Repository** (capa “salsa” / fuente única de verdad) → `ProductsRepository.kt`
>- **ViewModel + UI** (emplatado en Compose) → `ShopScreenViewModel.kt` + `ShopScreenUiState.kt`

---

## ✅ Checklist (lo que tiene que quedar al final)

- [ ] Dependencias de Room + KSP configuradas.
- [ ] Una `@Entity` definida.
- [ ] Un `@Dao` con operaciones y consultas (idealmente `Flow`).
- [ ] Una `RoomDatabase` con singleton.
- [ ] (Opcional) Prepopular la BD desde `assets/` (CSV/JSON).
- [ ] Un repositorio que abstraiga el DAO.
- [ ] Un ViewModel que consuma el repositorio y exponga `StateFlow`/`UiState`.

---

## 🧾 Plantillas copiables (copy/paste)

> Estas plantillas están pensadas para poder copiar/pegar y luego ajustar nombres/paquetes.
> Están inspiradas en la estructura real de MercadonaDB (`data/` + `ui/screens/`).
>
> **Nota sobre el package:** en los snippets verás `package com.example.mercadonadb...`.
> Cámbialo por tu package real (por ejemplo el de este proyecto: `net.iessochoa.sergiocontreras.mercadonadb`).

### 1) Dependencias (Room + KSP)

En `app/build.gradle.kts` (Module: app) el patrón mínimo es:

```kotlin
plugins {
    // ...existing plugins...
    alias(libs.plugins.ksp)
}

dependencies {
    // ...existing deps...
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
```

### 2) Entity (ejemplo tipo Product)

📄 `app/src/main/java/com/example/mercadonadb/data/Product.kt`

```kotlin
// archivo: Product.kt
// package com.example.mercadonadb.data

// imports:
// import androidx.room.ColumnInfo
// import androidx.room.Entity
// import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Si el dataset tiene un id externo (CSV/API), suele venir bien guardarlo
    @ColumnInfo(name = "external_id")
    val externalId: String? = null,

    val name: String,
    val category: String,

    // Nota: para dinero, en apps reales suele ir mejor guardar céntimos (Long)
    val price: Double,
    @ColumnInfo(name = "reference_price")
    val referencePrice: Double,

    @ColumnInfo(name = "reference_unit")
    val referenceUnit: String,

    // Tip: si luego quieres ordenar/filtrar por fecha real, usa TypeConverters
    @ColumnInfo(name = "record_date")
    val recordDate: String
)
```

### 3) DAO (CRUD + queries con Flow)

📄 `app/src/main/java/com/example/mercadonadb/data/ProductDao.kt`

```kotlin
// archivo: ProductDao.kt
// package com.example.mercadonadb.data

// imports:
// import androidx.room.*
// import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProduct(id: Int): Flow<Product?>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndRepopulate(products: List<Product>) {
        deleteAll()
        insertAll(products)
    }
}
```

### 4) RoomDatabase (singleton + prepopulate opcional desde assets)

📄 `app/src/main/java/com/example/mercadonadb/data/MercadoniaDatabase.kt`

```kotlin
// archivo: MercadoniaDatabase.kt
// package com.example.mercadonadb.data

// imports:
// import android.content.Context
// import androidx.room.*
// import androidx.sqlite.db.SupportSQLiteDatabase
// import kotlinx.coroutines.*

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class MercadoniaDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var Instance: MercadoniaDatabase? = null

        fun getDatabase(context: Context): MercadoniaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MercadoniaDatabase::class.java,
                    "mercadonia_database"
                )
                    // Si quieres prepopulate al crear la BD (opcional)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Importante: usa Instance ya construida evitando crear otra BD
                            val database = Instance ?: return
                            val dao = database.productDao()

                            CoroutineScope(Dispatchers.IO).launch {
                                // populateDatabase(context, dao)
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }

        // EJEMPLO (opcional): lee y parsea un CSV desde assets y lo inserta
        // Ajusta el parser a tu dataset; en MercadonaDB se usa una regex para comas fuera de comillas.
        @Suppress("unused")
        private suspend fun populateDatabase(context: Context, dao: ProductDao) {
            val csvPath = "database/mercadonia.csv"
            val inputStream = context.assets.open(csvPath)

            val lines = inputStream.bufferedReader().use { it.readLines() }
            if (lines.isEmpty()) return

            val splitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

            val products = lines
                .drop(1) // cabecera
                .map { it.split(splitRegex).map { cell -> cell.trim().trim('\"') } }
                .filter { it.size >= 8 }
                .mapNotNull { row ->
                    // Ajusta índices/campos según tu CSV real
                    val name = row[0]
                    val category = row[1]
                    val price = row[2].replace(",", ".").toDoubleOrNull() ?: return@mapNotNull null
                    val refPrice = row[3].replace(",", ".").toDoubleOrNull() ?: return@mapNotNull null
                    val refUnit = row[4]
                    val recordDate = row[5]
                    val externalId = row.getOrNull(6)

                    Product(
                        externalId = externalId,
                        name = name,
                        category = category,
                        price = price,
                        referencePrice = refPrice,
                        referenceUnit = refUnit,
                        recordDate = recordDate
                    )
                }

            dao.insertAll(products)
        }
    }
}
```

### 5) Repository (envolver DAO + punto único de acceso)

📄 `app/src/main/java/com/example/mercadonadb/data/ProductsRepository.kt`

```kotlin
// archivo: ProductsRepository.kt
// package com.example.mercadonadb.data

// imports:
// import android.content.Context
// import kotlinx.coroutines.flow.Flow

object ProductsRepository {

    private lateinit var productDao: ProductDao

    fun initialize(context: Context) {
        val database = MercadoniaDatabase.getDatabase(context)
        productDao = database.productDao()
    }

    private fun checkInitialized() {
        check(::productDao.isInitialized) {
            "ProductsRepository no inicializado. Llama a initialize(context) en Application/MainActivity."
        }
    }

    fun getProducts(): Flow<List<Product>> {
        checkInitialized()
        return productDao.getProducts()
    }

    fun getCategories(): Flow<List<String>> {
        checkInitialized()
        return productDao.getCategories()
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        checkInitialized()
        return productDao.getProductsByCategory(category)
    }

    suspend fun insert(product: Product) {
        checkInitialized()
        productDao.insert(product)
    }
}
```

### 6) UiState + ViewModel (consumir repository y exponer estado para Compose)

📄 `app/src/main/java/com/example/mercadonadb/ui/screens/ShopScreenUiState.kt`

```kotlin
// archivo: ShopScreenUiState.kt
// package com.example.mercadonadb.ui.screens

data class ShopScreenUiState(
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
```

📄 `app/src/main/java/com/example/mercadonadb/ui/screens/ShopScreenViewModel.kt`

```kotlin
// archivo: ShopScreenViewModel.kt
// package com.example.mercadonadb.ui.screens

// imports:
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import com.example.mercadonadb.data.ProductsRepository
// import kotlinx.coroutines.flow.*
// import kotlinx.coroutines.launch

class ShopScreenViewModel(
    private val repository: ProductsRepository = ProductsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopScreenUiState())
    val uiState: StateFlow<ShopScreenUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error") }
                }
                .collectLatest { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            selectedCategory = it.selectedCategory ?: categories.firstOrNull(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
```

### 7) Inicialización (no te olvides del “encendido”)

El patrón del repo es: **inicializa una vez** antes de usarlo.

Ejemplo en `MainActivity` o (mejor) en una clase `Application`:

```kotlin
ProductsRepository.initialize(applicationContext)
```

---

## 🛒 0) Comprar ingredientes (dependencias + plugins)

En este proyecto se usan **Version Catalog** (`gradle/libs.versions.toml`) y **KSP**.

### A. Version Catalog (referencia)

En `gradle/libs.versions.toml` encontrarás las entradas de Room (ejemplo real del proyecto):

- `androidx-room-runtime`
- `androidx-room-ktx`
- `androidx-room-compiler`

> La clave: **Room necesita un procesador de anotaciones** para generar código. En este repo se usa **KSP**.

### B. app/build.gradle.kts (Module: app)

En `app/build.gradle.kts` (del proyecto) verás el patrón:

- Plugin KSP habilitado.
- Dependencias `implementation(...)` para runtime/ktx.
- Dependencia `ksp(...)` para el compiler.

**Regla de oro:**

- `implementation(libs.androidx.room.runtime)` → runtime
- `implementation(libs.androidx.room.ktx)` → coroutines/Flow helpers
- `ksp(libs.androidx.room.compiler)` → generación de código

> Si olvidas `ksp(...)`, el proyecto compilará mal o Room no generará los DAO/Database correctamente.

---

## 🔪 1) Mise en place: define tu Entity (la “materia prima”)

En MercadonaDB, la entidad es `Product`:

📄 `app/src/main/java/.../data/Product.kt`

Puntos clave que debes copiar como patrón:

- `@Entity(tableName = "products")` para fijar el nombre de tabla.
- `@PrimaryKey(autoGenerate = true)` para clave interna autogenerada.
- Campos con tipos soportados por Room.

**Plantilla típica (idea):**

- id interno autogenerado (Int/Long)
- campos de negocio (nombre, categoría, precio, etc.)

**Consejos prácticos:**

- Si vas a buscar mucho por `category`, considera `@Index("category")`.
- Para dinero, Room permite `Double`, pero en apps reales suele ir mejor `BigDecimal` (requiere `TypeConverter`) o guardar en *céntimos* (`Long`).

---

## 🍳 2) El cuchillo: crea el DAO (queries + CRUD)

En MercadonaDB, el DAO es `ProductDao`:

📄 `app/src/main/java/.../data/ProductDao.kt`

### A. Operaciones básicas

- `@Insert(onConflict = ...)`
- `@Update`
- `@Delete`

### B. Consultas reactivas con Flow

Este proyecto basa la UI en datos reactivos:

- `fun getProducts(): Flow<List<Product>>`
- `fun getCategories(): Flow<List<String>>`

**Regla de oro:** si la UI tiene que “enterarse sola” de cambios, usa **Flow** desde el DAO.

### C. Helpers transaccionales

En el DAO del proyecto existe el patrón:

- `@Transaction suspend fun clearAndRepopulate(products: List<Product>)`

Esto es muy útil para:

- importar datos (“vaciar y rellenar”)
- sincronizaciones
- seeds de demo

---

## 🧁 3) El horno: crea la RoomDatabase + singleton

En MercadonaDB la base de datos es `MercadoniaDatabase`:

📄 `app/src/main/java/.../data/MercadoniaDatabase.kt`

### A. Anotación @Database

- `entities = [Product::class]`
- `version = 1`
- `exportSchema = false` (ok para prácticas; en apps reales suele ser `true`)

### B. Singleton (patrón obligatorio)

Este repo usa el patrón típico:

- `@Volatile private var Instance: MercadoniaDatabase?`
- `fun getDatabase(context)`
- `synchronized` para evitar crear 2 instancias en concurrencia

**Regla de oro:**

- Nunca crees la BD en varios sitios.
- Usa `applicationContext` para evitar leaks.

---

## 🥫 4) Extra: prepopular la BD desde assets (CSV real del proyecto)

MercadonaDB tiene un dataset en:

📄 `app/src/main/assets/database/mercadonia.csv`

Y un flujo de carga en:

📄 `MercadoniaDatabase.kt` (función de poblado)

### A. Cuándo hacerlo

- Ideal para demos, apps offline-first, catálogos iniciales.
- No recomendado si los datos cambian a menudo (ahí mejor descarga + cache).

### B. Dónde engancharlo

Patrón típico (como en el repo):

- `Room.databaseBuilder(...).addCallback(object : RoomDatabase.Callback() { onCreate(...) { ... } })`

En `onCreate`:

- leer el CSV de assets
- parsear por filas
- mapear a entidades
- insertar en una transacción

### C. Edge cases (muy reales)

Cuando importas CSV, ten en cuenta:

- **Comas dentro de comillas** ("1,20") → necesitas split “inteligente” (regex o parser). Este repo usa una regex.
- `toDouble()` puede fallar → usa `toDoubleOrNull()` y decide default/skip.
- CSV corrupto/filas incompletas → filtra por longitud mínima.
- Base de datos vacía durante unos ms/seg → la UI debe contemplar “cargando”.

---

## 🥘 5) La salsa: Repository (fuente única de verdad)

MercadonaDB usa un repositorio que envuelve el DAO:

📄 `app/src/main/java/.../data/ProductsRepository.kt`

Qué aporta:

- Un punto único para acceder a datos.
- Encapsula Room (la UI nunca debería conocer el DAO).
- Permite cambiar la fuente en el futuro (Room ↔️ Red ↔️ Cache).

Patrones del repo:

- Inicialización con `initialize(context)`.
- Comprobación `checkInitialized()` para fallar rápido si se usa mal.

> En proyectos grandes usarías Hilt/Koin. Aquí la inyección manual es perfecta para aprender.

---

## 👨‍🍳 6) El chef: ViewModel + UiState (MVVM)

En MercadonaDB:

- Estado: 📄 `ShopScreenUiState.kt`
- Lógica: 📄 `ShopScreenViewModel.kt`

Patrón recomendado (y alineado con el repo):

- `private val _uiState = MutableStateFlow(...)`
- `val uiState: StateFlow<ShopScreenUiState>`
- Llamadas al repositorio en `viewModelScope.launch { ... }`
- Si el repositorio devuelve `Flow`, lo colectas y actualizas estado.

**Tip:** separa datos persistentes (ej. categoría seleccionada) de estado de petición (`Loading/Error/Success`).

---

## 🍽️ 7) Emplatado: UI Compose que reacciona al Flow

La idea final:

1. El DAO emite `Flow`.
2. El repositorio expone ese `Flow`.
3. El ViewModel lo colecciona y actualiza `UiState`.
4. La pantalla Compose pinta según el estado.

En este proyecto, el flujo se ve claramente con categorías/productos.

---

## 🧪 Mini-chequeos (cuando algo falla)

- **“Room no genera código”** → ¿está `ksp(room-compiler)`?
- **Crash por null / init** → ¿se llamó a `ProductsRepository.initialize(context)` antes de usarlo?
- **UI vacía al arrancar** → ¿la BD está poblando aún? añade estado `Loading`.
- **CSV rompe por comas/decimales** → revisa parsing y `toDoubleOrNull()`.

---

## 🧩 Variantes comunes (por si lo quieres más profesional)

- Migraciones reales: `exportSchema = true` + `Migration(1,2)`.
- `TypeConverters` para fechas (`LocalDate`/`Instant`).
- Índices y claves únicas (por ejemplo `externalId`).
- Tests de DAO con `Room.inMemoryDatabaseBuilder`.

---

## Referencias dentro de este proyecto

- Entity: `Product.kt`
- DAO: `ProductDao.kt`
- Database: `MercadoniaDatabase.kt`
- CSV seed: `app/src/main/assets/database/mercadonia.csv`
- Repository: `ProductsRepository.kt`
- ViewModel/UI State: `ShopScreenViewModel.kt`, `ShopScreenUiState.kt`
