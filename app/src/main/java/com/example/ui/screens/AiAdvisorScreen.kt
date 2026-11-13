package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiChatMessage
import com.example.ai.AiChatUiState
import com.example.ai.AiTextUiState
import com.example.ai.GeneratedRecipeSuggestion
import com.example.ai.RecipeGeneratorUiState
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.viewmodel.AiAdvisorViewModel
import com.example.ui.viewmodel.CostViewModel
import com.example.util.CurrencyFormatter

@Composable
fun AiAdvisorScreen(
    costViewModel: CostViewModel,
    aiViewModel: AiAdvisorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSubTab by aiViewModel.selectedSubTab.collectAsState()
    val currentForm by costViewModel.calculatorState.collectAsState()
    val allProducts by costViewModel.allProducts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Asesor Gemini IA",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Asesor IA de Costos y Fórmulas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Optimiza márgenes, IVA Chile y recetas artesanales con Gemini",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Sub-tab selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                Triple(0, "Analizar", Icons.Default.Analytics),
                Triple(1, "Crear Receta", Icons.Default.Lightbulb),
                Triple(2, "Chat IA", Icons.Default.Chat)
            )
            modes.forEach { (index, label, icon) ->
                val isSelected = selectedSubTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { aiViewModel.selectSubTab(index) },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_mode_chip_$index")
                )
            }
        }

        when (selectedSubTab) {
            0 -> AuditRecipeTab(
                costViewModel = costViewModel,
                aiViewModel = aiViewModel
            )
            1 -> GenerateRecipeTab(
                costViewModel = costViewModel,
                aiViewModel = aiViewModel
            )
            2 -> ExpertChatTab(
                currentForm = currentForm,
                allProducts = allProducts,
                aiViewModel = aiViewModel
            )
        }
    }
}

