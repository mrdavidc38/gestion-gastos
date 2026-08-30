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
import com.example.data.CategoryEntity
import com.example.util.IconUtils

@Composable
fun CategoryChip(
    category: CategoryEntity?,
    modifier: Modifier = Modifier
) {
    if (category == null) return

    val color = Color(category.colorHex)
    val icon = IconUtils.getCategoryIcon(category.iconKey)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
