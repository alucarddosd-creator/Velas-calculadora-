package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProductEntity
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProductDetailDialog
import com.example.ui.screens.ProductsScreen
import com.example.ui.theme.CostosVelasTheme
import com.example.ui.viewmodel.AiAdvisorViewModel
import com.example.ui.viewmodel.CostViewModel
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            CostosVelasTheme {
                MainAppScreen()
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun MainAppScreen(
    viewModel: CostViewModel = viewModel(),
    aiViewModel: AiAdvisorViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val calculatorState by viewModel.calculatorState.collectAsState()
    val saveMessage by viewModel.saveSuccessMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var detailProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val navItems = listOf(
        NavTabItem("Calculadora", Icons.Filled.Calculate, Icons.Outlined.Calculate, "tab_calculator"),
        NavTabItem("Productos", Icons.Filled.Inventory, Icons.Outlined.Inventory, "tab_products"),
        NavTabItem("Dashboard", Icons.Filled.Analytics, Icons.Outlined.Analytics, "tab_dashboard"),
        NavTabItem("Asesor IA", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_ai_advisor")
    )

    LaunchedEffect(saveMessage) {
        saveMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CalculatorScreen(
                    viewModel = viewModel,
                    onAnalyzeWithAi = {
                        viewModel.selectTab(3)
                        aiViewModel.selectSubTab(0)
                        aiViewModel.analyzeCurrentRecipe(calculatorState)
                    }
                )
                1 -> ProductsScreen(
                    viewModel = viewModel,
                    onViewDetail = { product -> detailProduct = product }
                )
                2 -> DashboardScreen(viewModel = viewModel)
                3 -> AiAdvisorScreen(
                    costViewModel = viewModel,
                    aiViewModel = aiViewModel
                )
            }
        }

        detailProduct?.let { product ->
            ProductDetailDialog(
                product = product,
                onDismiss = { detailProduct = null },
                onEdit = { prod ->
                    detailProduct = null
                    viewModel.loadProductToCalculator(prod)
                }
            )
        }
    }
}
