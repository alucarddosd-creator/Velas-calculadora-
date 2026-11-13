package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiChatMessage
import com.example.ai.AiChatUiState
import com.example.ai.AiTextUiState
import com.example.ai.GeneratedRecipeSuggestion
import com.example.ai.RecipeGeneratorUiState
import com.example.data.ProductEntity
import com.example.model.ProductCategory
import com.example.util.CurrencyFormatter
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiAdvisorViewModel"
private const val GEMINI_MODEL = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

class AiAdvisorViewModel : ViewModel() {

    private val systemPrompt = """
        Eres un asesor experto en producción artesanal de velas de soya, sprays aromatizantes y difusores de varillas en Chile.
        Ayudas a emprendedores a optimizar sus costos en pesos chilenos (CLP), calcular el impacto del IVA del 19% en Chile,
        mejorar sus márgenes de ganancia, elegir porcentajes adecuados de fragancia (en ML), temperaturas de vertido de cera de soya,
        proporciones de alcohol de cereal 96° y estrategias de precio y marketing.
        Responde siempre en español claro, directo, práctico y bien estructurado.
    """.trimIndent()

    // Lazy initialization so Firebase.ai is only evaluated on first inference
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_MODEL,
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private val recipeSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "name" to Schema.string(description = "Nombre comercial atractivo del producto en español"),
                "category" to Schema.enumeration(
                    values = listOf("VELA_SOYA", "SPRAY_AROMATIZANTE", "AROMATIZANTE_VARILLAS"),
                    description = "Categoría exacta del producto. Para difusor de varillas siempre es 150ml."
                ),
                "primaryMaterialName" to Schema.string(
                    description = "Nombre del insumo base (ej: Cera de Soya APF 1 Kg o Alcohol de Cereal 96°)"
                ),
                "primaryMaterialUsedAmount" to Schema.integer(
                    description = "Cantidad usada por unidad en gramos (para vela) o ml (150ml total para varillas, 100 o 250ml total para spray)"
                ),
                "aromaName" to Schema.string(description = "Nombre de la combinación de esencias o fragancia"),
                "aromaUsedML" to Schema.integer(description = "Cantidad de esencia en ML por unidad"),
                "suggestedPublicPriceWithIva" to Schema.integer(
                    description = "Precio sugerido de venta al público en pesos chilenos CLP incluyendo IVA 19%"
                ),
                "pouringOrPrepTip" to Schema.string(
                    description = "Consejo técnico clave de preparación, temperatura de vertido o maceración"
                ),
                "marketingPitch" to Schema.string(
                    description = "Descripción breve persuasiva para Instagram o catálogo de ventas"
                )
            )
        )
    }

    private val structuredRecipeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_MODEL,
            systemInstruction = content { text(systemPrompt) },
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = recipeSchema
            }
        )
    }

    private var chatSession: Chat? = null

    private val _selectedSubTab = MutableStateFlow(0) // 0: Análisis, 1: Crear Receta, 2: Chat
    val selectedSubTab: StateFlow<Int> = _selectedSubTab.asStateFlow()

    private val _auditState = MutableStateFlow<AiTextUiState>(AiTextUiState.Idle)
    val auditState: StateFlow<AiTextUiState> = _auditState.asStateFlow()

    private val _recipeIdeaInput = MutableStateFlow("")
    val recipeIdeaInput: StateFlow<String> = _recipeIdeaInput.asStateFlow()

    private val _recipeGenState = MutableStateFlow<RecipeGeneratorUiState>(RecipeGeneratorUiState.Idle)
    val recipeGenState: StateFlow<RecipeGeneratorUiState> = _recipeGenState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _chatState = MutableStateFlow<AiChatUiState>(AiChatUiState.Idle)
    val chatState: StateFlow<AiChatUiState> = _chatState.asStateFlow()

    fun selectSubTab(index: Int) {
        _selectedSubTab.value = index
    }

    fun updateRecipeIdeaInput(value: String) {
        _recipeIdeaInput.value = value
    }

    fun updateChatInput(value: String) {
        _chatInput.value = value
    }

    fun clearChat() {
        chatSession = null
        _chatMessages.value = emptyList()
        _chatState.value = AiChatUiState.Idle
    }

    // 1. Streaming audit of the active calculator recipe
    fun analyzeCurrentRecipe(form: CalculatorFormState) {
        viewModelScope.launch {
            _auditState.value = AiTextUiState.Loading
            val accumulated = StringBuilder()
            val prompt = buildAuditPrompt(form)

            try {
                withTimeout(AI_TIMEOUT_MS) {
                    generalModel.generateContentStream(prompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        accumulated.append(text)
                        if (accumulated.isNotEmpty()) {
                            _auditState.value = AiTextUiState.Streaming(accumulated.toString())
                        }
                    }
                }
                if (accumulated.isNotBlank()) {
                    _auditState.value = AiTextUiState.Success(accumulated.toString())
                } else {
                    _auditState.value = AiTextUiState.Error("Gemini no devolvió contenido. Intenta nuevamente.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Recipe audit timed out", e)
                _auditState.value = AiTextUiState.Error(
                    "El análisis tardó más de 60 segundos. Verifica tu conexión e inténtalo nuevamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Recipe audit failed", e)
                _auditState.value = AiTextUiState.Error(
                    e.localizedMessage ?: "No se pudo completar el análisis con Gemini."
                )
            }
        }
    }

    // 2. Structured JSON generation of a new recipe proposal
    fun generateRecipeIdea(customPrompt: String? = null) {
        val idea = (customPrompt ?: _recipeIdeaInput.value).trim()
        if (idea.isBlank()) {
            _recipeGenState.value = RecipeGeneratorUiState.Error(
                "Escribe una idea de aroma o producto para generar la receta."
            )
            return
        }
        if (customPrompt != null) {
            _recipeIdeaInput.value = customPrompt
        }

        viewModelScope.launch {
            _recipeGenState.value = RecipeGeneratorUiState.Loading
            val prompt = """
                Diseña una receta rentable para el mercado chileno basada en esta idea: "$idea".
                Recuerda nuestras reglas de formato:
                - Si es AROMATIZANTE_VARILLAS, debe ser versión única de 150ml (ej. 120ml alcohol/vehículo + 30ml esencia).
                - Si es SPRAY_AROMATIZANTE, usa formato estándar de 100ml o 250ml (con ~10% esencia).
                - Si es VELA_SOYA, indica gramos de cera de soya y ~8% a 10% de esencia en ML.
                - El precio sugerido (suggestedPublicPriceWithIva) debe ser realista en CLP con IVA 19% incluido.
            """.trimIndent()

            try {
                val response = withTimeout(AI_TIMEOUT_MS) {
                    structuredRecipeModel.generateContent(prompt)
                }
                val jsonString = response.text
                    ?: throw IllegalStateException("El modelo devolvió una respuesta vacía.")

                val json = JSONObject(jsonString)
                val suggestion = GeneratedRecipeSuggestion(
                    name = json.getString("name"),
                    category = ProductCategory.fromName(json.getString("category")),
                    primaryMaterialName = json.getString("primaryMaterialName"),
                    primaryMaterialUsedAmount = json.getInt("primaryMaterialUsedAmount").coerceAtLeast(1),
                    aromaName = json.getString("aromaName"),
                    aromaUsedML = json.getInt("aromaUsedML").coerceAtLeast(1),
                    suggestedPublicPriceWithIva = json.getInt("suggestedPublicPriceWithIva").coerceAtLeast(1000),
                    pouringOrPrepTip = json.getString("pouringOrPrepTip"),
                    marketingPitch = json.getString("marketingPitch")
                )
                _recipeGenState.value = RecipeGeneratorUiState.Success(suggestion)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Structured recipe generation timed out", e)
                _recipeGenState.value = RecipeGeneratorUiState.Error(
                    "La generación tardó más de 60 segundos. Intenta nuevamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Structured recipe generation failed", e)
                _recipeGenState.value = RecipeGeneratorUiState.Error(
                    e.localizedMessage ?: "Error al generar la receta con Gemini."
                )
            }
        }
    }

    // 3. Multi-turn streaming chat with recipe & catalog context
    fun sendChatMessage(
        messageOverride: String? = null,
        currentForm: CalculatorFormState,
        catalog: List<ProductEntity>
    ) {
        val userText = (messageOverride ?: _chatInput.value).trim()
        if (userText.isBlank() || _chatState.value is AiChatUiState.Sending) return

        if (messageOverride == null) {
            _chatInput.value = ""
        }

        val userMessage = AiChatMessage(isUser = true, text = userText)
        val assistantPlaceholderId = System.nanoTime() + 1L
        val assistantPlaceholder = AiChatMessage(
            id = assistantPlaceholderId,
            isUser = false,
            text = "",
            isStreaming = true
        )

        _chatMessages.update { it + userMessage + assistantPlaceholder }
        _chatState.value = AiChatUiState.Sending

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                val activeChat = chatSession ?: createContextualChat(currentForm, catalog).also {
                    chatSession = it
                }

                withTimeout(AI_TIMEOUT_MS) {
                    activeChat.sendMessageStream(userText).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        accumulated.append(chunkText)
                        val partial = accumulated.toString()
                        _chatMessages.update { list ->
                            list.map { msg ->
                                if (msg.id == assistantPlaceholderId) {
                                    msg.copy(text = partial)
                                } else msg
                            }
                        }
                    }
                }

                val finalReply = accumulated.toString()
                if (finalReply.isBlank()) {
                    _chatMessages.update { list -> list.filterNot { it.id == assistantPlaceholderId } }
                    _chatState.value = AiChatUiState.Error("Gemini no devolvió respuesta. Intenta de nuevo.")
                } else {
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == assistantPlaceholderId) {
                                msg.copy(text = finalReply, isStreaming = false)
                            } else msg
                        }
                    }
                    _chatState.value = AiChatUiState.Idle
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat response timed out", e)
                _chatMessages.update { list -> list.filterNot { it.id == assistantPlaceholderId && it.text.isBlank() } }
                _chatState.value = AiChatUiState.Error(
                    "La respuesta excedió los 60 segundos. Por favor intenta nuevamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat inference failed", e)
                _chatMessages.update { list -> list.filterNot { it.id == assistantPlaceholderId && it.text.isBlank() } }
                _chatState.value = AiChatUiState.Error(
                    e.localizedMessage ?: "Error al conectar con Gemini."
                )
            }
        }
    }

    private fun createContextualChat(
        currentForm: CalculatorFormState,
        catalog: List<ProductEntity>
    ): Chat {
        val catalogSummary = if (catalog.isEmpty()) {
            "Aún no hay productos guardados en el catálogo."
        } else {
            catalog.take(8).joinToString("\n") { product ->
                "- ${product.name} (${product.parsedCategory.title}): Costo ${CurrencyFormatter.formatCLP(product.totalNetProductionCost)}, PVP ${CurrencyFormatter.formatCLP(product.suggestedPublicPriceWithIva)}, Margen ${CurrencyFormatter.formatPercent(product.marginPercent)}"
            }
        }

        val contextMsg = """
            Contexto actual del taller del usuario:
            Receta abierta en Calculadora: ${currentForm.name} (${currentForm.category.title})
            - Insumo base: ${currentForm.primaryMaterialName}, uso ${currentForm.primaryMaterialUsedAmountStr} ${currentForm.category.unitLabel} (costo neto ${CurrencyFormatter.formatCLP(currentForm.calculatedPrimaryCost)})
            - Esencia: ${currentForm.aromaName}, uso ${currentForm.aromaUsedMLStr} ml (costo neto ${CurrencyFormatter.formatCLP(currentForm.calculatedAromaCost)})
            - Envase + Accesorios + Packaging: ${CurrencyFormatter.formatCLP(currentForm.containerCost + currentForm.secondaryComponentCost + currentForm.labelAndPackagingCost)}
            - Mano de obra: ${CurrencyFormatter.formatCLP(currentForm.laborAndOverheadCost)}
            - Costo Neto Total: ${CurrencyFormatter.formatCLP(currentForm.totalNetProductionCost)}
            - PVP con IVA (19%): ${CurrencyFormatter.formatCLP(currentForm.suggestedPublicPriceWithIva)}
            - Margen de utilidad neto: ${CurrencyFormatter.formatPercent(currentForm.marginPercent)}
            
            Catálogo guardado:
            $catalogSummary
        """.trimIndent()

        return generalModel.startChat(
            history = listOf(
                content("user") { text("Hola, aquí está el contexto actual de mis costos y productos:\n$contextMsg") },
                content("model") { text("¡Perfecto! Ya tengo los costos, porcentajes de esencia y márgenes con IVA de tu taller en mente. ¿En qué te ayudo hoy?") }
            )
        )
    }

    private fun buildAuditPrompt(form: CalculatorFormState): String {
        val ratioInfo = if (form.primaryMaterialUsedAmount > 0) {
            val pct = (form.aromaUsedML / form.primaryMaterialUsedAmount) * 100.0
            String.format("%.1f%%", pct)
        } else "N/A"

        return """
            Analiza en detalle esta receta y estructura de costos de emprendimiento chileno (valores en CLP, IVA 19%):
            
            Producto: ${form.name.ifBlank { "Sin nombre" }}
            Categoría: ${form.category.title}
            Insumo principal: ${form.primaryMaterialName} -> ${form.primaryMaterialUsedAmountStr} ${form.category.unitLabel} (Costo: ${CurrencyFormatter.formatCLP(form.calculatedPrimaryCost)})
            Esencia: ${form.aromaName} -> ${form.aromaUsedMLStr} ML (Proporción respecto a base: $ratioInfo, Costo: ${CurrencyFormatter.formatCLP(form.calculatedAromaCost)})
            Envase: ${CurrencyFormatter.formatCLP(form.containerCost)}
            Complemento (Mecha/Gatillo/Varillas): ${CurrencyFormatter.formatCLP(form.secondaryComponentCost)}
            Etiqueta y Packaging: ${CurrencyFormatter.formatCLP(form.labelAndPackagingCost)}
            Mano de obra y energía: ${CurrencyFormatter.formatCLP(form.laborAndOverheadCost)}
            
            COSTO NETO TOTAL: ${CurrencyFormatter.formatCLP(form.totalNetProductionCost)}
            PRECIO PÚBLICO (Con IVA 19%): ${CurrencyFormatter.formatCLP(form.suggestedPublicPriceWithIva)}
            VALOR NETO VENTA: ${CurrencyFormatter.formatCLP(form.netPublicPrice)}
            IVA (19%): ${CurrencyFormatter.formatCLP(form.ivaAmount)}
            UTILIDAD NETA: ${CurrencyFormatter.formatCLP(form.netProfit)}
            MARGEN NETO: ${CurrencyFormatter.formatPercent(form.marginPercent)}
            MARKUP: ${CurrencyFormatter.formatPercent(form.markupPercent)}
            
            Entrega un diagnóstico breve en 4 puntos claros:
            1. Salud del margen y precio (si el precio es competitivo en Chile o si conviene ajustarlo).
            2. Mayor costo y oportunidad de ahorro en insumos/packaging.
            3. Revisión técnica de la fórmula (% de esencia y consejo práctico de elaboración/temperatura/maceración).
            4. Idea corta de texto vendedor para redes sociales.
        """.trimIndent()
    }
}
