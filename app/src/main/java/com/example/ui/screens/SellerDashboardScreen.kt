package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.ui.components.ExpressDeliveryBadge
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.dialPhoneNumber
import com.example.ui.viewmodel.MarketViewModel

// Preset sample photo choices for easy admin catalog uploading
val ADMIN_SAMPLE_PHOTOS = listOf(
    "Spinach / Greens" to "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80",
    "Joha Aromatic Rice" to "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80",
    "King Chili / Pepper" to "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=600&auto=format&fit=crop&q=80",
    "Eggs Tray" to "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=600&auto=format&fit=crop&q=80",
    "Pure Milk Bottle" to "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80",
    "Fresh Tomatoes" to "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&auto=format&fit=crop&q=80",
    "Assam Tea Tin" to "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80",
    "Raw Honey Jar" to "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80",
    "Mustard Oil" to "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop&q=80",
    "Organic Bananas" to "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop&q=80"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Inventory, 1 = Orders
    var showAddEditSheet by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var orderStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val lowStockCount = products.count { it.stockQuantity in 1..5 }
    val totalRevenue = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        productToEdit = null
                        showAddEditSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Item")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Header & Key Metrics
            AdminMetricsHeader(
                totalProducts = products.size,
                lowStockCount = lowStockCount,
                totalOrders = orders.size,
                revenue = totalRevenue
            )

            // Tab Navigation (Inventory vs Orders)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Inventory (${products.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Orders (${orders.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tab Content
            if (selectedTab == 0) {
                // Inventory Management View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Item Catalog & Stock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    productToEdit = null
                                    showAddEditSheet = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Product", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (products.isEmpty()) {
                        item {
                            EmptyAdminInventory {
                                productToEdit = null
                                showAddEditSheet = true
                            }
                        }
                    } else {
                        items(products, key = { it.id }) { product ->
                            AdminProductItemCard(
                                product = product,
                                onEdit = {
                                    productToEdit = product
                                    showAddEditSheet = true
                                },
                                onDelete = {
                                    viewModel.deleteProduct(product.id)
                                    Toast.makeText(context, "Deleted ${product.title}", Toast.LENGTH_SHORT).show()
                                },
                                onStockChange = { newStock ->
                                    viewModel.updateStock(product.id, newStock)
                                },
                                onToggleAvailability = { available ->
                                    viewModel.toggleAvailability(product.id, available)
                                }
                            )
                        }
                    }
                }
            } else {
                // Orders Management View
                val filteredOrders = if (orderStatusFilter == null) orders else orders.filter { it.status == orderStatusFilter }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status Filter Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = orderStatusFilter == null,
                                    onClick = { orderStatusFilter = null },
                                    label = { Text("All (${orders.size})") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            items(OrderStatus.values()) { status ->
                                val count = orders.count { it.status == status }
                                FilterChip(
                                    selected = orderStatusFilter == status,
                                    onClick = { orderStatusFilter = status },
                                    label = { Text("${status.label} ($count)") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    if (filteredOrders.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No orders in this status", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        items(filteredOrders, key = { it.id }) { order ->
                            AdminOrderItemCard(
                                order = order,
                                onUpdateStatus = { nextStatus ->
                                    viewModel.updateOrderStatus(order.id, nextStatus)
                                    Toast.makeText(context, "Order #${order.orderNumber} updated to ${nextStatus.label}", Toast.LENGTH_SHORT).show()
                                },
                                onCallCustomer = { dialPhoneNumber(context, order.customerPhone) }
                            )
                        }
                    }
                }
            }
        }

        // Add/Edit Product Bottom Sheet Modal
        if (showAddEditSheet) {
            AddEditProductSheet(
                product = productToEdit,
                onDismiss = { showAddEditSheet = false },
                onSave = { id, title, desc, price, unit, cat, stock, img, isSpecial ->
                    viewModel.saveProduct(id, title, desc, price, unit, cat, stock, img, isSpecial)
                    showAddEditSheet = false
                    Toast.makeText(context, if (id == null) "Added $title" else "Updated $title", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun AdminMetricsHeader(
    totalProducts: Int,
    lowStockCount: Int,
    totalOrders: Int,
    revenue: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("admin_metrics_header"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Store Admin Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Depot: 26.838432, 92.910880 • 1-Day Logistics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Local Seller",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniCard(
                    title = "Revenue",
                    value = "₹${String.format("%.0f", revenue)}",
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Orders",
                    value = "$totalOrders",
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "In Catalog",
                    value = "$totalProducts",
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Low Stock",
                    value = "$lowStockCount",
                    valueColor = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun AdminProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockChange: (Int) -> Unit,
    onToggleAvailability: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_product_card_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Sell,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${String.format("%.0f", product.price)} • ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Stock:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { if (product.stockQuantity > 0) onStockChange(product.stockQuantity - 1) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Text("-", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${product.stockQuantity}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onStockChange(product.stockQuantity + 1) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Text("+", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Actions
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (product.isAvailable) "Active" else "Hidden",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (product.isAvailable) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = product.isAvailable,
                        onCheckedChange = onToggleAvailability,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderItemCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onCallCustomer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${order.customerName} • ${order.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "📍 Delivery: ${order.landmarkName.ifBlank { order.deliveryAddress }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "📏 Haversine Distance: ${String.format("%.2f", order.distanceKm)} km from Depot",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    order.items.forEach { item ->
                        Text(
                            text = "• ${item.quantity}x ${item.productTitle} (${item.unit}) - ₹${String.format("%.0f", item.totalItemPrice)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Bill (${order.paymentMethod}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${String.format("%.0f", order.totalAmount)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status advancement buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCallCustomer,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Customer", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                when (order.status) {
                    OrderStatus.PLACED -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PACKED) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pack & Ready", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    OrderStatus.PACKED -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.OUT_FOR_DELIVERY) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dispatch Driver", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    OrderStatus.OUT_FOR_DELIVERY -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delivered", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    OrderStatus.DELIVERED -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = "Order Completed",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    OrderStatus.CANCELLED -> {
                        Text("Order Cancelled", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductSheet(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, desc: String, price: Double, unit: String, cat: ProductCategory, stock: Int, img: String, isSpecial: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.let { "%.0f".format(it) } ?: "50") }
    var unit by remember { mutableStateOf(product?.unit ?: "1 kg") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: ProductCategory.VEGETABLES) }
    var stockText by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "20") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: ADMIN_SAMPLE_PHOTOS[0].second) }
    var isLocalSpecial by remember { mutableStateOf(product?.isLocalSpecialty ?: true) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("add_edit_product_sheet"),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (product == null) "Add New Market Item" else "Edit Item Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Preview & Preset Picker
                Text(
                    text = "Product Photo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Quick photo preset pickers:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    items(ADMIN_SAMPLE_PHOTOS) { (name, url) ->
                        FilterChip(
                            selected = imageUrl == url,
                            onClick = { imageUrl = url },
                            label = { Text(name, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Or custom Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title *") },
                    placeholder = { Text("e.g. Fresh Organic Spinach (Palak)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_item_title_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category & Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false }
                        ) {
                            ProductCategory.values().filter { it != ProductCategory.ALL }.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Unit
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit / Pack") },
                        placeholder = { Text("1 kg, 500g, Tray of 12") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price & Initial Stock Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (₹) *") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_item_price_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock Qty *") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_item_stock_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Fresh harvest from local co-op, guaranteed 1-day freshness...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_item_desc_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val parsedPrice = priceText.toDoubleOrNull() ?: 50.0
                        val parsedStock = stockText.toIntOrNull() ?: 20
                        if (title.isNotBlank()) {
                            onSave(
                                product?.id,
                                title,
                                description,
                                parsedPrice,
                                unit,
                                selectedCategory,
                                parsedStock,
                                imageUrl,
                                isLocalSpecial
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_product_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (product == null) "Add Item to Market" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAdminInventory(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Your store inventory is empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add photos, descriptions and set stock levels to start selling locally with 1-day delivery.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add First Item")
        }
    }
}
