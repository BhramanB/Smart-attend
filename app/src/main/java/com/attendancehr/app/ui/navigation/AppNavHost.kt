package com.attendancehr.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.attendancehr.app.ui.screens.SplashScreen
import com.attendancehr.app.ui.screens.auth.ForgotPasswordScreen
import com.attendancehr.app.ui.screens.auth.LoginScreen
import com.attendancehr.app.ui.screens.auth.OnboardingScreen
import com.attendancehr.app.ui.screens.auth.OtpVerificationScreen
import com.attendancehr.app.ui.screens.auth.RegisterScreen
import com.attendancehr.app.ui.screens.auth.ResetPasswordScreen
import com.attendancehr.app.ui.screens.auth.SuccessScreen
import com.attendancehr.app.ui.screens.legal.PrivacyPolicyScreen
import com.attendancehr.app.ui.screens.legal.TermsScreen
import com.attendancehr.app.ui.screens.main.ApplyLeaveScreen
import com.attendancehr.app.ui.screens.main.AttendanceHistoryScreen
import com.attendancehr.app.ui.screens.main.CameraAttendanceScreen
import com.attendancehr.app.ui.screens.main.ChangePasswordScreen
import com.attendancehr.app.ui.screens.main.HolidaysScreen
import com.attendancehr.app.ui.screens.main.LeaveManagementScreen
import com.attendancehr.app.ui.screens.main.MainShell
import com.attendancehr.app.ui.screens.main.NotificationsScreen
import com.attendancehr.app.ui.screens.main.TeamScreen
import com.attendancehr.app.ui.viewmodel.AppViewModel

@Composable
fun AppNavHost(
    vm: AppViewModel,
    snackbarHost: @Composable () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val session by vm.session.collectAsState()

    Scaffold(snackbarHost = snackbarHost) { inner ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash,
            modifier = Modifier.padding(inner),
        ) {
            composable(Routes.Splash) {
                SplashScreen(
                    onFinished = {
                        val dest = when {
                            session.isLoggedIn -> Routes.Home
                            session.hasSeenOnboarding -> Routes.Login
                            else -> Routes.Onboarding
                        }
                        navController.navigate(dest) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.Onboarding) {
                OnboardingScreen(
                    onGetStarted = {
                        vm.markOnboardingSeen()
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Onboarding) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.Login) {
                LoginScreen(
                    onLogin = { email, password ->
                        vm.login(email, password) {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        }
                    },
                    onGoogleLogin = { idToken ->
                        vm.loginWithGoogle(idToken) {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        }
                    },
                    onForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                    onRegister = { navController.navigate(Routes.Register) },
                )
            }

            composable(Routes.Register) {
                RegisterScreen(
                    onRegister = { form ->
                        vm.register(
                            name = form.name,
                            email = form.email,
                            mobile = form.mobile,
                            organization = form.organization,
                            department = form.department,
                            idNumber = form.idNumber,
                            password = form.password,
                        )
                    },
                    onBack = { navController.popBackStack() },
                )
                val pendingEmail by vm.pendingEmailForOtp.collectAsState()
                LaunchedEffect(pendingEmail) {
                    if (pendingEmail != null) navController.navigate(Routes.OtpVerifyRegister)
                }
            }

            composable(Routes.OtpVerifyRegister) {
                val email by vm.pendingEmailForOtp.collectAsState()
                OtpVerificationScreen(
                    title = "Verify OTP",
                    subtitle = "Enter the 4-digit code sent to your email.",
                    email = email ?: "",
                    onResend = { vm.resendOtp(email ?: "") },
                    onVerify = { otp ->
                        vm.verifyOtp(email ?: "", otp) {
                            navController.navigate(Routes.Success) {
                                popUpTo(Routes.Register) { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable(Routes.ForgotPassword) {
                ForgotPasswordScreen(
                    onSendOtp = { email ->
                        vm.forgotPassword(email) {
                            navController.navigate(Routes.OtpVerifyReset)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.OtpVerifyReset) {
                val email by vm.pendingEmailForReset.collectAsState()
                OtpVerificationScreen(
                    title = "Verify OTP",
                    subtitle = "Enter the 4-digit code to reset your password.",
                    email = email ?: "",
                    onResend = { vm.resendOtp(email ?: "") },
                    onVerify = { otp ->
                        vm.verifyResetOtp(email ?: "", otp) {
                            navController.navigate(Routes.ResetPassword)
                        }
                    },
                )
            }

            composable(Routes.ResetPassword) {
                val email by vm.pendingEmailForReset.collectAsState()
                ResetPasswordScreen(
                    onUpdate = { newPass ->
                        vm.resetPassword(email ?: "", newPass) {
                            navController.navigate(Routes.Success) {
                                popUpTo(Routes.Login) { inclusive = false }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.Success) {
                SuccessScreen(
                    message = "All set! Continue to your dashboard.",
                    onContinue = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Success) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.Home) {
                MainShell(navController = navController, vm = vm, initialTab = Routes.Home)
            }
            composable(Routes.Attendance) {
                MainShell(navController = navController, vm = vm, initialTab = Routes.Attendance)
            }
            composable(Routes.Leaves) {
                MainShell(navController = navController, vm = vm, initialTab = Routes.Leaves)
            }
            composable(Routes.Profile) {
                MainShell(navController = navController, vm = vm, initialTab = Routes.Profile)
            }

            composable(Routes.CameraAttendance) {
                CameraAttendanceScreen(
                    onCaptured = { lat, lon ->
                        vm.checkIn(locationHint = "Lat: $lat, Lon: $lon")
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ApplyLeave) { ApplyLeaveScreen(vm = vm, onBack = { navController.popBackStack() }) }
            composable(Routes.Team) { TeamScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.Holidays) { HolidaysScreen(vm = vm, onBack = { navController.popBackStack() }) }
            composable(Routes.Notifications) {
                NotificationsScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.ChangePassword) { ChangePasswordScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.Terms) { TermsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.Privacy) { PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
            composable("attendance_history") {
                AttendanceHistoryScreen(vm = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
