package com.example.lfrivalsggiteration1.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.lfrivalsggiteration1.ui.theme.LFRIVALSGGITERATION1Theme
import com.example.lfrivalsggiteration1.ui.theme.RivalsRed

sealed class Screen(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Board   : Screen("Board",    Icons.AutoMirrored.Filled.List)
    data object Stats   : Screen("Meta",     Icons.Default.Info)
    data object MyStats : Screen("My Stats", Icons.Default.Star)
    data object Profile : Screen("Profile",  Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences by viewModel.userPreferences.collectAsState()
            LFRIVALSGGITERATION1Theme(darkTheme = preferences.darkMode) {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    var isLoggedIn by remember {
                        mutableStateOf(viewModel.isRememberMeEnabled())
                    }
                    if (!isLoggedIn) {
                        AuthManager(vm = viewModel, onAuthComplete = { isLoggedIn = true })
                    } else {
                        MainScaffold(vm = viewModel, onLogout = { isLoggedIn = false })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(vm: MainViewModel, onLogout: () -> Unit) {
    val tabs = listOf(Screen.Board, Screen.Stats, Screen.MyStats, Screen.Profile)
    var selectedTab by remember { mutableStateOf<Screen>(Screen.Board) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected  = selectedTab == screen,
                        onClick   = { selectedTab = screen },
                        icon      = { Icon(screen.icon, contentDescription = screen.label) },
                        label     = { Text(screen.label) },
                        colors    = NavigationBarItemDefaults.colors(
                            selectedIconColor = RivalsRed,
                            selectedTextColor = RivalsRed,
                            indicatorColor    = Color(0xFFF5F5F5)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                is Screen.Board   -> BoardScreen(vm)
                is Screen.Stats   -> StatsScreen(vm)
                is Screen.MyStats -> MyStatsScreen(vm)
                is Screen.Profile -> ProfileScreen(vm, onLogout)
            }
        }
    }
}