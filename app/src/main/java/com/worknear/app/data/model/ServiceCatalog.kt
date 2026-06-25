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
            ?: prettifySlug(categoryId)

    /** Turns a backend slug like "painting-water-proofing" into "Painting Water Proofing". */
    fun prettifySlug(slug: String): String =
        slug.split('-', '_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    /** Maps a backend category slug to a local icon, with a sensible fallback. */
    fun iconFor(slug: String): Int = when (slug) {
        "ac" -> R.drawable.ic_wn_ac
        "bathroom-cleaning" -> R.drawable.ic_wn_bathroom
        "carpenter" -> R.drawable.ic_wn_carpenter
        "chimney" -> R.drawable.ic_wn_appliance
        "electrician" -> R.drawable.ic_wn_electrician
        "fan-installation" -> R.drawable.ic_wn_fan
        "festival-lights-installation" -> R.drawable.ic_wn_bulb
        "full-home-cleaning" -> R.drawable.ic_wn_cleaning
        "furniture-assembly" -> R.drawable.ic_wn_furniture
        "geyser", "geyser-service-repair" -> R.drawable.ic_wn_geyser
        "kitchen-cleaning" -> R.drawable.ic_wn_kitchen_clean
        "laptop" -> R.drawable.ic_wn_laptop
        "living-bedroom-cleaning" -> R.drawable.ic_wn_bed
        "microwave" -> R.drawable.ic_wn_microwave
        "painting-water-proofing", "painting" -> R.drawable.ic_wn_painting
        "plumber" -> R.drawable.ic_wn_plumber
        "refrigerator" -> R.drawable.ic_wn_fridge
        "ro-water-purifier" -> R.drawable.ic_wn_purifier
        "stove" -> R.drawable.ic_wn_stove
        "television" -> R.drawable.ic_wn_tv
        "washing-machine" -> R.drawable.ic_wn_washing_machine
        "cleaning" -> R.drawable.ic_wn_cleaning
        "appliance" -> R.drawable.ic_wn_appliance
        else -> R.drawable.ic_wn_gear
    }

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

    /** Returns the bookable service list for a backend category slug. */
    fun servicesFor(categoryId: String): List<CatalogService> = when (categoryId) {
        "electrician" -> electricalServices
        "fan-installation" -> fanInstallServices
        "festival-lights-installation" -> festivalLightsServices
        "plumber" -> plumberServices
        "geyser", "geyser-service-repair" -> geyserServices
        "ac" -> acServices
        "painting-water-proofing", "painting" -> paintingServices
        "carpenter" -> carpenterServices
        "furniture-assembly" -> furnitureAssemblyServices
        "bathroom-cleaning" -> bathroomCleaningServices
        "kitchen-cleaning" -> kitchenCleaningServices
        "living-bedroom-cleaning" -> livingCleaningServices
        "full-home-cleaning" -> fullHomeCleaningServices
        "washing-machine" -> washingMachineServices
        "refrigerator" -> refrigeratorServices
        "television" -> televisionServices
        "chimney" -> chimneyServices
        "microwave" -> microwaveServices
        "stove" -> stoveServices
        "laptop" -> laptopServices
        "ro-water-purifier" -> roServices
        // legacy app slugs (kept for backward compatibility)
        "cleaning" -> cleaningServices
        "appliance" -> applianceServices
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

    private val fanInstallServices = listOf(
        CatalogService("ceiling_fan", "Ceiling fan", "Install / replace", 249, R.drawable.ic_wn_fan),
        CatalogService("exhaust_fan", "Exhaust fan", "Kitchen / bathroom", 299, R.drawable.ic_wn_fan),
        CatalogService("wall_fan", "Wall fan", "Mount & wire", 199, R.drawable.ic_wn_fan),
        CatalogService("fan_repair", "Fan repair", "Noise / not running", 199, R.drawable.ic_wn_tools, priceFrom = true)
    )

    private val festivalLightsServices = listOf(
        CatalogService("diwali_lights", "Diwali lights", "Decorative lighting", 599, R.drawable.ic_wn_bulb, priceFrom = true),
        CatalogService("event_lights", "Event / wedding", "Full setup", 1499, R.drawable.ic_wn_bulb, priceFrom = true),
        CatalogService("string_lights", "String / serial lights", "Balcony & windows", 399, R.drawable.ic_wn_bulb, priceFrom = true),
        CatalogService("lights_removal", "Removal & pack", "Take-down service", 299, R.drawable.ic_wn_tools)
    )

    private val geyserServices = listOf(
        CatalogService("geyser_install", "Installation", "Wall-mount fit", 499, R.drawable.ic_wn_geyser),
        CatalogService("geyser_no_heat", "Not heating", "Element / thermostat", 299, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("geyser_leak", "Leakage fix", "Tank / pipe leak", 249, R.drawable.ic_wn_pipe, priceFrom = true),
        CatalogService("geyser_service", "General service", "Descale & check", 349, R.drawable.ic_wn_gear)
    )

    private val furnitureAssemblyServices = listOf(
        CatalogService("bed_assembly", "Bed / cot", "Assemble & fix", 499, R.drawable.ic_wn_bed, priceFrom = true),
        CatalogService("wardrobe_assembly", "Wardrobe", "Modular fit", 699, R.drawable.ic_wn_drawer, priceFrom = true),
        CatalogService("table_chair", "Table & chairs", "Assemble set", 349, R.drawable.ic_wn_furniture, priceFrom = true),
        CatalogService("rack_shelf", "Rack / shelf", "Wall-mount fit", 299, R.drawable.ic_wn_shelf)
    )

    private val bathroomCleaningServices = listOf(
        CatalogService("bath_deep", "Bathroom deep clean", "Descale & sanitize", 399, R.drawable.ic_wn_bathroom),
        CatalogService("bath_tiles", "Tiles & floor", "Stain removal", 349, R.drawable.ic_wn_sparkle, priceFrom = true),
        CatalogService("bath_fittings", "Basin & fittings", "Taps & mirror", 249, R.drawable.ic_wn_washbasin),
        CatalogService("bath_toilet", "Toilet / WC", "Deep sanitize", 299, R.drawable.ic_wn_toilet)
    )

    private val kitchenCleaningServices = listOf(
        CatalogService("kitchen_deep", "Kitchen deep clean", "Degrease & sanitize", 449, R.drawable.ic_wn_kitchen),
        CatalogService("kitchen_cabinets", "Cabinets & shelves", "Inside-out wipe", 349, R.drawable.ic_wn_cabinet, priceFrom = true),
        CatalogService("kitchen_sink", "Sink & counter", "Scrub & polish", 249, R.drawable.ic_wn_washbasin),
        CatalogService("kitchen_surfaces", "Surfaces & hob", "Grease removal", 299, R.drawable.ic_wn_sparkle, priceFrom = true)
    )

    private val livingCleaningServices = listOf(
        CatalogService("living_dusting", "Dusting & wipe", "Surfaces & decor", 299, R.drawable.ic_wn_sparkle, priceFrom = true),
        CatalogService("living_floor", "Floor mopping", "Sweep & mop", 249, R.drawable.ic_wn_cleaning),
        CatalogService("living_sofa", "Sofa shampoo", "Per seat", 199, R.drawable.ic_wn_sofa, priceFrom = true),
        CatalogService("bedroom_full", "Full room clean", "Top-to-bottom", 399, R.drawable.ic_wn_bed, priceFrom = true)
    )

    private val fullHomeCleaningServices = listOf(
        CatalogService("home_1bhk", "1 BHK", "Full home clean", 1499, R.drawable.ic_wn_home, priceFrom = true),
        CatalogService("home_2bhk", "2 BHK", "Full home clean", 1999, R.drawable.ic_wn_home, priceFrom = true),
        CatalogService("home_3bhk", "3 BHK", "Full home clean", 2499, R.drawable.ic_wn_home, priceFrom = true),
        CatalogService("home_villa", "Villa / 4+ BHK", "Full home clean", 3499, R.drawable.ic_wn_home, priceFrom = true)
    )

    private val washingMachineServices = listOf(
        CatalogService("wm_repair", "Repair", "Diagnose & fix", 299, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("wm_install", "Installation", "Setup & test", 399, R.drawable.ic_wn_washing_machine),
        CatalogService("wm_drum_clean", "Drum cleaning", "Descale & sanitize", 349, R.drawable.ic_wn_sparkle),
        CatalogService("wm_not_spin", "Not spinning", "Motor / belt", 349, R.drawable.ic_wn_gear, priceFrom = true)
    )

    private val refrigeratorServices = listOf(
        CatalogService("fridge_repair", "Repair", "Diagnose & fix", 299, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("fridge_gas", "Gas refill", "Coolant top-up", 1499, R.drawable.ic_wn_canister, priceFrom = true),
        CatalogService("fridge_not_cool", "Not cooling", "Compressor check", 399, R.drawable.ic_wn_snow, priceFrom = true),
        CatalogService("fridge_service", "General service", "Clean & check", 349, R.drawable.ic_wn_fridge)
    )

    private val televisionServices = listOf(
        CatalogService("tv_repair", "Repair", "Display / sound", 399, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("tv_wall_mount", "Wall mounting", "Bracket & fit", 499, R.drawable.ic_wn_wall),
        CatalogService("tv_no_display", "No display", "Panel / backlight", 599, R.drawable.ic_wn_tv, priceFrom = true),
        CatalogService("tv_setup", "Setup & install", "Apps & tuning", 299, R.drawable.ic_wn_gear)
    )

    private val chimneyServices = listOf(
        CatalogService("chimney_service", "Service & clean", "Degrease filters", 499, R.drawable.ic_wn_sparkle),
        CatalogService("chimney_install", "Installation", "Mount & duct", 799, R.drawable.ic_wn_appliance, priceFrom = true),
        CatalogService("chimney_repair", "Repair", "Motor / suction", 399, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("chimney_auto", "Auto-clean service", "Full service", 599, R.drawable.ic_wn_gear)
    )

    private val microwaveServices = listOf(
        CatalogService("mw_repair", "Repair", "Diagnose & fix", 299, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("mw_not_heat", "Not heating", "Magnetron check", 399, R.drawable.ic_wn_gear, priceFrom = true),
        CatalogService("mw_install", "Installation", "Setup & test", 249, R.drawable.ic_wn_microwave),
        CatalogService("mw_service", "General service", "Clean & check", 299, R.drawable.ic_wn_sparkle)
    )

    private val stoveServices = listOf(
        CatalogService("stove_burner", "Burner repair", "Flame / ignition", 249, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("stove_gas_leak", "Gas leak fix", "Seal & test", 299, R.drawable.ic_wn_pipe),
        CatalogService("stove_install", "Installation", "Fit & test", 349, R.drawable.ic_wn_stove),
        CatalogService("stove_service", "General service", "Clean & tune", 249, R.drawable.ic_wn_gear)
    )

    private val laptopServices = listOf(
        CatalogService("laptop_repair", "Repair", "Diagnose & fix", 399, R.drawable.ic_wn_tools, priceFrom = true),
        CatalogService("laptop_screen", "Screen replace", "Panel fit", 1499, R.drawable.ic_wn_laptop, priceFrom = true),
        CatalogService("laptop_battery", "Battery / charging", "Replace & test", 999, R.drawable.ic_wn_mcb, priceFrom = true),
        CatalogService("laptop_software", "Software / OS", "Reinstall & tune", 499, R.drawable.ic_wn_gear, priceFrom = true)
    )

    private val roServices = listOf(
        CatalogService("ro_service", "General service", "Clean & check", 349, R.drawable.ic_wn_purifier),
        CatalogService("ro_filter", "Filter change", "Cartridge replace", 599, R.drawable.ic_wn_canister, priceFrom = true),
        CatalogService("ro_install", "Installation", "Mount & connect", 499, R.drawable.ic_wn_tools),
        CatalogService("ro_repair", "Repair", "Leak / no water", 349, R.drawable.ic_wn_gear, priceFrom = true)
    )
}
