package com.bartolomeo.clinics.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bartolomeo.clinics.app.AppConfig
import com.bartolomeo.clinics.app.ui.auth.AuthEvent
import com.bartolomeo.clinics.app.ui.auth.AuthViewModel
import com.bartolomeo.clinics.app.ui.screens.AvailabilityScreen
import com.bartolomeo.clinics.app.ui.screens.BookingResultScreen
import com.bartolomeo.clinics.app.ui.screens.BookingWaitScreen
import com.bartolomeo.clinics.app.ui.screens.LoginScreen
import com.bartolomeo.clinics.app.ui.screens.MyBookingsScreen
import com.bartolomeo.clinics.app.ui.screens.PhoneRequestScreen
import com.bartolomeo.clinics.app.ui.screens.ProfessionalDetailScreen
import com.bartolomeo.clinics.app.ui.screens.RegisterScreen
import com.bartolomeo.clinics.app.ui.screens.SearchScreen
import com.bartolomeo.clinics.app.ui.screens.SyncStatusScreen

@Composable
fun ClinicsNavGraph(
    navController: NavHostController,
    config: AppConfig,
    authViewModel: AuthViewModel,
    startDestination: String,
) {
    val authState by authViewModel.state.collectAsState()

    // Los resultados del flujo de sesion viajan como eventos, no como estado: si
    // fueran un flag, al girar el telefono dispararian la navegacion otra vez.
    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                AuthEvent.LoggedIn -> navController.navigate(Routes.SEARCH) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }

                // El LOGIN ya esta abajo en el pila: volver es exactamente lo que hace
                // popBackStack, y volver a navegar dejaria dos LOGIN seguidos.
                AuthEvent.Registered -> navController.popBackStack()

                // Se sale desde SEARCH, que es la raiz cuando se esta logueado.
                AuthEvent.SessionClosed -> navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SEARCH) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                state = authState,
                onLoginChange = authViewModel::onLoginChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onSubmit = authViewModel::submitLogin,
                onGoToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                state = authState,
                onLoginChange = authViewModel::onRegisterLoginChange,
                onPasswordChange = authViewModel::onRegisterPasswordChange,
                onFirstNameChange = authViewModel::onFirstNameChange,
                onLastNameChange = authViewModel::onLastNameChange,
                onEmailChange = authViewModel::onEmailChange,
                onImageUrlChange = authViewModel::onImageUrlChange,
                onLangKeyChange = authViewModel::onLangKeyChange,
                onSubmit = authViewModel::submitRegister,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onLogout = authViewModel::logout,
                logoutError = authState.error,
                onOpenProfessional = { navController.navigate(Routes.professional(it)) },
                onSyncStatus = { navController.navigate(Routes.SYNC_STATUS) },
            )
        }

        composable(
            route = Routes.PROFESSIONAL,
            arguments = listOf(navArgument(Routes.ARG_PROFESSIONAL_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments
                ?.getString(Routes.ARG_PROFESSIONAL_ID)
                .orEmpty()

            ProfessionalDetailScreen(
                professionalId = professionalId,
                onSeeAvailability = { navController.navigate(Routes.availability(professionalId)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.AVAILABILITY,
            arguments = listOf(navArgument(Routes.ARG_PROFESSIONAL_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments
                ?.getString(Routes.ARG_PROFESSIONAL_ID)
                .orEmpty()

            AvailabilityScreen(
                professionalId = professionalId,
                onSlotSelected = { navController.navigate(Routes.bookingWait(it)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.BOOKING_WAIT,
            arguments = listOf(navArgument(Routes.ARG_HOLD_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val holdId = backStackEntry.arguments?.getString(Routes.ARG_HOLD_ID).orEmpty()

            BookingWaitScreen(
                holdId = holdId,
                onRequestPhone = { navController.navigate(Routes.phoneRequest(holdId)) },
            )
        }

        composable(
            route = Routes.PHONE_REQUEST,
            arguments = listOf(navArgument(Routes.ARG_HOLD_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val holdId = backStackEntry.arguments?.getString(Routes.ARG_HOLD_ID).orEmpty()

            PhoneRequestScreen(
                holdId = holdId,
                onSubmitted = { navController.navigate(Routes.bookingResult("turno-1")) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.BOOKING_RESULT,
            arguments = listOf(navArgument(Routes.ARG_APPOINTMENT_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments
                ?.getString(Routes.ARG_APPOINTMENT_ID)
                .orEmpty()

            BookingResultScreen(
                appointmentId = appointmentId,
                onSeeBookings = { navController.navigate(Routes.MY_BOOKINGS) },
            )
        }

        composable(Routes.MY_BOOKINGS) {
            MyBookingsScreen(
                onOpenSyncStatus = { navController.navigate(Routes.SYNC_STATUS) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SYNC_STATUS) {
            SyncStatusScreen(
                config = config,
                onMyBookings = { navController.navigate(Routes.MY_BOOKINGS) },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
