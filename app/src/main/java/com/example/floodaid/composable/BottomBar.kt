import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.floodaid.models.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        Screen.Dashboard,
        Screen.Forum,
        Screen.FloodStatus,
        Screen.Map,
        Screen.Volunteer,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {

        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}


@Composable
fun RowScope.AddItem(
    screen: Screen,
    currentDestination: NavDestination?,
    navController: NavHostController,
) {
    val isForumScreen = currentDestination?.route?.contains("forum") == true // Check if the current route contains "forum"

    if (screen.icon != null && screen.title != null) {
        NavigationBarItem(
            label = {
                Text(text = screen.title)
            },
            icon = {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = "Navigation Icon"
                )
            },
            selected = when (screen) {
                Screen.Forum -> isForumScreen // Keep Forum selected if either Forum or CreateForumPost is active
                else -> currentDestination?.hierarchy?.any { it.route == screen.route } == true
            },
            onClick = {
                if (currentDestination?.route != screen.route) {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}
