package com.listocalixto.dailycosmos.ui.welcome.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentWelcomePage01Binding

class WelcomeFragmentPage01 : Fragment(R.layout.fragment_welcome_page01) {

    private lateinit var binding: FragmentWelcomePage01Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWelcomePage01Binding.bind(view)
    }

}