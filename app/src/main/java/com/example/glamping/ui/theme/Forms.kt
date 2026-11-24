@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.glamping.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamping.data.*
import kotlinx.coroutines.launch

private const val REQ = "Обязательное поле"
private fun String.phoneOk() = count { it.isDigit() } >= 10
private fun String.toMoney() = replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }

@Composable
private fun FormScreen(title: String, vm: MainViewModel, nav: NavController, content: @Composable ColumnScope.() -> Unit) =
    AppScreen(title, vm, onBack = { nav.popBackStack() }) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp), content = content
        )
    }

// ======================= КЛИЕНТ =======================
@Composable
fun ClientFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var last by remember { mutableStateOf("") }; var first by remember { mutableStateOf("") }
    var middle by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var passport by remember { mutableStateOf("") }; var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getClient(id)?.let { last = it.lastName; first = it.firstName; middle = it.middleName; phone = it.phone; passport = it.passport }
    }
    val passportOk = passport.count { it.isDigit() } == 10
    FormScreen(if (id == 0) "Новый клиент" else "Клиент", vm, nav) {
        Field(last, { last = it }, "Фамилия", if (tried && last.isBlank()) REQ else null)
        Field(first, { first = it }, "Имя", if (tried && first.isBlank()) REQ else null)
        Field(middle, { middle = it }, "Отчество", null)
        Field(phone, { phone = it }, "Телефон", if (tried && !phone.phoneOk()) "Минимум 10 цифр" else null, KeyboardType.Phone)
        Field(passport, { passport = it }, "Паспорт (серия и номер)", if (tried && !passportOk) "Ровно 10 цифр" else null, KeyboardType.Number)
        NavyButton("Сохранить", {
            tried = true
            if (last.isNotBlank() && first.isNotBlank() && phone.phoneOk() && passportOk) {
                vm.saveClient(Client(id, last.trim(), first.trim(), middle.trim(), phone.trim(), passport.trim()))
                nav.popBackStack()
            }
        }, Modifier.padding(top = 12.dp))
    }
}

// ======================= УСЛУГА =======================
@Composable
fun ServiceFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var name by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }; var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) { if (id != 0) vm.getService(id)?.let { name = it.serviceName; desc = it.description; price = it.price.toString() } }
    val priceVal = price.toMoney()
    FormScreen(if (id == 0) "Новая услуга" else "Услуга", vm, nav) {
        Field(name, { name = it }, "Название", if (tried && name.isBlank()) REQ else null)
        Field(desc, { desc = it }, "Описание", null, singleLine = false)
        Field(price, { price = it }, "Цена, ₽", if (tried && priceVal == null) "Введите число ≥ 0" else null, KeyboardType.Decimal)
        NavyButton("Сохранить", {
            tried = true
            if (name.isNotBlank() && priceVal != null) { vm.saveService(Service(id, name.trim(), desc.trim(), priceVal)); nav.popBackStack() }
        }, Modifier.padding(top = 12.dp))
    }
}

// ======================= КАТЕГОРИЯ =======================
@Composable
fun CategoryFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    var name by remember { mutableStateOf("") }; var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) { if (id != 0) vm.getCategory(id)?.let { name = it.categoryName } }
    FormScreen(if (id == 0) "Новая категория" else "Категория", vm, nav) {
        Field(name, { name = it }, "Название категории", if (tried && name.isBlank()) REQ else null)
        NavyButton("Сохранить", {
            tried = true
            if (name.isNotBlank()) { vm.saveCategory(ComfortCategory(id, name.trim())); nav.popBackStack() }
        }, Modifier.padding(top = 12.dp))
    }
}

