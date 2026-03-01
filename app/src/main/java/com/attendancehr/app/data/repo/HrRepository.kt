package com.attendancehr.app.data.repo

import com.attendancehr.app.data.model.AppNotification
import com.attendancehr.app.data.model.AttendanceRecord
import com.attendancehr.app.data.model.Classroom
import com.attendancehr.app.data.model.Holiday
import com.attendancehr.app.data.model.LeaveBalance
import com.attendancehr.app.data.model.LeaveRequest
import com.attendancehr.app.data.model.LeaveType
import com.attendancehr.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class AuthResult(
    val user: User,
)

sealed class AuthError(message: String) : Exception(message) {
    data class InvalidCredentials(override val message: String = "Invalid credentials") : AuthError(message)
    data class EmailAlreadyExists(override val message: String = "Email already exists") : AuthError(message)
    data class InvalidOtp(override val message: String = "Invalid OTP") : AuthError(message)
    data class OtpExpired(override val message: String = "Otp expired") : AuthError(message)
    data class Validation(override val message: String) : AuthError(message)
}

interface HrRepository {
    val currentUser: Flow<User?>
    val todayAttendance: Flow<AttendanceRecord?>
    val attendanceHistory: Flow<List<AttendanceRecord>>
    val leaveBalances: Flow<List<LeaveBalance>>
    val leaveHistory: Flow<List<LeaveRequest>>
    val holidays: Flow<List<Holiday>>
    val notifications: Flow<List<AppNotification>>

    // Classroom Features
    val managedClassroom: Flow<Classroom?>
    val classStudents: Flow<List<User>>
    val todayClassAttendance: Flow<List<AttendanceRecord>>

    suspend fun createClassroom(name: String): String // Returns class code
    suspend fun joinClassroom(code: String)

    suspend fun register(
        name: String,
        email: String,
        mobile: String,
        organization: String,
        department: String,
        idNumber: String,
        password: String,
    )

    suspend fun requestOtp(email: String): String
    suspend fun verifyOtp(email: String, otp: String)

    suspend fun login(email: String, password: String): AuthResult
    suspend fun loginWithGoogle(idToken: String): AuthResult
    suspend fun logout()

    suspend fun resetPassword(email: String, newPassword: String)

    suspend fun checkIn(deviceId: String? = null, locationHint: String? = null)
    suspend fun checkOut()

    suspend fun applyLeave(
        type: LeaveType,
        startDate: LocalDate,
        endDate: LocalDate,
        reason: String,
    )

    suspend fun markNotificationRead(notificationId: String)
}
