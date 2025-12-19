package net.iessochoa.sergiocontreras.mercadonadb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
                Room.databaseBuilder(context, MercadoniaDatabase::class.java, DATABASE_NAME)
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }

    }

}