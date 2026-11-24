package com.example.glamping.data

import android.content.Context
import android.graphics.*
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.ByteArrayOutputStream
import java.util.Calendar

@Database(
    entities = [ComfortCategory::class, AccommodationObject::class, Client::class, Settlement::class, Service::class, SettlementService::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun objectDao(): ObjectDao
    abstract fun clientDao(): ClientDao
    abstract fun settlementDao(): SettlementDao
    abstract fun serviceDao(): ServiceDao
    abstract fun settlementServiceDao(): SettlementServiceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "glamping.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) { super.onCreate(db); seed(db) }          // тестовые данные
                    override fun onOpen(db: SupportSQLiteDatabase) { super.onOpen(db); TRIGGERS.forEach { db.execSQL(it) } } // триггеры
                })
                .build().also { INSTANCE = it }
        }
    }
}

/** Триггеры целостности данных. */
private val TRIGGERS = listOf(
    // 1-2. Дата выезда позже даты заезда
    """CREATE TRIGGER IF NOT EXISTS trg_settle_dates_insert BEFORE INSERT ON settlements
    BEGIN
        SELECT RAISE(ABORT, 'Дата выезда должна быть позже даты заезда') WHERE NEW.checkOutDate <= NEW.checkInDate;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_settle_dates_update BEFORE UPDATE ON settlements
    BEGIN
        SELECT RAISE(ABORT, 'Дата выезда должна быть позже даты заезда') WHERE NEW.checkOutDate <= NEW.checkInDate;
    END""",
    // 3-4. Объект нельзя заселить дважды на пересекающиеся даты
    """CREATE TRIGGER IF NOT EXISTS trg_settle_overlap_insert BEFORE INSERT ON settlements
    WHEN NEW.status IN ('Забронировано', 'Заселён')
    BEGIN
        SELECT RAISE(ABORT, 'Объект уже занят на выбранные даты')
        WHERE EXISTS (SELECT 1 FROM settlements WHERE idAccommodationObject = NEW.idAccommodationObject
            AND status IN ('Забронировано', 'Заселён')
            AND NEW.checkInDate < checkOutDate AND NEW.checkOutDate > checkInDate);
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_settle_overlap_update BEFORE UPDATE ON settlements
    WHEN NEW.status IN ('Забронировано', 'Заселён')
    BEGIN
        SELECT RAISE(ABORT, 'Объект уже занят на выбранные даты')
        WHERE EXISTS (SELECT 1 FROM settlements WHERE id <> NEW.id AND idAccommodationObject = NEW.idAccommodationObject
            AND status IN ('Забронировано', 'Заселён')
            AND NEW.checkInDate < checkOutDate AND NEW.checkOutDate > checkInDate);
    END""",
    // 5-6. Количество услуги больше нуля
    """CREATE TRIGGER IF NOT EXISTS trg_ss_count_insert BEFORE INSERT ON settlement_services
    BEGIN
        SELECT RAISE(ABORT, 'Количество должно быть больше нуля') WHERE NEW.countService <= 0;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_ss_count_update BEFORE UPDATE ON settlement_services
    BEGIN
        SELECT RAISE(ABORT, 'Количество должно быть больше нуля') WHERE NEW.countService <= 0;
    END""",
    // 7-8. Цена и вместимость объекта
    """CREATE TRIGGER IF NOT EXISTS trg_object_check_insert BEFORE INSERT ON accommodation_objects
    BEGIN
        SELECT RAISE(ABORT, 'Цена не может быть отрицательной') WHERE NEW.pricePerNight < 0;
        SELECT RAISE(ABORT, 'Вместимость должна быть больше нуля') WHERE NEW.capacity <= 0;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_object_check_update BEFORE UPDATE ON accommodation_objects
    BEGIN
        SELECT RAISE(ABORT, 'Цена не может быть отрицательной') WHERE NEW.pricePerNight < 0;
        SELECT RAISE(ABORT, 'Вместимость должна быть больше нуля') WHERE NEW.capacity <= 0;
    END""",
    // 9-10. Цена услуги
    """CREATE TRIGGER IF NOT EXISTS trg_service_price_insert BEFORE INSERT ON services
    BEGIN
        SELECT RAISE(ABORT, 'Цена не может быть отрицательной') WHERE NEW.price < 0;
    END""",
    """CREATE TRIGGER IF NOT EXISTS trg_service_price_update BEFORE UPDATE ON services
    BEGIN
        SELECT RAISE(ABORT, 'Цена не может быть отрицательной') WHERE NEW.price < 0;
    END"""
)

