package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velas_costos_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.productDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: ProductDao) {
                // Sample Soy Candle
                dao.insertProduct(
                    ProductEntity(
                        name = "Vela Soya Lavanda & Vainilla 180g",
                        category = "VELA_SOYA",
                        primaryMaterialName = "Cera de Soya APF (1 Kg)",
                        primaryMaterialBulkCost = 8500.0,
                        primaryMaterialBulkAmount = 1000.0,
                        primaryMaterialUsedAmount = 162.0, // 90% cera
                        aromaName = "Esencia Lavanda Vainilla (100 ml)",
                        aromaBulkCost = 9800.0,
                        aromaBulkML = 100.0,
                        aromaUsedML = 18.0, // 10% esencia
                        containerCost = 850.0, // Frasco de vidrio
                        secondaryComponentCost = 180.0, // Mecha de algodón con pabilo
                        labelAndPackagingCost = 300.0, // Etiqueta + Caja
                        laborAndOverheadCost = 500.0, // Mano de obra y energía
                        suggestedPublicPriceWithIva = 9990.0,
                        notes = "Mezcla recomendada 10% fragancia a 65°C."
                    )
                )

                // Sample Home Spray
                dao.insertProduct(
                    ProductEntity(
                        name = "Spray Textil Citronela & Té Verde 100ml",
                        category = "SPRAY_AROMATIZANTE",
                        primaryMaterialName = "Base Alcohol Cereal + Agua (1000 ml)",
                        primaryMaterialBulkCost = 4500.0,
                        primaryMaterialBulkAmount = 1000.0,
                        primaryMaterialUsedAmount = 95.0,
                        aromaName = "Esencia Citronela (100 ml)",
                        aromaBulkCost = 8900.0,
                        aromaBulkML = 100.0,
                        aromaUsedML = 5.0, // 5% fragancia
                        containerCost = 750.0, // Frasco PET + Válvula Spray
                        secondaryComponentCost = 100.0,
                        labelAndPackagingCost = 250.0,
                        laborAndOverheadCost = 300.0,
                        suggestedPublicPriceWithIva = 5990.0,
                        notes = "Ideal para ambientes y textiles."
                    )
                )

                // Sample Reed Diffuser
                dao.insertProduct(
                    ProductEntity(
                        name = "Difusor de Varillas Frutos Rojos 150ml",
                        category = "AROMATIZANTE_VARILLAS",
                        primaryMaterialName = "Base Vehículo Difusor DPG (1000 ml)",
                        primaryMaterialBulkCost = 6200.0,
                        primaryMaterialBulkAmount = 1000.0,
                        primaryMaterialUsedAmount = 120.0,
                        aromaName = "Esencia Frutos Rojos (100 ml)",
                        aromaBulkCost = 10500.0,
                        aromaBulkML = 100.0,
                        aromaUsedML = 30.0, // 20% esencia concentrada
                        containerCost = 1200.0, // Frasco Difusor Vidrio
                        secondaryComponentCost = 450.0, // Set x6 Varillas Ratán
                        labelAndPackagingCost = 350.0,
                        laborAndOverheadCost = 400.0,
                        suggestedPublicPriceWithIva = 11990.0,
                        notes = "Aroma duradero de alta dispersión."
                    )
                )
            }
        }
    }
}
