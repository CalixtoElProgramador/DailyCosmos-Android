package com.listocalixto.dailycosmos.ui.settings.help.contact_me

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.application.AppConstants
import com.listocalixto.dailycosmos.data.model.User
import com.listocalixto.dailycosmos.databinding.FragmentContactMeBinding
import com.listocalixto.dailycosmos.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ContactMeFragment : Fragment(R.layout.fragment_contact_me) {

    private lateinit var binding: FragmentContactMeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentContactMeBinding.bind(view)
        disableErrorMessage()

        binding.buttonSendEmail.setOnClickListener { verifyInputs() }
    }

    private fun verifyInputs() {
        when {
            binding.inputMessage.text.isNullOrBlank() -> {
                binding.inputLayoutMessage.error = getString(R.string.error_field_empty)
            }
            else -> {
                navigateToEmailApp()
            }
        }
    }

    private fun navigateToEmailApp() {
        val subject = binding.inputSubject.text.toString()
        val message = binding.inputMessage.text.toString()
        val email = AppConstants.EMAIL
        val address = email.split(",".toRegex()).toTypedArray()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, address)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(emailIntent)
    }

    private fun disableErrorMessage() {
        binding.inputMessage.doAfterTextChanged {
            binding.inputLayoutMessage.isErrorEnabled = false
        }
    }

}

