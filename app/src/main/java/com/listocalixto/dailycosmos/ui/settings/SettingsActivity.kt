package com.listocalixto.dailycosmos.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configWindow()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_settings) as NavHostFragment
        val navController = navHostFragment.navController
        observeDestinationChange(navController)

        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }

    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.helpFragment -> {setToolbarTitle(getString(R.string.help))}
                R.id.settingsFragment -> {setToolbarTitle(getString(R.string.settings))}
            }
        }
    }

    private fun setToolbarTitle(title: String) {
        binding.topAppBar.title = title
    }

    private fun configWindow() {
        this.window?.addFlags((WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS))
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.window?.statusBarColor = this.resources.getColor(R.color.colorPrimaryVariant)
    }

}