package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.model.ProductCategory
import com.example.ui.components.ProfitMarginBadge
import com.example.ui.components.VisualCostChart
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.SageSecondary
import com.example.ui.viewmodel.CostViewModel
import com.example.util.CurrencyFormatter

@Composable
fun DashboardScreen(
    viewModel: CostViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val scrollState = rememberScrollState()

    // Calculated metrics across all products
    val totalProducts = products.size
    val avgMargin = if (products.isNotEmpty()) products.map { it.marginPercent }.average() else 0.0
    val avgProfit = if (products.isNotEmpty()) products.map { it.netProfit }.average() else 0.0
    val bestProduct = products.maxByOrNull { it.netProfit }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Dashboard de Márgenes y Reportes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Análisis automático de utilidad neta e IVA para tus productos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Global KPI Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                title = "Total Recetas",
                value = "$totalProducts registradas",
                icon = Icons.Default.Category,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "Margen Promedio",
                value = CurrencyFormatter.formatPercent(avgMargin),
                icon = Icons.Default.TrendingUp,
                valueColor = ProfitGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                title = "Ganancia Prom. Unidad",
                value = CurrencyFormatter.formatCLP(avgProfit),
                icon = Icons.Default.AttachMoney,
                valueColor = ProfitGreen,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "Top Producto",
                value = bestProduct?.name ?: "Sin datos",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
        }

        // Visual Cost Breakdown Chart
        VisualCostChart(products = products)

        // Profitability Breakdown by Category
        Text(
            text = "Análisis por Categoría de Producto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ProductCategory.entries.forEach { category ->
            val catProducts = products.filter { it.parsedCategory == category }
            CategoryReportCard(category = category, products = catProducts)
        }

        // Automatic Ranking & Profit Margin Summary Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Ranking de Utilidad por Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                if (products.isEmpty()) {
                    Text(
                        text = "Guarda productos para ver la tabla comparativa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    products.sortedByDescending { it.marginPercent }.forEach { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${product.parsedCategory.emoji} ${product.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Costo: ${CurrencyFormatter.formatCLP(product.totalNetProductionCost)} | Venta con IVA: ${CurrencyFormatter.formatCLP(product.suggestedPublicPriceWithIva)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${CurrencyFormatter.formatCLP(product.netProfit)}",
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen,
                                    fontSize = 13.sp
                                )
                                ProfitMarginBadge(marginPercent = product.marginPercent, showLabel = false)
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Chilean Tax Card (IVA 19%)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Explicativo Fiscal Chile (IVA 19%)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "En Chile, el Impuesto al Valor Agregado (IVA) es del 19%. " +
                            "Cuando vendes a precio público final (ej: $10.000 CLP), el valor neto " +
                            "real que recibe tu emprendimiento es $10.000 / 1,19 = $8.403 CLP, y la " +
                            "diferencia de $1.597 CLP corresponde al débito fiscal IVA recaudado para el SII.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryReportCard(
    category: ProductCategory,
    products: List<ProductEntity>
) {
    val count = products.size
    val avgCost = if (products.isNotEmpty()) products.map { it.totalNetProductionCost }.average() else 0.0
    val avgMargin = if (products.isNotEmpty()) products.map { it.marginPercent }.average() else 0.0
    val avgProfit = if (products.isNotEmpty()) products.map { it.netProfit }.average() else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = category.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$count recetas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Costo Promedio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatCLP(avgCost), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Text("Ganancia Promedio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatCLP(avgProfit), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ProfitGreen)
                }
                Column {
                    Text("Margen Medio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatPercent(avgMargin), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ProfitGreen)
                }
            }
        }
    }
}
