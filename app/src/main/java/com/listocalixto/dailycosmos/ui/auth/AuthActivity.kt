package com.listocalixto.dailycosmos.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivityAuthBinding
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DailyCosmos)
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_login) as NavHostFragment
        val navController = navHostFragment.navController
        observeDestinationChange(navController)

    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    if (binding.buttonsRegister.isVisible) {
                        binding.buttonsRegister.animation = AnimationUtils.loadAnimation(
                            this,
                            R.anim.fade_out_main
                        )
                        binding.buttonsRegister.visibility = View.GONE
                    }

                }
                R.id.successfulFragment -> {
                    binding.buttonsRegister.animation = AnimationUtils.loadAnimation(
                        this,
                        R.anim.fade_out_main
                    )
                    binding.buttonsRegister.visibility = View.GONE
                }
            }
        }
    }

}