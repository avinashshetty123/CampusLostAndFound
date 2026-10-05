package com.example.lostandfoundfrontend.data

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

// ─── UI State models ──────────────────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    /** Bumped on every error so the UI can replay its shake animation for repeated errors. */
    val errorCount: Int = 0,
    val user: UserDto? = null
)

data class ItemsUiState(
    /** True only when there is nothing on screen yet (shows skeletons). */
    val isLoading: Boolean = false,
    /** True when reloading while the previous results are still shown. */
    val isRefreshing: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

data class ItemDetailUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: String? = null,
    val isClaiming: Boolean = false
)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false
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

    /** True while a request has been pending long enough that the server is probably cold-starting. */
    private val _serverWaking = MutableStateFlow(false)
    val serverWaking: StateFlow<Boolean> = _serverWaking.asStateFlow()

    // One-shot messages, delivered to whichever screen is showing a snackbar
    private val _toasts = Channel<String>(Channel.BUFFERED)
    val toasts: Flow<String> = _toasts.receiveAsFlow()

    private var itemsJob: Job? = null
    private var lastStatus: String? = null
    private var lastSearch: String? = null

    init {
        // Public endpoint: doubles as a wake-up ping for a sleeping Render instance
        loadStats()
    }

    private fun toast(message: String) { _toasts.trySend(message) }

    private suspend fun <T> withWakeHint(block: suspend () -> T): T = coroutineScope {
        val hint = launch { delay(4_000); _serverWaking.value = true }
        try { block() } finally { hint.cancel(); _serverWaking.value = false }
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    private fun authError(message: String) {
        _authState.update { it.copy(isLoading = false, error = message, errorCount = it.errorCount + 1) }
    }

    fun clearAuthError() { _authState.update { it.copy(error = null) } }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || password.isEmpty()) return authError("Enter your email and password")
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val r = withWakeHint { repository.login(cleanEmail, password) }) {
                is Result.Success -> onAuthenticated(r.data)
                is Result.Error -> authError(r.message)
                else -> Unit
            }
        }
    }

    fun register(
        name: String, email: String, mobile: String,
        studentClass: String, department: String, password: String
    ) {
        val cleanEmail = email.trim()
        when {
            name.isBlank() -> return authError("Enter your full name")
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> return authError("Enter a valid email address")
            password.length < 6 -> return authError("Password must be at least 6 characters")
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val r = withWakeHint {
                repository.register(name.trim(), cleanEmail, mobile.trim(), studentClass.trim(), department.trim(), password)
            }) {
                is Result.Success -> onAuthenticated(r.data)
                is Result.Error -> authError(r.message)
                else -> Unit
            }
        }
    }

    private fun onAuthenticated(data: AuthResponse) {
        TokenStore.saveAuth(data.token, data.user)
        SessionManager.consume()
        _authState.value = AuthUiState(isLoggedIn = true, user = data.user)
        _profileState.value = ProfileUiState(user = data.user)
    }

    private fun resetSessionState() {
        itemsJob?.cancel()
        _itemsState.value = ItemsUiState()
        _myItemsState.value = ItemsUiState()
        _savedItemsState.value = ItemsUiState()
        _detailState.value = ItemDetailUiState()
        _profileState.value = ProfileUiState()
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            TokenStore.clear()
            resetSessionState()
            _authState.value = AuthUiState(isLoggedIn = false)
            onDone()
        }
    }

    /** Called when the server rejected our token (see [SessionManager]). */
    fun onSessionExpired() {
        resetSessionState()
        _authState.value = AuthUiState(isLoggedIn = false, error = "Your session expired. Please log in again.", errorCount = 1)
    }

    fun changePassword(current: String, new: String, onSuccess: () -> Unit) {
        if (new.length < 6) return toast("New password must be at least 6 characters")
        viewModelScope.launch {
            when (val r = repository.changePassword(current, new)) {
                is Result.Success -> { toast(r.data.message); onSuccess() }
                is Result.Error -> toast(r.message)
                else -> Unit
            }
        }
    }

    // ── Items ─────────────────────────────────────────────────────────────────

    fun loadItems(status: String? = lastStatus, search: String? = lastSearch) {
        lastStatus = status
        lastSearch = search
        itemsJob?.cancel() // drop stale responses when the filter changes quickly
        itemsJob = viewModelScope.launch {
            _itemsState.update { it.copy(isLoading = it.items.isEmpty(), isRefreshing = it.items.isNotEmpty(), error = null) }
            when (val r = withWakeHint { repository.getItems(status, search) }) {
                is Result.Success -> _itemsState.value = ItemsUiState(items = r.data.items.map { it.toItem() })
                is Result.Error -> _itemsState.update { it.copy(isLoading = false, isRefreshing = false, error = r.message) }
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

    /** Shows the cached copy immediately (if any) and refreshes it from the server. */
    fun openItem(itemId: String) {
        val cached = (_itemsState.value.items + _myItemsState.value.items + _savedItemsState.value.items)
            .firstOrNull { it.id == itemId }
        _detailState.value = ItemDetailUiState(item = cached, isLoading = cached == null)
        viewModelScope.launch {
            val r = withWakeHint { repository.getItemById(itemId) }
            _detailState.update { s ->
                if (s.item != null && s.item.id != itemId) return@update s // user moved on
                when (r) {
                    is Result.Success -> s.copy(item = r.data.item.toItem(), isLoading = false, error = null)
                    is Result.Error -> s.copy(isLoading = false, error = if (s.item == null) r.message else null)
                    else -> s
                }
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
            when (val r = withWakeHint {
                repository.createItem(title.trim(), description.trim(), location.trim(), status.name, category.trim(), contactInfo.trim())
            }) {
                is Result.Success -> {
                    var dto = r.data.item
                    if (imageFile != null) {
                        when (val up = repository.uploadItemImage(dto.id, imageFile)) {
                            is Result.Success -> dto = up.data.item
                            is Result.Error -> toast("Item posted, but the photo failed: ${up.message}")
                            else -> Unit
                        }
                        imageFile.delete()
                    }
                    val item = dto.toItem()
                    _itemsState.update { it.copy(items = listOf(item) + it.items) }
                    _myItemsState.update { it.copy(items = listOf(item) + it.items) }
                    _reportLoading.value = false
                    loadStats()
                    onSuccess(item)
                }
                is Result.Error -> {
                    _reportLoading.value = false
                    toast(r.message)
                }
                else -> Unit
            }
        }
    }

    fun claimItem(itemId: String, message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _detailState.update { it.copy(isClaiming = true) }
            val r = repository.claimItem(itemId, message.trim())
            _detailState.update { it.copy(isClaiming = false) }
            when (r) {
                is Result.Success -> {
                    val isMine = _detailState.value.item?.reporterId == TokenStore.getUserId()
                    updateItem(itemId) {
                        if (isMine) it.copy(isResolved = true)
                        else it.copy(isResolved = true, claimedByName = TokenStore.getUserName(), claimMessage = message.trim().ifBlank { null })
                    }
                    loadStats()
                    onSuccess()
                }
                is Result.Error -> toast(r.message)
                else -> Unit
            }
        }
    }

    fun deleteItem(itemId: String) {
        val removed = _myItemsState.value.items.firstOrNull { it.id == itemId }
        // Optimistic: animate it out right away, restore on failure
        _myItemsState.update { s -> s.copy(items = s.items.filterNot { it.id == itemId }) }
        _itemsState.update { s -> s.copy(items = s.items.filterNot { it.id == itemId }) }
        viewModelScope.launch {
            when (val r = repository.deleteItem(itemId)) {
                is Result.Success -> { toast(r.data.message); loadStats() }
                is Result.Error -> {
                    toast(r.message)
                    if (removed != null) loadMyItems()
                }
                else -> Unit
            }
        }
    }

    fun toggleSave(item: Item) {
        val nowSaved = !item.isSaved
        updateItem(item.id) { it.copy(isSaved = nowSaved) }
        if (!nowSaved) _savedItemsState.update { s -> s.copy(items = s.items.filterNot { it.id == item.id }) }
        viewModelScope.launch {
            val r = if (nowSaved) repository.saveItem(item.id) else repository.unsaveItem(item.id)
            when (r) {
                is Result.Success -> toast(r.data.message)
                is Result.Error -> {
                    updateItem(item.id) { it.copy(isSaved = !nowSaved) }
                    toast(r.message)
                }
                else -> Unit
            }
        }
    }

    /** Applies the same change to an item wherever it is shown. */
    private fun updateItem(id: String, transform: (Item) -> Item) {
        fun MutableStateFlow<ItemsUiState>.patch() =
            update { s -> s.copy(items = s.items.map { if (it.id == id) transform(it) else it }) }
        _itemsState.patch()
        _myItemsState.patch()
        _savedItemsState.patch()
        _detailState.update { s -> if (s.item?.id == id) s.copy(item = transform(s.item)) else s }
    }

    // ── User ──────────────────────────────────────────────────────────────────

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = it.user == null, error = null) }
            when (val r = withWakeHint { repository.getProfile() }) {
                is Result.Success -> _profileState.update { it.copy(isLoading = false, user = r.data.data) }
                is Result.Error -> _profileState.update { it.copy(isLoading = false, error = r.message) }
                else -> Unit
            }
        }
    }

    fun updateProfile(name: String, mobile: String, studentClass: String, department: String, onSuccess: () -> Unit) {
        if (name.isBlank()) return toast("Name cannot be empty")
        viewModelScope.launch {
            _profileState.update { it.copy(isSaving = true) }
            when (val r = repository.updateProfile(name.trim(), mobile.trim(), studentClass.trim(), department.trim())) {
                is Result.Success -> {
                    _profileState.update { it.copy(isSaving = false, user = r.data.data ?: it.user) }
                    toast("Profile saved")
                    onSuccess()
                }
                is Result.Error -> {
                    _profileState.update { it.copy(isSaving = false) }
                    toast(r.message)
                }
                else -> Unit
            }
        }
    }

    fun uploadAvatar(file: File) {
        viewModelScope.launch {
            _profileState.update { it.copy(isUploadingAvatar = true) }
            val r = repository.uploadAvatar(file)
            file.delete()
            when (r) {
                is Result.Success -> {
                    _profileState.update { it.copy(isUploadingAvatar = false, user = r.data.data ?: it.user) }
                    toast("Photo updated")
                }
                is Result.Error -> {
                    _profileState.update { it.copy(isUploadingAvatar = false) }
                    toast(r.message)
                }
                else -> Unit
            }
        }
    }

    fun updateNotifications(enabled: Boolean) {
        val previous = _profileState.value.user
        _profileState.update { it.copy(user = it.user?.copy(notificationsEnabled = enabled)) }
        viewModelScope.launch {
            when (val r = repository.updateNotifications(enabled)) {
                is Result.Success -> _profileState.update { it.copy(user = r.data.data ?: it.user) }
                is Result.Error -> {
                    _profileState.update { it.copy(user = previous) }
                    toast(r.message)
                }
                else -> Unit
            }
        }
    }

    fun loadMyItems() {
        viewModelScope.launch {
            _myItemsState.update { it.copy(isLoading = it.items.isEmpty(), isRefreshing = it.items.isNotEmpty(), error = null) }
            when (val r = withWakeHint { repository.getMyItems() }) {
                is Result.Success -> _myItemsState.value = ItemsUiState(items = r.data.items.map { it.toItem() })
                is Result.Error -> _myItemsState.update { it.copy(isLoading = false, isRefreshing = false, error = r.message) }
                else -> Unit
            }
        }
    }

    fun loadSavedItems() {
        viewModelScope.launch {
            _savedItemsState.update { it.copy(isLoading = it.items.isEmpty(), isRefreshing = it.items.isNotEmpty(), error = null) }
            when (val r = withWakeHint { repository.getSavedItems() }) {
                is Result.Success -> _savedItemsState.value = ItemsUiState(items = r.data.items.map { it.toItem() })
                is Result.Error -> _savedItemsState.update { it.copy(isLoading = false, isRefreshing = false, error = r.message) }
                else -> Unit
            }
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private fun ItemDto.toItem() = Item(
        id = id,
        title = title,
        description = description.orEmpty(),
        location = location.orEmpty(),
        status = if (status == "FOUND") ItemStatus.FOUND else ItemStatus.LOST,
        category = category.orEmpty(),
        contactInfo = contactInfo.orEmpty(),
        reporterId = reportedBy,
        reporterName = reporterName ?: "Anonymous",
        reporterEmail = reporterEmail.orEmpty(),
        reporterPhone = reporterPhone.orEmpty(),
        reporterDept = buildDeptLabel(reporterDept, reporterClass),
        reportedAt = formatDate(createdAt),
        imageUrl = imageUrl,
        isResolved = isResolved,
        isSaved = isSaved,
        claimedByName = claimedByName,
        claimMessage = claimMessage
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
