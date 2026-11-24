@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.glamping.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ---------- форматирование и даты ----------
private val ru = Locale("ru")
const val DAY_MS = 86_400_000L
fun fmtDate(ms: Long): String = SimpleDateFormat("d MMM yyyy", ru).format(Date(ms))
fun fmtShort(ms: Long): String = SimpleDateFormat("d MMM", ru).format(Date(ms))
fun money(v: Double): String = String.format(ru, "%,.0f ₽", v)
fun initialsOf(fullName: String): String {
    val p = fullName.trim().split(" ")
    return "${p.getOrNull(0)?.firstOrNull() ?: ' '}${p.getOrNull(1)?.firstOrNull() ?: ' '}".uppercase()
}
fun dayStart(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis
/** DatePicker работает в UTC, а в БД даты хранятся по локальному времени. */
fun pickerToLocal(utcMs: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMs }
    return Calendar.getInstance().apply { clear(); set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH)) }.timeInMillis
}
fun localToPicker(ms: Long): Long {
    val l = Calendar.getInstance().apply { timeInMillis = ms }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH)) }.timeInMillis
}
fun nightsBetween(inMs: Long, outMs: Long): Int = Math.round((outMs - inMs) / DAY_MS.toDouble()).toInt()

val Statuses = listOf("Забронировано", "Заселён", "Выселен", "Отменён")
fun statusColor(s: String) = when (s) { "Забронировано" -> Amber; "Заселён" -> Green; "Выселен" -> Muted; "Отменён" -> Coral; else -> Muted }

// ---------- фото (BLOB) ----------
/** Читает картинку, уменьшает до 1200 px и сжимает в JPEG, чтобы BLOB в базе был небольшим. */
fun loadPhotoBytes(context: Context, uri: Uri): ByteArray? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1200 || bounds.outHeight / sample > 1200) sample *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
        out.toByteArray()
    } catch (e: Exception) { null }
}

@Composable
fun PhotoImage(bytes: ByteArray?, modifier: Modifier) {
    val bmp = remember(bytes) { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
    if (bmp != null) Image(bmp, null, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Soft), Alignment.Center) { Icon(Icons.Default.Hotel, null, tint = Muted, modifier = Modifier.size(40.dp)) }
}

// ---------- каркас экрана: синяя шапка + белый лист ----------
@Composable
fun AppScreen(
    title: String, vm: MainViewModel, subtitle: String? = null, onBack: (() -> Unit)? = null,
    fab: Pair<String, () -> Unit>? = null, content: @Composable ColumnScope.() -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    Box(Modifier.fillMaxSize().background(NavyGradient)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp, 8.dp, 20.dp, 16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White) }
                else Spacer(Modifier.width(12.dp))
                Column {
                    Text(title.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    if (subtitle != null) Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Color.White),
                content = content
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(8.dp))
        if (fab != null) PillFab(fab.first, fab.second, Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp))
    }
}

// ---------- элементы ----------
@Composable
fun CircleIcon(icon: ImageVector, color: Color, size: Dp = 44.dp) =
    Box(Modifier.size(size).clip(CircleShape).background(color), Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
    }

@Composable
fun CircleText(text: String, color: Color, size: Dp = 44.dp) =
    Box(Modifier.size(size).clip(CircleShape).background(color), Alignment.Center) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.34f).sp)
    }

@Composable
fun RowCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.fillMaxWidth().clip(shape).border(1.dp, Line, shape).background(Color.White)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, content = content
    )
}

@Composable
fun Tag(text: String, color: Color = Cyan) =
    Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp))

@Composable
fun PillTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit) = Row(
    Modifier.fillMaxWidth().padding(20.dp, 14.dp, 20.dp, 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    options.forEachIndexed { i, text ->
        val sel = i == selected
        Box(
            Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                .background(if (sel) CyanGradient else androidx.compose.ui.graphics.SolidColor(Soft), RoundedCornerShape(12.dp))
                .clickable { onSelect(i) }.padding(vertical = 10.dp), Alignment.Center
        ) { Text(text, color = if (sel) Color.White else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) = Text(
    text, color = if (selected) Color.White else Navy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) Navy else Soft).clickable(onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 8.dp)
)

@Composable
fun NavyButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) = Box(
    modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(12.dp)).background(NavyGradient).clickable(onClick = onClick),
    Alignment.Center
) { Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }

