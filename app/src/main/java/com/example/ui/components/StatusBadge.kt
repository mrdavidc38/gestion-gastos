package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseStatus
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusOverdueRedBgDark
import com.example.ui.theme.StatusOverdueRedBgLight
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPaidGreenBgDark
import com.example.ui.theme.StatusPaidGreenBgLight
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingAmberBgDark
import com.example.ui.theme.StatusPendingAmberBgLight

@Composable
fun StatusBadge(
    status: ExpenseStatus,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val (label, fgColor, bgColor, icon) = when (status) {
        ExpenseStatus.PAID -> Quadruple(
            "Pagado",
            StatusPaidGreen,
            if (isDark) StatusPaidGreenBgDark.copy(alpha = 0.6f) else StatusPaidGreenBgLight,
            Icons.Default.CheckCircle
        )
        ExpenseStatus.PENDING -> Quadruple(
            "Pendiente",
            StatusPendingAmber,
            if (isDark) StatusPendingAmberBgDark.copy(alpha = 0.6f) else StatusPendingAmberBgLight,
            Icons.Default.Schedule
        )
        ExpenseStatus.OVERDUE -> Quadruple(
            "Vencido",
            StatusOverdueRed,
            if (isDark) StatusOverdueRedBgDark.copy(alpha = 0.6f) else StatusOverdueRedBgLight,
            Icons.Default.ErrorOutline
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = fgColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
