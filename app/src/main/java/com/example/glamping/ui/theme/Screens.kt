@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.glamping.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamping.data.*

// ======================= ПРИВЕТСТВИЕ =======================
@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Column(
            Modifier.weight(1.2f).fillMaxWidth().background(NavyGradient).statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Text("ДОБРО ПОЖАЛОВАТЬ!", color = Color.White, fontSize = 22.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(24.dp))
            Canvas(Modifier.size(210.dp)) { drawTent() }
            Spacer(Modifier.height(16.dp))
            Text("Глэмпинг", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.weight(0.8f).fillMaxWidth().navigationBarsPadding().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {
            Text("Учёт объектов размещения, клиентов, заселений и дополнительных услуг", color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            NavyButton("Начать работу", onStart)
        }
    }
}

private fun DrawScope.drawTent() {
    val w = size.width; val h = size.height
    drawLine(Color.White.copy(alpha = 0.5f), Offset(w * 0.02f, h * 0.88f), Offset(w * 0.98f, h * 0.88f), 4.dp.toPx(), StrokeCap.Round)
    drawPath(Path().apply { moveTo(w * 0.20f, h * 0.88f); lineTo(w * 0.5f, h * 0.20f); lineTo(w * 0.80f, h * 0.88f); close() }, Color.White)
    drawPath(Path().apply { moveTo(w * 0.41f, h * 0.88f); lineTo(w * 0.5f, h * 0.52f); lineTo(w * 0.59f, h * 0.88f); close() }, Cyan)
    drawLine(Color.White, Offset(w * 0.5f, h * 0.20f), Offset(w * 0.5f, h * 0.06f), 4.dp.toPx(), StrokeCap.Round)
    drawPath(Path().apply { moveTo(w * 0.5f, h * 0.06f); lineTo(w * 0.64f, h * 0.10f); lineTo(w * 0.5f, h * 0.15f); close() }, Orange)
    fun pine(x: Float, s: Float) {
        drawPath(Path().apply { moveTo(x, h * 0.88f - 70f * s); lineTo(x - 28f * s, h * 0.88f - 10f * s); lineTo(x + 28f * s, h * 0.88f - 10f * s); close() }, Color.White.copy(alpha = 0.85f))
        drawLine(Color.White.copy(alpha = 0.85f), Offset(x, h * 0.88f - 10f * s), Offset(x, h * 0.88f), 4.dp.toPx())
    }
    pine(w * 0.10f, 1.3f); pine(w * 0.90f, 1.6f); pine(w * 0.97f, 1.0f)
    drawCircle(Color.White.copy(alpha = 0.9f), 3.dp.toPx(), Offset(w * 0.18f, h * 0.20f))
    drawCircle(Color.White.copy(alpha = 0.9f), 2.dp.toPx(), Offset(w * 0.30f, h * 0.08f))
    drawCircle(Color.White.copy(alpha = 0.9f), 3.dp.toPx(), Offset(w * 0.82f, h * 0.22f))
}

// ======================= ГЛАВНАЯ =======================
@Composable
fun HomeScreen(vm: MainViewModel, nav: NavController) {
    val settlements by vm.settlements.collectAsState()
    val objects by vm.objects.collectAsState()
    val clients by vm.clients.collectAsState()
    val services by vm.services.collectAsState()
    val categories by vm.categories.collectAsState()
    val staying = remember(settlements) { settlements.count { it.status == "Заселён" } }
    val revenue = remember(settlements) { settlements.filter { it.status == "Выселен" }.sumOf { it.totalCost } }
    val recent = remember(settlements) { settlements.take(4) }

    Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(NavyGradient).statusBarsPadding().padding(20.dp, 16.dp, 20.dp, 52.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(Color.White), Alignment.Center) { Icon(Icons.Default.Hotel, null, tint = Navy) }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Глэмпинг", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("панель администратора", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ВЫРУЧКА (ВЫЕХАВШИЕ ГОСТИ)", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                Text(money(revenue), color = Navy, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Сейчас проживают: $staying", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                SmallNavyButton("Все заселения") { nav.navigate("settlements") }
            }
        }

        Column(
            Modifier.offset(y = (-24).dp).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White).padding(20.dp, 24.dp, 20.dp, 8.dp).navigationBarsPadding()
        ) {
            Text("ПОСЛЕДНИЕ ЗАСЕЛЕНИЯ", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(8.dp))
            if (recent.isEmpty()) Text("Заселений пока нет", color = Muted)
            recent.forEach { s ->
                Row(
                    Modifier.fillMaxWidth().clickable { nav.navigate("settlement_edit/${s.id}") }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleText(initialsOf(s.clientName), statusColor(s.status), 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.clientName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${s.objectTitle}, ${fmtShort(s.checkInDate)} – ${fmtShort(s.checkOutDate)}", color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(money(s.totalCost), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(s.status, color = statusColor(s.status), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                HorizontalDivider(color = Line)
            }
            Text("ещё >>", color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                modifier = Modifier.align(Alignment.End).clickable { nav.navigate("settlements") }.padding(vertical = 10.dp))

            Spacer(Modifier.height(10.dp))
            Text("РАЗДЕЛЫ", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                SectionTile("Объекты", objects.size, Icons.Default.Hotel, Cyan, Modifier.weight(1f)) { nav.navigate("objects") }
                SectionTile("Клиенты", clients.size, Icons.Default.Groups, Orange, Modifier.weight(1f)) { nav.navigate("clients") }
                SectionTile("Заселения", settlements.size, Icons.Default.CalendarMonth, Coral, Modifier.weight(1f)) { nav.navigate("settlements") }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                SectionTile("Услуги", services.size, Icons.Default.RoomService, Pink, Modifier.weight(1f)) { nav.navigate("services") }
                SectionTile("Категории", categories.size, Icons.Default.Star, Purple, Modifier.weight(1f)) { nav.navigate("categories") }
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTile(title: String, count: Int, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) =
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        CircleIcon(icon, color, 68.dp)
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("$count", color = Muted, fontSize = 12.sp)
    }

// ======================= ЗАСЕЛЕНИЯ =======================
@Composable
fun SettlementsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.settlements.collectAsState()
    var q by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val tabs = listOf("Все", "Бронь", "Проживают", "Выехали")
    val tabStatus = listOf(null, "Забронировано", "Заселён", "Выселен")
    val list = remember(all, q, tab) {
        all.filter { s ->
            (tabStatus[tab] == null || s.status == tabStatus[tab]) &&
                (q.isBlank() || "${s.clientName} ${s.objectTitle} ${s.status}".contains(q.trim(), ignoreCase = true))
        }
    }
    AppScreen("Заселения", vm, "${all.size} в базе", { nav.popBackStack() }, "Новое заселение" to { nav.navigate("settlement_edit/0") }) {
        PillTabs(tabs, tab) { tab = it }
        SearchBar(q, { q = it }, "Клиент, объект или статус")
        ListBody(list, { it.id }, if (all.isEmpty()) "Заселений пока нет" else "Ничего не найдено") { s ->
            RowCard(Modifier.animateItem(), onClick = { nav.navigate("settlement_edit/${s.id}") }) {
                CircleText(initialsOf(s.clientName), statusColor(s.status), 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.clientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(s.objectTitle, color = Muted, fontSize = 13.sp)
                    Text("${fmtShort(s.checkInDate)} – ${fmtShort(s.checkOutDate)} (${s.nights} ноч.)", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Tag(s.status, statusColor(s.status))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(money(s.totalCost), color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton({ pending = { vm.deleteSettlement(s.id) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить заселение? Заказанные услуги тоже будут удалены.", { action(); pending = null }, { pending = null }) }
}

// ======================= ОБЪЕКТЫ =======================
@Composable
fun ObjectsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.objects.collectAsState()
    val categories by vm.categories.collectAsState()
    var q by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf<Int?>(null) }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q, cat) {
        all.filter { o ->
            (cat == null || o.obj.idComfortCategory == cat) &&
                (q.isBlank() || "${o.obj.objectType} ${o.categoryName} ${o.obj.description}".contains(q.trim(), ignoreCase = true))
        }
    }
    AppScreen("Объекты", vm, "${all.size} в базе", { nav.popBackStack() }, "Новый объект" to { nav.navigate("object_edit/0") }) {
        SearchBar(q, { q = it }, "Тип, категория или описание")
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(20.dp, 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill("Все", cat == null) { cat = null }
            categories.forEach { c -> FilterPill(c.categoryName, cat == c.id) { cat = c.id } }
        }
        ListBody(list, { it.obj.id }, if (all.isEmpty()) "Объектов пока нет" else "Ничего не найдено") { item ->
            val o = item.obj
            val shape = RoundedCornerShape(18.dp)
            Column(
                Modifier.animateItem().fillMaxWidth().clip(shape).border(1.dp, Line, shape).background(Color.White)
                    .clickable { nav.navigate("object_edit/${o.id}") }
            ) {
                Box {
                    PhotoImage(o.photo, Modifier.fillMaxWidth().height(170.dp))
                    Text(item.categoryName, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 4.dp))
                    Text("${money(o.pricePerNight)} / ночь", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(8.dp)).background(CyanGradient).padding(horizontal = 10.dp, vertical = 4.dp))
                }
                Column(Modifier.padding(14.dp, 10.dp, 6.dp, 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${o.objectType} №${o.id}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Вместимость: ${o.capacity} чел.", color = Muted, fontSize = 13.sp)
                        }
                        IconButton({ pending = { vm.deleteObject(o) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                    }
                    Text(o.description, color = Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 8.dp))
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить объект? Если он есть в заселениях, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}

// ======================= КЛИЕНТЫ =======================
@Composable
fun ClientsScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.clients.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) {
        all.filter { q.isBlank() || "${it.lastName} ${it.firstName} ${it.middleName} ${it.phone} ${it.passport}".contains(q.trim(), ignoreCase = true) }
    }
    AppScreen("Клиенты", vm, "${all.size} в базе", { nav.popBackStack() }, "Новый клиент" to { nav.navigate("client_edit/0") }) {
        SearchBar(q, { q = it }, "ФИО, телефон или паспорт")
        ListBody(list, { it.id }, if (all.isEmpty()) "Клиентов пока нет" else "Ничего не найдено") { c ->
            RowCard(Modifier.animateItem(), onClick = { nav.navigate("client_edit/${c.id}") }) {
                CircleText(initialsOf("${c.lastName} ${c.firstName}"), Orange, 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("${c.lastName} ${c.firstName} ${c.middleName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(c.phone, color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Паспорт: ${c.passport}", color = Muted, fontSize = 12.sp)
                }
                IconButton({ pending = { vm.deleteClient(c) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить клиента? Если у него есть заселения, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}

// ======================= УСЛУГИ =======================
@Composable
fun ServicesScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.services.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) { all.filter { q.isBlank() || "${it.serviceName} ${it.description}".contains(q.trim(), ignoreCase = true) } }
    val colors = listOf(Cyan, Orange, Coral, Pink, Green, Purple, Teal)
    AppScreen("Услуги", vm, "${all.size} в прайсе", { nav.popBackStack() }, "Новая услуга" to { nav.navigate("service_edit/0") }) {
        SearchBar(q, { q = it }, "Название или описание")
        ListBody(list, { it.id }, if (all.isEmpty()) "Услуг пока нет" else "Ничего не найдено") { s ->
            RowCard(Modifier.animateItem(), onClick = { nav.navigate("service_edit/${s.id}") }) {
                CircleIcon(Icons.Default.RoomService, colors[s.id % colors.size], 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(s.description, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(money(s.price), color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton({ pending = { vm.deleteService(s) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
                }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить услугу? Если она есть в заселениях, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}

// ======================= КАТЕГОРИИ КОМФОРТА =======================
@Composable
fun CategoriesScreen(vm: MainViewModel, nav: NavController) {
    val all by vm.categories.collectAsState()
    val objects by vm.objects.collectAsState()
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val list = remember(all, q) { all.filter { q.isBlank() || it.categoryName.contains(q.trim(), ignoreCase = true) } }
    AppScreen("Категории", vm, "${all.size} в справочнике", { nav.popBackStack() }, "Новая категория" to { nav.navigate("category_edit/0") }) {
        SearchBar(q, { q = it }, "Название категории")
        ListBody(list, { it.id }, if (all.isEmpty()) "Категорий пока нет" else "Ничего не найдено") { c ->
            val count = objects.count { it.obj.idComfortCategory == c.id }
            RowCard(Modifier.animateItem(), onClick = { nav.navigate("category_edit/${c.id}") }) {
                CircleIcon(Icons.Default.Star, Purple, 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Объектов: $count", color = Muted, fontSize = 13.sp)
                }
                IconButton({ pending = { vm.deleteCategory(c) } }) { Icon(Icons.Default.Delete, "Удалить", tint = Coral) }
            }
        }
    }
    pending?.let { action -> ConfirmDialog("Удалить категорию? Если к ней относятся объекты, удаление будет отклонено.", { action(); pending = null }, { pending = null }) }
}
