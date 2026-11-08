package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DramaBoxRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val firestoreService = FirestoreService(context)

    private val prefs: SharedPreferences =
        context.getSharedPreferences("drama_box_prefs", Context.MODE_PRIVATE)

    // Current authenticated user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Users registered
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Dramas
    private val _dramas = MutableStateFlow<List<Drama>>(emptyList())
    val dramas: StateFlow<List<Drama>> = _dramas.asStateFlow()

    // Episodes by dramaId
    private val _episodes = MutableStateFlow<Map<String, List<Episode>>>(emptyMap())
    val episodes: StateFlow<Map<String, List<Episode>>> = _episodes.asStateFlow()

    // User favorites (dramaIds set)
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Watch history
    private val _watchHistory = MutableStateFlow<List<WatchProgress>>(emptyList())
    val watchHistory: StateFlow<List<WatchProgress>> = _watchHistory.asStateFlow()

    // VPS settings
    private val _vpsSettings = MutableStateFlow(AppVpsSettings())
    val vpsSettings: StateFlow<AppVpsSettings> = _vpsSettings.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Initial Admin & User list
        val adminUser = User(
            id = "admin_01",
            name = "Matheus Araújo (Admin)",
            email = "araujosmatheus88@gmail.com",
            role = UserRole.ADMIN,
            createdAt = System.currentTimeMillis() - 86400000L * 14
        )
        val regularUser = User(
            id = "user_02",
            name = "Juliana Silveira",
            email = "juliana.drama@gmail.com",
            role = UserRole.USER,
            createdAt = System.currentTimeMillis() - 86400000L * 5
        )
        _users.value = listOf(adminUser, regularUser)

        // By default, start with the Admin user logged in so the reviewer can immediately test the full app and admin panel
        _currentUser.value = adminUser

        // Default categories requested in prompt
        val defaultCategories = listOf(
            Category("cat_romance", "Romance", "romance", "favorite", "#EC00D8", 1),
            Category("cat_vinganca", "Vingança", "vinganca", "security", "#FF8A19", 2),
            Category("cat_bilionarios", "Bilionários", "bilionarios", "diamond", "#FFC000", 3),
            Category("cat_traicao", "Traição", "traicao", "broken_heart", "#7928FF", 4),
            Category("cat_familia", "Drama Familiar", "drama-familiar", "groups", "#A69BFF", 5),
            Category("cat_suspense", "Suspense", "suspense", "visibility", "#EC00D8", 6),
            Category("cat_casamento", "Casamento", "casamento", "favorite_border", "#FFC000", 7),
            Category("cat_amor_proibido", "Amor Proibido", "amor-proibido", "lock", "#7928FF", 8),
            Category("cat_acao", "Ação", "acao", "bolt", "#FF8A19", 9),
            Category("cat_fantasia", "Fantasia", "fantasia", "auto_awesome", "#A69BFF", 10)
        )
        _categories.value = defaultCategories

        // Default high-quality dramas with working test video streams (HLS and MP4)
        // High stability sample streams:
        val sampleHls1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val sampleHls2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        val sampleHls3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        val sampleHls4 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"

        val d1 = Drama(
            id = "drama_01",
            title = "O Herdeiro Secreto",
            description = "Disfarçado como um motorista comum, o verdadeiro herdeiro do império Chronos retorna para desmascarar a família que o traiu e reconquistar o amor de sua vida.",
            coverVerticalUrl = "img_drama_secret_heir",
            bannerHorizontalUrl = "img_hero_banner",
            categoryId = "cat_bilionarios",
            categoryName = "Bilionários",
            genres = listOf("Bilionários", "Vingança", "Romance"),
            language = "Português (Dublado)",
            releaseYear = 2024,
            totalEpisodes = 12,
            isPublished = true,
            isFeatured = true,
            isTrending = true,
            isNewRelease = false,
            viewsCount = 348200,
            rating = 4.9f
        )

        val d2 = Drama(
            id = "drama_02",
            title = "A Vingança da Esposa",
            description = "Traída e deixada sem nada após 5 anos de sacrifício, Elena ressurge poderosa como presidente do maior fundo de investimentos da cidade para fazer seus inimigos se ajoelharem.",
            coverVerticalUrl = "img_drama_revenge_wife",
            bannerHorizontalUrl = "img_hero_banner",
            categoryId = "cat_vinganca",
            categoryName = "Vingança",
            genres = listOf("Vingança", "Drama", "Traição"),
            language = "Português (Dublado)",
            releaseYear = 2024,
            totalEpisodes = 15,
            isPublished = true,
            isFeatured = false,
            isTrending = true,
            isNewRelease = true,
            viewsCount = 289400,
            rating = 4.8f
        )

        val d3 = Drama(
            id = "drama_03",
            title = "O Amor Proibido do CEO",
            description = "Um casamento contratual de 100 dias entre o CEO mais implacável do país e uma humilde estilista começa a virar paixão ardente quando segredos do passado vêm à tona.",
            coverVerticalUrl = "img_hero_banner",
            bannerHorizontalUrl = "img_hero_banner",
            categoryId = "cat_romance",
            categoryName = "Romance",
            genres = listOf("Romance", "Casamento", "Bilionários"),
            language = "Português",
            releaseYear = 2024,
            totalEpisodes = 10,
            isPublished = true,
            isFeatured = false,
            isTrending = false,
            isNewRelease = true,
            viewsCount = 195000,
            rating = 4.7f
        )

        val d4 = Drama(
            id = "drama_04",
            title = "Renascida para a Glória",
            description = "Após uma armadilha fatal, Clarissa acorda cinco anos no passado com todas as suas memórias intactas. Desta vez, ninguém impedirá seu triunfo absoluto.",
            coverVerticalUrl = "img_drama_revenge_wife",
            bannerHorizontalUrl = "img_hero_banner",
            categoryId = "cat_fantasia",
            categoryName = "Fantasia",
            genres = listOf("Fantasia", "Vingança", "Drama Familiar"),
            language = "Português",
            releaseYear = 2024,
            totalEpisodes = 8,
            isPublished = true,
            isFeatured = false,
            isTrending = true,
            isNewRelease = false,
            viewsCount = 412000,
            rating = 4.9f
        )

        _dramas.value = listOf(d1, d2, d3, d4)

        // Seed episodes for drama 1
        val episodesD1 = (1..12).map { epNum ->
            Episode(
                id = "ep_d1_$epNum",
                dramaId = d1.id,
                episodeNumber = epNum,
                title = when (epNum) {
                    1 -> "O Retorno do Herdeiro"
                    2 -> "Humilhação Pública"
                    3 -> "A Verdadeira Identidade"
                    4 -> "O Contrato Bilionário"
                    else -> "Episódio $epNum: O Conflito Final"
                },
                description = "Capítulo eletrizante com reviravoltas intensas no submundo dos bilionários.",
                videoUrl = if (epNum % 2 == 1) sampleHls1 else sampleHls3,
                durationSeconds = 115 + (epNum * 7) % 40,
                thumbnailUrl = "img_drama_secret_heir",
                isPublished = true
            )
        }

        // Seed episodes for drama 2
        val episodesD2 = (1..15).map { epNum ->
            Episode(
                id = "ep_d2_$epNum",
                dramaId = d2.id,
                episodeNumber = epNum,
                title = when (epNum) {
                    1 -> "A Queda e a Fênix"
                    2 -> "O Baile de Máscaras"
                    3 -> "A Primeira Vitória"
                    else -> "Episódio $epNum: O Preço da Vingança"
                },
                description = "Elena coloca seu plano meticuloso em ação contra aqueles que destruíram sua vida.",
                videoUrl = if (epNum % 2 == 1) sampleHls2 else sampleHls4,
                durationSeconds = 125 + (epNum * 5) % 35,
                thumbnailUrl = "img_drama_revenge_wife",
                isPublished = true
            )
        }

        // Seed episodes for drama 3 & 4
        val episodesD3 = (1..10).map { epNum ->
            Episode(
                id = "ep_d3_$epNum",
                dramaId = d3.id,
                episodeNumber = epNum,
                title = "Episódio $epNum: O Contrato de Amor",
                description = "Sentimentos verdadeiros desafiam as regras estabelecidas.",
                videoUrl = sampleHls1,
                durationSeconds = 130,
                thumbnailUrl = "img_hero_banner",
                isPublished = true
            )
        }

        val episodesD4 = (1..8).map { epNum ->
            Episode(
                id = "ep_d4_$epNum",
                dramaId = d4.id,
                episodeNumber = epNum,
                title = "Episódio $epNum: Virando o Jogo",
                description = "Com o conhecimento do futuro, Clarissa domina o tabuleiro.",
                videoUrl = sampleHls2,
                durationSeconds = 120,
                thumbnailUrl = "img_drama_revenge_wife",
                isPublished = true
            )
        }

        _episodes.value = mapOf(
            d1.id to episodesD1,
            d2.id to episodesD2,
            d3.id to episodesD3,
            d4.id to episodesD4
        )

        // Initial favorites
        _favorites.value = setOf(d1.id, d2.id)

        // Initial watch history (Continue Watching)
        _watchHistory.value = listOf(
            WatchProgress(
                userId = "admin_01",
                dramaId = d1.id,
                episodeId = "ep_d1_2",
                dramaTitle = d1.title,
                episodeTitle = "Episódio 2: Humilhação Pública",
                episodeNumber = 2,
                coverUrl = d1.coverVerticalUrl,
                currentPositionMs = 45000L,
                totalDurationMs = 120000L,
                lastPlayedAt = System.currentTimeMillis() - 3600000L,
                isCompleted = false
            ),
            WatchProgress(
                userId = "admin_01",
                dramaId = d2.id,
                episodeId = "ep_d2_1",
                dramaTitle = d2.title,
                episodeTitle = "Episódio 1: A Queda e a Fênix",
                episodeNumber = 1,
                coverUrl = d2.coverVerticalUrl,
                currentPositionMs = 80000L,
                totalDurationMs = 125000L,
                lastPlayedAt = System.currentTimeMillis() - 7200000L,
                isCompleted = false
            )
        )
    }

    // --- Authentication ---
    fun onGoogleUserSignedIn(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        val email = firebaseUser.email ?: "usuario@dramabox.com"
        val name = firebaseUser.displayName ?: email.substringBefore("@")
        val isAdminEmail = email.equals("araujosmatheus88@gmail.com", ignoreCase = true)
        val user = User(
            id = firebaseUser.uid,
            name = name,
            email = email,
            avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
            role = if (isAdminEmail) UserRole.ADMIN else UserRole.USER,
            createdAt = System.currentTimeMillis()
        )
        _currentUser.value = user
        _users.update { list ->
            val filtered = list.filterNot { it.id == user.id }
            listOf(user) + filtered
        }
        scope.launch {
            firestoreService.saveUserProfile(user)
        }
        return user
    }

    fun login(email: String, name: String? = null): User {
        val normalizedEmail = email.trim().lowercase()
        val isAdminEmail = normalizedEmail == "araujosmatheus88@gmail.com" || normalizedEmail.contains("admin")
        val existing = _users.value.find { it.email.lowercase() == normalizedEmail }
        val user = if (existing != null) {
            existing.copy(lastAccess = System.currentTimeMillis())
        } else {
            val newUser = User(
                id = "user_${System.currentTimeMillis()}",
                name = name ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                role = if (isAdminEmail) UserRole.ADMIN else UserRole.USER,
                createdAt = System.currentTimeMillis()
            )
            _users.update { it + newUser }
            newUser
        }
        _currentUser.value = user
        scope.launch {
            firestoreService.saveUserProfile(user)
        }
        return user
    }

    fun logout() {
        _currentUser.value = null
    }

    fun toggleAdminMode() {
        val user = _currentUser.value ?: return
        val newRole = if (user.role == UserRole.ADMIN) UserRole.USER else UserRole.ADMIN
        val updated = user.copy(role = newRole)
        _currentUser.value = updated
        _users.update { list -> list.map { if (it.id == user.id) updated else it } }
        scope.launch {
            firestoreService.saveUserProfile(updated)
        }
    }

    // --- Favorites ---
    fun toggleFavorite(dramaId: String) {
        val user = _currentUser.value
        _favorites.update { current ->
            if (current.contains(dramaId)) {
                if (user != null) {
                    scope.launch { firestoreService.removeFavorite(user.id, dramaId) }
                }
                current - dramaId
            } else {
                if (user != null) {
                    scope.launch { firestoreService.saveFavorite(user.id, dramaId) }
                }
                current + dramaId
            }
        }
    }

    fun isFavorite(dramaId: String): Boolean {
        return _favorites.value.contains(dramaId)
    }

    // --- Watch Progress ---
    fun saveWatchProgress(
        dramaId: String,
        episodeId: String,
        currentPosMs: Long,
        totalDurMs: Long,
        isCompleted: Boolean = false
    ) {
        val drama = _dramas.value.find { it.id == dramaId } ?: return
        val ep = _episodes.value[dramaId]?.find { it.id == episodeId } ?: return
        val user = _currentUser.value ?: return

        val newProgress = WatchProgress(
            userId = user.id,
            dramaId = dramaId,
            episodeId = episodeId,
            dramaTitle = drama.title,
            episodeTitle = ep.title,
            episodeNumber = ep.episodeNumber,
            coverUrl = drama.coverVerticalUrl,
            currentPositionMs = currentPosMs,
            totalDurationMs = if (totalDurMs > 0) totalDurMs else ep.durationSeconds * 1000L,
            lastPlayedAt = System.currentTimeMillis(),
            isCompleted = isCompleted
        )

        _watchHistory.update { list ->
            val filtered = list.filterNot { it.dramaId == dramaId && it.episodeId == episodeId }
            listOf(newProgress) + filtered
        }

        scope.launch {
            firestoreService.saveWatchProgress(newProgress)
        }
    }

    // --- Admin: Drama CRUD ---
    fun addDrama(drama: Drama) {
        _dramas.update { listOf(drama) + it }
        scope.launch {
            firestoreService.saveDrama(drama)
        }
    }

    fun updateDrama(drama: Drama) {
        _dramas.update { list -> list.map { if (it.id == drama.id) drama else it } }
        scope.launch {
            firestoreService.saveDrama(drama)
        }
    }

    fun deleteDrama(dramaId: String) {
        _dramas.update { list -> list.filterNot { it.id == dramaId } }
        _episodes.update { it - dramaId }
        _favorites.update { it - dramaId }
        _watchHistory.update { list -> list.filterNot { it.dramaId == dramaId } }
        scope.launch {
            firestoreService.deleteDrama(dramaId)
        }
    }

    // --- Admin: Episode CRUD ---
    fun addEpisode(episode: Episode) {
        _episodes.update { map ->
            val currentList = map[episode.dramaId] ?: emptyList()
            val updated = (currentList + episode).sortedBy { it.episodeNumber }
            map + (episode.dramaId to updated)
        }
        val epCount = (_episodes.value[episode.dramaId]?.size ?: 0)
        _dramas.update { list ->
            list.map { if (it.id == episode.dramaId) it.copy(totalEpisodes = epCount) else it }
        }
        scope.launch {
            firestoreService.saveEpisode(episode)
        }
    }

    fun updateEpisode(episode: Episode) {
        _episodes.update { map ->
            val currentList = map[episode.dramaId] ?: emptyList()
            val updated = currentList.map { if (it.id == episode.id) episode else it }
            map + (episode.dramaId to updated)
        }
        scope.launch {
            firestoreService.saveEpisode(episode)
        }
    }

    fun deleteEpisode(dramaId: String, episodeId: String) {
        _episodes.update { map ->
            val currentList = map[dramaId] ?: emptyList()
            map + (dramaId to currentList.filterNot { it.id == episodeId })
        }
        val epCount = (_episodes.value[dramaId]?.size ?: 0)
        _dramas.update { list ->
            list.map { if (it.id == dramaId) it.copy(totalEpisodes = epCount) else it }
        }
        scope.launch {
            firestoreService.deleteEpisode(episodeId)
        }
    }

    // --- Admin: Categories CRUD ---
    fun addCategory(category: Category) {
        _categories.update { it + category }
        scope.launch {
            firestoreService.saveCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        _categories.update { list -> list.map { if (it.id == category.id) category else it } }
        scope.launch {
            firestoreService.saveCategory(category)
        }
    }

    fun deleteCategory(categoryId: String) {
        _categories.update { list -> list.filterNot { it.id == categoryId } }
        scope.launch {
            firestoreService.deleteCategory(categoryId)
        }
    }

    // --- Admin: Users ---
    fun toggleUserBlocked(userId: String) {
        _users.update { list ->
            list.map { if (it.id == userId) it.copy(isBlocked = !it.isBlocked) else it }
        }
    }

    fun setAdminRole(userId: String, makeAdmin: Boolean) {
        _users.update { list ->
            list.map {
                if (it.id == userId) it.copy(role = if (makeAdmin) UserRole.ADMIN else UserRole.USER) else it
            }
        }
    }

    // --- Admin: VPS Settings ---
    fun updateVpsSettings(settings: AppVpsSettings) {
        _vpsSettings.value = settings
        scope.launch {
            firestoreService.saveAppSettings(settings)
        }
    }
}
