package com.example.lostandfoundfrontend.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

// ─── UI State models ──────────────────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val user: UserDto? = null
)

data class ItemsUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

data class ItemDetailUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: String? = null,
    val claimSuccess: Boolean = false
)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

data class StatsUiState(
    val total: Long = 0,
    val lost: Long = 0,
    val found: Long = 0,
    val resolved: Long = 0
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class LostFoundViewModel : ViewModel() {

    private val repository = LostFoundRepository()

    private val _authState = MutableStateFlow(AuthUiState(isLoggedIn = TokenStore.isLoggedIn()))
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _itemsState = MutableStateFlow(ItemsUiState())
    val itemsState: StateFlow<ItemsUiState> = _itemsState.asStateFlow()

    private val _myItemsState = MutableStateFlow(ItemsUiState())
    val myItemsState: StateFlow<ItemsUiState> = _myItemsState.asStateFlow()

    private val _savedItemsState = MutableStateFlow(ItemsUiState())
    val savedItemsState: StateFlow<ItemsUiState> = _savedItemsState.asStateFlow()

    private val _detailState = MutableStateFlow(ItemDetailUiState())
    val detailState: StateFlow<ItemDetailUiState> = _detailState.asStateFlow()

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _statsState = MutableStateFlow(StatsUiState())
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    private val _reportLoading = MutableStateFlow(false)
    val reportLoading: StateFlow<Boolean> = _reportLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // ── Auth ──────────────────────────────────────────────────────────────────

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            when (val r = repository.login(email, password)) {
                is Result.Success -> {
                    TokenStore.saveAuth(r.data.token, r.data.user)
                    _authState.value = AuthUiState(isLoggedIn = true, user = r.data.user)
                    onSuccess()
                }
                is Result.Error -> _authState.value = AuthUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun register(
        name: String, email: String, mobile: String,
        studentClass: String, department: String, password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            when (val r = repository.register(name, email, mobile, studentClass, department, password)) {
                is Result.Success -> {
                    TokenStore.saveAuth(r.data.token, r.data.user)
                    _authState.value = AuthUiState(isLoggedIn = true, user = r.data.user)
                    onSuccess()
                }
                is Result.Error -> _authState.value = AuthUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            TokenStore.clear()
            _authState.value = AuthUiState(isLoggedIn = false)
            _profileState.value = ProfileUiState()
            onDone()
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            when (val r = repository.changePassword(current, new)) {
                is Result.Success -> _toastMessage.value = r.data.message
                is Result.Error -> _toastMessage.value = r.message
                else -> Unit
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            when (val r = repository.forgotPassword(email)) {
                is Result.Success -> _toastMessage.value = r.data.message
                is Result.Error -> _toastMessage.value = r.message
                else -> Unit
            }
        }
    }

    // ── Items ─────────────────────────────────────────────────────────────────

    fun loadItems(status: String? = null, search: String? = null) {
        viewModelScope.launch {
            _itemsState.value = ItemsUiState(isLoading = true)
            when (val r = repository.getItems(status, search)) {
                is Result.Success -> _itemsState.value = ItemsUiState(items = r.data.items.map { it.toItem() })
                is Result.Error -> _itemsState.value = ItemsUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            when (val r = repository.getStats()) {
                is Result.Success -> _statsState.value = StatsUiState(
                    total = r.data.totalItems,
                    lost = r.data.lostItems,
                    found = r.data.foundItems,
                    resolved = r.data.resolvedItems
                )
                else -> Unit
            }
        }
    }

    fun loadItemDetail(itemId: String) {
        viewModelScope.launch {
            _detailState.value = ItemDetailUiState(isLoading = true)
            when (val r = repository.getItemById(itemId)) {
                is Result.Success -> _detailState.value = ItemDetailUiState(item = r.data.item.toItem())
                is Result.Error -> _detailState.value = ItemDetailUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun reportItem(
        title: String, description: String, location: String,
        status: ItemStatus, category: String, contactInfo: String,
        imageFile: File? = null,
        onSuccess: (Item) -> Unit
    ) {
        viewModelScope.launch {
            _reportLoading.value = true
            when (val r = repository.createItem(title, description, location, status.name, category, contactInfo)) {
                is Result.Success -> {
                    val item = r.data.item
                    if (imageFile != null) {
                        repository.uploadItemImage(item.id, imageFile)
                    }
                    _reportLoading.value = false
                    loadItems() // refresh home list
                    onSuccess(item.toItem())
                }
                is Result.Error -> {
                    _reportLoading.value = false
                    _toastMessage.value = r.message
                }
                else -> Unit
            }
        }
    }

    fun claimItem(itemId: String, message: String) {
        viewModelScope.launch {
            when (val r = repository.claimItem(itemId, message)) {
                is Result.Success -> {
                    _detailState.value = _detailState.value.copy(claimSuccess = true)
                    _toastMessage.value = r.data.message
                    loadItems()
                }
                is Result.Error -> _toastMessage.value = r.message
                else -> Unit
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            when (val r = repository.deleteItem(itemId)) {
                is Result.Success -> {
                    _toastMessage.value = r.data.message
                    loadMyItems()
                    loadItems()
                }
                is Result.Error -> _toastMessage.value = r.message
                else -> Unit
            }
        }
    }

    fun saveItem(itemId: String) {
        viewModelScope.launch {
            when (val r = repository.saveItem(itemId)) {
                is Result.Success -> _toastMessage.value = r.data.message
                is Result.Error -> _toastMessage.value = r.message
                else -> Unit
            }
        }
    }

    fun unsaveItem(itemId: String) {
        viewModelScope.launch {
            when (val r = repository.unsaveItem(itemId)) {
                is Result.Success -> {
                    _toastMessage.value = r.data.message
                    loadSavedItems()
                }
                is Result.Error -> _toastMessage.value = r.message
                else -> Unit
            }
        }
    }

    // ── User ──────────────────────────────────────────────────────────────────

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileUiState(isLoading = true)
            when (val r = repository.getProfile()) {
                is Result.Success -> _profileState.value = ProfileUiState(user = r.data.data)
                is Result.Error -> _profileState.value = ProfileUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun updateProfile(name: String, mobile: String, studentClass: String, department: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            when (val r = repository.updateProfile(name, mobile, studentClass, department)) {
                is Result.Success -> {
                    _profileState.value = ProfileUiState(user = r.data.data, saveSuccess = true)
                    _toastMessage.value = "Profile saved successfully!"
                    onSuccess()
                }
                is Result.Error -> {
                    _profileState.value = _profileState.value.copy(isLoading = false, error = r.message)
                    _toastMessage.value = r.message
                }
                else -> Unit
            }
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            when (val r = repository.updateNotifications(enabled)) {
                is Result.Success -> _profileState.value = _profileState.value.copy(user = r.data.data)
                else -> Unit
            }
        }
    }

    fun loadMyItems() {
        viewModelScope.launch {
            _myItemsState.value = ItemsUiState(isLoading = true)
            when (val r = repository.getMyItems()) {
                is Result.Success -> _myItemsState.value = ItemsUiState(items = r.data.items.map { it.toItem() })
                is Result.Error -> _myItemsState.value = ItemsUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun loadSavedItems() {
        viewModelScope.launch {
            _savedItemsState.value = ItemsUiState(isLoading = true)
            when (val r = repository.getSavedItems()) {
                is Result.Success -> _savedItemsState.value = ItemsUiState(items = r.data.items.map { it.toItem() })
                is Result.Error -> _savedItemsState.value = ItemsUiState(error = r.message)
                else -> Unit
            }
        }
    }

    fun clearToast() { _toastMessage.value = null }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private fun ItemDto.toItem() = Item(
        id = id,
        title = title,
        description = description,
        location = location,
        status = if (status == "FOUND") ItemStatus.FOUND else ItemStatus.LOST,
        contactInfo = contactInfo ?: "",
        reporterName = reporterName,
        reporterEmail = reporterEmail ?: "",
        reporterPhone = reporterPhone ?: "",
        reporterDept = buildDeptLabel(reporterDept, reporterClass),
        reportedAt = formatDate(createdAt),
        imageUrl = imageUrl,
        isResolved = isResolved,
        isSaved = isSaved
    )

    private fun buildDeptLabel(dept: String?, cls: String?): String {
        return listOfNotNull(cls, dept).joinToString(" • ").ifBlank { "Campus Student" }
    }

    private fun formatDate(raw: String?): String {
        if (raw == null) return ""
        return try {
            val instant = java.time.Instant.parse(raw)
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("MMM dd, yyyy • hh:mm a")
                .withZone(java.time.ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            raw
        }
    }
}
