package com.listocalixto.dailycosmos.ui.auth.successful

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentSuccessfulBinding


class SuccessfulFragment : Fragment(R.layout.fragment_successful) {

    private lateinit var binding: FragmentSuccessfulBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSuccessfulBinding.bind(view)

    }

}