package com.attendancehr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attendancehr.app.data.repo.FirebaseHrRepository
import com.attendancehr.app.data.session.SessionStore
import com.attendancehr.app.ui.theme.AttendanceHRTheme
import com.attendancehr.app.ui.viewmodel.AppViewModel
import com.attendancehr.app.ui.viewmodel.AppViewModelFactory
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionStore = SessionStore(applicationContext)
        // Switch to Firebase Repository
        val repo = FirebaseHrRepository()
        val factory = AppViewModelFactory(sessionStore, repo)

        setContent {
            AttendanceHRTheme {
                val vm: AppViewModel = viewModel(factory = factory)
                AttendanceHRApp(vm = vm)
            }
        }
    }
}

fun generateOTP(): String {
    return (100000..999999).random().toString()
}

fun sendOTP(email: String, otp: String) {
    Thread {
        try {
            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"

            val session = Session.getInstance(props,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        // Use App Password for Gmail, not your actual login password
                        return PasswordAuthentication("bhramanbhagat@gmail.com", "YOUR_GMAIL_APP_PASSWORD")
                    }
                })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress("bhramanbhagat@gmail.com"))
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
            )
            message.subject = "Your OTP Code"
            message.setText("Your OTP is: $otp")

            Transport.send(message)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}