@Composable
fun SmallNavyButton(label: String, onClick: () -> Unit) = Text(
    label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(NavyGradient).clickable(onClick = onClick).padding(horizontal = 26.dp, vertical = 9.dp)
)

@Composable
fun PillFab(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) = Row(
    modifier.shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = CyanDark, spotColor = CyanDark).clip(RoundedCornerShape(16.dp))
        .background(CyanGradient).clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(Icons.Default.Add, null, tint = Color.White); Spacer(Modifier.width(8.dp))
    Text(label, color = Color.White, fontWeight = FontWeight.Bold)
}

// ---------- поля ввода (подпись над полем, как в примере) ----------
@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Cyan, unfocusedBorderColor = Line,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, cursorColor = Cyan,
    errorBorderColor = Coral, errorSupportingTextColor = Coral, errorContainerColor = Color.White
)

@Composable
private fun Label(text: String) = Text(text.uppercase(), color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp))

@Composable
fun SearchBar(value: String, onChange: (String) -> Unit, hint: String) = OutlinedTextField(
    value, onChange, placeholder = { Text(hint, color = Muted) }, singleLine = true,
    leadingIcon = { Icon(Icons.Default.Search, null, tint = Cyan) },
    trailingIcon = { if (value.isNotEmpty()) IconButton({ onChange("") }) { Icon(Icons.Default.Close, "Очистить") } },
    shape = RoundedCornerShape(12.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth().padding(20.dp, 16.dp, 20.dp, 4.dp)
)

@Composable
fun Field(value: String, onChange: (String) -> Unit, label: String, error: String?, keyboard: KeyboardType = KeyboardType.Text, singleLine: Boolean = true) =
    Column {
        Label(label)
        OutlinedTextField(
            value, onChange, isError = error != null, singleLine = singleLine, minLines = if (singleLine) 1 else 3,
            supportingText = { error?.let { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            shape = RoundedCornerShape(8.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth()
        )
    }

@Composable
fun Dropdown(label: String, options: List<Pair<Int, String>>, selectedId: Int?, error: String?, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Label(label)
        ExposedDropdownMenuBox(expanded, { expanded = it }) {
            OutlinedTextField(
                value = options.firstOrNull { it.first == selectedId }?.second ?: "", onValueChange = {}, readOnly = true,
                isError = error != null, supportingText = { error?.let { Text(it) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                shape = RoundedCornerShape(8.dp), colors = fieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                options.forEach { DropdownMenuItem({ Text(it.second) }, { onSelect(it.first); expanded = false }) }
            }
        }
    }
}

@Composable
fun DateField(label: String, millis: Long?, error: String?, onPick: (Long) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Column {
        Label(label)
        Box {
            OutlinedTextField(
                value = millis?.let { fmtDate(it) } ?: "", onValueChange = {}, readOnly = true,
                isError = error != null, supportingText = { error?.let { Text(it) } },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = Cyan) },
                shape = RoundedCornerShape(8.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth()
            )
            Box(Modifier.matchParentSize().clickable { show = true })
        }
    }
    if (show) {
        val st = rememberDatePickerState(initialSelectedDateMillis = millis?.let(::localToPicker))
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = { TextButton({ st.selectedDateMillis?.let { onPick(pickerToLocal(it)) }; show = false }) { Text("Готово") } },
            dismissButton = { TextButton({ show = false }) { Text("Отмена") } }
        ) { DatePicker(st) }
    }
}

@Composable
fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss, shape = RoundedCornerShape(20.dp), containerColor = Color.White,
    title = { Text("Удаление", fontWeight = FontWeight.Bold) }, text = { Text(text, color = Muted) },
    confirmButton = { TextButton(onConfirm) { Text("Удалить", color = Coral, fontWeight = FontWeight.Bold) } },
    dismissButton = { TextButton(onDismiss) { Text("Отмена") } }
)

@Composable
fun <T> ListBody(data: List<T>, key: (T) -> Any, empty: String, row: @Composable LazyItemScope.(T) -> Unit) {
    if (data.isEmpty()) Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(empty, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Измените запрос или добавьте запись кнопкой внизу", color = Muted, fontSize = 14.sp)
        }
    } else LazyColumn(
        contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) { items(data, key = key) { row(this, it) } }
}
