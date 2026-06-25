package com.worknear.app.ui.address

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.worknear.app.R
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.AddressDto
import com.worknear.app.data.remote.dto.AddressRequestBody
import com.worknear.app.data.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class AddressType(val label: String, @DrawableRes val iconRes: Int) {
    HOME("Home", R.drawable.ic_wn_home),
    WORK("Work", R.drawable.ic_wn_work),
    OTHER("Other", R.drawable.ic_wn_location)
}

data class SavedAddress(
    val id: String,
    val type: AddressType,
    val line1: String,
    val cityState: String,
    val landmark: String = "",
    val isDefault: Boolean = false
) {
    /** Single-line representation used on the home address pill. */
    val display: String get() = "$line1, $cityState"
}

/**
 * Saved-address store backed by the `/api/v1/me/addresses` API. UI reads the in-memory
 * [addresses] snapshot list for instant updates; mutations are applied optimistically and
 * then reconciled with the server. When no repository is bound (or the API is unreachable)
 * the store still works purely in-memory so the design flow keeps running offline.
 */
object AddressStore {

    /** Maximum number of saved addresses a customer may keep. */
    const val MAX_ADDRESSES = 3

    private var repo: AccountRepository? = null
    private var scope: CoroutineScope? = null

    val addresses: SnapshotStateList<SavedAddress> = mutableStateListOf()

    var selectedId: String? by mutableStateOf(null)
        private set

    /** The address used for the next booking, or null when the customer has none saved. */
    val selected: SavedAddress?
        get() = addresses.firstOrNull { it.id == selectedId } ?: addresses.firstOrNull()

    fun byId(id: String?): SavedAddress? = addresses.firstOrNull { it.id == id }

    /** True while the customer is still under the [MAX_ADDRESSES] limit. */
    val canAddMore: Boolean get() = addresses.size < MAX_ADDRESSES

    /** Wires the backend repository and a long-lived scope, then loads saved addresses. */
    fun bind(repository: AccountRepository, externalScope: CoroutineScope) {
        repo = repository
        scope = externalScope
        refresh()
    }

    /** Reloads the saved addresses from the server (no-op when unbound). */
    fun refresh() {
        val r = repo ?: return
        scope?.launch { reload(r) }
    }

    private suspend fun reload(r: AccountRepository) {
        when (val res = r.getAddresses()) {
            is ApiResult.Success -> applyServer(res.data)
            is ApiResult.Error -> Unit // keep current in-memory state on failure
        }
    }

    private fun applyServer(list: List<AddressDto>) {
        val mapped = list.map { it.toSaved() }
        addresses.clear()
        addresses.addAll(mapped)
        if (selectedId == null || addresses.none { it.id == selectedId }) {
            selectedId = (mapped.firstOrNull { it.isDefault } ?: mapped.firstOrNull())?.id
        }
    }

    fun select(id: String) {
        if (addresses.any { it.id == id }) selectedId = id
    }

    /** Adds a new address. No-op (returns false) once the limit is reached. */
    fun add(address: SavedAddress): Boolean {
        if (!canAddMore) return false
        if (address.isDefault) clearDefault()
        addresses.add(address)
        if (address.isDefault || addresses.size == 1) selectedId = address.id
        val r = repo ?: return true
        scope?.launch {
            r.createAddress(address.toBody())
            reload(r) // reconcile ids / default with the server (also reverts on failure)
        }
        return true
    }

    fun update(address: SavedAddress) {
        val index = addresses.indexOfFirst { it.id == address.id }
        if (index < 0) return
        if (address.isDefault) clearDefault()
        addresses[index] = address
        val r = repo ?: return
        scope?.launch {
            r.updateAddress(address.id, address.toBody())
            reload(r)
        }
    }

    fun delete(id: String) {
        addresses.removeAll { it.id == id }
        if (selectedId == id) selectedId = addresses.firstOrNull()?.id
        val r = repo ?: return
        scope?.launch {
            r.deleteAddress(id)
            reload(r)
        }
    }

    fun setDefault(id: String) {
        clearDefault()
        val index = addresses.indexOfFirst { it.id == id }
        if (index < 0) return
        val updated = addresses[index].copy(isDefault = true)
        addresses[index] = updated
        selectedId = id
        val r = repo ?: return
        scope?.launch {
            r.updateAddress(id, updated.toBody())
            reload(r)
        }
    }

    fun newId(): String = "local-${System.currentTimeMillis()}"

    private fun clearDefault() {
        for (i in addresses.indices) {
            if (addresses[i].isDefault) addresses[i] = addresses[i].copy(isDefault = false)
        }
    }
}

private fun SavedAddress.toBody(): AddressRequestBody {
    val parts = cityState.split(" - ", limit = 2)
    val city = parts.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
    val pincode = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
    return AddressRequestBody(
        label = type.label,
        line1 = line1,
        line2 = landmark.takeIf { it.isNotBlank() },
        city = city,
        pincode = pincode,
        makeDefault = isDefault
    )
}

private fun AddressDto.toSaved(): SavedAddress {
    val type = when (label?.trim()?.lowercase()) {
        "work" -> AddressType.WORK
        "other" -> AddressType.OTHER
        else -> AddressType.HOME
    }
    val cityState = listOfNotNull(
        city?.takeIf { it.isNotBlank() },
        pincode?.takeIf { it.isNotBlank() }
    ).joinToString(" - ")
    return SavedAddress(
        id = id.orEmpty(),
        type = type,
        line1 = line1.orEmpty(),
        cityState = cityState,
        landmark = line2.orEmpty(),
        isDefault = isDefault
    )
}
