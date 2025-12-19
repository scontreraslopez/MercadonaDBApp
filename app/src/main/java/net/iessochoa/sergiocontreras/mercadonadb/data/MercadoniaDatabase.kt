package net.iessochoa.sergiocontreras.mercadonadb.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        @Volatile
        private var Instance: MercadoniaDatabase? = null

        fun getDatabase(context: Context) : MercadoniaDatabase {
            //if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    MercadoniaDatabase::class.java,
                    DATABASE_NAME
                )
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .addCallback(getResponseCallback(context))
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }

        private fun getResponseCallback(context: Context): Callback {
            return object : Callback() {
                //onCreate solo se ejecuta cuando la BBDD se crea por primera vez
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    //Lanzamos una corrutina en segundo plano (IO)
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(context, getDatabase(context).productDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(context: Context, productDao: ProductDao) {
            //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            context.assets.open("mercadonia.csv")
                .bufferedReader() //Extension kotlin mejor que java InputStreamReader
                .useLines { lines -> // useLines cierra el flujo automáticamente al terminar
                    lines
                        .drop(1) // Para cabeceras de CSV, saltarlas. Si no no lo pondría
                        .map { line -> line.split(";")} // Transformamos cada línea en una lista de Strings
                        .filter { it.size >= 8 } // Filtramos líneas mal formadas o vacías
                        .forEach { data ->
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