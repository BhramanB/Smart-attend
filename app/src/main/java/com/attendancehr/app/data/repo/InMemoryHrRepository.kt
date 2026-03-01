package com.attendancehr.app.data.repo

import com.attendancehr.app.data.model.AppNotification
import com.attendancehr.app.data.model.AttendanceRecord
import com.attendancehr.app.data.model.Classroom
import com.attendancehr.app.data.model.Holiday
import com.attendancehr.app.data.model.LeaveBalance
import com.attendancehr.app.data.model.LeaveRequest
import com.attendancehr.app.data.model.LeaveStatus
import com.attendancehr.app.data.model.LeaveType
import com.attendancehr.app.data.model.NotificationType
import com.attendancehr.app.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random

class InMemoryHrRepository : HrRepository {
    private data class StoredUser(
        val user: User,
        val passwordHash: String,
        val isVerified: Boolean,
    )

    private data class OtpState(
        val otp: String,
        val expiresAt: Instant,
    )

    private val storedUsersByEmail = linkedMapOf<String, StoredUser>()
    private val otpByEmail = linkedMapOf<String, OtpState>()
    private val attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    private val leaves = MutableStateFlow<List<LeaveRequest>>(emptyList())
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    private val _currentUser = MutableStateFlow<User?>(null)

    private val _classrooms = MutableStateFlow<Map<String, Classroom>>(emptyMap())

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        // Pre-add the requested user for testing
        val email = "bhramanbhagat@gmail.com"
        val user = User(
            name = "Bhraman Bhagat",
            email = email,
            mobile = "+91-98765-43210",
            organization = "Attendance HR",
            department = "Management",
            idNumber = "BH-2007",
        )
        storedUsersByEmail[email] = StoredUser(
            user = user,
            passwordHash = sha256("bhraman2007"),
            isVerified = true,
        )
    }

    private val _holidays = MutableStateFlow(
        listOf(
            Holiday(
                date = LocalDate.now().withMonth(1).withDayOfMonth(26),
                title = "Republic Day",
                description = "National holiday",
            ),
            Holiday(
                date = LocalDate.now().withMonth(8).withDayOfMonth(15),
                title = "Independence Day",
                description = "National holiday",
            ),
        ).sortedBy { it.date }
    )

    private val _leaveBalances = MutableStateFlow(
        listOf(
            LeaveBalance(LeaveType.Casual, remainingDays = 6, totalDays = 12),
            LeaveBalance(LeaveType.Medical, remainingDays = 4, totalDays = 10),
            LeaveBalance(LeaveType.Annual, remainingDays = 10, totalDays = 18),
        )
    )

    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override val todayAttendance: Flow<AttendanceRecord?> =
        combine(attendance, _currentUser) { list, user ->
            val userId = user?.id ?: return@combine null
            val today = LocalDate.now(zoneId)
            list.lastOrNull { it.userId == userId && it.date == today }
        }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, null)

    override val attendanceHistory: Flow<List<AttendanceRecord>> =
        combine(attendance, _currentUser) { list, user ->
            val userId = user?.id ?: return@combine emptyList()
            list.filter { it.userId == userId }.sortedByDescending { it.date }
        }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    override val leaveBalances: Flow<List<LeaveBalance>> = _leaveBalances.asStateFlow()

    override val leaveHistory: Flow<List<LeaveRequest>> =
        combine(leaves, _currentUser) { list, user ->
            val userId = user?.id ?: return@combine emptyList()
            list.filter { it.userId == userId }.sortedByDescending { it.createdAt }
        }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    override val holidays: Flow<List<Holiday>> = _holidays.asStateFlow()

    override val notifications: Flow<List<AppNotification>> = _notifications.asStateFlow()

    // Classroom Features
    override val managedClassroom: Flow<Classroom?> = combine(_classrooms, _currentUser) { classrooms, user ->
        classrooms.values.firstOrNull { it.adminId == user?.id }
    }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, null)

    override val classStudents: Flow<List<User>> = flow {
        // In-memory mock: return students joined to the current user's classroom
        emit(emptyList<User>())
    }

    override val todayClassAttendance: Flow<List<AttendanceRecord>> = flow {
        emit(emptyList<AttendanceRecord>())
    }

    override suspend fun createClassroom(name: String): String {
        val user = _currentUser.value ?: throw AuthError.Validation("Not logged in")
        val code = UUID.randomUUID().toString().take(6).uppercase()
        val classroom = Classroom(adminId = user.id, name = name, code = code)
        _classrooms.update { it + (classroom.id to classroom) }
        _currentUser.update { it?.copy(managedClassroomId = classroom.id) }
        return code
    }

    override suspend fun joinClassroom(code: String) {
        val classroom = _classrooms.value.values.firstOrNull { it.code == code.uppercase() }
            ?: throw AuthError.Validation("Invalid class code")
        _currentUser.update { it?.copy(classroomId = classroom.id) }
    }

    override suspend fun register(
        name: String,
        email: String,
        mobile: String,
        organization: String,
        department: String,
        idNumber: String,
        password: String,
    ) {
        val normalizedEmail = email.trim().lowercase()
        requireFields(
            name to "Name",
            normalizedEmail to "Email",
            mobile to "Mobile Number",
            organization to "College/Company Name",
            department to "Department",
            idNumber to "ID Number",
            password to "Password",
        )
        if (storedUsersByEmail.containsKey(normalizedEmail)) throw AuthError.EmailAlreadyExists()

        val user = User(
            name = name.trim(),
            email = normalizedEmail,
            mobile = mobile.trim(),
            organization = organization.trim(),
            department = department.trim(),
            idNumber = idNumber.trim(),
        )

        storedUsersByEmail[normalizedEmail] = StoredUser(
            user = user,
            passwordHash = sha256(password),
            isVerified = false,
        )

        requestOtp(normalizedEmail)
    }

    override suspend fun requestOtp(email: String): String {
        val normalizedEmail = email.trim().lowercase()
        if (!storedUsersByEmail.containsKey(normalizedEmail)) {
            throw AuthError.Validation("Account not found for this email")
        }
        val otp = Random.nextInt(1000, 9999).toString()
        otpByEmail[normalizedEmail] = OtpState(
            otp = otp,
            expiresAt = Instant.now().plusSeconds(5 * 60),
        )

        pushNotification(
            type = NotificationType.Security,
            title = "OTP generated",
            message = "Your OTP is $otp (demo). In production, this is sent via email/SMS.",
        )
        return otp
    }

    override suspend fun verifyOtp(email: String, otp: String) {
        val normalizedEmail = email.trim().lowercase()
        val state = otpByEmail[normalizedEmail] ?: throw AuthError.InvalidOtp()
        if (Instant.now().isAfter(state.expiresAt)) throw AuthError.OtpExpired()
        if (state.otp != otp.trim()) throw AuthError.InvalidOtp()

        val stored = storedUsersByEmail[normalizedEmail] ?: throw AuthError.Validation("Account not found")
        storedUsersByEmail[normalizedEmail] = stored.copy(isVerified = true)
        otpByEmail.remove(normalizedEmail)

        pushNotification(
            type = NotificationType.Security,
            title = "Verification successful",
            message = "Your account has been verified.",
        )
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val stored = storedUsersByEmail[normalizedEmail] ?: throw AuthError.InvalidCredentials()
        if (stored.passwordHash != sha256(password)) throw AuthError.InvalidCredentials()
        if (!stored.isVerified) throw AuthError.Validation("Please verify OTP before login")

        _currentUser.value = stored.user
        pushNotification(
            type = NotificationType.Security,
            title = "Signed in",
            message = "Welcome back, ${stored.user.name}.",
        )
        return AuthResult(stored.user)
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        // Mock implementation for demo
        val email = "google-user@example.com"
        val user = User(
            id = "google-uid-123",
            name = "Google User",
            email = email,
            mobile = "+91-00000-00000",
            organization = "Google Inc",
            department = "Cloud",
            idNumber = "G-101",
        )
        _currentUser.value = user
        pushNotification(
            type = NotificationType.Security,
            title = "Signed in with Google",
            message = "Welcome, ${user.name}.",
        )
        return AuthResult(user)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun resetPassword(email: String, newPassword: String) {
        val normalizedEmail = email.trim().lowercase()
        requireFields(newPassword to "New Password")
        val stored = storedUsersByEmail[normalizedEmail] ?: throw AuthError.Validation("Account not found")
        storedUsersByEmail[normalizedEmail] = stored.copy(passwordHash = sha256(newPassword))
        pushNotification(
            type = NotificationType.Security,
            title = "Password updated",
            message = "Your password was updated successfully.",
        )
    }

    override suspend fun checkIn(deviceId: String?, locationHint: String?) {
        val userId = _currentUser.value?.id ?: throw AuthError.Validation("Not logged in")
        val today = LocalDate.now(zoneId)
        val now = Instant.now()

        attendance.update { list ->
            val existing = list.lastOrNull { it.userId == userId && it.date == today }
            if (existing != null && existing.checkInAt != null && existing.checkOutAt == null) return@update list

            val updated = AttendanceRecord(
                userId = userId,
                date = today,
                checkInAt = now,
                deviceId = deviceId,
                locationHint = locationHint,
                classroomId = _currentUser.value?.classroomId
            )
            list + updated
        }

        pushNotification(
            type = NotificationType.Attendance,
            title = "Checked in",
            message = "Check-in recorded at ${now.atZone(zoneId).toLocalTime().withNano(0)}.",
        )
    }

    override suspend fun checkOut() {
        val userId = _currentUser.value?.id ?: throw AuthError.Validation("Not logged in")
        val today = LocalDate.now(zoneId)
        val now = Instant.now()

        attendance.update { list ->
            val idx = list.indexOfLast { it.userId == userId && it.date == today }
            if (idx < 0) return@update list
            val rec = list[idx]
            if (rec.checkInAt == null || rec.checkOutAt != null) return@update list
            list.toMutableList().also { it[idx] = rec.copy(checkOutAt = now) }
        }

        pushNotification(
            type = NotificationType.Attendance,
            title = "Checked out",
            message = "Check-out recorded at ${now.atZone(zoneId).toLocalTime().withNano(0)}.",
        )
    }

    override suspend fun applyLeave(type: LeaveType, startDate: LocalDate, endDate: LocalDate, reason: String) {
        val userId = _currentUser.value?.id ?: throw AuthError.Validation("Not logged in")
        requireFields(reason to "Reason")
        if (endDate.isBefore(startDate)) throw AuthError.Validation("End date must be after start date")

        leaves.update { list ->
            list + LeaveRequest(
                userId = userId,
                type = type,
                startDate = startDate,
                endDate = endDate,
                reason = reason.trim(),
                status = LeaveStatus.Pending,
            )
        }

        pushNotification(
            type = NotificationType.Leave,
            title = "Leave requested",
            message = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} leave submitted (${startDate} to ${endDate}).",
        )
    }

    override suspend fun markNotificationRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isUnread = false) else it }
        }
    }

    private fun requireFields(vararg fields: Pair<String, String>) {
        fields.forEach { (value, label) ->
            if (value.trim().isEmpty()) throw AuthError.Validation("$label is required")
        }
    }

    private fun pushNotification(type: NotificationType, title: String, message: String) {
        _notifications.update { list ->
            listOf(
                AppNotification(
                    type = type,
                    title = title,
                    message = message,
                )
            ) + list
        }
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
