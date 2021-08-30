package com.listocalixto.dailycosmos.ui.welcome.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentWelcomePage03Binding

class WelcomeFragmentPage03 : Fragment(R.layout.fragment_welcome_page03) {

    private lateinit var binding: FragmentWelcomePage03Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWelcomePage03Binding.bind(view)
    }

}