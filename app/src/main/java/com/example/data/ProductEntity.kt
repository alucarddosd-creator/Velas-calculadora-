package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ProductCategory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // VELA_SOYA, SPRAY_AROMATIZANTE, AROMATIZANTE_VARILLAS
    val primaryMaterialName: String = "Materia Prima Base",
    val primaryMaterialBulkCost: Double = 0.0,
    val primaryMaterialBulkAmount: Double = 1.0,
    val primaryMaterialUsedAmount: Double = 0.0,
    val aromaName: String = "Esencia / Aroma",
    val aromaBulkCost: Double = 0.0,
    val aromaBulkML: Double = 1.0,
    val aromaUsedML: Double = 0.0,
    val containerCost: Double = 0.0,
    val secondaryComponentCost: Double = 0.0,
    val labelAndPackagingCost: Double = 0.0,
    val laborAndOverheadCost: Double = 0.0,
    val suggestedPublicPriceWithIva: Double = 0.0,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val parsedCategory: ProductCategory
        get() = ProductCategory.fromName(category)

    val calculatedPrimaryCost: Double
        get() = if (primaryMaterialBulkAmount > 0) {
            (primaryMaterialBulkCost / primaryMaterialBulkAmount) * primaryMaterialUsedAmount
        } else 0.0

    val calculatedAromaCost: Double
        get() = if (aromaBulkML > 0) {
            (aromaBulkCost / aromaBulkML) * aromaUsedML
        } else 0.0

    val totalNetProductionCost: Double
        get() = calculatedPrimaryCost + calculatedAromaCost + containerCost +
                secondaryComponentCost + labelAndPackagingCost + laborAndOverheadCost

    // Chilean IVA calculation (19% / factor 1.19)
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
}
