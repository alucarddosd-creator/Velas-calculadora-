package com.example.ai

import com.example.model.ProductCategory

sealed interface AiTextUiState {
    data object Idle : AiTextUiState
    data object Loading : AiTextUiState
    data class Streaming(val text: String) : AiTextUiState
    data class Success(val text: String) : AiTextUiState
    data class Error(val message: String) : AiTextUiState
}

data class GeneratedRecipeSuggestion(
    val name: String,
    val category: ProductCategory,
    val primaryMaterialName: String,
    val primaryMaterialUsedAmount: Int,
    val aromaName: String,
    val aromaUsedML: Int,
    val suggestedPublicPriceWithIva: Int,
    val pouringOrPrepTip: String,
    val marketingPitch: String
)

sealed interface RecipeGeneratorUiState {
    data object Idle : RecipeGeneratorUiState
    data object Loading : RecipeGeneratorUiState
    data class Success(val suggestion: GeneratedRecipeSuggestion) : RecipeGeneratorUiState
    data class Error(val message: String) : RecipeGeneratorUiState
}

data class AiChatMessage(
    val id: Long = System.nanoTime(),
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

sealed interface AiChatUiState {
    data object Idle : AiChatUiState
    data object Sending : AiChatUiState
    data class Error(val message: String) : AiChatUiState
}
