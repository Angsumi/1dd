package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderStatus
import com.example.ui.screens.CloudHostingGuideScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.DeliveryPartnerScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MarketViewModel) {
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartTotalCount.collectAsStateWithLifecycle()

    val activeDeliveriesCount = allOrders.count {
        it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "LocalMart",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            modifier = Modifier.size(11.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = "1-DAY DELIVERY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Depot: 26.8384, 92.9108 (Haversine Logistics)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_navigation_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Customer Store
                NavigationBarItem(
                    selected = selectedRole == AppRole.CUSTOMER,
                    onClick = { viewModel.selectRole(AppRole.CUSTOMER) },
                    icon = {
                        if (cartCount > 0) {
                            BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                                Icon(Icons.Default.Storefront, contentDescription = "Shop")
                            }
                        } else {
                            Icon(Icons.Default.Storefront, contentDescription = "Shop")
                        }
                    },
                    label = { Text("Storefront", fontWeight = if (selectedRole == AppRole.CUSTOMER) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_customer_tab")
                )

                // Seller Admin Dashboard
                NavigationBarItem(
                    selected = selectedRole == AppRole.SELLER_ADMIN,
                    onClick = { viewModel.selectRole(AppRole.SELLER_ADMIN) },
                    icon = {
                        Icon(Icons.Default.Inventory2, contentDescription = "Seller Admin")
                    },
                    label = { Text("Seller", fontWeight = if (selectedRole == AppRole.SELLER_ADMIN) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_seller_tab")
                )

                // Delivery Partner Dashboard
                NavigationBarItem(
                    selected = selectedRole == AppRole.DELIVERY_PARTNER,
                    onClick = { viewModel.selectRole(AppRole.DELIVERY_PARTNER) },
                    icon = {
                        if (activeDeliveriesCount > 0) {
                            BadgedBox(badge = { Badge { Text("$activeDeliveriesCount") } }) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = "Delivery Partner")
                            }
                        } else {
                            Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = "Delivery Partner")
                        }
                    },
                    label = { Text("Delivery", fontWeight = if (selectedRole == AppRole.DELIVERY_PARTNER) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_delivery_tab")
                )

                // Cloud Hosting & Firebase Sync
                NavigationBarItem(
                    selected = selectedRole == AppRole.CLOUD_HOSTING,
                    onClick = { viewModel.selectRole(AppRole.CLOUD_HOSTING) },
                    icon = {
                        Icon(Icons.Default.Cloud, contentDescription = "Cloud Hosting")
                    },
                    label = { Text("Hosting", fontWeight = if (selectedRole == AppRole.CLOUD_HOSTING) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_hosting_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedRole,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "role_navigation"
            ) { role ->
                when (role) {
                    AppRole.CUSTOMER -> CustomerHomeScreen(viewModel = viewModel)
                    AppRole.SELLER_ADMIN -> SellerDashboardScreen(viewModel = viewModel)
                    AppRole.DELIVERY_PARTNER -> DeliveryPartnerScreen(viewModel = viewModel)
                    AppRole.CLOUD_HOSTING -> CloudHostingGuideScreen(viewModel = viewModel)
                }
            }
        }
    }
}
