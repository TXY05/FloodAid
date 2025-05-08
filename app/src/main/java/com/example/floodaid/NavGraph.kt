package com.example.floodaid

import VolunteerDetail
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.floodaid.models.Screen
import com.example.floodaid.roomDatabase.database.FloodAidDatabase
import com.example.floodaid.roomDatabase.Repository.FirestoreRepository
import com.example.floodaid.screen.Dashboard
import com.example.floodaid.screen.floodstatus.FloodStatus
import com.example.floodaid.screen.Notification
import com.example.floodaid.screen.floodstatus.FloodStatusRepository
import com.example.floodaid.screen.floodstatus.FloodStatusViewModelFactory
//import com.example.floodaid.screen.forum.CreateForumPost
import com.example.floodaid.screen.forum.Forum
import com.example.floodaid.screen.forum.ForumEvent
import com.example.floodaid.screen.forum.PostEditor
import com.example.floodaid.screen.login.Login
import com.example.floodaid.screen.login.RegisterProfile
import com.example.floodaid.screen.login.Signup
import com.example.floodaid.screen.login.Welcome
import com.example.floodaid.screen.login.WelcomeLoading
import com.example.floodaid.screen.map_UI.Map
import com.example.floodaid.screen.map_UI.MapViewModel
import com.example.floodaid.screen.map_UI.SOSButton
import com.example.floodaid.screen.map_UI.SOSButtonPlacement
import com.example.floodaid.screen.map_UI.SOSViewModel
import com.example.floodaid.screen.profile.Profile
import com.example.floodaid.screen.profile.ProfileViewModel
import com.example.floodaid.screen.volunteer.AddVolunteerEvent
import com.example.floodaid.screen.volunteer.EditVolunteerEvent
import com.example.floodaid.screen.volunteer.Volunteer
import com.example.floodaid.screen.volunteer.VolunteerHistory
import com.example.floodaid.screen.volunteer.VolunteerRegister
import com.example.floodaid.screen.volunteer.VolunteerViewModel
import com.example.floodaid.viewmodel.AuthViewModel
import com.example.floodaid.viewmodel.FloodStatusViewModel
import com.example.floodaid.viewmodel.ForumViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    onEvent: (ForumEvent) -> Unit,
    authViewModel: AuthViewModel,
    volunteerViewModel: VolunteerViewModel,
    forumViewModel: ForumViewModel,
    mapViewModel: MapViewModel,
    profileViewModel: ProfileViewModel
) {
    val sosViewModel: SOSViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.WelcomeLoading.route) {
        composable(route = Screen.Dashboard.route) {
            val context = LocalContext.current
            val database = FloodAidDatabase.getInstance(context)
            val dao = database.floodStatusDao()
            val repository = FloodStatusRepository(dao, FirestoreRepository())
            val firestoreRepository = FirestoreRepository()
            Box {
                Dashboard(
                    navController = navController,
                    authViewModel = authViewModel,
                    repository = repository,
                    dao = dao,
                    firestoreRepository = firestoreRepository,
                    profileViewModel = profileViewModel
                )
                SOSButton(
                    viewModel = sosViewModel,
                    placement = SOSButtonPlacement.DASHBOARD
                )
            }
        }
        composable(route = Screen.FloodStatus.route) {
            val context = LocalContext.current
            val database = FloodAidDatabase.getInstance(context)
            val dao = database.floodStatusDao()
            val repository = FloodStatusRepository(dao, FirestoreRepository())
            val firestoreRepository = FirestoreRepository()
            val viewModel: FloodStatusViewModel = viewModel(
                factory = FloodStatusViewModelFactory(
                    repository,
                    dao,
                    firestoreRepository,
                    SavedStateHandle()
                )
            )
            FloodStatus(navController = navController, viewModel = viewModel, database = database, mapViewModel)
            LaunchedEffect(Unit) {
                repository.initializePredefinedDistricts()
            }
        }



        composable(route = Screen.Map.route) {
            key("persistent_map") {
                Box {
                    Map(navController = navController, mapViewModel)
                    SOSButton(
                        viewModel = sosViewModel,
                        placement = SOSButtonPlacement.MAP
                    )
                }
            }
        }

        composable(route = Screen.Notification.route) {
            Notification(navController = navController,
                viewModel = profileViewModel)
        }

        composable(route = Screen.Profile.route) {
            Profile(navController = navController,
                viewModel = profileViewModel)
        }

        composable(route = Screen.Signup.route) {
            Signup(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.Volunteer.route) {
            Volunteer(navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable(
            route = "volunteerDetail/{eventId}",
            arguments =
                listOf(
                    navArgument("eventId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

            VolunteerDetail(
                eventId = eventId,
                navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable("volunteer_main") {
            Volunteer(
                navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable("addVolunteerEvent") {
            AddVolunteerEvent(navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable(
            route = "editVolunteerEvent/{eventId}",
            arguments =
                listOf(
                    navArgument("eventId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

            EditVolunteerEvent(
                eventId = eventId,
                navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable("volunteerHistory") {
            VolunteerHistory(navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable("volunteerRegister") {
            VolunteerRegister(navController = navController,
                viewModel = volunteerViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable(route = Screen.Welcome.route) {
            Welcome(
                navController = navController
            )
        }

        composable(route = Screen.Login.route) {
            Login(
                navController = navController,
                authViewModel = authViewModel,
            )
        }

        composable(route = Screen.WelcomeLoading.route) {
            WelcomeLoading(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.RegisterProfile.route) {
            RegisterProfile(
                navController = navController,
            )
        }

        composable(route = Screen.Forum.route) {
            Forum(
                navController = navController,
                viewModel = forumViewModel
            )
        }

        composable(route = Screen.ForumPostEditor.route) {
            PostEditor(
                navController = navController,
                onEvent = onEvent,
                viewModel = forumViewModel
            )
        }
    }
}