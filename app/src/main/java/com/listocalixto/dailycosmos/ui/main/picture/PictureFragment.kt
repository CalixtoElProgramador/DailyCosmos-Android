package com.listocalixto.dailycosmos.ui.main.picture

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentPictureBinding
import com.listocalixto.dailycosmos.ui.main.MainViewModel
import com.listocalixto.dailycosmos.ui.main.PictureArgs
import java.io.*

const val REQUEST_PERMISSION_WRITE_STORAGE = 200

class PictureFragment : Fragment(R.layout.fragment_picture) {

    private val viewModelShared by activityViewModels<MainViewModel>()
    private var bitmap: Bitmap? = null

    private lateinit var args: PictureArgs
    private lateinit var binding: FragmentPictureBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bottomNavigation =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)!!
        if (bottomNavigation.isVisible) {
            bottomNavigation.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.slide_out_bottom
                )
                visibility = View.GONE
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPictureBinding.bind(view)

        viewModelShared.getArgsToPicture().value?.let {
            args = it
        }

        if (args.hdurl.isNotEmpty()) {
            setBitmapFromInternet(args.hdurl)
        } else {
            setBitmapFromInternet(args.url)
        }

        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.btnCopyLink.setOnClickListener { copyLinkToClipboard() }

        binding.btnShareImage.setOnClickListener { shareLink() }

        binding.btnSaveImage.setOnClickListener {
            if (binding.photoView.drawable == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.wait_for_the_image_to_load),
                    Toast.LENGTH_SHORT
                ).show()
            } else { checkPermissionsStorage() }
        }
    }

    private fun setBitmapFromInternet(link: String) {
        Glide.with(requireContext()).asBitmap().load(link)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    binding.photoView.setImageBitmap(resource)
                    bitmap = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    binding.photoView.setImageBitmap(bitmap)
                }
            })
    }

    private fun shareLink() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            if (args.hdurl.isNotEmpty()) {
                this.putExtra(Intent.EXTRA_TEXT, args.hdurl)
            } else {
                this.putExtra(Intent.EXTRA_TEXT, args.url)
            }
            type = "text/plain"
        }
        startActivity(shareIntent)
    }

    @SuppressLint("ShowToast")
    private fun copyLinkToClipboard() {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = if (args.hdurl.isNotEmpty()) {
            ClipData.newPlainText("Image link", args.hdurl)
        } else {
            ClipData.newPlainText("Image link", args.url)
        }
        clipboard.setPrimaryClip(clip)
        Snackbar.make(binding.photoView, getString(R.string.link_copied), Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun checkPermissionsStorage() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    saveImage()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        (arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)),
                        REQUEST_PERMISSION_WRITE_STORAGE
                    )
                }
            } else {
                saveImage()
            }
        } else {
            saveImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_WRITE_STORAGE && permissions.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("ShowToast")
    private fun saveImage() {
        var outPutStream: OutputStream? = null
        var file: File? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = requireActivity().contentResolver
            val values = ContentValues()
            val filename = args.title

            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DailyCosmos")
            values.put(MediaStore.Images.Media.IS_PENDING, 1)

            val collection: Uri =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val imageUri = resolver.insert(collection, values)

            try {
                outPutStream = imageUri?.let { resolver.openOutputStream(it) }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            imageUri?.let {
                resolver.update(it, values, null, null)
            }

        } else {
            val imagePath: String =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val filename = "${args.title}.jpg"

            file = File(imagePath, filename)

            try {
                outPutStream = FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }

        val saved: Boolean? = bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outPutStream)
        if (saved == true) {
            Snackbar.make(
                binding.btnSaveImage,
                R.string.picture_was_saved_successfully,
                Snackbar.LENGTH_LONG
            ).show()

            binding.btnSaveImage.isEnabled = false
            binding.btnSaveImage.alpha = 0.4f

        }

        if (outPutStream != null) {
            try {
                outPutStream.flush()
                outPutStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (file != null) {
            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(file.toString()),
                null,
                null
            )
        }
    }
}