package net.iessochoa.sergiocontreras.mercadonadb.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "products")
data class Product (
    // 1. ID Interno vs ID del CSV. Generamos el nuestro propio
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo (name = "id")
    val id: Long = 0,

    @ColumnInfo (name = "externalId") val externalId: String?,
    @ColumnInfo (name = "supermarket") val supermarket: String,
    @ColumnInfo (name = "category") val category: String,
    @ColumnInfo (name = "product_name") val name: String, //Evitar palabras reservadas que nos la lían
    @ColumnInfo (name = "price") val price: Double, //Puede dar problemas de precisión pero para esto sirve
    @ColumnInfo (name = "reference_price") val referencePrice: Double,
    @ColumnInfo (name = "reference_unit") val referenceUnit: String,
    @ColumnInfo (name = "insert_date") val recordDate: String
)


