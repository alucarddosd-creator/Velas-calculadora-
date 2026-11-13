package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.SageSecondary
import com.example.util.CurrencyFormatter

@Composable
fun VisualCostChart(
    products: List<ProductEntity>,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    // Show up to top 5 products
    val displayProducts = remember(products) {
        products.sortedByDescending { it.netProfit }.take(5)
    }

    var startAnimation by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnimation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Desglose Visual: Costo Neto vs Ganancia Neta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Comparativa de las mejores recetas por margen de ganancia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Costo Neto")
                LegendItem(color = SageSecondary, label = "IVA Chile (19%)")
                LegendItem(color = ProfitGreen, label = "Ganancia Neta")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Stacked Bar Chart for each product
            displayProducts.forEach { product ->
                val totalValue = product.suggestedPublicPriceWithIva.coerceAtLeast(1.0)
                val costWeight = (product.totalNetProductionCost / totalValue).toFloat().coerceIn(0f, 1f)
                val ivaWeight = (product.ivaAmount / totalValue).toFloat().coerceIn(0f, 1f)
                val profitWeight = (product.netProfit / totalValue).toFloat().coerceIn(0f, 1f)

                val primaryColor = MaterialTheme.colorScheme.primary
                val ivaColor = SageSecondary
                val profitColor = ProfitGreen

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = product.parsedCategory.emoji,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatCLP(product.suggestedPublicPriceWithIva),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Draw stacked progress bar on Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())

                        // Background container
                        drawRoundRect(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            size = Size(width, height),
                            cornerRadius = cornerRadius
                        )

                        // Cost segment
                        val costWidth = width * costWeight * progress
                        if (costWidth > 0) {
                            drawRoundRect(
                                color = primaryColor,
                                size = Size(costWidth, height),
                                cornerRadius = cornerRadius
                            )
                        }

                        // IVA segment
                        val ivaWidth = width * ivaWeight * progress
                        if (ivaWidth > 0) {
                            drawRoundRect(
                                color = ivaColor,
                                topLeft = Offset(costWidth, 0f),
                                size = Size(ivaWidth, height),
                                cornerRadius = cornerRadius
                            )
                        }

                        // Profit segment
                        val profitWidth = width * profitWeight * progress
                        if (profitWidth > 0) {
                            drawRoundRect(
                                color = profitColor,
                                topLeft = Offset(costWidth + ivaWidth, 0f),
                                size = Size(profitWidth, height),
                                cornerRadius = cornerRadius
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Costo: ${CurrencyFormatter.formatCLP(product.totalNetProductionCost)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ganancia Neta: +${CurrencyFormatter.formatCLP(product.netProfit)} (${CurrencyFormatter.formatPercent(product.marginPercent)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
