package com.listocalixto.dailycosmos.ui.auth.register

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.data.remote.auth.AuthDataSource
import com.listocalixto.dailycosmos.databinding.FragmentRegister03Binding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModelFactory
import com.listocalixto.dailycosmos.repository.auth.AuthRepoImpl
import com.listocalixto.dailycosmos.core.Result

const val REQUEST_IMAGE_CAPTURE = 1

@Suppress("DEPRECATION")
class Register03Fragment : Fragment(R.layout.fragment_register03) {

    private val viewModelShared: RegisterViewModel by activityViewModels()
    private val viewModelFirebase by viewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepoImpl(AuthDataSource()))
    }

    private var bitmap: Bitmap? = null

    private lateinit var binding: FragmentRegister03Binding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegister03Binding.bind(view)

        configWindow()
        getInputsFromViewModel()

        binding.profilePicture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_no_photo_app),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        activity?.findViewById<MaterialButton>(R.id.button_next)?.setOnClickListener {
            if (bitmap == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_a_photo),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getArgsFromViewModel()
            }
        }
    }

    private fun getArgsFromViewModel() {
        bitmap?.let { image ->
            viewModelShared.setBitmap(image)
            viewModelShared.getPerson().value?.let { person ->
                viewModelShared.getPassword().value?.let { password ->
                    viewModelShared.getBitmap().value?.let { bitmap ->
                        createUser(bitmap, password, person)
                    }
                }
            }
        }
    }

    private fun nextFragment() {
        findNavController().navigate(R.id.action_register03Fragment_to_mainActivity)
        requireActivity().onBackPressed()
    }

    private fun createUser(bitmap: Bitmap, password: Password, person: Person) {
        viewModelFirebase.signUp(
            person.name,
            person.lastname,
            person.email,
            password.passwrod,
            bitmap
        ).observe(viewLifecycleOwner, { result ->
            when (result) {
                is Result.Loading -> {
                    isEnabledViews(false)
                }
                is Result.Success -> {
                    nextFragment()
                    isEnabledViews(true)
                }
                is Result.Failure -> {
                    isEnabledViews(true)
                    Toast.makeText(
                        requireContext(),
                        "Error: ${result.exception}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.profilePicture.setImageBitmap(imageBitmap)
            bitmap = imageBitmap
            bitmap?.let {
                viewModelShared.setBitmap(bitmap!!)
            }
        }
    }

    private fun configWindow() {
        activity?.window?.addFlags((WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS))
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity?.window?.statusBarColor =
            requireActivity().resources.getColor(R.color.colorPrimaryVariantLogin)
    }

    private fun getInputsFromViewModel() {
        viewModelShared.getBitmap().let {
            binding.profilePicture.setImageBitmap(it.value)
            bitmap = it.value
        }
    }

    private fun isEnabledViews(boolean: Boolean) {
        binding.profilePicture.isEnabled = boolean
        activity?.apply {
            findViewById<MaterialButton>(R.id.button_next)?.isEnabled = boolean
            findViewById<MaterialButton>(R.id.button_back)?.isEnabled = boolean
        }
        if (boolean) {
            binding.lottieLoading.visibility = View.GONE

        } else {
            binding.lottieLoading.visibility = View.VISIBLE
        }
    }
}