package com.example.glamping.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.glamping.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(AppDatabase.get(app))
    private fun <T> Flow<T>.state(init: T) = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), init)

    val messages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val categories = repo.allCategories.state(emptyList())
    val objects = repo.objectsWithCategory.state(emptyList())
    val clients = repo.allClients.state(emptyList())
    val settlements = repo.detailedSettlements.state(emptyList())
    val services = repo.allServices.state(emptyList())
    fun items(settlementId: Int) = repo.items(settlementId)

    suspend fun getCategory(id: Int) = repo.getCategory(id)
    suspend fun getObject(id: Int) = repo.getObject(id)
    suspend fun getClient(id: Int) = repo.getClient(id)
    suspend fun getService(id: Int) = repo.getService(id)
    suspend fun getSettlement(id: Int) = repo.getSettlement(id)

    private fun errorText(e: Exception): String {
        val m = e.message.orEmpty()
        return when {
            "уже занят" in m -> "Объект уже занят на выбранные даты"
            "Дата выезда" in m -> "Дата выезда должна быть позже даты заезда"
            "Количество должно" in m -> "Количество должно быть больше нуля"
            "Цена не может" in m -> "Цена не может быть отрицательной"
            "Вместимость" in m -> "Вместимость должна быть больше нуля"
            else -> "Операция невозможна: запись связана с другими данными"
        }
    }
    private fun safe(block: suspend () -> Unit) = viewModelScope.launch {
        try { block() } catch (e: Exception) { messages.emit(errorText(e)) }
    }
    private suspend fun attempt(block: suspend () -> Unit): Boolean =
        try { block(); true } catch (e: Exception) { messages.emit(errorText(e)); false }

    /** «Оформить заселение»: заселение + услуги одной транзакцией. */
    suspend fun placeSettlement(s: Settlement, items: List<SettlementService>) = attempt { repo.placeSettlement(s, items) }
    suspend fun saveSettlement(s: Settlement) = attempt { repo.saveSettlement(s) }
    suspend fun saveObject(o: AccommodationObject) = attempt { repo.saveObject(o) }
    fun deleteSettlement(id: Int) = safe { repo.deleteSettlement(id) }
    fun upsertItem(ss: SettlementService) = safe { repo.upsertItem(ss) }
    fun deleteItem(settlementId: Int, serviceId: Int) = safe { repo.deleteItem(settlementId, serviceId) }

    fun deleteObject(o: AccommodationObject) = safe { repo.deleteObject(o) }
    fun saveClient(c: Client) = safe { repo.saveClient(c) }
    fun deleteClient(c: Client) = safe { repo.deleteClient(c) }
    fun saveService(s: Service) = safe { repo.saveService(s) }
    fun deleteService(s: Service) = safe { repo.deleteService(s) }
    fun saveCategory(c: ComfortCategory) = safe { repo.saveCategory(c) }
    fun deleteCategory(c: ComfortCategory) = safe { repo.deleteCategory(c) }
}
