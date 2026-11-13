package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.util.CurrencyFormatter

@Composable
fun ProfitMarginBadge(
    marginPercent: Double,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val (backgroundColor, textColor, iconEmoji) = when {
        marginPercent >= 40.0 -> Triple(ProfitGreenContainer, ProfitGreen, "📈")
        marginPercent >= 20.0 -> Triple(WarningAmberContainer, WarningAmber, "📊")
        else -> Triple(LossRedContainer, LossRed, "⚠️")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = "$iconEmoji ", fontSize = 12.sp)
        Text(
            text = "${CurrencyFormatter.formatPercent(marginPercent)}${if (showLabel) " Margen" else ""}",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
