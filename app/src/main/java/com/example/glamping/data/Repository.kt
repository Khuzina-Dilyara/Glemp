package com.example.glamping.data

import androidx.room.withTransaction

class AppRepository(private val db: AppDatabase) {
    private val categories = db.categoryDao()
    private val objects = db.objectDao()
    private val clients = db.clientDao()
    private val settlements = db.settlementDao()
    private val services = db.serviceDao()
    private val links = db.settlementServiceDao()

    val allCategories = categories.getAll()
    val objectsWithCategory = objects.getAllWithCategory()
    val allClients = clients.getAll()
    val detailedSettlements = settlements.getAllDetailed()
    val allServices = services.getAll()
    fun items(settlementId: Int) = links.getItems(settlementId)

    suspend fun getCategory(id: Int) = categories.getById(id)
    suspend fun saveCategory(c: ComfortCategory) { if (c.id == 0) categories.insert(c) else categories.update(c) }
    suspend fun deleteCategory(c: ComfortCategory) = categories.delete(c)

    suspend fun getObject(id: Int) = objects.getById(id)
    suspend fun saveObject(o: AccommodationObject) { if (o.id == 0) objects.insert(o) else objects.update(o) }
    suspend fun deleteObject(o: AccommodationObject) = objects.delete(o)

    suspend fun getClient(id: Int) = clients.getById(id)
    suspend fun saveClient(c: Client) { if (c.id == 0) clients.insert(c) else clients.update(c) }
    suspend fun deleteClient(c: Client) = clients.delete(c)

    suspend fun getService(id: Int) = services.getById(id)
    suspend fun saveService(s: Service) { if (s.id == 0) services.insert(s) else services.update(s) }
    suspend fun deleteService(s: Service) = services.delete(s)

    suspend fun getSettlement(id: Int) = settlements.getById(id)
    suspend fun saveSettlement(s: Settlement) { if (s.id == 0) settlements.insert(s) else settlements.update(s) }
    suspend fun deleteSettlement(id: Int) = settlements.deleteById(id)

    suspend fun upsertItem(ss: SettlementService) = links.upsert(ss)
    suspend fun deleteItem(settlementId: Int, serviceId: Int) = links.deleteLink(settlementId, serviceId)

    /**
     * Аналог хранимой процедуры «Оформить заселение».
     * Заселение и все заказанные услуги сохраняются ОДНОЙ транзакцией.
     * Если триггер отклонит любую вставку (например, объект занят), откатывается всё.
     */
    suspend fun placeSettlement(s: Settlement, items: List<SettlementService>): Int = db.withTransaction {
        val id = settlements.insert(s.copy(id = 0)).toInt()
        items.forEach { links.insert(it.copy(idSettlement = id)) }
        id
    }
}