@Composable
private fun AuditRecipeTab(
    costViewModel: CostViewModel,
    aiViewModel: AiAdvisorViewModel
) {
    val form by costViewModel.calculatorState.collectAsState()
    val auditState by aiViewModel.auditState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active recipe summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Receta Actual en Calculadora",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${form.category.emoji} ${form.name.ifBlank { "Nueva Receta" }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Costo: ${CurrencyFormatter.formatCLP(form.totalNetProductionCost)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "PVP: ${CurrencyFormatter.formatCLP(form.suggestedPublicPriceWithIva)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Margen: ${CurrencyFormatter.formatPercent(form.marginPercent)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { aiViewModel.analyzeCurrentRecipe(form) },
                    enabled = auditState !is AiTextUiState.Loading && auditState !is AiTextUiState.Streaming,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_analyze_recipe_button")
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (auditState is AiTextUiState.Loading || auditState is AiTextUiState.Streaming) {
                            "Analizando con Gemini..."
                        } else {
                            "Diagnosticar Costos y Fórmula"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (val state = auditState) {
            is AiTextUiState.Idle -> {
                EmptyAiPromptCard(
                    title = "Diagnóstico Inteligente",
                    description = "Presiona el botón superior para que Gemini evalúe tus costos en CLP, el impacto del IVA (19%), porcentaje de esencia y te sugiera mejoras concretas."
                )
            }
            is AiTextUiState.Loading -> {
                LoadingAiCard("Consultando a Gemini sobre tu receta...")
            }
            is AiTextUiState.Streaming -> {
                AiResultCard(
                    title = "Análisis en progreso...",
                    body = state.text,
                    isStreaming = true
                )
            }
            is AiTextUiState.Success -> {
                AiResultCard(
                    title = "Diagnóstico de Gemini",
                    body = state.text,
                    isStreaming = false
                )
            }
            is AiTextUiState.Error -> {
                ErrorAiCard(
                    message = state.message,
                    onRetry = { aiViewModel.analyzeCurrentRecipe(form) }
                )
            }
        }
    }
}

@Composable
private fun GenerateRecipeTab(
    costViewModel: CostViewModel,
    aiViewModel: AiAdvisorViewModel
) {
    val ideaInput by aiViewModel.recipeIdeaInput.collectAsState()
    val genState by aiViewModel.recipeGenState.collectAsState()
    val scrollState = rememberScrollState()

    val quickIdeas = listOf(
        "Vela soya vainilla canela 200g",
        "Spray textil lavanda relajante 100ml",
        "Difusor varillas eucalipto menta 150ml",
        "Spray hogar frutos rojos 250ml"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Describe el producto que quieres lanzar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ideaInput,
                    onValueChange = { aiViewModel.updateRecipeIdeaInput(it) },
                    label = { Text("Ej: Vela navideña de pino y naranja 180g") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_recipe_idea_input"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickIdeas) { suggestion ->
                        SuggestionChip(
                            onClick = { aiViewModel.generateRecipeIdea(suggestion) },
                            label = { Text(suggestion, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { aiViewModel.generateRecipeIdea() },
                    enabled = genState !is RecipeGeneratorUiState.Loading,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_generate_recipe_button")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (genState is RecipeGeneratorUiState.Loading) {
                            "Diseñando receta..."
                        } else {
                            "Generar Receta con IA"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (val state = genState) {
            is RecipeGeneratorUiState.Idle -> {
                EmptyAiPromptCard(
                    title = "Generador de Recetas Estructuradas",
                    description = "Gemini calculará proporciones de cera o alcohol de cereal, mililitros de esencia, precio sugerido con IVA 19% y un consejo técnico listo para cargar en la calculadora."
                )
            }
            is RecipeGeneratorUiState.Loading -> {
                LoadingAiCard("Calculando proporciones y precio sugerido...")
            }
            is RecipeGeneratorUiState.Success -> {
                GeneratedRecipeCard(
                    suggestion = state.suggestion,
                    onLoadToCalculator = {
                        costViewModel.loadGeneratedRecipe(state.suggestion)
                    }
                )
            }
            is RecipeGeneratorUiState.Error -> {
                ErrorAiCard(
                    message = state.message,
                    onRetry = { aiViewModel.generateRecipeIdea() }
                )
            }
        }
    }
}

@Composable
private fun GeneratedRecipeCard(
    suggestion: GeneratedRecipeSuggestion,
    onLoadToCalculator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${suggestion.category.emoji} ${suggestion.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = ProfitGreenContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.formatCLP(suggestion.suggestedPublicPriceWithIva.toDouble()),
                        color = ProfitGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = "Base: ${suggestion.primaryMaterialName} (${suggestion.primaryMaterialUsedAmount} ${suggestion.category.unitLabel})",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Esencia: ${suggestion.aromaName} (${suggestion.aromaUsedML} ml)",
                style = MaterialTheme.typography.bodyMedium
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Tip Técnico",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = suggestion.pouringOrPrepTip,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Copy Sugerido para Venta",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = suggestion.marketingPitch,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = onLoadToCalculator,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_load_generated_recipe_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cargar en Calculadora", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExpertChatTab(
    currentForm: com.example.ui.viewmodel.CalculatorFormState,
    allProducts: List<com.example.data.ProductEntity>,
    aiViewModel: AiAdvisorViewModel
) {
    val messages by aiViewModel.chatMessages.collectAsState()
    val chatInput by aiViewModel.chatInput.collectAsState()
    val chatState by aiViewModel.chatState.collectAsState()
    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "¿Cómo mejorar mi margen?",
        "Temperatura de vertido soya",
        "¿Cómo fijar precios con IVA 19%?"
    )

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick questions & Clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickQuestions) { q ->
                    SuggestionChip(
                        onClick = {
                            aiViewModel.sendChatMessage(
                                messageOverride = q,
                                currentForm = currentForm,
                                catalog = allProducts
                            )
                        },
                        label = { Text(q, fontSize = 11.sp) }
                    )
                }
            }
            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = { aiViewModel.clearChat() },
                    modifier = Modifier.testTag("ai_clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Limpiar conversación",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Chat messages area
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                EmptyAiPromptCard(
                    title = "Chat Experto del Taller",
                    description = "Pregunta sobre mezclas de aceites esenciales, fijación de aromas en alcohol de cereal, costos por mayor o estrategias de venta en Chile. Gemini conoce tu receta abierta y tus productos guardados."
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubble(message = msg)
                    }
                }
            }
        }

        if (chatState is AiChatUiState.Error) {
            val errorMsg = (chatState as AiChatUiState.Error).message
            Surface(
                color = LossRedContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = LossRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMsg,
                        color = LossRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { aiViewModel.updateChatInput(it) },
                placeholder = { Text("Pregunta a Gemini sobre tus costos...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input")
            )
            IconButton(
                onClick = {
                    aiViewModel.sendChatMessage(
                        currentForm = currentForm,
                        catalog = allProducts
                    )
                },
                enabled = chatInput.isNotBlank() && chatState !is AiChatUiState.Sending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (chatInput.isNotBlank() && chatState !is AiChatUiState.Sending) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .testTag("ai_chat_send_button")
            ) {
                if (chatState is AiChatUiState.Sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar pregunta",
                        tint = if (chatInput.isNotBlank()) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (message.isUser) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text.ifBlank { "Escribiendo..." },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun EmptyAiPromptCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingAiCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AiResultCard(
    title: String,
    body: String,
    isStreaming: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isStreaming) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ErrorAiCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LossRedContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = LossRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Error de Gemini IA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = LossRed
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = LossRed
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reintentar", color = LossRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}
