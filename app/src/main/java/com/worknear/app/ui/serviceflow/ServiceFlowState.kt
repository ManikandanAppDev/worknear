package com.worknear.app.ui.serviceflow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.worknear.app.data.model.CatalogService
import com.worknear.app.data.model.ServiceCatalog

/** A line item in the service cart. */
data class CartItem(
    val service: CatalogService,
    val quantity: Int = 1,
    val typeId: String? = null
) {
    /** Per-unit price including the chosen variant delta. */
    val unitPrice: Int
        get() {
            val delta = service.types.firstOrNull { it.id == typeId }?.priceDelta ?: 0
            return service.price + delta
        }

    val lineTotal: Int get() = unitPrice * quantity
}

/**
 * In-memory state shared across the multi-service booking flow
 * (Service Builder -> Configure -> Best Matches). Kept process-local for the design flow.
 */
object ServiceFlowState {

    var categoryId by mutableStateOf("electrician")
        private set

    /** Currently selected professional id on the Best Matches screen. */
    var selectedProId by mutableStateOf<String?>(null)

    /** Scheduling selection. */
    var scheduledDate by mutableStateOf<String?>(null)
    var scheduledTime by mutableStateOf<String?>(null)
    var notes by mutableStateOf("")
    var address by mutableStateOf("12, Park Street, Chennai - 600001")

    /** serviceId -> CartItem */
    val cart: SnapshotStateMap<String, CartItem> = mutableStateMapOf()

    /** Fixed inspection / visit fee added to the wallet lock. */
    const val VISIT_FEE = 149

    fun startCategory(id: String) {
        if (id != categoryId) {
            categoryId = id
            cart.clear()
            selectedProId = null
        }
    }

    fun isSelected(serviceId: String): Boolean = cart.containsKey(serviceId)

    /** Adds the service to the cart if not already present (idempotent). */
    fun ensureSelected(service: CatalogService) {
        if (!cart.containsKey(service.id)) toggle(service)
    }

    fun toggle(service: CatalogService) {
        if (cart.containsKey(service.id)) {
            cart.remove(service.id)
        } else {
            val defaultType = service.types.firstOrNull()?.id
            cart[service.id] = CartItem(service = service, quantity = 1, typeId = defaultType)
        }
    }

    fun setQuantity(serviceId: String, quantity: Int) {
        val item = cart[serviceId] ?: return
        if (quantity <= 0) {
            cart.remove(serviceId)
        } else {
            cart[serviceId] = item.copy(quantity = quantity)
        }
    }

    fun setType(serviceId: String, typeId: String) {
        val item = cart[serviceId] ?: return
        cart[serviceId] = item.copy(typeId = typeId)
    }

    val selectedCount: Int get() = cart.size

    val servicesTotal: Int get() = cart.values.sumOf { it.lineTotal }

    /** Total amount locked on the wallet when the booking is confirmed. */
    val lockAmount: Int get() = if (cart.isEmpty()) 0 else servicesTotal + VISIT_FEE

    fun reset() {
        cart.clear()
        selectedProId = null
        scheduledDate = null
        scheduledTime = null
        notes = ""
    }

    val selectedPro: MatchProfessional?
        get() = MatchData.professionals.firstOrNull { it.id == selectedProId }

    fun seedSampleIfEmpty() {
        if (cart.isEmpty()) {
            val services = ServiceCatalog.servicesFor(categoryId)
            services.take(3).forEach { toggle(it) }
        }
    }
}
