package com.example.glamping.data

import androidx.room.*

@Entity(tableName = "comfort_categories")
data class ComfortCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String
)

@Entity(
    tableName = "accommodation_objects",
    foreignKeys = [ForeignKey(ComfortCategory::class, ["id"], ["idComfortCategory"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("idComfortCategory")]
)
data class AccommodationObject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val objectType: String,
    val capacity: Int,
    val idComfortCategory: Int,
    val pricePerNight: Double,
    val description: String,
    val photo: ByteArray? = null      // BLOB: фото объекта (JPEG)
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val phone: String,
    val passport: String
)

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(Client::class, ["id"], ["idClient"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(AccommodationObject::class, ["id"], ["idAccommodationObject"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("idClient"), Index("idAccommodationObject")]
)
data class Settlement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idClient: Int,
    val idAccommodationObject: Int,
    val checkInDate: Long,            // начало дня, epoch millis
    val checkOutDate: Long,
    val status: String                // Забронировано / Заселён / Выселен / Отменён
)

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceName: String,
    val description: String,
    val price: Double
)

// Составной первичный ключ (settlement_id, service_id), как на схеме
@Entity(
    tableName = "settlement_services",
    primaryKeys = ["idSettlement", "idService"],
    foreignKeys = [
        ForeignKey(Settlement::class, ["id"], ["idSettlement"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Service::class, ["id"], ["idService"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("idService")]
)
data class SettlementService(
    val idSettlement: Int,
    val idService: Int,
    val countService: Int
)

// ---- результаты JOIN-запросов ----
data class ObjectWithCategory(@Embedded val obj: AccommodationObject, val categoryName: String)

data class SettlementDetails(
    val id: Int,
    val checkInDate: Long,
    val checkOutDate: Long,
    val status: String,
    val clientName: String,
    val objectTitle: String,
    val nights: Int,
    val totalCost: Double
)

data class SettlementItem(val idService: Int, val serviceName: String, val price: Double, val countService: Int)
