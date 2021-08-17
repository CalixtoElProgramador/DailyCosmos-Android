package com.listocalixto.dailycosmos.ui.auth.welcome.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentWelcomePage02Binding


class WelcomeFragmentPage02 : Fragment(R.layout.fragment_welcome_page02) {

    private lateinit var binding: FragmentWelcomePage02Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWelcomePage02Binding.bind(view)
    }
}