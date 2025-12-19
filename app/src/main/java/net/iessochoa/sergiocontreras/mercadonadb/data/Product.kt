package net.iessochoa.sergiocontreras.mercadonadb.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Product(
    // 1. ID Interno vs ID del CSV
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "supermarket") val supermarket: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo (name = "name") val name: String,
    @ColumnInfo (name = "price") val productPrice: Double,
    @ColumnInfo (name = "reference_price") val referencePrice: Double,
    @ColumnInfo (name = "reference_unit") val referenceUnit: String,
    @ColumnInfo (name = "insert_date") val insertDate: LocalDateTime
)

    val reference_price
    val reference_uit
    val insert_date


    supermarket, category, name, price, reference_price, reference_unit, insert_date

