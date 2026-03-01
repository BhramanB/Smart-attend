package com.attendancehr.app

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.rememberNavController
import com.attendancehr.app.ui.navigation.AppNavHost
import com.attendancehr.app.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun AttendanceHRApp(vm: AppViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val messages by vm.messages.collectAsState()
    LaunchedEffect(messages) {
        val msg = messages.firstOrNull() ?: return@LaunchedEffect
        scope.launch { snackbarHostState.showSnackbar(msg.text) }
        vm.consumeMessage()
    }

    AppNavHost(
        navController = navController,
        vm = vm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

