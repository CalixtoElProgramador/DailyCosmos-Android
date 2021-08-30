package com.listocalixto.dailycosmos.ui.auth.register

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentRegister03Binding
import com.listocalixto.dailycosmos.presentation.auth.AuthViewModel
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.auth.Password
import com.listocalixto.dailycosmos.ui.auth.Person
import com.listocalixto.dailycosmos.ui.auth.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

const val REQUEST_IMAGE_CAPTURE = 1
const val REQUEST_IMAGE_GALLERY = 2

@AndroidEntryPoint
class Register03Fragment : Fragment(R.layout.fragment_register03) {

    private val viewModelShared by activityViewModels<RegisterViewModel>()
    private val viewModelFirebase by activityViewModels<AuthViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()

    private var bitmap: Bitmap? = null
    private var optionProfilePicture = -1
    private var externalStoragePermission = -1
    private var cameraPermission = -1

    private lateinit var binding: FragmentRegister03Binding
    private lateinit var buttonNext: MaterialButton
    private lateinit var buttonBack: MaterialButton
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)

        binding.profilePicture.setOnClickListener { showBottomSheetDialog() }
        buttonNext.setOnClickListener { verifyInputs() }
        buttonBack.setOnClickListener { activity?.onBackPressed() }

    }

    private fun initVars(view: View) {
        binding = FragmentRegister03Binding.bind(view)
        bottomSheetDialog = BottomSheetDialog(requireActivity())
        buttonNext = activity?.findViewById(R.id.button_next)!!
        buttonBack = activity?.findViewById(R.id.button_back)!!
        externalStoragePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        cameraPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        getInputsFromViewModel()
    }

    private fun showBottomSheetDialog() {
        val bottomSheet = inflateBottomSheetLayout()
        val buttonGallery = bottomSheet.findViewById<ImageView>(R.id.image_gallery)
        val buttonCamera = bottomSheet.findViewById<ImageView>(R.id.image_camera)

        buttonGallery.setOnClickListener { requestReadExternalStoragePermission() }
        buttonCamera.setOnClickListener { requestCameraPermission() }

        bottomSheetDialog.setContentView(bottomSheet)
        bottomSheetDialog.show()
    }

    private fun requestCameraPermission() {
        bottomSheetDialog.dismiss()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                cameraPermission -> {
                    navigateToCamaraApp()
                    bottomSheetDialog.dismiss()
                }
                else -> {
                    optionProfilePicture = REQUEST_IMAGE_CAPTURE
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        } else {
            navigateToCamaraApp()
        }

    }

    private fun requestReadExternalStoragePermission() {
        bottomSheetDialog.dismiss()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                externalStoragePermission -> {
                    navigateToGalleryApp()
                    bottomSheetDialog.dismiss()
                }
                else -> {
                    optionProfilePicture = REQUEST_IMAGE_GALLERY
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else {
            navigateToGalleryApp()
            bottomSheetDialog.dismiss()
        }
    }

    private val startActivityGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && optionProfilePicture == REQUEST_IMAGE_GALLERY) {
                val data = result.data?.data
                binding.profilePicture.setImageURI(data)
                val drawable = binding.profilePicture.drawable
                val imageBitmap = drawable?.toBitmap()
                bitmap = imageBitmap?.let { Bitmap.createScaledBitmap(it, 500, 500, false) }
                bitmap?.let {
                    viewModelShared.setBitmap(bitmap!!)
                }

            }
        }

    private fun navigateToGalleryApp() {
        optionProfilePicture = REQUEST_IMAGE_GALLERY
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        try {
            startActivityGallery.launch(galleryIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_no_photo_app),
                Toast.LENGTH_SHORT
            ).show()
        }
        bottomSheetDialog.dismiss()
    }

    private fun inflateBottomSheetLayout(): View {
        val bottomSheet = LayoutInflater.from(requireContext()).inflate(
            R.layout.layout_bottom_sheet_profile_picture,
            activity?.findViewById<ConstraintLayout>(R.id.bottomSheetDialog)
        )
        return bottomSheet
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted && optionProfilePicture == REQUEST_IMAGE_GALLERY -> navigateToGalleryApp()
                isGranted && optionProfilePicture == REQUEST_IMAGE_CAPTURE -> navigateToCamaraApp()
                else -> showSnackbarMessage()
            }
        }

    @SuppressLint("ShowToast")
    private fun showSnackbarMessage() {
        Snackbar.make(
            requireContext(),
            buttonNext,
            getString(R.string.user_need_to_enable_permissions),
            Snackbar.LENGTH_SHORT
        )
            .setAnchorView(buttonNext)
            .show()
    }

    private fun verifyInputs() {
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

    private fun navigateToCamaraApp() {
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
        bottomSheetDialog.dismiss()
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

    private fun nextFragment(wasAnonymous: Boolean) {
        val activityNavHost = requireActivity().findViewById<View>(R.id.nav_host_activity)
        dataStoreUtils.saveValueFirstTime(1)
        if (wasAnonymous) {
            Navigation.findNavController(activityNavHost).navigate(R.id.action_registerParentFragment_to_mainParentFragment)
        } else {
            Navigation.findNavController(activityNavHost).navigate(R.id.action_authParentFragment_to_mainParentFragment)
        }

    }

    private fun createUser(bitmap: Bitmap, password: Password, person: Person) {
        var isAnonymous = false
        if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
            isAnonymous = true
        }
        viewModelFirebase.signUp(
            person.name,
            person.lastname,
            person.email,
            password.password,
            bitmap
        ).observe(viewLifecycleOwner, { result ->
            when (result) {
                is Result.Loading -> {
                    isEnabledViews(false)
                }
                is Result.Success -> {
                    isEnabledViews(true)
                    nextFragment(isAnonymous)
                }
                is Result.Failure -> {
                    isEnabledViews(true)
                    when(result.exception) {
                        is FirebaseNetworkException -> {
                            showErrorSnackbarMessage(getString(R.string.error_internet_connection_login))
                        }
                        is FirebaseAuthUserCollisionException -> {
                            showErrorSnackbarMessage(getString(R.string.error_email_registered))
                        }
                        else -> {
                            Log.d("FailureCreateUser", "Failure: ${result.exception} ")
                            showErrorSnackbarMessage(getString(R.string.error_something_went_wrong))
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("ShowToast")
    private fun showErrorSnackbarMessage(message: String) {
        val colorError = MaterialColors.getColor(requireView(), R.attr.colorError)
        Snackbar.make(binding.profilePicture, message, Snackbar.LENGTH_INDEFINITE)
            .setDuration(5000)
            .setAnchorView(buttonNext)
            .setBackgroundTint(colorError)
            .show()
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