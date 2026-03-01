package com.attendancehr.app.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TimeToLeave
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.components.SecondaryButton
import com.attendancehr.app.ui.navigation.Routes
import com.attendancehr.app.ui.viewmodel.AppViewModel

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    navController: NavHostController,
    vm: AppViewModel,
    initialTab: String,
) {
    val showSheet = remember { mutableStateOf(false) }
    val today by vm.todayAttendance.collectAsState()

    val tabs = listOf(
        BottomTab(Routes.Home, "Home", Icons.Outlined.Home),
        BottomTab(Routes.Attendance, "History", Icons.Outlined.CalendarMonth),
        BottomTab(Routes.Leaves, "Leaves", Icons.Outlined.TimeToLeave),
        BottomTab(Routes.Profile, "Profile", Icons.Outlined.Person),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        selected = initialTab == tab.route,
                        onClick = {
                            if (initialTab != tab.route) {
                                navController.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet.value = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
            }
        },
    ) { inner ->
        Box(modifier = Modifier.padding(inner)) {
            when (initialTab) {
                Routes.Home -> HomeDashboardScreen(
                    vm = vm,
                    onOpenNotifications = { navController.navigate(Routes.Notifications) },
                    onOpenCameraAttendance = { navController.navigate(Routes.CameraAttendance) },
                )
                Routes.Attendance -> AttendanceHistoryScreen(
                    vm = vm,
                    onBack = null,
                )
                Routes.Leaves -> LeaveManagementScreen(
                    vm = vm,
                    onApply = { navController.navigate(Routes.ApplyLeave) },
                )
                Routes.Profile -> ProfileScreen(
                    vm = vm,
                    onOpenNotifications = { navController.navigate(Routes.Notifications) },
                    onOpenHolidays = { navController.navigate(Routes.Holidays) },
                    onOpenTeam = { navController.navigate(Routes.Team) },
                    onChangePassword = { navController.navigate(Routes.ChangePassword) },
                    onTerms = { navController.navigate(Routes.Terms) },
                    onPrivacy = { navController.navigate(Routes.Privacy) },
                    onLogout = {
                        vm.logout {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Home) { inclusive = true }
                            }
                        }
                    },
                )
            }
        }
    }

    if (showSheet.value) {
        ModalBottomSheet(onDismissRequest = { showSheet.value = false }) {
            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            PrimaryButton(
                text = if (today?.checkInAt == null) "Check-In (Camera + GPS)" else if (today?.checkOutAt == null) "Check-Out" else "Checked out",
                enabled = today?.checkOutAt == null,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                onClick = {
                    if (today?.checkInAt == null) {
                        showSheet.value = false
                        navController.navigate(Routes.CameraAttendance)
                    } else {
                        vm.checkOut()
                        showSheet.value = false
                    }
                },
            )
            SecondaryButton(
                text = "Apply Leave",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                onClick = {
                    showSheet.value = false
                    navController.navigate(Routes.ApplyLeave)
                },
            )
            SecondaryButton(
                text = "Attendance History",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                onClick = {
                    showSheet.value = false
                    navController.navigate(Routes.Attendance)
                },
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 18.dp))
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(tab: BottomTab, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
        label = { Text(text = tab.label) },
        alwaysShowLabel = true,
    )
}
