package com.example.model

enum class ExpenseStatus {
    PENDING,
    PAID,
    OVERDUE
}

enum class RecurrenceFrequency(val displayName: String) {
    NONE("Una vez"),
    DAILY("Diario"),
    WEEKLY("Semanal"),
    BIWEEKLY("Cada 15 días"),
    MONTHLY("Mensual"),
    YEARLY("Anual"),
    CUSTOM("Personalizado")
}

enum class PaymentMethodType(val displayName: String, val iconKey: String) {
    CASH("Efectivo", "payments"),
    DEBIT_CARD("Tarjeta Débito", "credit_card"),
    CREDIT_CARD("Tarjeta Crédito", "credit_card"),
    BANK_TRANSFER("Transferencia", "account_balance"),
    DIGITAL_WALLET("Billetera Digital", "account_balance_wallet"),
    OTHER("Otro", "more_horiz")
}

enum class RecurrenceEditOption {
    ONLY_THIS,
    THIS_AND_FUTURE,
    ALL_SERIES
}

enum class CurrencyOption(val code: String, val symbol: String, val displayName: String) {
    COP("COP", "$", "Peso Colombiano (COP)"),
    USD("USD", "$", "Dólar Estadounidense (USD)"),
    EUR("EUR", "€", "Euro (EUR)"),
    MXN("MXN", "$", "Peso Mexicano (MXN)"),
    ARS("ARS", "$", "Peso Argentino (ARS)"),
    CLP("CLP", "$", "Peso Chileno (CLP)"),
    PEN("PEN", "S/", "Sol Peruano (PEN)")
}
