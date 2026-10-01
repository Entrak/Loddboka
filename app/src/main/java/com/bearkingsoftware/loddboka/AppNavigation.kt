package com.bearkingsoftware.loddboka

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.bearkingsoftware.loddboka.ui.settings.TesterTasksScreen
import com.bearkingsoftware.loddboka.ui.trekning.Historikkskjerm
import com.bearkingsoftware.loddboka.ui.trekning.Trekningsskjerm
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel

/** Bottom-bar tabs only. */
sealed class TabDestination(
    val route: String,
    @param:StringRes val title: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Hovedskjerm : TabDestination(
        "hovedskjerm",
        R.string.screen_title_loddboker,
        Icons.Filled.List,
        Icons.Outlined.List
    )
    data object Trekningsskjerm : TabDestination(
        "trekningsskjerm",
        R.string.screen_title_trekk,
        Icons.Filled.Star,
        Icons.Outlined.Star
    )
    data object Settings : TabDestination(
        "settings_skjerm",
        R.string.screen_title_settings,
        Icons.Filled.Settings,
        Icons.Outlined.Settings
    )
}

/** Non-tab routes (no bottom-bar icons). */
object Routes {
    const val InitialConfiguration = "initial_configuration"
    const val LeggTilRediger = "legg_til_rediger_skjerm"
    const val Historikk = "historikkskjerm"
    const val TesterTasks = "tester_tasks"

    fun leggTilRediger(id: Long = -1L): String = "$LeggTilRediger?id=$id"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: LoddbokViewModel = viewModel()
    val trekningState by viewModel.trekningState.collectAsState()
    val localeManager = LocalLocaleManager.current

    val tabs = listOf(
        TabDestination.Hovedskjerm,
        TabDestination.Trekningsskjerm,
        TabDestination.Settings,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = tabs.any { it.route == currentDestination?.route }

    // Always start at hovedskjerm so tab popUpTo(findStartDestination) stays valid
    // after first-run. Push InitialConfiguration only when needed.
    var navReady by remember { mutableStateOf(false) }
    var openFirstRun by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        openFirstRun = localeManager.isFirstRun()
        navReady = true
    }

    if (navReady) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.title),
                                        modifier = Modifier.size(40.dp)
                                    )
                                },
                                label = { Text(stringResource(tab.title)) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
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
            NavHost(
                navController = navController,
                startDestination = TabDestination.Hovedskjerm.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.InitialConfiguration) {
                    InitialConfigurationScreen(
                        onConfigurationComplete = {
                            navController.navigate(TabDestination.Hovedskjerm.route) {
                                popUpTo(Routes.InitialConfiguration) { inclusive = true }
                            }
                        }
                    )
                }
                composable(TabDestination.Hovedskjerm.route) {
                    Hovedskjerm(navController, viewModel)
                }
                composable(
                    route = "${Routes.LeggTilRediger}?id={id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: -1L
                    LeggTilRedigerSkjerm(navController, viewModel, id)
                }
                composable(TabDestination.Trekningsskjerm.route) {
                    Trekningsskjerm(
                        viewModel = viewModel,
                        onNavigateToHistorikk = { navController.navigate(Routes.Historikk) }
                    )
                }
                composable(Routes.Historikk) {
                    Historikkskjerm(
                        vinnere = trekningState.vinnere,
                        onClose = { navController.popBackStack() }
                    )
                }
                composable(TabDestination.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onOpenTesterTasks = { navController.navigate(Routes.TesterTasks) }
                    )
                }
                composable(Routes.TesterTasks) {
                    TesterTasksScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        LaunchedEffect(openFirstRun) {
            if (openFirstRun) {
                navController.navigate(Routes.InitialConfiguration) {
                    launchSingleTop = true
                }
            }
        }
    }
}