private fun date(y: Int, m: Int, d: Int): Long = Calendar.getInstance().apply { clear(); set(y, m - 1, d) }.timeInMillis

/** Рисует простую «фотографию» объекта (небо, холмы, палатка), чтобы в базе были BLOB-изображения. */
private fun placeholderPhoto(i: Int): ByteArray {
    val w = 640; val h = 400
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val skies = listOf(
        0xFF1B2A5C to 0xFFF6A56B, 0xFF0F3D5E to 0xFF7FD1C7, 0xFF3B1F5C to 0xFFF08A8A,
        0xFF1F4E3D to 0xFFB8E0A0, 0xFF16324F to 0xFF9CC7F5
    )
    val (top, bottom) = skies[i % skies.size]
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), top.toInt(), bottom.toInt(), Shader.TileMode.CLAMP)
    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    p.shader = null
    p.color = 0xFFFFF3C4.toInt()
    c.drawCircle(w * (0.2f + 0.08f * (i % 5)), h * 0.24f, 34f, p)
    p.color = 0xFF14342B.toInt()
    val hill = Path().apply {
        moveTo(0f, h * 0.78f); quadTo(w * 0.3f, h * 0.52f, w * 0.6f, h * 0.76f)
        quadTo(w * 0.85f, h * 0.60f, w.toFloat(), h * 0.74f); lineTo(w.toFloat(), h.toFloat()); lineTo(0f, h.toFloat()); close()
    }
    c.drawPath(hill, p)
    val cx = w * 0.5f
    val base = h * 0.80f
    p.color = 0xFFF4E1C1.toInt()
    if (i % 2 == 0) c.drawArc(RectF(cx - 120f, base - 120f, cx + 120f, base + 120f), 180f, 180f, true, p)
    else c.drawPath(Path().apply { moveTo(cx - 130f, base); lineTo(cx, base - 150f); lineTo(cx + 130f, base); close() }, p)
    p.color = 0xFFFFC857.toInt()
    c.drawRoundRect(RectF(cx - 22f, base - 62f, cx + 22f, base), 8f, 8f, p)
    p.color = 0xFF0E241E.toInt()
    c.drawRect(0f, base, w.toFloat(), h.toFloat(), p)
    val out = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
    return out.toByteArray()
}

