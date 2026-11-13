package com.example.model

enum class ProductCategory(
    val title: String,
    val description: String,
    val emoji: String,
    val unitLabel: String,
    val typicalSize: String
) {
    VELA_SOYA(
        title = "Vela de Soya",
        description = "Cera de soya vegetal, mecha, fragancia concentrada y contenedor",
        emoji = "🕯️",
        unitLabel = "gramos (g)",
        typicalSize = "180 g"
    ),
    SPRAY_AROMATIZANTE(
        title = "Spray Aromatizante",
        description = "Base hidroalcohólica/alcohol de cereales, esencia en ML y frasco atomizador",
        emoji = "💨",
        unitLabel = "mililitros (ml)",
        typicalSize = "100 ml"
    ),
    AROMATIZANTE_VARILLAS(
        title = "Aromatizante de Varillas",
        description = "Base vehicular difusora, esencia pura en ML, frasco y varillas de ratán",
        emoji = "🌿",
        unitLabel = "mililitros (ml)",
        typicalSize = "150 ml"
    );

    companion object {
        fun fromName(name: String): ProductCategory {
            return entries.firstOrNull { it.name == name } ?: VELA_SOYA
        }
    }
}
