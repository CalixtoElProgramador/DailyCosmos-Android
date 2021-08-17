package com.listocalixto.dailycosmos.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentRegister02Binding


class Register02Fragment : Fragment(R.layout.fragment_register02) {

    private lateinit var binding: FragmentRegister02Binding
    private val viewModelShared: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegister02Binding.bind(view)
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

    private fun saveInputsToViewModel() {
        val password = binding.inputPassword.text.toString()
        val passwordConfirm = binding.inputPasswordConfirm.text.toString()
        viewModelShared.setPassword(Password(password, passwordConfirm))
    }

    private fun validateInputs() {
        when {
            binding.inputPassword.text.isNullOrEmpty() -> binding.inputLayoutPassword.error =
                getString(R.string.error_field_empty)
            binding.inputPasswordConfirm.text.isNullOrEmpty() -> binding.inputLayoutPasswordConfirm.error =
                getString(R.string.error_field_empty)
            binding.inputPassword.text.toString().length < 8 -> binding.inputLayoutPassword.error =
                getString(
                    R.string.error_password_short
                )
            binding.inputPassword.text.toString() != binding.inputPasswordConfirm.text.toString() -> binding.inputLayoutPasswordConfirm.error =
                getString(
                    R.string.error_passwords_not_same
                )
            else -> {
                saveInputsToViewModel()
                nextFragment()
            }

        }
    }

    private fun nextFragment() {
        findNavController().navigate(R.id.action_register02Fragment_to_register03Fragment)
    }


    private fun getInputsFromViewModel() {
        viewModelShared.getPassword().value?.let { it ->
            binding.inputPassword.setText(it.password)
            binding.inputPasswordConfirm.setText(it.passwordConfirm)
        }
    }

    private fun setErrorEnabledAfterChanges() {
        binding.inputPassword.doAfterTextChanged {
            binding.inputLayoutPassword.isErrorEnabled = false
        }
        binding.inputPasswordConfirm.doAfterTextChanged {
            binding.inputLayoutPasswordConfirm.isErrorEnabled = false
        }
    }

}