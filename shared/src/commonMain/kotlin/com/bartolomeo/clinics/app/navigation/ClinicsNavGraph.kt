package com.bartolomeo.clinics.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bartolomeo.clinics.app.AppConfig
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
) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { navController.enterApp() },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(onRegistered = { navController.enterApp() })
        }

        composable(Routes.SEARCH) {
            SearchScreen(
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

// Al entrar, el login deja de estar detrás en el stack: el botón atrás no debe
// devolver a un formulario ya resuelto.
private fun NavHostController.enterApp() {
    navigate(Routes.SEARCH) {
        popUpTo(Routes.LOGIN) { inclusive = true }
    }
}