// ======================= ОБЪЕКТ (с фото) =======================
@Composable
fun ObjectFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val categories by vm.categories.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var type by remember { mutableStateOf("") }; var capacity by remember { mutableStateOf("") }
    var catId by remember { mutableStateOf<Int?>(null) }; var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }; var photo by remember { mutableStateOf<ByteArray?>(null) }
    var tried by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (id != 0) vm.getObject(id)?.let {
            type = it.objectType; capacity = it.capacity.toString(); catId = it.idComfortCategory
            price = it.pricePerNight.toString(); desc = it.description; photo = it.photo
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) loadPhotoBytes(context, uri)?.let { photo = it }
    }
    val capVal = capacity.toIntOrNull()?.takeIf { it in 1..50 }
    val priceVal = price.toMoney()
    FormScreen(if (id == 0) "Новый объект" else "Объект", vm, nav) {
        PhotoImage(photo, Modifier.fillMaxWidth().height(180.dp).padding(bottom = 4.dp).then(Modifier))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton({ picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shape = RoundedCornerShape(10.dp)) { Text("Выбрать фото") }
            if (photo != null) TextButton({ photo = null }) { Text("Убрать", color = Coral) }
        }
        Field(type, { type = it }, "Тип объекта", if (tried && type.isBlank()) REQ else null)
        Dropdown("Категория комфорта", categories.map { it.id to it.categoryName }, catId, if (tried && catId == null) "Выберите категорию" else null) { catId = it }
        Field(capacity, { capacity = it }, "Вместимость, человек", if (tried && capVal == null) "Целое число 1–50" else null, KeyboardType.Number)
        Field(price, { price = it }, "Цена за ночь, ₽", if (tried && priceVal == null) "Введите число ≥ 0" else null, KeyboardType.Decimal)
        Field(desc, { desc = it }, "Описание", null, singleLine = false)
        NavyButton("Сохранить", {
            tried = true
            if (type.isNotBlank() && catId != null && capVal != null && priceVal != null) {
                scope.launch {
                    val ok = vm.saveObject(AccommodationObject(id, type.trim(), capVal, catId!!, priceVal, desc.trim(), photo))
                    if (ok) nav.popBackStack()
                }
            }
        }, Modifier.padding(top = 12.dp))
    }
}

