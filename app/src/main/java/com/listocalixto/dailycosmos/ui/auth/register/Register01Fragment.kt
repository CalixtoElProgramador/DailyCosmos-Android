package com.listocalixto.dailycosmos.ui.auth.register

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.remote.auth.AuthDataSource
import com.listocalixto.dailycosmos.databinding.FragmentRegister01Binding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModelFactory
import com.listocalixto.dailycosmos.domain.auth.AuthRepoImpl
import com.listocalixto.dailycosmos.core.Result

@Suppress("DEPRECATION")
class Register01Fragment : Fragment(R.layout.fragment_register01) {

    private lateinit var binding: FragmentRegister01Binding

    private val patternEmail = Patterns.EMAIL_ADDRESS.toRegex()
    private val viewModelShared: RegisterViewModel by activityViewModels()
    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepoImpl(AuthDataSource()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showButtons()
    }

    private fun showButtons() {
        Handler().postDelayed({
            activity?.findViewById<LinearLayout>(R.id.buttons_register)?.apply {
                animation =
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.fade_in_main
                    )
                visibility = View.VISIBLE
            }
        }, 400)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegister01Binding.bind(view)
        getInputsFromViewModel()
        setErrorEnabledAfterChanges()

        activity?.findViewById<MaterialButton>(R.id.button_next)?.setOnClickListener {
            validateInputs()
        }

        activity?.findViewById<MaterialButton>(R.id.button_back)?.setOnClickListener {
            backTo()
        }
    }

    private fun backTo() {
        saveInputsToViewModel()
        activity?.onBackPressed()
    }

    private fun getInputsFromViewModel() {
        viewModelShared.getPerson().value?.let { person ->
            binding.inputName.setText(person.name)
            binding.inputLastName.setText(person.lastname)
            binding.inputEmail.setText(person.email)
        }
    }

    private fun setErrorEnabledAfterChanges() {
        binding.inputName.doAfterTextChanged {
            binding.inputLayoutName.isErrorEnabled = false
        }
        binding.inputLastName.doAfterTextChanged {
            binding.inputLayoutLastName.isErrorEnabled = false
        }
        binding.inputEmail.doAfterTextChanged {
            binding.inputLayoutEmail.isErrorEnabled = false
        }
    }

    private fun validateInputs() {
        when {
            binding.inputName.text.isNullOrEmpty() -> binding.inputLayoutName.error =
                getString(R.string.error_field_empty)
            binding.inputLastName.text.isNullOrEmpty() -> binding.inputLayoutLastName.error =
                getString(R.string.error_field_empty)
            binding.inputEmail.text.isNullOrEmpty() -> binding.inputLayoutEmail.error =
                getString(R.string.error_field_empty)
            !binding.inputEmail.text.toString()
                .matches(patternEmail) -> binding.inputLayoutEmail.error =
                getString(R.string.error_email_no_valid)
            else -> isEmailRegistered(binding.inputEmail.text.toString().trim())
        }
    }

    private fun isEmailRegistered(email: String) {
        viewModel.isEmailRegister(email).observe(viewLifecycleOwner, { result ->
            when (result) {
                is Result.Loading -> {
                    setEnabledViews(false)
                }
                is Result.Success -> {
                    setEnabledViews(true)
                    if (!result.data) {
                        binding.inputLayoutEmail.error = getString(R.string.error_email_in_use)
                    } else {
                        saveInputsToViewModel()
                        nextFragment()
                    }
                }
                is Result.Failure -> {
                    setEnabledViews(true)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${result.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    @SuppressLint("ResourceAsColor")
    private fun setEnabledViews(boolean: Boolean) {
        binding.inputLayoutName.isEnabled = boolean
        binding.inputLayoutLastName.isEnabled = boolean
        binding.inputLayoutEmail.isEnabled = boolean
        activity?.findViewById<MaterialButton>(R.id.button_back)?.isEnabled = boolean
        activity?.findViewById<MaterialButton>(R.id.button_next)?.isEnabled = boolean

        if (boolean) {
            binding.lottieLoading.visibility = View.INVISIBLE

        } else {
            binding.lottieLoading.visibility = View.VISIBLE
        }
    }


    private fun saveInputsToViewModel() {
        val name = binding.inputName.text.toString().trim()
        val lastname = binding.inputLastName.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        viewModelShared.setPerson(Person(name, lastname, email))
    }

    private fun nextFragment() {
        if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
            findNavController().navigate(R.id.action_register01Fragment2_to_register02Fragment2)
        } else {
            findNavController().navigate(R.id.action_register01Fragment_to_register02Fragment)
        }
    }

}
