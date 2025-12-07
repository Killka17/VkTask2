package com.vktask2

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vktask2.data.AppContainer
import com.vktask2.notifications.PictureNotifier
import com.vktask2.ui.screens.PhotoGridScreen
import com.vktask2.ui.theme.VkTask2Theme

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy { AppContainer(applicationContext) }
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(appContainer)
    }

    private val notifier by lazy { PictureNotifier(this) }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForNotificationPermissionIfNeeded()

        setContent {
            VkTask2Theme {
                val state by viewModel.state.collectAsState()

                PhotoGridScreen(
                    state = state,
                    onRetry = { viewModel.refresh() },
                    onLoadMore = { viewModel.loadMore() },
                    onPhotoClick = { photo, position ->
                        notifier.showClick(photo, position)
                    }
                )
            }
        }
    }

    private fun askForNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

