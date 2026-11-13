package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProductEntity
import com.example.data.ProductRepository
import com.example.model.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorFormState(
    val id: Long? = null,
    val name: String = "Mi Nueva Vela de Soya",
    val category: ProductCategory = ProductCategory.VELA_SOYA,
    
    // Primary material
    val primaryMaterialName: String = "Cera de Soya APF (1 Kg)",
    val primaryMaterialBulkCostStr: String = "8500",
    val primaryMaterialBulkAmountStr: String = "1000",
    val primaryMaterialUsedAmountStr: String = "180",
    
    // Aroma / Fragrance
    val aromaName: String = "Esencia Lavanda Vainilla (100 ml)",
    val aromaBulkCostStr: String = "9800",
    val aromaBulkMLStr: String = "100",
    val aromaUsedMLStr: String = "18",
    
    // Packaging & Labor
    val containerCostStr: String = "850",
    val secondaryComponentCostStr: String = "180", // Wick / Straws / Trigger
    val labelAndPackagingCostStr: String = "300",
    val laborAndOverheadCostStr: String = "500",
    
    // Suggested Public Price (includes Chile 19% IVA)
    val suggestedPublicPriceWithIvaStr: String = "8990",
    val notes: String = ""
) {
    // Parsed numerical values
    val primaryMaterialBulkCost: Double get() = primaryMaterialBulkCostStr.toDoubleOrNull() ?: 0.0
    val primaryMaterialBulkAmount: Double get() = primaryMaterialBulkAmountStr.toDoubleOrNull() ?: 1.0
    val primaryMaterialUsedAmount: Double get() = primaryMaterialUsedAmountStr.toDoubleOrNull() ?: 0.0

    val aromaBulkCost: Double get() = aromaBulkCostStr.toDoubleOrNull() ?: 0.0
    val aromaBulkML: Double get() = aromaBulkMLStr.toDoubleOrNull() ?: 1.0
    val aromaUsedML: Double get() = aromaUsedMLStr.toDoubleOrNull() ?: 0.0

    val containerCost: Double get() = containerCostStr.toDoubleOrNull() ?: 0.0
    val secondaryComponentCost: Double get() = secondaryComponentCostStr.toDoubleOrNull() ?: 0.0
    val labelAndPackagingCost: Double get() = labelAndPackagingCostStr.toDoubleOrNull() ?: 0.0
    val laborAndOverheadCost: Double get() = laborAndOverheadCostStr.toDoubleOrNull() ?: 0.0

    val suggestedPublicPriceWithIva: Double get() = suggestedPublicPriceWithIvaStr.toDoubleOrNull() ?: 0.0

    // Calculations
    val calculatedPrimaryCost: Double
        get() = if (primaryMaterialBulkAmount > 0) (primaryMaterialBulkCost / primaryMaterialBulkAmount) * primaryMaterialUsedAmount else 0.0

    val calculatedAromaCost: Double
        get() = if (aromaBulkML > 0) (aromaBulkCost / aromaBulkML) * aromaUsedML else 0.0

    val totalNetProductionCost: Double
        get() = calculatedPrimaryCost + calculatedAromaCost + containerCost +
                secondaryComponentCost + labelAndPackagingCost + laborAndOverheadCost

    val netPublicPrice: Double
        get() = suggestedPublicPriceWithIva / 1.19

    val ivaAmount: Double
        get() = suggestedPublicPriceWithIva - netPublicPrice

    val netProfit: Double
        get() = netPublicPrice - totalNetProductionCost

    val marginPercent: Double
        get() = if (netPublicPrice > 0) (netProfit / netPublicPrice) * 100.0 else 0.0

    val markupPercent: Double
        get() = if (totalNetProductionCost > 0) (netProfit / totalNetProductionCost) * 100.0 else 0.0

    fun toEntity(): ProductEntity {
        return ProductEntity(
            id = id ?: 0,
            name = name.ifBlank { "Producto ${category.title}" },
            category = category.name,
            primaryMaterialName = primaryMaterialName,
            primaryMaterialBulkCost = primaryMaterialBulkCost,
            primaryMaterialBulkAmount = primaryMaterialBulkAmount,
            primaryMaterialUsedAmount = primaryMaterialUsedAmount,
            aromaName = aromaName,
            aromaBulkCost = aromaBulkCost,
            aromaBulkML = aromaBulkML,
            aromaUsedML = aromaUsedML,
            containerCost = containerCost,
            secondaryComponentCost = secondaryComponentCost,
            labelAndPackagingCost = labelAndPackagingCost,
            laborAndOverheadCost = laborAndOverheadCost,
            suggestedPublicPriceWithIva = suggestedPublicPriceWithIva,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
    }
}

class CostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepository

    val allProducts: StateFlow<List<ProductEntity>>

    private val _calculatorState = MutableStateFlow(CalculatorFormState())
    val calculatorState: StateFlow<CalculatorFormState> = _calculatorState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Calculadora, 1: Mis Productos, 2: Dashboard & Reportes
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ProductCategory?>(null)
    val selectedCategoryFilter: StateFlow<ProductCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _saveSuccessMessage = MutableStateFlow<String?>(null)
    val saveSuccessMessage: StateFlow<String?> = _saveSuccessMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProductRepository(database.productDao())
        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setCategoryFilter(category: ProductCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSuccessMessage() {
        _saveSuccessMessage.value = null
    }

    // Category preset switch
    fun selectCategory(category: ProductCategory) {
        when (category) {
            ProductCategory.VELA_SOYA -> {
                _calculatorState.value = CalculatorFormState(
                    id = null,
                    name = "Vela de Soya Lavanda",
                    category = ProductCategory.VELA_SOYA,
                    primaryMaterialName = "Cera de Soya (1 Kg)",
                    primaryMaterialBulkCostStr = "8500",
                    primaryMaterialBulkAmountStr = "1000",
                    primaryMaterialUsedAmountStr = "180",
                    aromaName = "Esencia Aromática (100 ml)",
                    aromaBulkCostStr = "9800",
                    aromaBulkMLStr = "100",
                    aromaUsedMLStr = "18",
                    containerCostStr = "850",
                    secondaryComponentCostStr = "180", // Mecha pabilo
                    labelAndPackagingCostStr = "300",
                    laborAndOverheadCostStr = "500",
                    suggestedPublicPriceWithIvaStr = "8990",
                    notes = "Receta de vela de soya con 10% fragancia."
                )
            }
            ProductCategory.SPRAY_AROMATIZANTE -> {
                _calculatorState.value = CalculatorFormState(
                    id = null,
                    name = "Spray Aromatizante 100ml",
                    category = ProductCategory.SPRAY_AROMATIZANTE,
                    primaryMaterialName = "Alcohol de Cereal 96° + Agua (1000 ml)",
                    primaryMaterialBulkCostStr = "4500",
                    primaryMaterialBulkAmountStr = "1000",
                    primaryMaterialUsedAmountStr = "90",
                    aromaName = "Esencia Concentrada (100 ml)",
                    aromaBulkCostStr = "8900",
                    aromaBulkMLStr = "100",
                    aromaUsedMLStr = "10",
                    containerCostStr = "750",
                    secondaryComponentCostStr = "350", // Gatillo atomizador
                    labelAndPackagingCostStr = "250",
                    laborAndOverheadCostStr = "300",
                    suggestedPublicPriceWithIvaStr = "5990",
                    notes = "Spray ambiental/textil concentrado al 10% en alcohol de cereal."
                )
            }
            ProductCategory.AROMATIZANTE_VARILLAS -> {
                _calculatorState.value = CalculatorFormState(
                    id = null,
                    name = "Difusor de Varillas 150ml",
                    category = ProductCategory.AROMATIZANTE_VARILLAS,
                    primaryMaterialName = "Alcohol de Cereal 96° / Vehículo (1000 ml)",
                    primaryMaterialBulkCostStr = "5500",
                    primaryMaterialBulkAmountStr = "1000",
                    primaryMaterialUsedAmountStr = "120",
                    aromaName = "Esencia Pura para Difusor (100 ml)",
                    aromaBulkCostStr = "10500",
                    aromaBulkMLStr = "100",
                    aromaUsedMLStr = "30",
                    containerCostStr = "1300",
                    secondaryComponentCostStr = "450", // Set de varillas de ratán
                    labelAndPackagingCostStr = "350",
                    laborAndOverheadCostStr = "400",
                    suggestedPublicPriceWithIvaStr = "11990",
                    notes = "Difusor de varillas versión única de 150ml (120ml alcohol de cereal + 30ml esencia)."
                )
            }
        }
    }

    // Helper to switch volume variant for Spray (100ml / 250ml)
    fun applyVolumeVariant(volumeML: Int) {
        val current = _calculatorState.value
        val isSpray = current.category == ProductCategory.SPRAY_AROMATIZANTE

        if (!isSpray) return

        if (volumeML == 100) {
            _calculatorState.update {
                it.copy(
                    name = "Spray Aromatizante 100ml",
                    primaryMaterialUsedAmountStr = "90",
                    aromaUsedMLStr = "10",
                    containerCostStr = "750",
                    suggestedPublicPriceWithIvaStr = "5990"
                )
            }
        } else if (volumeML == 250) {
            _calculatorState.update {
                it.copy(
                    name = "Spray Aromatizante 250ml",
                    primaryMaterialUsedAmountStr = "225",
                    aromaUsedMLStr = "25",
                    containerCostStr = "1250",
                    suggestedPublicPriceWithIvaStr = "9990"
                )
            }
        }
    }

    // Form Field Updates
    fun updateForm(
        name: String? = null,
        primaryMaterialName: String? = null,
        primaryMaterialBulkCostStr: String? = null,
        primaryMaterialBulkAmountStr: String? = null,
        primaryMaterialUsedAmountStr: String? = null,
        aromaName: String? = null,
        aromaBulkCostStr: String? = null,
        aromaBulkMLStr: String? = null,
        aromaUsedMLStr: String? = null,
        containerCostStr: String? = null,
        secondaryComponentCostStr: String? = null,
        labelAndPackagingCostStr: String? = null,
        laborAndOverheadCostStr: String? = null,
        suggestedPublicPriceWithIvaStr: String? = null,
        notes: String? = null
    ) {
        _calculatorState.update { current ->
            current.copy(
                name = name ?: current.name,
                primaryMaterialName = primaryMaterialName ?: current.primaryMaterialName,
                primaryMaterialBulkCostStr = primaryMaterialBulkCostStr ?: current.primaryMaterialBulkCostStr,
                primaryMaterialBulkAmountStr = primaryMaterialBulkAmountStr ?: current.primaryMaterialBulkAmountStr,
                primaryMaterialUsedAmountStr = primaryMaterialUsedAmountStr ?: current.primaryMaterialUsedAmountStr,
                aromaName = aromaName ?: current.aromaName,
                aromaBulkCostStr = aromaBulkCostStr ?: current.aromaBulkCostStr,
                aromaBulkMLStr = aromaBulkMLStr ?: current.aromaBulkMLStr,
                aromaUsedMLStr = aromaUsedMLStr ?: current.aromaUsedMLStr,
                containerCostStr = containerCostStr ?: current.containerCostStr,
                secondaryComponentCostStr = secondaryComponentCostStr ?: current.secondaryComponentCostStr,
                labelAndPackagingCostStr = labelAndPackagingCostStr ?: current.labelAndPackagingCostStr,
                laborAndOverheadCostStr = laborAndOverheadCostStr ?: current.laborAndOverheadCostStr,
                suggestedPublicPriceWithIvaStr = suggestedPublicPriceWithIvaStr ?: current.suggestedPublicPriceWithIvaStr,
                notes = notes ?: current.notes
            )
        }
    }

    // Helper to auto-suggest target price based on desired margin % (e.g. 50% or 60%)
    fun applyTargetMargin(desiredMarginPercent: Double) {
        val state = _calculatorState.value
        val cost = state.totalNetProductionCost
        if (cost <= 0) return

        // Target Net Public Price = Cost / (1 - MarginFraction)
        // Then Public Price With 19% IVA = Target Net * 1.19
        val marginFraction = (desiredMarginPercent / 100.0).coerceAtMost(0.9)
        val targetNetPrice = cost / (1.0 - marginFraction)
        val targetPublicPriceWithIva = targetNetPrice * 1.19

        // Round to nearest 100 CLP for clean pricing (e.g., 8990 or 9500)
        val roundedPrice = (Math.round(targetPublicPriceWithIva / 50.0) * 50).toDouble()

        _calculatorState.update {
            it.copy(suggestedPublicPriceWithIvaStr = roundedPrice.toInt().toString())
        }
    }

    // Save or update product
    fun saveCurrentProduct() {
        viewModelScope.launch {
            val entity = _calculatorState.value.toEntity()
            if (entity.id == 0L) {
                repository.insertProduct(entity)
                _saveSuccessMessage.value = "¡Producto guardado exitosamente en tus reportes!"
            } else {
                repository.updateProduct(entity)
                _saveSuccessMessage.value = "¡Producto actualizado exitosamente!"
            }
        }
    }

    // Load saved product into calculator for editing/review
    fun loadProductToCalculator(product: ProductEntity) {
        _calculatorState.value = CalculatorFormState(
            id = product.id,
            name = product.name,
            category = product.parsedCategory,
            primaryMaterialName = product.primaryMaterialName,
            primaryMaterialBulkCostStr = product.primaryMaterialBulkCost.toInt().toString(),
            primaryMaterialBulkAmountStr = product.primaryMaterialBulkAmount.toInt().toString(),
            primaryMaterialUsedAmountStr = product.primaryMaterialUsedAmount.toInt().toString(),
            aromaName = product.aromaName,
            aromaBulkCostStr = product.aromaBulkCost.toInt().toString(),
            aromaBulkMLStr = product.aromaBulkML.toInt().toString(),
            aromaUsedMLStr = product.aromaUsedML.toInt().toString(),
            containerCostStr = product.containerCost.toInt().toString(),
            secondaryComponentCostStr = product.secondaryComponentCost.toInt().toString(),
            labelAndPackagingCostStr = product.labelAndPackagingCost.toInt().toString(),
            laborAndOverheadCostStr = product.laborAndOverheadCost.toInt().toString(),
            suggestedPublicPriceWithIvaStr = product.suggestedPublicPriceWithIva.toInt().toString(),
            notes = product.notes
        )
        _selectedTab.value = 0 // Navigate to calculator tab
    }

    // Duplicate product
    fun duplicateProduct(product: ProductEntity) {
        viewModelScope.launch {
            val duplicate = product.copy(
                id = 0,
                name = "${product.name} (Copia)",
                updatedAt = System.currentTimeMillis()
            )
            repository.insertProduct(duplicate)
            _saveSuccessMessage.value = "¡Copia creada correctamente!"
        }
    }

    // Delete product
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _saveSuccessMessage.value = "Producto eliminado."
        }
    }

    // Reset calculator to new blank preset
    fun createNewCalculator() {
        selectCategory(ProductCategory.VELA_SOYA)
    }

    // Load AI-generated recipe suggestion into the calculator
    fun loadGeneratedRecipe(suggestion: com.example.ai.GeneratedRecipeSuggestion) {
        selectCategory(suggestion.category)
        _calculatorState.update { current ->
            current.copy(
                id = null,
                name = suggestion.name,
                primaryMaterialName = suggestion.primaryMaterialName,
                primaryMaterialUsedAmountStr = suggestion.primaryMaterialUsedAmount.toString(),
                aromaName = suggestion.aromaName,
                aromaUsedMLStr = suggestion.aromaUsedML.toString(),
                suggestedPublicPriceWithIvaStr = suggestion.suggestedPublicPriceWithIva.toString(),
                notes = suggestion.pouringOrPrepTip
            )
        }
        _saveSuccessMessage.value = "¡Receta generada por Gemini cargada en la calculadora!"
        _selectedTab.value = 0
    }
}
