package com.example.model

enum class UserRole {
    USER,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String = "",
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccess: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false
)

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val iconName: String = "category",
    val colorHex: String = "#EC00D8",
    val sortOrder: Int = 0
)

data class Episode(
    val id: String,
    val dramaId: String,
    val seasonId: String = "s1",
    val episodeNumber: Int,
    val title: String,
    val description: String = "",
    val videoUrl: String,
    val durationSeconds: Int = 120, // Short dramas are typically 1-3 minutes
    val thumbnailUrl: String = "",
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Season(
    val id: String,
    val dramaId: String,
    val seasonNumber: Int,
    val title: String,
    val episodes: List<Episode> = emptyList()
)

data class Drama(
    val id: String,
    val title: String,
    val description: String,
    val coverVerticalUrl: String,
    val bannerHorizontalUrl: String = "",
    val categoryId: String,
    val categoryName: String,
    val genres: List<String> = emptyList(),
    val language: String = "Português",
    val releaseYear: Int = 2024,
    val totalEpisodes: Int = 10,
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isNewRelease: Boolean = false,
    val viewsCount: Long = 12500,
    val rating: Float = 4.8f,
    val createdAt: Long = System.currentTimeMillis(),
    val seasons: List<Season> = emptyList()
)

data class FavoriteItem(
    val userId: String,
    val dramaId: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class WatchProgress(
    val userId: String,
    val dramaId: String,
    val episodeId: String,
    val dramaTitle: String,
    val episodeTitle: String,
    val episodeNumber: Int,
    val coverUrl: String,
    val currentPositionMs: Long,
    val totalDurationMs: Long,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    val progressFraction: Float
        get() = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs).coerceIn(0f, 1f) else 0f
}

data class AppVpsSettings(
    val vpsBaseUrl: String = "https://meuservidor.com/videos/",
    val cdnEnabled: Boolean = false,
    val defaultHlsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)
