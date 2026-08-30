package com.example.util

import com.example.model.CurrencyOption
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {

    private val spanishLocale = Locale("es", "CO")

    fun formatCurrency(amount: Double, currency: CurrencyOption = CurrencyOption.COP): String {
        val symbols = DecimalFormatSymbols(spanishLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }

        return when (currency) {
            CurrencyOption.COP, CurrencyOption.CLP -> {
                val formatter = DecimalFormat("#,##0", symbols)
                "${currency.symbol} ${formatter.format(amount)}"
            }
            CurrencyOption.USD, CurrencyOption.EUR, CurrencyOption.MXN, CurrencyOption.PEN, CurrencyOption.ARS -> {
                val formatter = if (amount % 1.0 == 0.0) {
                    DecimalFormat("#,##0", symbols)
                } else {
                    DecimalFormat("#,##0.00", symbols)
                }
                "${currency.symbol} ${formatter.format(amount)}"
            }
        }
    }

    fun formatFriendlyDate(dateMillis: Long, includeYearIfDifferent: Boolean = true): String {
        val calDate = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val calToday = Calendar.getInstance()

        val isSameDay = calDate.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)

        val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = calDate.get(Calendar.YEAR) == calTomorrow.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == calTomorrow.get(Calendar.DAY_OF_YEAR)

        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = calDate.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                calDate.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) return "Hoy"
        if (isTomorrow) return "Mañana"
        if (isYesterday) return "Ayer"

        val day = calDate.get(Calendar.DAY_OF_MONTH)
        val monthName = getMonthName(calDate.get(Calendar.MONTH))

        return if (calDate.get(Calendar.YEAR) != calToday.get(Calendar.YEAR) && includeYearIfDifferent) {
            "$day de $monthName ${calDate.get(Calendar.YEAR)}"
        } else {
            "$day de $monthName"
        }
    }

    fun formatShortDate(dateMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthShort = getMonthShortName(cal.get(Calendar.MONTH))
        return "$day $monthShort"
    }

    fun formatFullDate(dateMillis: Long): String {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", spanishLocale)
        val formatted = sdf.format(Date(dateMillis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
    }

    fun formatMonthYear(month: Int, year: Int): String {
        val monthName = getMonthName(month - 1)
        return "${monthName.replaceFirstChar { it.uppercase() }} $year"
    }

    fun getMonthName(monthIndex: Int): String {
        return when (monthIndex) {
            0 -> "enero"
            1 -> "febrero"
            2 -> "marzo"
            3 -> "abril"
            4 -> "mayo"
            5 -> "junio"
            6 -> "julio"
            7 -> "agosto"
            8 -> "septiembre"
            9 -> "octubre"
            10 -> "noviembre"
            11 -> "diciembre"
            else -> ""
        }
    }

    fun getMonthShortName(monthIndex: Int): String {
        return when (monthIndex) {
            0 -> "ene"
            1 -> "feb"
            2 -> "mar"
            3 -> "abr"
            4 -> "may"
            5 -> "jun"
            6 -> "jul"
            7 -> "ago"
            8 -> "sep"
            9 -> "oct"
            10 -> "nov"
            11 -> "dic"
            else -> ""
        }
    }

    fun getDayOfWeekShortName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Lun"
            Calendar.TUESDAY -> "Mar"
            Calendar.WEDNESDAY -> "Mié"
            Calendar.THURSDAY -> "Jue"
            Calendar.FRIDAY -> "Vie"
            Calendar.SATURDAY -> "Sáb"
            Calendar.SUNDAY -> "Dom"
            else -> ""
        }
    }

    fun normalizeToStartOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun getStartOfWeek(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfWeek(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun getStartOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }
}
