package com.example.lfrivalsggiteration1.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lfrivalsggiteration1.ui.theme.LFRIVALSGGITERATION1Theme


sealed class Screen(val label: String, val icon: ImageVector) {
    data object Board: Screen("Board", Icons.AutoMirrored.Filled.List)
    data object Profile: Screen("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LFRIVALSGGITERATION1Theme {
                MainScaffold(viewModel)
            }
        }
    }
}

@Composable
fun MainScaffold(vm: MainViewModel) {
    val tabs = listOf(Screen.Board, Screen.Profile)
    var selectedTab by remember { mutableStateOf<Screen>(Screen.Board) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedTab == screen,
                        onClick = { selectedTab = screen },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.tertiary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            is Screen.Board -> BoardScreen(vm, Modifier.padding(innerPadding))
            is Screen.Profile -> ProfileScreen(vm, Modifier.padding(innerPadding))
        }
    }
}