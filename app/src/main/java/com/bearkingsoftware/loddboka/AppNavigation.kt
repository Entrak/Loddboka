package com.bearkingsoftware.loddboka

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bearkingsoftware.loddboka.ui.InitialConfigurationScreen
import com.bearkingsoftware.loddboka.ui.hovedskjerm.Hovedskjerm
import com.bearkingsoftware.loddboka.ui.leggtil.LeggTilRedigerSkjerm
import com.bearkingsoftware.loddboka.ui.settings.SettingsScreen
import com.bearkingsoftware.loddboka.ui.trekning.Historikkskjerm
import com.bearkingsoftware.loddboka.ui.trekning.Trekningsskjerm
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel

sealed class Screen(
    val route: String,
    @param:StringRes val title: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Hovedskjerm : Screen("hovedskjerm", R.string.screen_title_loddboker, Icons.Filled.Style, Icons.Outlined.Style)
    object Trekningsskjerm : Screen("trekningsskjerm", R.string.screen_title_trekk, Icons.Filled.Casino, Icons.Outlined.Casino)
    object SettingsScreen : Screen("settings_skjerm", R.string.screen_title_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    object InitialConfigurationScreen : Screen("initial_configuration", 0, Icons.Filled.Settings, Icons.Outlined.Settings) // Icons and title won't be used

    // Screens without bottom bar items
    object LeggTilRedigerSkjerm : Screen("legg_til_rediger_skjerm", R.string.screen_title_legg_til_rediger, Icons.Filled.Style, Icons.Outlined.Style) // Icons won't be used
    object Historikkskjerm : Screen("historikkskjerm", R.string.screen_title_historikk, Icons.Filled.Casino, Icons.Outlined.Casino) // Icons won't be used
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: LoddbokViewModel = viewModel()
    val trekningState by viewModel.trekningState.collectAsState()
    val context = LocalContext.current
    val localeManager = remember { LocaleManager(context) }

    val items = listOf(
        Screen.Hovedskjerm,
        Screen.Trekningsskjerm,
        Screen.SettingsScreen,
    )

    var showBottomBar by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    showBottomBar = items.any { it.route == currentDestination?.route }

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (localeManager.isFirstRun()) {
            Screen.InitialConfigurationScreen.route
        } else {
            Screen.Hovedskjerm.route
        }
    }

    startDestination?.let { startRoute ->
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        items.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = stringResource(screen.title),
                                        modifier = Modifier.size(40.dp)
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(navController = navController, startDestination = startRoute, modifier = Modifier.padding(padding)) {
                composable(Screen.InitialConfigurationScreen.route) {
                    InitialConfigurationScreen {
                        navController.navigate(Screen.Hovedskjerm.route) {
                            popUpTo(Screen.InitialConfigurationScreen.route) { inclusive = true }
                        }
                    }
                }
                composable(Screen.Hovedskjerm.route) {
                    Hovedskjerm(navController, viewModel)
                }
                composable(
                    route = "${Screen.LeggTilRedigerSkjerm.route}?id={id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: -1L
                    LeggTilRedigerSkjerm(navController, viewModel, id)
                }
                composable(Screen.Trekningsskjerm.route) {
                    Trekningsskjerm(
                        viewModel = viewModel,
                        onNavigateToHistorikk = { navController.navigate(Screen.Historikkskjerm.route) }
                    )
                }
                composable(Screen.Historikkskjerm.route) {
                    Historikkskjerm(
                        vinnere = trekningState.vinnere,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(Screen.SettingsScreen.route) {
                    SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