private fun seed(db: SupportSQLiteDatabase) {
    val day = 24L * 60 * 60 * 1000
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val cats = listOf("Эконом", "Стандарт", "Комфорт", "Комфорт+", "Бизнес", "Премиум", "Романтик", "Семейный", "Люкс", "Эксклюзив")
    cats.forEachIndexed { i, n -> db.execSQL("INSERT INTO comfort_categories(id, categoryName) VALUES (?,?)", arrayOf(i + 1, n)) }

    // тип, вместимость, категория, цена за ночь, описание
    val objects = listOf(
        listOf("Сферический купол", 2, 4, 9500.0, "Прозрачный купол с видом на звёздное небо, кровать king-size, мини-бар"),
        listOf("A-frame домик", 4, 3, 8200.0, "Двухуровневый домик с панорамным окном, террасой и мангальной зоной"),
        listOf("Юрта", 6, 2, 5400.0, "Просторная юрта с печкой, тремя кроватями и обеденной зоной"),
        listOf("Домик на дереве", 2, 7, 14500.0, "Романтичный домик среди сосен, джакузи на террасе, завтрак в номер"),
        listOf("Шале у озера", 6, 6, 16800.0, "Двухэтажное шале в 20 метрах от воды, камин, сауна и собственный причал"),
        listOf("Бабл-тент", 2, 5, 11000.0, "Прозрачная палатка для двоих с отоплением и ночным небом над головой"),
        listOf("Эко-бунгало", 3, 2, 4800.0, "Уютное бунгало из дерева с душем и небольшой кухней"),
        listOf("Лесная хижина", 4, 1, 3900.0, "Простая хижина в лесу для тех, кто ценит тишину и природу"),
        listOf("Панорамный купол", 4, 8, 12400.0, "Семейный купол с двумя спальнями и панорамным видом на долину"),
        listOf("Тент-люкс", 2, 10, 18000.0, "Эксклюзивный тент с личным шеф-поваром, купелью и вечерним костром")
    )
    objects.forEachIndexed { i, o ->
        db.execSQL(
            "INSERT INTO accommodation_objects(id, objectType, capacity, idComfortCategory, pricePerNight, description, photo) VALUES (?,?,?,?,?,?,?)",
            arrayOf(i + 1, o[0], o[1], o[2], o[3], o[4], placeholderPhoto(i))
        )
    }

    // фамилия, имя, отчество, телефон, паспорт
    val clients = listOf(
        listOf("Смирнов", "Алексей", "Петрович", "+79105000001", "4510 123401"),
        listOf("Кузнецова", "Мария", "Ивановна", "+79105000002", "4511 123402"),
        listOf("Попов", "Дмитрий", "Сергеевич", "+79105000003", "4512 123403"),
        listOf("Соколова", "Анна", "Викторовна", "+79105000004", "4513 123404"),
        listOf("Михайлов", "Игорь", "Олегович", "+79105000005", "4514 123405"),
        listOf("Новикова", "Екатерина", "Андреевна", "+79105000006", "4515 123406"),
        listOf("Фёдоров", "Павел", "Николаевич", "+79105000007", "4516 123407"),
        listOf("Морозова", "Ольга", "Дмитриевна", "+79105000008", "4517 123408"),
        listOf("Волков", "Артём", "Александрович", "+79105000009", "4518 123409"),
        listOf("Алексеева", "Дарья", "Сергеевна", "+79105000010", "4519 123410")
    )
    clients.forEachIndexed { i, c ->
        db.execSQL("INSERT INTO clients(id, lastName, firstName, middleName, phone, passport) VALUES (?,?,?,?,?,?)", arrayOf(i + 1, c[0], c[1], c[2], c[3], c[4]))
    }

    val services = listOf(
        listOf("Баня на дровах", "Русская баня с вениками и травяным чаем, 2 часа", 2500.0),
        listOf("Завтрак", "Фермерский завтрак на террасе", 600.0),
        listOf("Трансфер от станции", "Встреча на станции и доставка до глэмпинга", 1500.0),
        listOf("Мангальная зона", "Аренда мангала, решётки и дров", 800.0),
        listOf("Прокат велосипедов", "Горный велосипед на весь день", 700.0),
        listOf("Экскурсия по лесу", "Прогулка с гидом по экотропе", 1200.0),
        listOf("Чан с горячей водой", "Купель на дровах под открытым небом", 3500.0),
        listOf("Фотосессия", "Часовая фотосессия на природе", 4500.0),
        listOf("Рыбалка", "Снасти и наживка, место на озере", 1800.0),
        listOf("Романтический ужин", "Ужин для двоих при свечах", 5000.0)
    )
    services.forEachIndexed { i, s -> db.execSQL("INSERT INTO services(id, serviceName, description, price) VALUES (?,?,?,?)", arrayOf(i + 1, s[0], s[1], s[2])) }

    // клиент, объект, заезд (смещение, дней), выезд, статус
    val settlements = listOf(
        listOf(1, 1, -2, 1, "Заселён"), listOf(2, 2, 0, 3, "Заселён"), listOf(3, 3, 2, 5, "Забронировано"),
        listOf(4, 4, 5, 7, "Забронировано"), listOf(5, 5, -6, -3, "Выселен"), listOf(6, 6, -10, -8, "Выселен"),
        listOf(7, 7, 1, 4, "Забронировано"), listOf(8, 8, -4, -2, "Выселен"), listOf(9, 9, 3, 6, "Отменён"),
        listOf(10, 10, 7, 10, "Забронировано")
    )
    settlements.forEachIndexed { i, s ->
        db.execSQL(
            "INSERT INTO settlements(id, idClient, idAccommodationObject, checkInDate, checkOutDate, status) VALUES (?,?,?,?,?,?)",
            arrayOf(i + 1, s[0], s[1], today + (s[2] as Int) * day, today + (s[3] as Int) * day, s[4])
        )
    }

    // заселение, услуга, количество
    val items = listOf(
        listOf(1, 1, 1), listOf(1, 2, 4), listOf(2, 3, 2), listOf(2, 4, 1), listOf(3, 5, 2), listOf(4, 10, 1), listOf(5, 6, 2),
        listOf(5, 2, 2), listOf(6, 9, 1), listOf(7, 7, 1), listOf(8, 8, 1), listOf(8, 2, 2), listOf(9, 1, 1), listOf(10, 10, 1), listOf(10, 3, 2)
    )
    items.forEach { db.execSQL("INSERT INTO settlement_services(idSettlement, idService, countService) VALUES (?,?,?)", arrayOf(it[0], it[1], it[2])) }
}
