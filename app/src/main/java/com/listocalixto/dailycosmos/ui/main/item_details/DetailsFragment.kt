package com.listocalixto.dailycosmos.ui.main.item_details

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentDetailsBinding

@Suppress("DEPRECATION")
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var binding: FragmentDetailsBinding
    private val args by navArgs<DetailsFragmentArgs>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars(view)
        configWindow()
        setInformation()
        onClickImage()
    }

    private fun configWindow() {
        activity?.window?.addFlags((WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS))
    }

    private fun onClickImage() {
        binding.imgApodPicture.setOnClickListener {
            if (binding.imgApodPicture.drawable == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.wait_for_the_image_to_load),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val action = DetailsFragmentDirections.actionDetailsFragmentToPictureFragment(
                    args.hdurl,
                    args.title,
                    args.url
                )
                findNavController().navigate(action)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setInformation() {
        if (args.hdurl.isEmpty()) {
            Glide.with(requireContext()).load(args.url).into(binding.imgApodPicture)
        } else {
            Glide.with(requireContext()).load(args.hdurl).into(binding.imgApodPicture)
        }
        binding.textApodTitle.text = args.title
        binding.textApodDate.text = args.date
        if (args.explanation.isEmpty()) {
            binding.textApodExplanation.text = getString(R.string.no_description)
        } else {
            binding.textApodExplanation.text = args.explanation
        }
        if (args.copyright.isEmpty()) {
            binding.textApodCopyright.visibility = View.GONE
        } else {
            binding.textApodCopyright.text = "Copyright: ${args.copyright}"
        }
    }

    private fun initVars(view: View) {
        binding = FragmentDetailsBinding.bind(view)
    }
}