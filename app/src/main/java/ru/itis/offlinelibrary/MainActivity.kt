package ru.itis.offlinelibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ru.itis.offlinelibrary.ui.screens.DashboardScreen
import ru.itis.offlinelibrary.ui.screens.NewsScreen
import ru.itis.offlinelibrary.ui.theme.OfflineLibraryTheme
import ru.itis.offlinelibrary.viewmodel.NewsViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflineLibraryTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: NewsViewModel) {

    // Какая вкладка сейчас активна: 0 = новости, 1 = дашборд
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("📰") },
                    label = { Text("Новости") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.refreshCacheStats() // обновляем статистику при переходе
                    },
                    icon = { Text("📊") },
                    label = { Text("Кэш") }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> NewsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}