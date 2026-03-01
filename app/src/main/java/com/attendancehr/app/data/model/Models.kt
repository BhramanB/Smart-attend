package com.attendancehr.app.data.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class UserRole {
    Employee,
    Student,
    Lecturer,
    Admin,
}

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val mobile: String,
    val organization: String,
    val department: String,
    val idNumber: String,
    val role: UserRole = UserRole.Employee,
    val classroomId: String? = null, // For students to join a class
    val managedClassroomId: String? = null, // For admin/lecturer who created the class
)

data class Classroom(
    val id: String = UUID.randomUUID().toString(),
    val adminId: String,
    val name: String,
    val code: String, // Code students use to join
    val createdAt: Instant = Instant.now()
)

data class AttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: LocalDate,
    val checkInAt: Instant? = null,
    val checkOutAt: Instant? = null,
    val deviceId: String? = null,
    val locationHint: String? = null,
    val classroomId: String? = null,
) {
    val totalDuration: Duration?
        get() {
            val inAt = checkInAt ?: return null
            val outAt = checkOutAt ?: return null
            return Duration.between(inAt, outAt).coerceAtLeast(Duration.ZERO)
        }
}

enum class LeaveType { Casual, Medical, Annual }

enum class LeaveStatus { Approved, Pending, Rejected }

data class LeaveBalance(
    val type: LeaveType,
    val remainingDays: Int,
    val totalDays: Int,
)

data class LeaveRequest(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: LeaveType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String,
    val status: LeaveStatus = LeaveStatus.Pending,
    val adminRemark: String? = null,
    val createdAt: Instant = Instant.now(),
)

data class Holiday(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val title: String,
    val description: String,
)

enum class NotificationType { Attendance, Leave, Holiday, Security, General }

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val createdAt: Instant = Instant.now(),
    val isUnread: Boolean = true,
)
