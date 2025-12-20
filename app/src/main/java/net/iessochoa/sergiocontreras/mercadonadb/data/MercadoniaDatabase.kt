package net.iessochoa.sergiocontreras.mercadonadb.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Project: MercadonaDB
 * From: net.iessochoa.sergiocontreras.mercadonadb.data
 * Created by: Contr
 * On: 19/12/2025 at 16:25
 * Creado en Settings -> Editor -> File and Code Templates
 */

@Database(entities=[Product::class], version = 1, exportSchema = false) //Puede ser interesante hacerlo true
abstract class MercadoniaDatabase: RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        const val DATABASE_NAME = "mercadonia.db"
        // Tag para filtrar en el Logcat
        private const val TAG = "MercadoniaDB"

        @Volatile
        private var Instance: MercadoniaDatabase? = null

        fun getDatabase(context: Context): MercadoniaDatabase {
            return Instance ?: synchronized(this) {
                // Pasamos una referencia segura usando un Provider o una Lambda para romper el ciclo
                // Pero para mantener tu estructura simple, usaremos el contexto directamente en el callback con cuidado.
                Room.databaseBuilder(
                    context.applicationContext, // Usar siempre applicationContext
                    MercadoniaDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(MercadoniaDatabaseCallback(context))
                    .build()
                    .also { Instance = it }
            }
        }

        /**
         * Movemos el Callback a una clase interna o variable para mayor claridad.
         * Pasamos el contexto para poder acceder a los assets.
         */
        private class MercadoniaDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "onCreate: Se ha creado la BBDD por primera vez. Iniciando carga de datos...")

                // Usamos GlobalScope o un Scope propio del Application es mejor,
                // pero para este ejemplo CoroutineScope(Dispatchers.IO) está bien.
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // IMPORTANTE: Accedemos a la base de datos a través de la clase padre
                        // asegurándonos de que 'Instance' ya no sea null cuando la corrutina arranque.
                        val database = getDatabase(context)
                        populateDatabase(context, database.productDao())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fatal en la corrutina de carga: ${e.message}", e)
                    }
                }
            }
        }

        private fun getResponseCallback(context: Context): Callback {
            return object : Callback() {
                //onCreate solo se ejecuta cuando la BBDD se crea por primera vez
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d(TAG, "onCreate: Se ha creado la BBDD por primera vez. Iniciando carga de datos...")

                    //Lanzamos una corrutina en segundo plano (IO)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // IMPORTANTE: Accedemos a la base de datos a través de la clase padre
                            // asegurándonos de que 'Instance' ya no sea null cuando la corrutina arranque.
                            val database = getDatabase(context)
                            populateDatabase(context, database.productDao())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fatal en la corrutina de carga: ${e.message}", e)
                        }
                    }
                }
            }
        }

        suspend fun populateDatabase(context: Context, productDao: ProductDao) {
            //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            // Es un estándar para leer CSVs complejos sin usar librerías externas.
            val csvRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

            context.assets.open("database/mercadonia.csv")
                .bufferedReader() //Extension kotlin mejor que java InputStreamReader
                .useLines { lines -> // useLines cierra el flujo automáticamente al terminar
                    //Log.d(TAG, "onCreate: leídas ${lines.count()} líneas")
                    lines
                        .drop(1) // Para cabeceras de CSV, saltarlas. Si no no lo pondría
                        .map { line -> line.split(csvRegex)} // Transformamos cada línea en una lista de Strings. ¡ATENCION AL DELIMITADOR!
                        .filter { it.size >= 8 } // Filtramos líneas mal formadas o vacías
                        .forEach { data ->
                            Log.d(TAG, "onCreate: $data")
                            //Por cada línea válida, creamos e insertamos
                            val product = Product(
                                externalId = data[0],
                                supermarket = data[1],
                                category = data[2],
                                name = data[3],
                                price = data[4].toDouble(),
                                referencePrice = data[5].toDouble(),
                                referenceUnit = data[6],
                                recordDate = data[7]
                            )
                            productDao.insertProduct(product)
                        }


                }
        }

    }

}