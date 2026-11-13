package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.model.ProductCategory
import com.example.ui.components.ProfitMarginBadge
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.CostViewModel
import com.example.util.CurrencyFormatter

@Composable
fun ProductsScreen(
    viewModel: CostViewModel,
    onViewDetail: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val context = LocalContext.current

    val filteredProducts = remember(products, searchQuery, selectedCategoryFilter) {
        products.filter { product ->
            val matchesCategory = selectedCategoryFilter == null || product.parsedCategory == selectedCategoryFilter
            val matchesQuery = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true) || product.notes.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Catálogo de Productos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredProducts.size} recetas guardadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Buscar por nombre o fragancia...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_products_input")
        )

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("Todos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            items(ProductCategory.entries.toTypedArray()) { category ->
                val isSelected = selectedCategoryFilter == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategoryFilter(if (isSelected) null else category) },
                    label = { Text("${category.emoji} ${category.title}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Products List
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🕯️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron productos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Crea una receta en la Calculadora y guárdala aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductItemCard(
                        product = product,
                        onViewDetail = { onViewDetail(product) },
                        onEdit = { viewModel.loadProductToCalculator(product) },
                        onDuplicate = { viewModel.duplicateProduct(product) },
                        onDelete = { viewModel.deleteProduct(product) },
                        onShare = { shareProductReport(context, product) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductItemCard(
    product: ProductEntity,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = product.parsedCategory.emoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Cost and Profit summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Costo Neto Producción", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        CurrencyFormatter.formatCLP(product.totalNetProductionCost),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Column {
                    Text("Precio Venta (Con IVA)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        CurrencyFormatter.formatCLP(product.suggestedPublicPriceWithIva),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Ganancia Neta", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        CurrencyFormatter.formatCLP(product.netProfit),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = ProfitGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onViewDetail) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "Ver reporte", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar en calculadora", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDuplicate) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onShare) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun shareProductReport(context: Context, product: ProductEntity) {
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
        - Markup sobre Costo: ${CurrencyFormatter.formatPercent(product.markupPercent)}
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, reportText)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir Reporte de Producto"))
}
