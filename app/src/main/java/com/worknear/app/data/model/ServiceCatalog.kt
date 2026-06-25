package com.worknear.app.data.model

import androidx.annotation.DrawableRes
import com.worknear.app.R

/** A selectable service variant (e.g. light type: Normal / Fancy / Chandelier). */
data class ServiceType(
    val id: String,
    val label: String,
    val priceDelta: Int = 0
)

/** A single bookable service inside a category. */
data class CatalogService(
    val id: String,
    val name: String,
    val subtitle: String,
    val price: Int,
    @DrawableRes val iconRes: Int,
    val priceFrom: Boolean = false,
    val types: List<ServiceType> = emptyList()
) {
    val hasTypes: Boolean get() = types.isNotEmpty()
}

/** A service tile shown on the Home "Popular Services" grid. */
data class HomeServiceTile(
    val categoryId: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

/** Filter chip inside the service builder. */
data class ServiceChip(val id: String, val label: String)

object ServiceCatalog {

    val homeTiles = listOf(
        HomeServiceTile("cleaning", "Cleaning", R.drawable.ic_wn_cleaning),
        HomeServiceTile("ac", "AC Repair", R.drawable.ic_wn_ac),
        HomeServiceTile("plumber", "Plumber", R.drawable.ic_wn_plumber),
        HomeServiceTile("electrician", "Electrician", R.drawable.ic_wn_electrician),
        HomeServiceTile("painting", "Painting", R.drawable.ic_wn_painting),
        HomeServiceTile("appliance", "Appliance Repair", R.drawable.ic_wn_appliance),
        HomeServiceTile("carpenter", "Carpenter", R.drawable.ic_wn_carpenter),
        HomeServiceTile("more", "More", R.drawable.ic_wn_more)
    )

    fun titleFor(categoryId: String): String =
        homeTiles.firstOrNull { it.categoryId == categoryId }?.label
            ?: categoryId.replaceFirstChar { it.uppercase() }

    fun chipsFor(categoryId: String): List<ServiceChip> = when (categoryId) {
        "electrician" -> chips("Popular", "Fan", "Light", "Switch", "Wiring")
        "cleaning" -> chips("Popular", "Bathroom", "Kitchen", "Home", "Sofa")
        "ac" -> chips("Popular", "Service", "Install", "Gas", "Repair")
        "plumber" -> chips("Popular", "Tap", "Pipe", "Geyser", "Basin")
        "painting" -> chips("Popular", "Interior", "Exterior", "Texture", "Waterproof")
        "appliance" -> chips("Popular", "Washer", "Fridge", "TV", "Microwave")
        "carpenter" -> chips("Popular", "Furniture", "Door", "Bed", "Locks")
        else -> chips("Popular")
    }

    private fun chips(vararg labels: String): List<ServiceChip> =
        labels.map { ServiceChip(it.lowercase(), it) }

    /** Returns the bookable service list for a category. */
    fun servicesFor(categoryId: String): List<CatalogService> = when (categoryId) {
        "electrician" -> electricalServices
        "cleaning" -> cleaningServices
        "ac" -> acServices
        "plumber" -> plumberServices
        "painting" -> paintingServices
        "appliance" -> applianceServices
        "carpenter" -> carpenterServices
        else -> generalServices
    }

    private val electricalServices = listOf(
        CatalogService(
            id = "fan_install",
            name = "Fan installation",
            subtitle = "Standard ceiling fan",
            price = 249,
            iconRes = R.drawable.ic_wn_fan
        ),
        CatalogService(
            id = "light_work",
            name = "Light work",
            subtitle = "Installation / repair",
            price = 149,
            iconRes = R.drawable.ic_wn_bulb,
            priceFrom = true,
            types = listOf(
                ServiceType("normal", "Normal", 0),
                ServiceType("fancy", "Fancy", 120),
                ServiceType("chandelier", "Chandelier", 400)
            )
        ),
        CatalogService(
            id = "switch_replace",
            name = "Switch replacement",
            subtitle = "Replace old switches",
            price = 199,
            iconRes = R.drawable.ic_wn_switch
        ),
        CatalogService(
            id = "wiring_inspect",
            name = "Wiring inspection",
            subtitle = "Home electrical check",
            price = 149,
            iconRes = R.drawable.ic_wn_wiring,
            priceFrom = true
        ),
        CatalogService(
            id = "mcb_fuse",
            name = "MCB / Fuse replacement",
            subtitle = "Restore tripping circuits",
            price = 249,
            iconRes = R.drawable.ic_wn_mcb
        ),
        CatalogService(
            id = "wall_socket",
            name = "Wall socket installation",
            subtitle = "New power points",
            price = 199,
            iconRes = R.drawable.ic_wn_socket
        )
    )

    private val cleaningServices = listOf(
        CatalogService("bathroom_clean", "Bathroom cleaning", "Deep clean & sanitize", 399, R.drawable.ic_wn_toilet),
        CatalogService("kitchen_clean", "Kitchen cleaning", "Degrease & sanitize", 449, R.drawable.ic_wn_cabinet),
        CatalogService("full_home_clean", "Full home cleaning", "Top-to-bottom clean", 1499, R.drawable.ic_wn_home, priceFrom = true),
        CatalogService("sofa_clean", "Sofa cleaning", "Per seat shampoo", 199, R.drawable.ic_wn_sofa, priceFrom = true),
        CatalogService("bedroom_clean", "Bedroom cleaning", "Dusting & mopping", 349, R.drawable.ic_wn_bed)
    )

    private val acServices = listOf(
        CatalogService("ac_service", "AC service", "Cooling & filter clean", 499, R.drawable.ic_wn_ac),
        CatalogService("ac_install", "AC installation", "Split / window fit", 1299, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("ac_gas", "AC gas refill", "Top-up refrigerant", 1999, R.drawable.ic_wn_canister, priceFrom = true),
        CatalogService("ac_repair", "AC repair", "Diagnose & fix", 299, R.drawable.ic_wn_carpenter, priceFrom = true),
        CatalogService("ac_deep_clean", "AC deep clean", "Jet wash service", 699, R.drawable.ic_wn_sparkle)
    )

    private val plumberServices = listOf(
        CatalogService("tap_repair", "Tap repair", "Fix leaking taps", 149, R.drawable.ic_wn_tap),
        CatalogService("pipe_leak", "Pipe leakage", "Seal & replace", 249, R.drawable.ic_wn_pipe, priceFrom = true),
        CatalogService("geyser_install", "Geyser installation", "Wall-mount fit", 499, R.drawable.ic_wn_geyser),
        CatalogService("basin_work", "Wash basin work", "Install / unclog", 349, R.drawable.ic_wn_washbasin),
        CatalogService("toilet_repair", "Toilet / flush repair", "Fix flush & leaks", 299, R.drawable.ic_wn_flush)
    )

    private val paintingServices = listOf(
        CatalogService("interior_paint", "Interior painting", "Per room / sq.ft", 2999, R.drawable.ic_wn_painting, priceFrom = true),
        CatalogService("exterior_paint", "Exterior painting", "Weather-proof coat", 4999, R.drawable.ic_wn_home, priceFrom = true),
        CatalogService("wall_texture", "Wall texture / POP", "Designer finish", 3999, R.drawable.ic_wn_texture, priceFrom = true),
        CatalogService("waterproofing", "Waterproofing", "Leak & damp proofing", 3499, R.drawable.ic_wn_waterproof, priceFrom = true),
        CatalogService("touch_up", "Touch-up painting", "Patch & repair", 799, R.drawable.ic_wn_brush)
    )

    private val applianceServices = listOf(
        CatalogService("washing_machine", "Washing machine", "Repair & service", 299, R.drawable.ic_wn_appliance, priceFrom = true),
        CatalogService("refrigerator", "Refrigerator", "Cooling & repair", 349, R.drawable.ic_wn_fridge, priceFrom = true),
        CatalogService("microwave", "Microwave", "Heating issues", 299, R.drawable.ic_wn_microwave, priceFrom = true),
        CatalogService("television", "Television", "Display & sound", 399, R.drawable.ic_wn_tv, priceFrom = true),
        CatalogService("geyser_repair", "Geyser repair", "No hot water fix", 299, R.drawable.ic_wn_geyser, priceFrom = true)
    )

    private val carpenterServices = listOf(
        CatalogService("furniture_repair", "Furniture repair", "Fix & polish", 299, R.drawable.ic_wn_sofa, priceFrom = true),
        CatalogService("door_repair", "Door repair", "Hinges & alignment", 349, R.drawable.ic_wn_door),
        CatalogService("bed_cot", "Bed / cot work", "Assemble & repair", 499, R.drawable.ic_wn_bed),
        CatalogService("drawer_lock", "Drawer & locks", "Install / replace", 249, R.drawable.ic_wn_drawer),
        CatalogService("shelf_install", "Shelf installation", "Wall-mount fit", 399, R.drawable.ic_wn_shelf)
    )

    private val generalServices = listOf(
        CatalogService("general_repair", "General repair", "Handyman home visit", 199, R.drawable.ic_wn_gear, priceFrom = true),
        CatalogService("deep_clean", "Home deep clean", "Full home sanitize", 1799, R.drawable.ic_wn_cleaning, priceFrom = true),
        CatalogService("appliance_install", "Appliance install", "Mount & setup", 499, R.drawable.ic_wn_appliance),
        CatalogService("electrical_check", "Electrical check", "Safety inspection", 149, R.drawable.ic_wn_guarantee)
    )
}
