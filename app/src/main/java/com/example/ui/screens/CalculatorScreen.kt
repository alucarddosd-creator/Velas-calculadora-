package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProductCategory
import com.example.ui.components.CurrencyOutlinedTextField
import com.example.ui.components.ProfitMarginBadge
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.SageSecondary
import com.example.ui.viewmodel.CostViewModel
import com.example.util.CurrencyFormatter

@Composable
fun CalculatorScreen(
    viewModel: CostViewModel,
    onAnalyzeWithAi: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.calculatorState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // Leave space for sticky bottom live summary
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calculadora Artesanal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calcula el costo exacto e IVA Chile de tu producto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.createNewCalculator() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Limpiar", fontSize = 12.sp)
                }
            }

            // Category Selector Chips
            Text(
                text = "Selecciona Tipo de Producto",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductCategory.entries.forEach { cat ->
                    val isSelected = state.category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(cat.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("category_chip_${cat.name}")
                    )
                }
            }

            // Hero Summary Card (Vibrant Purple Theme)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ANÁLISIS DE MARGEN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = state.name.ifBlank { "Nueva Receta" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = state.category.emoji + " " + state.category.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = CurrencyFormatter.formatCLP(state.suggestedPublicPriceWithIva),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PVP Final (Inc. IVA 19%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatPercent(state.marginPercent),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text = "Margen Utilidad",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "COSTO DE PRODUCCIÓN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text = CurrencyFormatter.formatCLP(state.totalNetProductionCost),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "UTILIDAD NETA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primaryContainer
                            )
                            Text(
                                text = CurrencyFormatter.formatCLP(state.netProfit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onAnalyzeWithAi,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculator_analyze_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Analizar con Gemini IA",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analizar receta y margen con Gemini IA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Quick Variant Selector for Sprays (100ml / 250ml) and Reed Diffusers (150ml única)
            if (state.category == ProductCategory.SPRAY_AROMATIZANTE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Tamaño Estándar de Envase (Presione para ajustar receta):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val is100Selected = state.primaryMaterialUsedAmount.toInt() in 70..110
                            val is250Selected = state.primaryMaterialUsedAmount.toInt() in 190..260

                            FilterChip(
                                selected = is100Selected,
                                onClick = { viewModel.applyVolumeVariant(100) },
                                label = { Text("Variante 100 ML", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = is250Selected,
                                onClick = { viewModel.applyVolumeVariant(250) },
                                label = { Text("Variante 250 ML", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else if (state.category == ProductCategory.AROMATIZANTE_VARILLAS) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Formato Estándar de Difusor:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text("Variante Única 150 ML", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Considera envase unitario de 150ml, 120ml de alcohol de cereal, 30ml de esencia pura y set de varillas.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Product Name Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nombre de la Receta / Producto",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateForm(name = it) },
                        label = { Text("Ej: Vela Soya Vainilla 180g / Spray Lavanda 100ml") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_name")
                    )
                }
            }

            // Step 1: Primary Material (Soya / Alcohol de cereal)
            StepCard(
                stepNumber = "1",
                title = when (state.category) {
                    ProductCategory.VELA_SOYA -> "1. Cera de Soya (en Gramos)"
                    ProductCategory.SPRAY_AROMATIZANTE -> "1. Alcohol de Cereal (en ML)"
                    ProductCategory.AROMATIZANTE_VARILLAS -> "1. Alcohol de Cereal / Vehículo (en ML)"
                },
                subtitle = when (state.category) {
                    ProductCategory.VELA_SOYA -> "Cálculo exacto por peso de soya en gramos (g)"
                    ProductCategory.SPRAY_AROMATIZANTE -> "Cálculo por volumen de Alcohol de Cereal 96° en ML"
                    ProductCategory.AROMATIZANTE_VARILLAS -> "Cálculo por volumen de Vehículo Difusor/Alcohol en ML"
                },
                icon = Icons.Default.WaterDrop,
                calculatedCost = state.calculatedPrimaryCost
            ) {
                OutlinedTextField(
                    value = state.primaryMaterialName,
                    onValueChange = { viewModel.updateForm(primaryMaterialName = it) },
                    label = {
                        Text(
                            when (state.category) {
                                ProductCategory.VELA_SOYA -> "Nombre / Marca (Ej: Cera de Soya APF 1 Kg)"
                                else -> "Nombre / Marca (Ej: Alcohol de Cereal 96° 1 Litro)"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyOutlinedTextField(
                        value = state.primaryMaterialBulkCostStr,
                        onValueChange = { viewModel.updateForm(primaryMaterialBulkCostStr = it) },
                        label = "Costo Bulto/Bidón",
                        helperText = "Precio compra bulto",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_primary_bulk_cost")
                    )

                    CurrencyOutlinedTextField(
                        value = state.primaryMaterialBulkAmountStr,
                        onValueChange = { viewModel.updateForm(primaryMaterialBulkAmountStr = it) },
                        label = "Contenido Bulto",
                        suffix = state.category.unitLabel,
                        isCurrency = false,
                        helperText = if (state.category == ProductCategory.VELA_SOYA) "Ej: 1000 g" else "Ej: 1000 ml",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyOutlinedTextField(
                    value = state.primaryMaterialUsedAmountStr,
                    onValueChange = { viewModel.updateForm(primaryMaterialUsedAmountStr = it) },
                    label = when (state.category) {
                        ProductCategory.VELA_SOYA -> "Gramos (g) de Soya usados por unidad"
                        else -> "Mililitros (ML) de Alcohol de Cereal por unidad"
                    },
                    suffix = state.category.unitLabel,
                    isCurrency = false,
                    helperText = "Típico: ${state.category.typicalSize}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_primary_used_amount")
                )
            }

            // Step 2: Fragrance / Essence in ML
            StepCard(
                stepNumber = "2",
                title = "2. Esencia / Fragancia (en ML)",
                subtitle = "Calculado por volumen consumido en ML",
                icon = Icons.Default.Opacity,
                calculatedCost = state.calculatedAromaCost
            ) {
                OutlinedTextField(
                    value = state.aromaName,
                    onValueChange = { viewModel.updateForm(aromaName = it) },
                    label = { Text("Nombre de la Esencia (Ej: Esencia Vainilla 100 ML)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyOutlinedTextField(
                        value = state.aromaBulkCostStr,
                        onValueChange = { viewModel.updateForm(aromaBulkCostStr = it) },
                        label = "Costo Frasco Esencia",
                        helperText = "Precio de compra",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_aroma_bulk_cost")
                    )

                    CurrencyOutlinedTextField(
                        value = state.aromaBulkMLStr,
                        onValueChange = { viewModel.updateForm(aromaBulkMLStr = it) },
                        label = "Contenido Frasco",
                        suffix = "ML",
                        isCurrency = false,
                        helperText = "Ej: 100 ML o 250 ML",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyOutlinedTextField(
                    value = state.aromaUsedMLStr,
                    onValueChange = { viewModel.updateForm(aromaUsedMLStr = it) },
                    label = "Esencia usada en ML por unidad final",
                    suffix = "ML",
                    isCurrency = false,
                    helperText = "Ej: 18 ML para vela, 10 ML para spray, 20 ML para varillas",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_aroma_used_ml")
                )
            }

            // Step 3: Container & Components (Envase por costo unitario)
            StepCard(
                stepNumber = "3",
                title = "3. Envase y Componentes Unitarios",
                subtitle = "Envase unitario, pabilo/varillas/válvula y packaging",
                icon = Icons.Default.LocalOffer,
                calculatedCost = state.containerCost + state.secondaryComponentCost + state.labelAndPackagingCost
            ) {
                CurrencyOutlinedTextField(
                    value = state.containerCostStr,
                    onValueChange = { viewModel.updateForm(containerCostStr = it) },
                    label = when (state.category) {
                        ProductCategory.VELA_SOYA -> "Costo Unitario del Frasco/Contenedor ($)"
                        ProductCategory.SPRAY_AROMATIZANTE -> "Costo Unitario del Frasco PET/Vidrio ($)"
                        ProductCategory.AROMATIZANTE_VARILLAS -> "Costo Unitario del Envase Difusor ($)"
                    },
                    helperText = "Costo unitario por envase",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_container_cost")
                )

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyOutlinedTextField(
                    value = state.secondaryComponentCostStr,
                    onValueChange = { viewModel.updateForm(secondaryComponentCostStr = it) },
                    label = when (state.category) {
                        ProductCategory.VELA_SOYA -> "Costo Mecha / Pabilo por unidad ($)"
                        ProductCategory.SPRAY_AROMATIZANTE -> "Costo Gatillo / Atomizador por unidad ($)"
                        ProductCategory.AROMATIZANTE_VARILLAS -> "Costo Set de Varillas (Ratán/Fibra) ($)"
                    },
                    helperText = "Componente complementario",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyOutlinedTextField(
                    value = state.labelAndPackagingCostStr,
                    onValueChange = { viewModel.updateForm(labelAndPackagingCostStr = it) },
                    label = "Costo Etiqueta + Packaging por unidad ($)",
                    helperText = "Caja, bolsa o cinta",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Step 4: Labor & Overhead
            StepCard(
                stepNumber = "4",
                title = "Mano de Obra y Costos Operativos",
                subtitle = "Gas, electricidad y tiempo de producción",
                icon = Icons.Default.Work,
                calculatedCost = state.laborAndOverheadCost
            ) {
                CurrencyOutlinedTextField(
                    value = state.laborAndOverheadCostStr,
                    onValueChange = { viewModel.updateForm(laborAndOverheadCostStr = it) },
                    label = "Costo Mano de Obra por unidad",
                    helperText = "Tu salario por unidad producida",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Step 5: Suggested Sale Price & Chile 19% IVA Breakdown
            StepCard(
                stepNumber = "5",
                title = "Precio Público Sugerido (Con IVA 19%)",
                subtitle = "Establece tu precio al cliente final",
                icon = Icons.Default.Calculate,
                calculatedCost = 0.0,
                showCost = false
            ) {
                Text(
                    text = "Aplica un margen rápido sugerido:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(40.0, 50.0, 60.0, 75.0, 100.0).forEach { targetMargin ->
                        OutlinedButton(
                            onClick = { viewModel.applyTargetMargin(targetMargin) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${targetMargin.toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CurrencyOutlinedTextField(
                    value = state.suggestedPublicPriceWithIvaStr,
                    onValueChange = { viewModel.updateForm(suggestedPublicPriceWithIvaStr = it) },
                    label = "Precio de Venta al Público (Final con IVA)",
                    helperText = "Incluye el 19% IVA de Chile",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_public_price_iva")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Box inside Step 5
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Desglose Fiscal (Chile IVA 19%):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Valor Neto Público:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            CurrencyFormatter.formatCLP(state.netPublicPrice),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monto IVA (19%):", style = MaterialTheme.typography.bodySmall, color = SageSecondary)
                        Text(
                            CurrencyFormatter.formatCLP(state.ivaAmount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SageSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.updateForm(notes = it) },
                    label = { Text("Notas / Recomendaciones de temperatura o vertido") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Sticky Live Floating Bottom Sheet Result Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COSTO NETO: ${CurrencyFormatter.formatCLP(state.totalNetProductionCost)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ganancia Neta: ${CurrencyFormatter.formatCLP(state.netProfit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProfitGreen
                        )
                    }

                    ProfitMarginBadge(marginPercent = state.marginPercent)

                    Button(
                        onClick = { viewModel.saveCurrentProduct() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    calculatedCost: Double,
    showCost: Boolean = true,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showCost) {
                    Text(
                        text = CurrencyFormatter.formatCLP(calculatedCost),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}
