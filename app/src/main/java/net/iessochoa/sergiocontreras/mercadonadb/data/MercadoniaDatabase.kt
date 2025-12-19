package net.iessochoa.sergiocontreras.mercadonadb.data

import androidx.room.Database
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

}