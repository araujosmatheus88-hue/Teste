package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.Episode
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    dramaId: String,
    episodeId: String,
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drama by viewModel.selectedDrama.collectAsState()
    val allEpisodesMap by viewModel.episodes.collectAsState()
    val episodes = allEpisodesMap[dramaId] ?: emptyList()
    val currentEp by viewModel.currentPlayingEpisode.collectAsState()

    val episode = currentEp ?: episodes.find { it.id == episodeId } ?: episodes.firstOrNull()

    // Player state
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var isVerticalMode by remember { mutableStateOf(true) }
    var showEpisodeSelector by remember { mutableStateOf(false) }

    // ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Handle back button: save progress before leaving
    fun saveProgressAndExit() {
        if (episode != null && drama != null) {
            viewModel.repository.saveWatchProgress(
                dramaId = dramaId,
                episodeId = episode.id,
                currentPosMs = exoPlayer.currentPosition,
                totalDurMs = exoPlayer.duration.coerceAtLeast(0L),
                isCompleted = exoPlayer.currentPosition >= (exoPlayer.duration * 0.9f)
            )
        }
        exoPlayer.stop()
        exoPlayer.release()
        viewModel.navigateBack()
    }

    BackHandler {
        if (showEpisodeSelector) {
            showEpisodeSelector = false
        } else {
            saveProgressAndExit()
        }
    }

    // Set media source when episode changes
    LaunchedEffect(episode?.id) {
        val ep = episode ?: return@LaunchedEffect
        playbackError = null
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(ep.videoUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            playbackError = "Não foi possível carregar o vídeo da VPS: ${e.message}"
        }
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    // Mark as completed and auto-play next episode
                    if (episode != null && drama != null) {
                        viewModel.repository.saveWatchProgress(
                            dramaId = dramaId,
                            episodeId = episode.id,
                            currentPosMs = totalDurationMs,
                            totalDurMs = totalDurationMs,
                            isCompleted = true
                        )
                    }
                    val hasNext = viewModel.playNextEpisode(dramaId)
                    if (!hasNext) {
                        isPlaying = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playbackError = "Erro no fluxo da VPS (${error.errorCodeName}). Verifique a URL do episódio no painel admin."
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Periodic progress saver & timer loop
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            if (exoPlayer.isPlaying) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)

                // Auto-save progress every 5 seconds
                if (episode != null && currentPositionMs > 1000) {
                    viewModel.repository.saveWatchProgress(
                        dramaId = dramaId,
                        episodeId = episode.id,
                        currentPosMs = currentPositionMs,
                        totalDurMs = totalDurationMs
                    )
                }
            }
            delay(1000)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .clickable { showControls = !showControls }
            .testTag("video_player_screen")
    ) {
        // ExoPlayer Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom Jetpack Compose controls for DRAMA BOX branding
                    resizeMode = if (isVerticalMode) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.resizeMode = if (isVerticalMode) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                DeepBlack.copy(alpha = 0.85f),
                                Color.Transparent,
                                DeepBlack.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                // Top Header Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { saveProgressAndExit() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                            .testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextWhite
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = drama?.title ?: "Mininovela",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = episode?.let { "EP ${it.episodeNumber}: ${it.title}" } ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = Lavender),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Format Toggle (Vertical 9:16 vs Landscape Fit)
                    IconButton(
                        onClick = { isVerticalMode = !isVerticalMode },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = if (isVerticalMode) Icons.Default.CropPortrait else Icons.Default.Fullscreen,
                            contentDescription = "Formato de tela",
                            tint = if (isVerticalMode) NeonPink else TextWhite
                        )
                    }

                    // Episode Selector Drawer Button
                    IconButton(
                        onClick = { showEpisodeSelector = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                            .testTag("player_episodes_list_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = "Lista de Episódios",
                            tint = AccentYellow
                        )
                    }
                }

                // Center Play / Pause & Skip Buttons
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Previous Episode
                    IconButton(
                        onClick = { viewModel.playPreviousEpisode(dramaId) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Episódio anterior",
                            tint = TextWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Rewind -10s
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                            exoPlayer.seekTo(target)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Retroceder 10s",
                            tint = TextWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Play / Pause Neon Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonPink, ElectricPurple)
                                )
                            )
                            .clickable {
                                if (exoPlayer.isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                            tint = TextWhite,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Forward +10s
                    IconButton(
                        onClick = {
                            val target = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(target)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Avançar 10s",
                            tint = TextWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Next Episode
                    IconButton(
                        onClick = { viewModel.playNextEpisode(dramaId) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Próximo episódio",
                            tint = TextWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Progress Bar & Timers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Slider(
                        value = if (totalDurationMs > 0) currentPositionMs.toFloat() else 0f,
                        onValueChange = { newPos ->
                            currentPositionMs = newPos.toLong()
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPositionMs)
                        },
                        valueRange = 0f..(if (totalDurationMs > 0) totalDurationMs.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonPink,
                            activeTrackColor = NeonPink,
                            inactiveTrackColor = DeepSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentMin = currentPositionMs / 60000
                        val currentSec = (currentPositionMs % 60000) / 1000
                        val totalMin = totalDurationMs / 60000
                        val totalSec = (totalDurationMs % 60000) / 1000

                        Text(
                            text = "%02d:%02d".format(currentMin, currentSec),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "Servidor VPS • HLS/MP4",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                        )

                        Text(
                            text = "%02d:%02d".format(totalMin, totalSec),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Error message banner if video fails to load
        if (playbackError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aviso do Servidor de Vídeo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = playbackError ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                episode?.let { ep ->
                                    val mediaItem = MediaItem.fromUri(Uri.parse(ep.videoUrl))
                                    exoPlayer.setMediaItem(mediaItem)
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                    playbackError = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                        ) {
                            Text("Tentar Novamente", color = TextWhite)
                        }
                    }
                }
            }
        }

        // Episode Selection Bottom Sheet / Drawer
        if (showEpisodeSelector) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlack.copy(alpha = 0.85f))
                    .clickable { showEpisodeSelector = false }
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .align(Alignment.BottomCenter)
                        .clickable(enabled = false) { /* Prevent dismiss on sheet tap */ }
                        .border(1.dp, DeepCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selecionar Episódio",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )

                            IconButton(onClick = { showEpisodeSelector = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(episodes) { ep ->
                                val isSelected = ep.id == episode?.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricPurple.copy(alpha = 0.35f) else DeepCard)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonPink else DeepCardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.playEpisode(dramaId, ep.id)
                                            showEpisodeSelector = false
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) NeonPink else DeepSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${ep.episodeNumber}",
                                                color = TextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = ep.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = TextWhite
                                            )
                                            val minutes = ep.durationSeconds / 60
                                            val seconds = ep.durationSeconds % 60
                                            Text(
                                                text = "%02d:%02d min".format(minutes, seconds),
                                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Reproduzindo agora",
                                            tint = NeonPink,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
