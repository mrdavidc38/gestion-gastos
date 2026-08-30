package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastoplan_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(1, "Vivienda", "home", 0xFF2B5B84, true),
            CategoryEntity(2, "Alimentación", "restaurant", 0xFFE07A5F, true),
            CategoryEntity(3, "Transporte", "directions_car", 0xFF3D5A80, true),
            CategoryEntity(4, "Combustible", "local_gas_station", 0xFFEE6C4D, true),
            CategoryEntity(5, "Servicios", "flash_on", 0xFF81B29A, true),
            CategoryEntity(6, "Telefonía", "phone_iphone", 0xFFF4A261, true),
            CategoryEntity(7, "Entretenimiento", "movie", 0xFF9B5DE5, true),
            CategoryEntity(8, "Compras", "shopping_bag", 0xFFF15BB5, true),
            CategoryEntity(9, "Salud", "medical_services", 0xFF00BBF9, true),
            CategoryEntity(10, "Educación", "school", 0xFF00F5D4, true),
            CategoryEntity(11, "Viajes", "flight", 0xFF4EA8DE, true),
            CategoryEntity(12, "Deudas", "account_balance", 0xFFE63946, true),
            CategoryEntity(13, "Suscripciones", "subscriptions", 0xFF7209B7, true),
            CategoryEntity(14, "Personal", "person", 0xFF4361EE, true),
            CategoryEntity(15, "Otros", "category", 0xFF6C757D, true)
        )
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val budgetDao = database.budgetDao()
            val expenseDao = database.expenseDao()

            categoryDao.insertCategories(DEFAULT_CATEGORIES)

            // Initial demo budget for current month (August 2026 / current)
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(
                    id = 1,
                    month = 8,
                    year = 2026,
                    expectedIncome = 6000000.0,
                    totalBudget = 4000000.0
                )
            )

            // Initial realistic expenses representing the user's prompt scenario
            val calendar = java.util.Calendar.getInstance()
            calendar.set(2026, java.util.Calendar.AUGUST, 30, 0, 0, 0)
            val today = calendar.timeInMillis

            calendar.set(2026, java.util.Calendar.AUGUST, 15, 0, 0, 0)
            val pastDate1 = calendar.timeInMillis
            calendar.set(2026, java.util.Calendar.AUGUST, 20, 0, 0, 0)
            val pastDate2 = calendar.timeInMillis

            calendar.set(2026, java.util.Calendar.AUGUST, 31, 0, 0, 0)
            val tomorrow = calendar.timeInMillis

            calendar.set(2026, java.util.Calendar.SEPTEMBER, 5, 0, 0, 0)
            val sept5 = calendar.timeInMillis

            calendar.set(2026, java.util.Calendar.SEPTEMBER, 10, 0, 0, 0)
            val sept10 = calendar.timeInMillis

            calendar.set(2026, java.util.Calendar.SEPTEMBER, 12, 0, 0, 0)
            val sept12 = calendar.timeInMillis

            calendar.set(2026, java.util.Calendar.SEPTEMBER, 15, 0, 0, 0)
            val sept15 = calendar.timeInMillis

            val sampleExpenses = listOf(
                ExpenseEntity(
                    name = "Mercado Quincenal",
                    plannedAmount = 350000.0,
                    actualAmount = 345000.0,
                    categoryId = 2,
                    dateMillis = pastDate1,
                    status = com.example.model.ExpenseStatus.PAID,
                    paymentMethod = com.example.model.PaymentMethodType.DEBIT_CARD,
                    notes = "Compras en supermercado"
                ),
                ExpenseEntity(
                    name = "Servicios Públicos (Luz y Agua)",
                    plannedAmount = 180000.0,
                    actualAmount = 175000.0,
                    categoryId = 5,
                    dateMillis = pastDate2,
                    status = com.example.model.ExpenseStatus.PAID,
                    paymentMethod = com.example.model.PaymentMethodType.BANK_TRANSFER,
                    notes = "Factura de servicios"
                ),
                ExpenseEntity(
                    name = "Mercado",
                    plannedAmount = 180000.0,
                    actualAmount = null,
                    categoryId = 2,
                    dateMillis = today,
                    status = com.example.model.ExpenseStatus.PENDING,
                    paymentMethod = com.example.model.PaymentMethodType.DEBIT_CARD,
                    notes = "Verduras y proteínas"
                ),
                ExpenseEntity(
                    name = "Internet Fibra Óptica",
                    plannedAmount = 95000.0,
                    actualAmount = 95000.0,
                    categoryId = 6,
                    dateMillis = today,
                    status = com.example.model.ExpenseStatus.PAID,
                    paymentMethod = com.example.model.PaymentMethodType.BANK_TRANSFER,
                    isRecurring = true,
                    recurrenceFrequency = com.example.model.RecurrenceFrequency.MONTHLY,
                    recurrenceSeriesId = java.util.UUID.randomUUID().toString(),
                    recurrenceDayOfMonth = 30
                ),
                ExpenseEntity(
                    name = "Gasolina",
                    plannedAmount = 80000.0,
                    actualAmount = null,
                    categoryId = 4,
                    dateMillis = tomorrow,
                    status = com.example.model.ExpenseStatus.PENDING,
                    paymentMethod = com.example.model.PaymentMethodType.DEBIT_CARD
                ),
                ExpenseEntity(
                    name = "Arriendo Apartamento",
                    plannedAmount = 900000.0,
                    actualAmount = null,
                    categoryId = 1,
                    dateMillis = sept5,
                    status = com.example.model.ExpenseStatus.PENDING,
                    paymentMethod = com.example.model.PaymentMethodType.BANK_TRANSFER,
                    isRecurring = true,
                    recurrenceFrequency = com.example.model.RecurrenceFrequency.MONTHLY,
                    recurrenceSeriesId = java.util.UUID.randomUUID().toString(),
                    recurrenceDayOfMonth = 5,
                    notes = "Transferencia al propietario"
                ),
                ExpenseEntity(
                    name = "Netflix",
                    plannedAmount = 35900.0,
                    actualAmount = null,
                    categoryId = 13,
                    dateMillis = sept10,
                    status = com.example.model.ExpenseStatus.PENDING,
                    paymentMethod = com.example.model.PaymentMethodType.CREDIT_CARD,
                    isRecurring = true,
                    recurrenceFrequency = com.example.model.RecurrenceFrequency.MONTHLY,
                    recurrenceSeriesId = java.util.UUID.randomUUID().toString(),
                    recurrenceDayOfMonth = 10
                ),
                ExpenseEntity(
                    name = "Mercado de Reposición",
                    plannedAmount = 180000.0,
                    actualAmount = null,
                    categoryId = 2,
                    dateMillis = sept12,
                    status = com.example.model.ExpenseStatus.PENDING,
                    paymentMethod = com.example.model.PaymentMethodType.DEBIT_CARD
                ),
                ExpenseEntity(
                    name = "Pago Tarjeta de Crédito",
                    plannedAmount = 450000.0,
                    actualAmount = null,
                    categoryId = 12,
                    dateMillis = sept15,
                    status = com.example.model.ExpenseStatus.PENDING,
                    paymentMethod = com.example.model.PaymentMethodType.BANK_TRANSFER
                )
            )

            expenseDao.insertExpenses(sampleExpenses)
        }
    }
}
