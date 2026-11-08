package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.ui.components.DramaBoxBottomBar
import com.example.ui.components.DramaBoxTopBar
import com.example.ui.screens.*
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.DramaBoxTheme
import com.example.viewmodel.DramaBoxViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: DramaBoxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Attempt silent auto sign-in with Google if already authorized
        val credentialManager = androidx.credentials.CredentialManager.create(this)
        com.example.data.AuthManager.attemptAutoSignIn(
            context = this,
            credentialManager = credentialManager,
            onAuthSuccess = { firebaseUser ->
                viewModel.repository.onGoogleUserSignedIn(firebaseUser)
            },
            onUnauthenticated = { },
            scope = viewModel.viewModelScope
        )

        setContent {
            DramaBoxTheme {
                DramaBoxApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DramaBoxApp(viewModel: DramaBoxViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for feedback messages
    LaunchedEffect(Unit) {
        viewModel.feedbackMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Top-level back handler
    BackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Splash) {
        viewModel.navigateBack()
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen()
        }
        is Screen.VideoPlayer -> {
            VideoPlayerScreen(
                dramaId = screen.dramaId,
                episodeId = screen.episodeId,
                viewModel = viewModel
            )
        }
        is Screen.DramaDetail -> {
            DramaDetailScreen(
                dramaId = screen.dramaId,
                viewModel = viewModel
            )
        }
        is Screen.AdminPanel -> {
            AdminPanelScreen(
                viewModel = viewModel
            )
        }
        else -> {
            // Main App Shell with TopBar and BottomBar
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlack),
                containerColor = DeepBlack,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    DramaBoxTopBar(
                        currentUser = currentUser,
                        onSearchClick = {
                            viewModel.navigateTo(Screen.Explore)
                        },
                        onProfileClick = {
                            viewModel.navigateTo(Screen.Profile)
                        },
                        onAdminClick = {
                            viewModel.navigateTo(Screen.AdminPanel)
                        }
                    )
                },
                bottomBar = {
                    DramaBoxBottomBar(
                        currentScreen = screen,
                        onTabSelected = { targetScreen ->
                            viewModel.navigateTo(targetScreen)
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = screen,
                        label = "MainScreenTransition"
                    ) { target ->
                        when (target) {
                            Screen.Home -> HomeScreen(viewModel = viewModel)
                            Screen.Explore -> ExploreScreen(viewModel = viewModel)
                            Screen.NewReleases -> NewReleasesScreen(viewModel = viewModel)
                            Screen.MyList -> MyListScreen(viewModel = viewModel)
                            Screen.Profile -> ProfileScreen(viewModel = viewModel)
                            else -> HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
