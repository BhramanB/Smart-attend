package com.attendancehr.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendancehr.app.data.model.LeaveType
import com.attendancehr.app.data.repo.AuthError
import com.attendancehr.app.data.repo.HrRepository
import com.attendancehr.app.data.session.SessionState
import com.attendancehr.app.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiMessage(val text: String)

class AppViewModel(
    private val sessionStore: SessionStore,
    private val repo: HrRepository,
) : ViewModel() {
    val session = sessionStore.session.stateIn(viewModelScope, SharingStarted.Eagerly, SessionState())

    val currentUser = repo.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val todayAttendance = repo.todayAttendance.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val attendanceHistory = repo.attendanceHistory.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val leaveBalances = repo.leaveBalances.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val leaveHistory = repo.leaveHistory.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val holidays = repo.holidays.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val notifications = repo.notifications.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Classroom/Admin Features
    val managedClassroom = repo.managedClassroom.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val classStudents = repo.classStudents.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val todayClassAttendance = repo.todayClassAttendance.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingEmailForOtp = MutableStateFlow<String?>(null)
    val pendingEmailForOtp: StateFlow<String?> = _pendingEmailForOtp.asStateFlow()

    private val _pendingEmailForReset = MutableStateFlow<String?>(null)
    val pendingEmailForReset: StateFlow<String?> = _pendingEmailForReset.asStateFlow()

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    fun consumeMessage() {
        _messages.value = _messages.value.drop(1)
    }

    fun markOnboardingSeen() = viewModelScope.launch {
        sessionStore.markOnboardingSeen()
    }

    fun createClassroom(name: String) = viewModelScope.launch {
        runCatching { repo.createClassroom(name) }
            .onSuccess { code -> _messages.value = _messages.value + UiMessage("Classroom created! Code: $code") }
            .onFailure { pushError(it) }
    }

    fun joinClassroom(code: String) = viewModelScope.launch {
        runCatching { repo.joinClassroom(code) }
            .onSuccess { _messages.value = _messages.value + UiMessage("Joined classroom successfully!") }
            .onFailure { pushError(it) }
    }

    fun register(
        name: String,
        email: String,
        mobile: String,
        organization: String,
        department: String,
        idNumber: String,
        password: String,
    ) = viewModelScope.launch {
        runCatching {
            repo.register(name, email, mobile, organization, department, idNumber, password)
            repo.requestOtp(email)
        }.onSuccess { otp ->
            _pendingEmailForOtp.value = email.trim().lowercase()
            _messages.value = _messages.value + UiMessage("Registration successful! Your OTP is $otp")
        }.onFailure { pushError(it) }
    }

    fun resendOtp(email: String) = viewModelScope.launch {
        runCatching { repo.requestOtp(email) }
            .onSuccess { otp ->
                _messages.value = _messages.value + UiMessage("New OTP sent! Your OTP is $otp")
            }
            .onFailure { pushError(it) }
    }

    fun verifyOtp(email: String, otp: String, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repo.verifyOtp(email, otp) }
            .onSuccess {
                onSuccess()
            }
            .onFailure { pushError(it) }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repo.login(email, password) }
            .onSuccess { result ->
                sessionStore.setLoggedIn(result.user.id)
                onSuccess()
            }
            .onFailure { pushError(it) }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repo.loginWithGoogle(idToken) }
            .onSuccess { result ->
                sessionStore.setLoggedIn(result.user.id)
                onSuccess()
            }
            .onFailure { pushError(it) }
    }

    fun logout(onDone: () -> Unit) = viewModelScope.launch {
        sessionStore.logout()
        repo.logout()
        onDone()
    }

    fun forgotPassword(email: String, onOtpSent: () -> Unit) = viewModelScope.launch {
        runCatching { repo.requestOtp(email) }
            .onSuccess { otp ->
                _pendingEmailForReset.value = email.trim().lowercase()
                _messages.value = _messages.value + UiMessage("OTP sent! Your OTP is $otp")
                onOtpSent()
            }
            .onFailure { pushError(it) }
    }

    fun verifyResetOtp(email: String, otp: String, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repo.verifyOtp(email, otp) }
            .onSuccess { onSuccess() }
            .onFailure { pushError(it) }
    }

    fun resetPassword(email: String, newPassword: String, onSuccess: () -> Unit) = viewModelScope.launch {
        runCatching { repo.resetPassword(email, newPassword) }
            .onSuccess { onSuccess() }
            .onFailure { pushError(it) }
    }

    fun checkIn(locationHint: String? = null) = viewModelScope.launch {
        runCatching { repo.checkIn(deviceId = "Android", locationHint = locationHint) }
            .onFailure { pushError(it) }
    }

    fun checkOut() = viewModelScope.launch {
        runCatching { repo.checkOut() }
            .onFailure { pushError(it) }
    }

    fun applyLeave(
        type: LeaveType,
        startDate: LocalDate,
        endDate: LocalDate,
        reason: String,
        onSuccess: () -> Unit,
    ) = viewModelScope.launch {
        runCatching { repo.applyLeave(type, startDate, endDate, reason) }
            .onSuccess { onSuccess() }
            .onFailure { pushError(it) }
    }

    fun markNotificationRead(id: String) = viewModelScope.launch {
        repo.markNotificationRead(id)
    }

    private fun pushError(t: Throwable) {
        val msg = when (t) {
            is AuthError -> t.message ?: "Something went wrong"
            else -> t.message ?: "Something went wrong"
        }
        _messages.value = _messages.value + UiMessage(msg)
    }
}
