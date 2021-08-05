package com.listocalixto.dailycosmos.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivityLoginBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
                    binding.buttonsRegister.animation = AnimationUtils.loadAnimation(
                        this,
                        R.anim.fade_out_main
                    )
                    binding.buttonsRegister.visibility = View.GONE
                }
                R.id.successfulFragment -> {
                    binding.buttonsRegister.animation = AnimationUtils.loadAnimation(
                        this,
                        R.anim.fade_out_main
                    )
                    binding.buttonsRegister.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

}