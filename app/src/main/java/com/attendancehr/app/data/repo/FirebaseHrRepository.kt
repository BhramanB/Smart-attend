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
import com.attendancehr.app.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class FirebaseHrRepository : HrRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                fetchUserDetails(firebaseUser.uid)
            } else {
                _currentUser.value = null
            }
        }
    }

    private fun fetchUserDetails(uid: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: return@addSnapshotListener
                _currentUser.value = User(
                    id = uid,
                    name = data["name"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    mobile = data["mobile"] as? String ?: "",
                    organization = data["organization"] as? String ?: "",
                    department = data["department"] as? String ?: "",
                    idNumber = data["idNumber"] as? String ?: "",
                    role = UserRole.valueOf(data["role"] as? String ?: UserRole.Employee.name),
                    classroomId = data["classroomId"] as? String,
                    managedClassroomId = data["managedClassroomId"] as? String,
                )
            }
        }
    }

    override val managedClassroom: Flow<Classroom?> = callbackFlow {
        _currentUser.collect { user ->
            if (user?.managedClassroomId == null) {
                trySend(null)
            } else {
                val listener = db.collection("classrooms").document(user.managedClassroomId)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null && snapshot.exists()) {
                            val data = snapshot.data!!
                            trySend(Classroom(
                                id = snapshot.id,
                                adminId = data["adminId"] as String,
                                name = data["name"] as String,
                                code = data["code"] as String,
                                createdAt = Instant.ofEpochMilli(data["createdAt"] as Long)
                            ))
                        }
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    override val classStudents: Flow<List<User>> = callbackFlow {
        _currentUser.collect { user ->
            val classId = user?.managedClassroomId ?: user?.classroomId
            if (classId == null) {
                trySend(emptyList())
            } else {
                val listener = db.collection("users")
                    .whereEqualTo("classroomId", classId)
                    .addSnapshotListener { snapshot, _ ->
                        val list = snapshot?.documents?.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            User(
                                id = doc.id,
                                name = data["name"] as String,
                                email = data["email"] as String,
                                mobile = data["mobile"] as String,
                                organization = data["organization"] as String,
                                department = data["department"] as String,
                                idNumber = data["idNumber"] as String,
                                role = UserRole.valueOf(data["role"] as String),
                                classroomId = data["classroomId"] as? String,
                            )
                        } ?: emptyList()
                        trySend(list)
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    override val todayClassAttendance: Flow<List<AttendanceRecord>> = callbackFlow {
        _currentUser.collect { user ->
            val classId = user?.managedClassroomId
            if (classId == null) {
                trySend(emptyList())
            } else {
                val today = LocalDate.now().toString()
                val listener = db.collection("attendance")
                    .whereEqualTo("classroomId", classId)
                    .whereEqualTo("date", today)
                    .addSnapshotListener { snapshot, _ ->
                        val list = snapshot?.documents?.mapNotNull { doc ->
                            parseAttendance(doc.id, doc.data!!)
                        } ?: emptyList()
                        trySend(list)
                    }
                awaitClose { listener.remove() }
            }
        }
    }

    override suspend fun createClassroom(name: String): String {
        val user = _currentUser.value ?: throw AuthError.Validation("Not logged in")
        val code = UUID.randomUUID().toString().take(6).uppercase()
        val classId = UUID.randomUUID().toString()
        
        val classData = mapOf(
            "adminId" to user.id,
            "name" to name,
            "code" to code,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("classrooms").document(classId).set(classData).await()
        db.collection("users").document(user.id).update("managedClassroomId", classId, "role", UserRole.Admin.name).await()
        return code
    }

    override suspend fun joinClassroom(code: String) {
        val user = _currentUser.value ?: throw AuthError.Validation("Not logged in")
        val snapshot = db.collection("classrooms")
            .whereEqualTo("code", code.trim().uppercase())
            .get().await()
        
        val classDoc = snapshot.documents.firstOrNull() ?: throw AuthError.Validation("Invalid class code")
        db.collection("users").document(user.id).update("classroomId", classDoc.id).await()
    }

    override val todayAttendance: Flow<AttendanceRecord?> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val today = LocalDate.now().toString()
        val listener = db.collection("attendance")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("date", today)
            .addSnapshotListener { snapshot, _ ->
                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    trySend(parseAttendance(doc.id, doc.data!!))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override val attendanceHistory: Flow<List<AttendanceRecord>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("attendance")
            .whereEqualTo("userId", user.uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    parseAttendance(doc.id, doc.data!!)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override val leaveBalances: Flow<List<LeaveBalance>> = flow {
        emit(
            listOf(
                LeaveBalance(LeaveType.Casual, remainingDays = 6, totalDays = 12),
                LeaveBalance(LeaveType.Medical, remainingDays = 4, totalDays = 10),
                LeaveBalance(LeaveType.Annual, remainingDays = 10, totalDays = 18),
            )
        )
    }

    override val leaveHistory: Flow<List<LeaveRequest>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("leaves")
            .whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    parseLeave(doc.id, doc.data!!)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override val holidays: Flow<List<Holiday>> = callbackFlow {
        val listener = db.collection("holidays")
            .orderBy("date")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Holiday(
                        id = doc.id,
                        date = LocalDate.parse(data["date"] as String),
                        title = data["title"] as String,
                        description = data["description"] as String,
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override val notifications: Flow<List<AppNotification>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("notifications")
            .whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AppNotification(
                        id = doc.id,
                        type = NotificationType.valueOf(data["type"] as String),
                        title = data["title"] as String,
                        message = data["message"] as String,
                        createdAt = Instant.ofEpochMilli(data["createdAt"] as Long),
                        isUnread = data["isUnread"] as Boolean,
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
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
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw AuthError.Validation("Registration failed")
        
        val userData = mapOf(
            "name" to name,
            "email" to email,
            "mobile" to mobile,
            "organization" to organization,
            "department" to department,
            "idNumber" to idNumber,
            "role" to UserRole.Employee.name,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).set(userData).await()
    }

    override suspend fun requestOtp(email: String): String = "1234"

    override suspend fun verifyOtp(email: String, otp: String) {}

    override suspend fun login(email: String, password: String): AuthResult {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw AuthError.InvalidCredentials()
        return fetchAndEmitUser(firebaseUser.uid, firebaseUser.email ?: "")
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw AuthError.InvalidCredentials()

        val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
        if (!userDoc.exists()) {
            val userData = mapOf(
                "name" to (firebaseUser.displayName ?: ""),
                "email" to (firebaseUser.email ?: ""),
                "role" to UserRole.Employee.name,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users").document(firebaseUser.uid).set(userData).await()
        }

        return fetchAndEmitUser(firebaseUser.uid, firebaseUser.email ?: "")
    }

    private suspend fun fetchAndEmitUser(uid: String, email: String): AuthResult {
        val snapshot = db.collection("users").document(uid).get().await()
        val data = snapshot.data
        
        val user = User(
            id = uid,
            name = data?.get("name") as? String ?: "",
            email = data?.get("email") as? String ?: email,
            mobile = data?.get("mobile") as? String ?: "",
            organization = data?.get("organization") as? String ?: "",
            department = data?.get("department") as? String ?: "",
            idNumber = data?.get("idNumber") as? String ?: "",
            role = UserRole.valueOf(data?.get("role") as? String ?: UserRole.Employee.name),
            classroomId = data?.get("classroomId") as? String,
            managedClassroomId = data?.get("managedClassroomId") as? String,
        )
        _currentUser.value = user
        return AuthResult(user)
    }

    override suspend fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    override suspend fun resetPassword(email: String, newPassword: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun checkIn(deviceId: String?, locationHint: String?) {
        val user = _currentUser.value ?: return
        val today = LocalDate.now().toString()
        val now = System.currentTimeMillis()
        
        val attendanceData = mapOf(
            "userId" to user.id,
            "date" to today,
            "checkInAt" to now,
            "deviceId" to deviceId,
            "locationHint" to locationHint,
            "classroomId" to user.classroomId
        )
        db.collection("attendance").add(attendanceData).await()
    }

    override suspend fun checkOut() {
        val user = auth.currentUser ?: return
        val today = LocalDate.now().toString()
        val now = System.currentTimeMillis()

        val snapshot = db.collection("attendance")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("date", today)
            .get().await()
        
        val doc = snapshot.documents.firstOrNull() ?: return
        db.collection("attendance").document(doc.id).update("checkOutAt", now).await()
    }

    override suspend fun applyLeave(type: LeaveType, startDate: LocalDate, endDate: LocalDate, reason: String) {
        val user = auth.currentUser ?: return
        val leaveData = mapOf(
            "userId" to user.uid,
            "type" to type.name,
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString(),
            "reason" to reason,
            "status" to LeaveStatus.Pending.name,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("leaves").add(leaveData).await()
    }

    override suspend fun markNotificationRead(notificationId: String) {
        db.collection("notifications").document(notificationId).update("isUnread", false)
    }

    private fun parseAttendance(id: String, data: Map<String, Any>): AttendanceRecord {
        return AttendanceRecord(
            id = id,
            userId = data["userId"] as String,
            date = LocalDate.parse(data["date"] as String),
            checkInAt = (data["checkInAt"] as? Long)?.let { Instant.ofEpochMilli(it) },
            checkOutAt = (data["checkOutAt"] as? Long)?.let { Instant.ofEpochMilli(it) },
            deviceId = data["deviceId"] as? String,
            locationHint = data["locationHint"] as? String,
            classroomId = data["classroomId"] as? String,
        )
    }

    private fun parseLeave(id: String, data: Map<String, Any>): LeaveRequest {
        return LeaveRequest(
            id = id,
            userId = data["userId"] as String,
            type = LeaveType.valueOf(data["type"] as String),
            startDate = LocalDate.parse(data["startDate"] as String),
            endDate = LocalDate.parse(data["endDate"] as String),
            reason = data["reason"] as String,
            status = LeaveStatus.valueOf(data["status"] as String),
            adminRemark = data["adminRemark"] as? String,
            createdAt = Instant.ofEpochMilli(data["createdAt"] as Long),
        )
    }
}
