package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService(context: Context) {

    private val TAG = "DramaBoxFirestore"

    // Custom database instance configured from XML
    val db: FirebaseFirestore = try {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    } catch (e: Exception) {
        Log.w(TAG, "Fallback to default firestore instance: ${e.message}")
        FirebaseFirestore.getInstance()
    }

    private val auth get() = Firebase.auth

    // --- Users ---
    suspend fun saveUserProfile(user: User) {
        try {
            val docRef = db.collection("users").document(user.id)
            val data = mapOf(
                "uid" to user.id,
                "name" to user.name,
                "email" to user.email,
                "avatarUrl" to user.avatarUrl,
                "role" to user.role.name,
                "isBlocked" to user.isBlocked,
                "createdAt" to FieldValue.serverTimestamp(),
                "lastAccess" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveUserProfile error: ${e.message}")
        }
    }

    // --- Dramas ---
    suspend fun saveDrama(drama: Drama) {
        try {
            val docRef = db.collection("dramas").document(drama.id)
            val data = mapOf(
                "title" to drama.title,
                "description" to drama.description,
                "coverVerticalUrl" to drama.coverVerticalUrl,
                "bannerHorizontalUrl" to drama.bannerHorizontalUrl,
                "categoryId" to drama.categoryId,
                "categoryName" to drama.categoryName,
                "genres" to drama.genres,
                "language" to drama.language,
                "releaseYear" to drama.releaseYear,
                "totalEpisodes" to drama.totalEpisodes,
                "isPublished" to drama.isPublished,
                "isFeatured" to drama.isFeatured,
                "isTrending" to drama.isTrending,
                "isNewRelease" to drama.isNewRelease,
                "viewsCount" to drama.viewsCount,
                "rating" to drama.rating.toDouble(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveDrama error: ${e.message}")
        }
    }

    suspend fun deleteDrama(dramaId: String) {
        try {
            db.collection("dramas").document(dramaId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteDrama error: ${e.message}")
        }
    }

    // --- Episodes ---
    suspend fun saveEpisode(episode: Episode) {
        try {
            val docRef = db.collection("episodes").document(episode.id)
            val data = mapOf(
                "dramaId" to episode.dramaId,
                "seasonId" to episode.seasonId,
                "episodeNumber" to episode.episodeNumber,
                "title" to episode.title,
                "description" to episode.description,
                "videoUrl" to episode.videoUrl,
                "durationSeconds" to episode.durationSeconds,
                "thumbnailUrl" to episode.thumbnailUrl,
                "isPublished" to episode.isPublished,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveEpisode error: ${e.message}")
        }
    }

    suspend fun deleteEpisode(episodeId: String) {
        try {
            db.collection("episodes").document(episodeId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteEpisode error: ${e.message}")
        }
    }

    // --- Categories ---
    suspend fun saveCategory(category: Category) {
        try {
            val docRef = db.collection("categories").document(category.id)
            val data = mapOf(
                "name" to category.name,
                "slug" to category.slug,
                "iconName" to category.iconName,
                "colorHex" to category.colorHex,
                "sortOrder" to category.sortOrder,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveCategory error: ${e.message}")
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        try {
            db.collection("categories").document(categoryId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "deleteCategory error: ${e.message}")
        }
    }

    // --- Favorites ---
    suspend fun saveFavorite(userId: String, dramaId: String) {
        try {
            val id = "${userId}_$dramaId"
            val data = mapOf(
                "userId" to userId,
                "dramaId" to dramaId,
                "addedAt" to FieldValue.serverTimestamp()
            )
            db.collection("favorites").document(id).set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveFavorite error: ${e.message}")
        }
    }

    suspend fun removeFavorite(userId: String, dramaId: String) {
        try {
            val id = "${userId}_$dramaId"
            db.collection("favorites").document(id).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "removeFavorite error: ${e.message}")
        }
    }

    // --- Watch History ---
    suspend fun saveWatchProgress(progress: WatchProgress) {
        try {
            val id = "${progress.userId}_${progress.dramaId}_${progress.episodeId}"
            val data = mapOf(
                "userId" to progress.userId,
                "dramaId" to progress.dramaId,
                "episodeId" to progress.episodeId,
                "dramaTitle" to progress.dramaTitle,
                "episodeTitle" to progress.episodeTitle,
                "episodeNumber" to progress.episodeNumber,
                "coverUrl" to progress.coverUrl,
                "currentPositionMs" to progress.currentPositionMs,
                "totalDurationMs" to progress.totalDurationMs,
                "isCompleted" to progress.isCompleted,
                "lastPlayedAt" to FieldValue.serverTimestamp()
            )
            db.collection("watchHistory").document(id).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveWatchProgress error: ${e.message}")
        }
    }

    // --- App & VPS Settings ---
    suspend fun saveAppSettings(settings: AppVpsSettings) {
        try {
            val data = mapOf(
                "vpsBaseUrl" to settings.vpsBaseUrl,
                "cdnEnabled" to settings.cdnEnabled,
                "defaultHlsEnabled" to settings.defaultHlsEnabled,
                "appVersion" to settings.appVersion
            )
            db.collection("appSettings").document("general").set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveAppSettings error: ${e.message}")
        }
    }
}
