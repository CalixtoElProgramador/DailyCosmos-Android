package com.listocalixto.dailycosmos.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivityAuthBinding
import com.listocalixto.dailycosmos.databinding.ActivityRegisterGuestBinding

class RegisterGuestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterGuestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_register) as NavHostFragment
        val navController = navHostFragment.navController

    }
}