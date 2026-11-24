package com.example.glamping.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM comfort_categories ORDER BY id") fun getAll(): Flow<List<ComfortCategory>>
    @Query("SELECT * FROM comfort_categories WHERE id = :id") suspend fun getById(id: Int): ComfortCategory?
    @Insert suspend fun insert(c: ComfortCategory): Long
    @Update suspend fun update(c: ComfortCategory)
    @Delete suspend fun delete(c: ComfortCategory)
}

@Dao
interface ObjectDao {
    @Query("""SELECT o.*, c.categoryName AS categoryName FROM accommodation_objects o
        INNER JOIN comfort_categories c ON c.id = o.idComfortCategory ORDER BY o.id""")
    fun getAllWithCategory(): Flow<List<ObjectWithCategory>>
    @Query("SELECT * FROM accommodation_objects WHERE id = :id") suspend fun getById(id: Int): AccommodationObject?
    @Insert suspend fun insert(o: AccommodationObject): Long
    @Update suspend fun update(o: AccommodationObject)
    @Delete suspend fun delete(o: AccommodationObject)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY lastName") fun getAll(): Flow<List<Client>>
    @Query("SELECT * FROM clients WHERE id = :id") suspend fun getById(id: Int): Client?
    @Insert suspend fun insert(c: Client): Long
    @Update suspend fun update(c: Client)
    @Delete suspend fun delete(c: Client)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY serviceName") fun getAll(): Flow<List<Service>>
    @Query("SELECT * FROM services WHERE id = :id") suspend fun getById(id: Int): Service?
    @Insert suspend fun insert(s: Service): Long
    @Update suspend fun update(s: Service)
    @Delete suspend fun delete(s: Service)
}

@Dao
interface SettlementDao {
    // JOIN: клиент и объект по названиям; число ночей и итог (ночи x цена + услуги) считаются в запросе
    @Query("""
        SELECT s.id AS id, s.checkInDate AS checkInDate, s.checkOutDate AS checkOutDate, s.status AS status,
               (c.lastName || ' ' || c.firstName) AS clientName,
               (o.objectType || ' №' || o.id) AS objectTitle,
               CAST(ROUND((s.checkOutDate - s.checkInDate) / 86400000.0) AS INTEGER) AS nights,
               (CAST(ROUND((s.checkOutDate - s.checkInDate) / 86400000.0) AS INTEGER) * o.pricePerNight
                + COALESCE((SELECT SUM(sv.price * ss.countService) FROM settlement_services ss
                            INNER JOIN services sv ON sv.id = ss.idService WHERE ss.idSettlement = s.id), 0)) AS totalCost
        FROM settlements s
        INNER JOIN clients c ON c.id = s.idClient
        INNER JOIN accommodation_objects o ON o.id = s.idAccommodationObject
        ORDER BY s.checkInDate DESC
    """)
    fun getAllDetailed(): Flow<List<SettlementDetails>>
    @Query("SELECT * FROM settlements WHERE id = :id") suspend fun getById(id: Int): Settlement?
    @Insert suspend fun insert(s: Settlement): Long
    @Update suspend fun update(s: Settlement)
    @Query("DELETE FROM settlements WHERE id = :id") suspend fun deleteById(id: Int)
}

@Dao
interface SettlementServiceDao {
    @Query("""SELECT ss.idService AS idService, sv.serviceName AS serviceName, sv.price AS price, ss.countService AS countService
        FROM settlement_services ss INNER JOIN services sv ON sv.id = ss.idService
        WHERE ss.idSettlement = :settlementId ORDER BY sv.serviceName""")
    fun getItems(settlementId: Int): Flow<List<SettlementItem>>
    @Insert suspend fun insert(ss: SettlementService)
    @Upsert suspend fun upsert(ss: SettlementService)
    @Query("DELETE FROM settlement_services WHERE idSettlement = :settlementId AND idService = :serviceId")
    suspend fun deleteLink(settlementId: Int, serviceId: Int)
}
