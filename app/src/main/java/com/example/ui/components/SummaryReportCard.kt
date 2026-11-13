package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.SageSecondary
import com.example.util.CurrencyFormatter

@Composable
fun SummaryReportCard(
    product: ProductEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = product.parsedCategory.emoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = product.parsedCategory.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                ProfitMarginBadge(marginPercent = product.marginPercent)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // Itemized Costs Breakdown Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Desglose Detallado de Costos",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CostRowItem(
                label = product.primaryMaterialName,
                subtitle = "${CurrencyFormatter.formatNumber(product.primaryMaterialUsedAmount)} ${product.parsedCategory.unitLabel} de ${CurrencyFormatter.formatNumber(product.primaryMaterialBulkAmount)}",
                cost = product.calculatedPrimaryCost
            )

            CostRowItem(
                label = product.aromaName,
                subtitle = "${CurrencyFormatter.formatNumber(product.aromaUsedML)} ML de ${CurrencyFormatter.formatNumber(product.aromaBulkML)} ML",
                cost = product.calculatedAromaCost
            )

            CostRowItem(
                label = "Envase / Contenedor",
                subtitle = "Frasco o contenedor principal",
                cost = product.containerCost
            )

            CostRowItem(
                label = when (product.parsedCategory) {
                    com.example.model.ProductCategory.VELA_SOYA -> "Pabilo / Mecha de algodón"
                    com.example.model.ProductCategory.SPRAY_AROMATIZANTE -> "Gatillo / Atomizador Spray"
                    com.example.model.ProductCategory.AROMATIZANTE_VARILLAS -> "Set de Varillas Difusoras"
                },
                subtitle = "Accesorios y tapas",
                cost = product.secondaryComponentCost
            )

            CostRowItem(
                label = "Etiqueta y Empaque",
                subtitle = "Diseño, impresión y caja/bolsa",
                cost = product.labelAndPackagingCost
            )

            CostRowItem(
                label = "Mano de Obra y Energía",
                subtitle = "Costo operativo por unidad",
                cost = product.laborAndOverheadCost
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Total Cost Highlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COSTO NETO TOTAL DE PRODUCCIÓN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = CurrencyFormatter.formatCLP(product.totalNetProductionCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chilean Tax & Utility Breakdown
            Text(
                text = "Estructura de Venta al Público (Chile)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FinancialRow(
                    label = "Precio de Venta Sugerido (Público con IVA)",
                    value = CurrencyFormatter.formatCLP(product.suggestedPublicPriceWithIva),
                    isBold = true
                )
                FinancialRow(
                    label = "Subtotal Valor Neto Venta",
                    value = CurrencyFormatter.formatCLP(product.netPublicPrice)
                )
                FinancialRow(
                    label = "Monto IVA Débito Fiscal (19%)",
                    value = CurrencyFormatter.formatCLP(product.ivaAmount),
                    color = SageSecondary
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                FinancialRow(
                    label = "GANANCIA NETA POR UNIDAD",
                    value = CurrencyFormatter.formatCLP(product.netProfit),
                    isBold = true,
                    color = ProfitGreen
                )
                FinancialRow(
                    label = "Margen de Utilidad sobre Venta Neta",
                    value = CurrencyFormatter.formatPercent(product.marginPercent),
                    isBold = true,
                    color = ProfitGreen
                )
                FinancialRow(
                    label = "Porcentaje Markup sobre Costo",
                    value = CurrencyFormatter.formatPercent(product.markupPercent)
                )
            }

            if (product.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 6.dp, top = 2.dp)
                    )
                    Text(
                        text = "Notas: ${product.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CostRowItem(
    label: String,
    subtitle: String,
    cost: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = CurrencyFormatter.formatCLP(cost),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FinancialRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
