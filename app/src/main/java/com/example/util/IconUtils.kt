package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {

    val AVAILABLE_CATEGORY_ICONS = listOf(
        "home" to "Vivienda",
        "restaurant" to "Alimentación",
        "directions_car" to "Transporte",
        "local_gas_station" to "Combustible",
        "flash_on" to "Servicios",
        "phone_iphone" to "Telefonía",
        "movie" to "Entretenimiento",
        "shopping_bag" to "Compras",
        "medical_services" to "Salud",
        "school" to "Educación",
        "flight" to "Viajes",
        "account_balance" to "Deudas / Finanzas",
        "subscriptions" to "Suscripciones",
        "person" to "Personal",
        "work" to "Trabajo",
        "category" to "Otros"
    )

    val AVAILABLE_CATEGORY_COLORS = listOf(
        0xFF2B5B84, // Navy blue
        0xFFE07A5F, // Terracotta
        0xFF3D5A80, // Steel blue
        0xFFEE6C4D, // Burnt orange
        0xFF81B29A, // Sage green
        0xFFF4A261, // Sand / Gold
        0xFF9B5DE5, // Purple
        0xFFF15BB5, // Pink
        0xFF00BBF9, // Cyan
        0xFF00F5D4, // Mint
        0xFF4EA8DE, // Sky blue
        0xFFE63946, // Red
        0xFF7209B7, // Deep violet
        0xFF4361EE, // Royal blue
        0xFF6C757D, // Slate grey
        0xFF2A9D8F  // Teal
    )

    fun getCategoryIcon(key: String): ImageVector {
        return when (key.lowercase()) {
            "home" -> Icons.Default.Home
            "restaurant", "fastfood" -> Icons.Default.Restaurant
            "directions_car", "car" -> Icons.Default.DirectionsCar
            "local_gas_station", "gas" -> Icons.Default.LocalGasStation
            "flash_on", "bolt", "services" -> Icons.Default.FlashOn
            "phone_iphone", "phone", "phone_android" -> Icons.Default.PhoneIphone
            "movie", "tv", "entertainment" -> Icons.Default.Movie
            "shopping_bag", "shopping_cart", "shop" -> Icons.Default.ShoppingBag
            "medical_services", "health", "hospital" -> Icons.Default.MedicalServices
            "school", "education" -> Icons.Default.School
            "flight", "travel" -> Icons.Default.Flight
            "account_balance", "bank" -> Icons.Default.AccountBalance
            "subscriptions", "subs" -> Icons.Default.Subscriptions
            "person", "personal" -> Icons.Default.Person
            "work" -> Icons.Default.Work
            else -> Icons.Default.Category
        }
    }

    fun getPaymentMethodIcon(iconKey: String): ImageVector {
        return when (iconKey.lowercase()) {
            "payments", "cash" -> Icons.Default.Payments
            "credit_card", "debit_card" -> Icons.Default.CreditCard
            "account_balance", "transfer" -> Icons.Default.AccountBalance
            "account_balance_wallet", "wallet" -> Icons.Default.AccountBalanceWallet
            else -> Icons.Default.MoreHoriz
        }
    }
}