// ======================= ЗАСЕЛЕНИЕ =======================
@Composable
fun SettlementFormScreen(vm: MainViewModel, nav: NavController, id: Int) {
    val clients by vm.clients.collectAsState()
    val objects by vm.objects.collectAsState()
    val services by vm.services.collectAsState()
    val items by remember(id) { vm.items(id) }.collectAsState(emptyList())
    var clientId by remember { mutableStateOf<Int?>(null) }
    var objectId by remember { mutableStateOf<Int?>(null) }
    var checkIn by remember { mutableStateOf<Long?>(null) }
    var checkOut by remember { mutableStateOf<Long?>(null) }
    var status by remember { mutableStateOf<Int?>(0) }
    var tried by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var addItem by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<SettlementItem?>(null) }
    val draft = remember { mutableStateListOf<Pair<Int, Int>>() }   // (id услуги, количество) нового заселения
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        if (id != 0) vm.getSettlement(id)?.let {
            clientId = it.idClient; objectId = it.idAccommodationObject; checkIn = it.checkInDate; checkOut = it.checkOutDate
            status = Statuses.indexOf(it.status).coerceAtLeast(0)
        } else { val t = dayStart(System.currentTimeMillis()); checkIn = t; checkOut = t + DAY_MS }
    }
    val nights = if (checkIn != null && checkOut != null) nightsBetween(checkIn!!, checkOut!!) else 0
    val objPrice = objects.firstOrNull { it.obj.id == objectId }?.obj?.pricePerNight ?: 0.0
    val servicesTotal = if (id == 0) draft.sumOf { (sid, q) -> (services.firstOrNull { it.id == sid }?.price ?: 0.0) * q }
    else items.sumOf { it.price * it.countService }
    val total = maxOf(nights, 0) * objPrice + servicesTotal

    FormScreen(if (id == 0) "Новое заселение" else "Заселение", vm, nav) {
        Dropdown("Клиент", clients.map { it.id to "${it.lastName} ${it.firstName}" }, clientId, if (tried && clientId == null) "Выберите клиента" else null) { clientId = it }
        Dropdown("Объект", objects.map { it.obj.id to "${it.obj.objectType} №${it.obj.id}, до ${it.obj.capacity} чел., ${money(it.obj.pricePerNight)}" }, objectId, if (tried && objectId == null) "Выберите объект" else null) { objectId = it }
        DateField("Дата заезда", checkIn, if (tried && checkIn == null) REQ else null) { checkIn = it }
        DateField("Дата выезда", checkOut, if (tried && checkOut == null) REQ else if (tried && nights <= 0) "Позже даты заезда" else null) { checkOut = it }
        Dropdown("Статус", Statuses.mapIndexed { i, s -> i to s }, status, null) { status = it }

        Spacer(Modifier.height(10.dp))
        Text("ДОПОЛНИТЕЛЬНЫЕ УСЛУГИ", color = Navy, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        if (id == 0) {
            draft.forEachIndexed { index, (sid, q) ->
                val s = services.firstOrNull { it.id == sid }
                RowCard(Modifier.padding(bottom = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(s?.serviceName ?: "—", fontWeight = FontWeight.SemiBold)
                        Text("$q × ${money(s?.price ?: 0.0)}", color = Muted, fontSize = 13.sp)
                    }
                    IconButton({ draft.removeAt(index) }) { Icon(Icons.Default.Delete, "Убрать", tint = Coral) }
                }
            }
        } else {
            items.forEach { it ->
                RowCard(Modifier.padding(bottom = 8.dp), onClick = { editItem = it }) {
                    Column(Modifier.weight(1f)) {
                        Text(it.serviceName, fontWeight = FontWeight.SemiBold)
                        Text("${it.countService} × ${money(it.price)}", color = Muted, fontSize = 13.sp)
                    }
                    IconButton({ vm.deleteItem(id, it.idService) }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
        OutlinedButton({ addItem = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) { Text("Добавить услугу") }

        Spacer(Modifier.height(8.dp))
        Text("Ночей: ${maxOf(nights, 0)}", color = Muted, fontSize = 13.sp)
        Text("Итого: ${money(total)}", color = Navy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            if (id == 0) "Заселение и услуги сохраняются одной транзакцией. Занятость объекта проверяет триггер БД."
            else "Изменения услуг и дат проверяются триггерами БД.",
            color = Muted, fontSize = 12.sp
        )

        NavyButton(if (id == 0) "Оформить заселение" else "Сохранить", {
            tried = true
            if (clientId != null && objectId != null && checkIn != null && checkOut != null && nights > 0 && !saving) {
                val s = Settlement(id, clientId!!, objectId!!, checkIn!!, checkOut!!, Statuses[status ?: 0])
                saving = true
                scope.launch {
                    val ok = if (id == 0) vm.placeSettlement(s, draft.map { SettlementService(0, it.first, it.second) }) else vm.saveSettlement(s)
                    saving = false
                    if (ok) nav.popBackStack()
                }
            }
        }, Modifier.padding(top = 8.dp))
    }

    if (addItem || editItem != null) {
        ServiceDialog(services, editItem, onDismiss = { addItem = false; editItem = null }) { serviceId, qty ->
            if (id == 0) {
                val i = draft.indexOfFirst { it.first == serviceId }
                if (i >= 0) draft[i] = serviceId to (draft[i].second + qty) else draft.add(serviceId to qty)
            } else {
                val current = items.firstOrNull { it.idService == serviceId }?.countService ?: 0
                val newCount = if (editItem != null) qty else current + qty
                vm.upsertItem(SettlementService(id, serviceId, newCount))
            }
            addItem = false; editItem = null
        }
    }
}

@Composable
private fun ServiceDialog(services: List<Service>, initial: SettlementItem?, onDismiss: () -> Unit, onSave: (Int, Int) -> Unit) {
    var serviceId by remember { mutableStateOf(initial?.idService) }
    var qty by remember { mutableStateOf(initial?.countService?.toString() ?: "1") }
    var tried by remember { mutableStateOf(false) }
    val q = qty.toIntOrNull()?.takeIf { it > 0 }
    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(20.dp), containerColor = Color.White,
        title = { Text(if (initial == null) "Добавить услугу" else initial.serviceName, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (initial == null) Dropdown("Услуга", services.map { it.id to "${it.serviceName}, ${money(it.price)}" }, serviceId, if (tried && serviceId == null) "Выберите услугу" else null) { serviceId = it }
                Field(qty, { qty = it }, "Количество", if (tried && q == null) "Целое число > 0" else null, KeyboardType.Number)
            }
        },
        confirmButton = { TextButton({ tried = true; if (serviceId != null && q != null) onSave(serviceId!!, q) }) { Text("Сохранить", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
    )
}
