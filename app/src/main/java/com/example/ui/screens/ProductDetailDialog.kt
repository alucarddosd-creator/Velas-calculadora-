package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.ProductEntity
import com.example.ui.components.SummaryReportCard
import com.example.util.CurrencyFormatter

@Composable
fun ProductDetailDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onEdit: (ProductEntity) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reporte Completo de Costos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .verticalScroll(scrollState)
                ) {
                    SummaryReportCard(product = product)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            shareProductDetail(context, product)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onEdit(product)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                }
            }
        }
    }
}

private fun shareProductDetail(context: Context, product: ProductEntity) {
    val reportText = """
        📊 REPORTES DE COSTOS Y UTILIDAD - ${product.name.uppercase()}
        ----------------------------------------------
        Categoría: ${product.parsedCategory.title}
        
        🔹 DESGLOSE DE COSTOS NETOS:
        - Insumo Principal (${product.primaryMaterialName}): ${CurrencyFormatter.formatCLP(product.calculatedPrimaryCost)}
        - Esencia (${product.aromaName}): ${CurrencyFormatter.formatCLP(product.calculatedAromaCost)}
        - Contenedor/Envase: ${CurrencyFormatter.formatCLP(product.containerCost)}
        - Accesorios (Mecha/Gatillo/Varillas): ${CurrencyFormatter.formatCLP(product.secondaryComponentCost)}
        - Etiqueta y Packaging: ${CurrencyFormatter.formatCLP(product.labelAndPackagingCost)}
        - Mano de obra / Energía: ${CurrencyFormatter.formatCLP(product.laborAndOverheadCost)}
        
        👉 COSTO NETO TOTAL: ${CurrencyFormatter.formatCLP(product.totalNetProductionCost)}
        
        💵 PRECIO PÚBLICO (Chile):
        - Precio Final Cliente (con IVA 19%): ${CurrencyFormatter.formatCLP(product.suggestedPublicPriceWithIva)}
        - Subtotal Valor Neto Venta: ${CurrencyFormatter.formatCLP(product.netPublicPrice)}
        - Monto IVA (19%): ${CurrencyFormatter.formatCLP(product.ivaAmount)}
        
        📈 RENTABILIDAD NETA:
        - Ganancia Neta por Unidad: ${CurrencyFormatter.formatCLP(product.netProfit)}
        - Margen de Utilidad Neto: ${CurrencyFormatter.formatPercent(product.marginPercent)}
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, reportText)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
}
