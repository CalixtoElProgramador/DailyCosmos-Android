package com.listocalixto.dailycosmos.ui.settings.help.contact_me

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.data.model.User
import com.listocalixto.dailycosmos.databinding.FragmentContactMeBinding
import com.listocalixto.dailycosmos.ui.settings.SettingsViewModel
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class ContactMeFragment : Fragment(R.layout.fragment_contact_me) {

    private val viewModelShared by activityViewModels<SettingsViewModel>()

    private lateinit var binding: FragmentContactMeBinding
    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentContactMeBinding.bind(view)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding.buttonSendEmail.setOnClickListener { sendEmail() }
        viewModelShared.getUser().value?.let {
            user = it
        }
    }

    private fun sendEmail() {
        val userName =  AppConstants.EMAIL
        val password =  AppConstants.PASSWORD
        // FYI: passwords as a command arguments isn't safe
        // They go into your bash/zsh history and are visible when running ps

        val emailFrom = AppConstants.EMAIL
        val emailTo = user.email
        val emailCC = "correo.basura0811@gmail.com"

        val subject = "SMTP Test"
        val text = "Hello Kotlin Mail"

        val props = Properties()
        putIfMissing(props, "mail.smtp.host", "smtp.office365.com")
        putIfMissing(props, "mail.smtp.port", "587")
        putIfMissing(props, "mail.smtp.auth", "true")
        putIfMissing(props, "mail.smtp.starttls.enable", "true")

        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(userName, password)
            }
        })

        session.debug = true

        try {
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(emailFrom))
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo, false))
            mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(emailCC, false))
            mimeMessage.setText(text)
            mimeMessage.subject = subject
            mimeMessage.sentDate = Date()

            val smtpTransport = session.getTransport("smtp")
            smtpTransport.connect()
            smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
            smtpTransport.close()
        } catch (messagingException: MessagingException) {
            messagingException.printStackTrace()
        }
    }

    private fun putIfMissing(props: Properties, key: String, value: String) {
        if (!props.containsKey(key)) {
            props[key] = value
        }
    }

}

