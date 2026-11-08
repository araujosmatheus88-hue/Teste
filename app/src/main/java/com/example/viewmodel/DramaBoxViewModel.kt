package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DramaBoxRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object Home : Screen()
    object Explore : Screen()
    object NewReleases : Screen()
    object MyList : Screen()
    object Profile : Screen()
    data class DramaDetail(val dramaId: String) : Screen()
    data class VideoPlayer(val dramaId: String, val episodeId: String) : Screen()
    object AdminPanel : Screen()
}

enum class ExploreSort {
    POPULAR,
    RECENT,
    FEATURED,
    VIEWS
}

class DramaBoxViewModel(application: Application) : AndroidViewModel(application) {

    val repository = DramaBoxRepository(application.applicationContext)

    // Navigation back stack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val currentScreen: StateFlow<Screen> = _screenStack.map { it.lastOrNull() ?: Screen.Home }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Screen.Splash)

    // Data from Repository
    val currentUser = repository.currentUser
    val users = repository.users
    val categories = repository.categories
    val dramas = repository.dramas
    val episodes = repository.episodes
    val favorites = repository.favorites
    val watchHistory = repository.watchHistory
    val vpsSettings = repository.vpsSettings

    // Explore filter & search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _exploreSort = MutableStateFlow(ExploreSort.POPULAR)
    val exploreSort: StateFlow<ExploreSort> = _exploreSort.asStateFlow()

    // Filtered dramas in Explore
    val filteredDramas: StateFlow<List<Drama>> = combine(
        dramas,
        searchQuery,
        selectedCategoryId,
        exploreSort
    ) { dramaList, query, catId, sort ->
        var result = dramaList.filter { it.isPublished }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.categoryName.lowercase().contains(q) ||
                it.genres.any { g -> g.lowercase().contains(q) }
            }
        }

        if (catId != null) {
            result = result.filter { it.categoryId == catId }
        }

        when (sort) {
            ExploreSort.POPULAR -> result.sortedByDescending { it.rating }
            ExploreSort.RECENT -> result.sortedByDescending { it.createdAt }
            ExploreSort.FEATURED -> result.sortedByDescending { if (it.isFeatured) 1 else 0 }
            ExploreSort.VIEWS -> result.sortedByDescending { it.viewsCount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected drama for detail screen
    private val _selectedDramaId = MutableStateFlow<String?>(null)
    val selectedDrama: StateFlow<Drama?> = combine(dramas, _selectedDramaId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Currently playing episode
    private val _currentPlayingEpisode = MutableStateFlow<Episode?>(null)
    val currentPlayingEpisode: StateFlow<Episode?> = _currentPlayingEpisode.asStateFlow()

    // Splash loading animation flag
    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    // Toast / Feedback message
    private val _feedbackMessage = MutableSharedFlow<String>()
    val feedbackMessage: SharedFlow<String> = _feedbackMessage.asSharedFlow()

    init {
        // Splash screen timer
        viewModelScope.launch {
            delay(2400) // Smooth entrance and branding duration
            _isSplashFinished.value = true
            navigateTo(Screen.Home)
        }
    }

    // Navigation methods
    fun navigateTo(screen: Screen) {
        _screenStack.update { stack ->
            // If navigating to a main tab, clear backstack to that tab
            when (screen) {
                Screen.Home, Screen.Explore, Screen.NewReleases, Screen.MyList, Screen.Profile -> {
                    listOf(screen)
                }
                else -> stack + screen
            }
        }
    }

    fun navigateBack(): Boolean {
        if (_screenStack.value.size > 1) {
            _screenStack.update { it.dropLast(1) }
            return true
        }
        return false
    }

    fun openDramaDetail(dramaId: String) {
        _selectedDramaId.value = dramaId
        navigateTo(Screen.DramaDetail(dramaId))
    }

    fun playEpisode(dramaId: String, episodeId: String) {
        _selectedDramaId.value = dramaId
        val epList = episodes.value[dramaId] ?: emptyList()
        val ep = epList.find { it.id == episodeId } ?: epList.firstOrNull()
        _currentPlayingEpisode.value = ep
        if (ep != null) {
            navigateTo(Screen.VideoPlayer(dramaId, ep.id))
        }
    }

    fun playNextEpisode(dramaId: String): Boolean {
        val epList = episodes.value[dramaId] ?: return false
        val current = _currentPlayingEpisode.value ?: return false
        val next = epList.firstOrNull { it.episodeNumber == current.episodeNumber + 1 }
        return if (next != null) {
            _currentPlayingEpisode.value = next
            true
        } else false
    }

    fun playPreviousEpisode(dramaId: String): Boolean {
        val epList = episodes.value[dramaId] ?: return false
        val current = _currentPlayingEpisode.value ?: return false
        val prev = epList.firstOrNull { it.episodeNumber == current.episodeNumber - 1 }
        return if (prev != null) {
            _currentPlayingEpisode.value = prev
            true
        } else false
    }

    // Search & Filter
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun setSort(sort: ExploreSort) {
        _exploreSort.value = sort
    }

    // Favorites
    fun toggleFavorite(dramaId: String) {
        repository.toggleFavorite(dramaId)
        val isFav = repository.isFavorite(dramaId)
        emitFeedback(if (isFav) "Adicionado aos Favoritos!" else "Removido dos Favoritos")
    }

    // Auth
    fun login(email: String, name: String? = null) {
        val user = repository.login(email, name)
        emitFeedback("Bem-vindo(a), ${user.name}!")
        navigateTo(Screen.Home)
    }

    fun logout() {
        repository.logout()
        emitFeedback("Sessão encerrada")
        navigateTo(Screen.Home)
    }

    fun toggleAdminMode() {
        repository.toggleAdminMode()
        val role = currentUser.value?.role
        emitFeedback("Modo alterado para: ${if (role == UserRole.ADMIN) "Administrador" else "Usuário Comum"}")
    }

    private fun emitFeedback(msg: String) {
        viewModelScope.launch {
            _feedbackMessage.emit(msg)
        }
    }
}
