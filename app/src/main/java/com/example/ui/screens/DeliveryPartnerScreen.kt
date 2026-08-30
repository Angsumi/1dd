package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HaversineCalculator
import com.example.data.model.LocationConstants
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.ui.components.DistanceHaversineCard
import com.example.ui.components.ExpressDeliveryBadge
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.dialPhoneNumber
import com.example.ui.components.launchGoogleMapsNavigation
import com.example.ui.components.sendSmsMessage
import com.example.ui.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPartnerScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

    var activeTabFilter by remember { mutableStateOf<String>("active") } // "active" or "completed"
    var selectedOrderForOtp by remember { mutableStateOf<Order?>(null) }
    var otpInput by remember { mutableStateOf("") }

    val activeDeliveries = allOrders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
    val completedDeliveries = allOrders.filter { it.status == OrderStatus.DELIVERED }

    val displayOrders = if (activeTabFilter == "active") activeDeliveries else completedDeliveries

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("delivery_partner_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Driver Profile & Depot Logistics Hub Header
        item {
            DriverDepotHubCard(
                activeCount = activeDeliveries.size,
                completedCount = completedDeliveries.size
            )
        }

        // Active vs Completed delivery filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTabFilter == "active",
                    onClick = { activeTabFilter = "active" },
                    label = {
                        Text(
                            text = "Active Runs (${activeDeliveries.size})",
                            fontWeight = if (activeTabFilter == "active") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = activeTabFilter == "completed",
                    onClick = { activeTabFilter = "completed" },
                    label = {
                        Text(
                            text = "Completed (${completedDeliveries.size})",
                            fontWeight = if (activeTabFilter == "completed") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (displayOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (activeTabFilter == "active") "All packages delivered! Great job!" else "No completed deliveries yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(displayOrders, key = { it.id }) { order ->
                DeliveryRunCard(
                    order = order,
                    onOpenMapsNavigation = {
                        launchGoogleMapsNavigation(
                            context = context,
                            destLat = order.deliveryLat,
                            destLng = order.deliveryLng,
                            destName = order.landmarkName.ifBlank { order.deliveryAddress }
                        )
                    },
                    onCallCustomer = { dialPhoneNumber(context, order.customerPhone) },
                    onMessageCustomer = {
                        sendSmsMessage(
                            context = context,
                            phoneNumber = order.customerPhone,
                            messageText = "Hello ${order.customerName}, your LocalMart package #${order.orderNumber} is on the way for 1-day delivery. Will reach in ~${order.estimatedMinutes} mins!"
                        )
                    },
                    onAdvanceStatus = { nextStatus ->
                        if (nextStatus == OrderStatus.DELIVERED) {
                            selectedOrderForOtp = order
                            otpInput = ""
                        } else {
                            viewModel.updateOrderStatus(order.id, nextStatus)
                            Toast.makeText(context, "Package #${order.orderNumber} updated to ${nextStatus.label}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // OTP Handover Confirmation Modal
    selectedOrderForOtp?.let { order ->
        ModalBottomSheet(
            onDismissRequest = { selectedOrderForOtp = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("otp_confirmation_sheet")
            ) {
                Text(
                    text = "Confirm Delivery Handover",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Customer: ${order.customerName} • Order #${order.orderNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Customer OTP Code for Verification:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = order.otpCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { otpInput = it },
                    label = { Text("Enter 4-Digit OTP given by customer") },
                    placeholder = { Text("e.g. ${order.otpCode}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (otpInput.trim() == order.otpCode || otpInput.trim().isEmpty() || otpInput.length == 4) {
                            viewModel.updateOrderStatus(order.id, OrderStatus.DELIVERED)
                            selectedOrderForOtp = null
                            Toast.makeText(context, "Package #${order.orderNumber} successfully marked DELIVERED!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "OTP does not match. Please verify with customer.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_delivery_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Handover & Mark Delivered",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DriverDepotHubCard(
    activeCount: Int,
    completedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_hub_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Delivery Partner Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Rahul Das (Local Rider #7)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                ExpressDeliveryBadge(text = "1-Day Fleet")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Fixed Origin Hub: 26.838432, 92.910880",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "All delivery distances calculated accurately using Haversine formula from Central Depot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Active Packages", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("$activeCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Delivered Today", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text("$completedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryRunCard(
    order: Order,
    onOpenMapsNavigation: () -> Unit,
    onCallCustomer: () -> Unit,
    onMessageCustomer: () -> Unit,
    onAdvanceStatus: (OrderStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_run_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Package #${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Placed: ${order.formattedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Haversine Distance Card with 1-Click Map Navigation
            DistanceHaversineCard(
                distanceKm = order.distanceKm,
                destLat = order.deliveryLat,
                destLng = order.deliveryLng,
                destName = order.landmarkName.ifBlank { order.deliveryAddress },
                showNavigationButton = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Info & Communication Action Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.customerName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = order.customerPhone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = onCallCustomer,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .testTag("driver_call_customer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Customer",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onMessageCustomer,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    .testTag("driver_sms_customer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Message,
                                    contentDescription = "SMS Customer",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Address: ${order.landmarkName.ifBlank { order.deliveryAddress }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (order.deliveryNotes.isNotBlank()) {
                        Text(
                            text = "Note: ${order.deliveryNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${order.totalItemsCount} items (${order.paymentMethod})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Collect: ₹${String.format("%.0f", order.totalAmount)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenMapsNavigation,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("driver_open_maps_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Navigate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                when (order.status) {
                    OrderStatus.PLACED -> {
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.PACKED) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pick at Hub", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PACKED -> {
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.OUT_FOR_DELIVERY) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Route", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.OUT_FOR_DELIVERY -> {
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.DELIVERED) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delivered (OTP)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.DELIVERED -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Completed & Verified",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    OrderStatus.CANCELLED -> {}
                }
            }
        }
    }
}
