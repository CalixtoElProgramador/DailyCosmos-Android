package com.listocalixto.dailycosmos.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.color.MaterialColors
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivitySettingsBinding
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val dataStoreUtils by viewModels<UtilsViewModel>()

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
                R.id.appInfoFragment -> {setToolbarTitle(getString(R.string.application_info))}
            }
        }
    }

    private fun setToolbarTitle(title: String) {
        binding.topAppBar.title = title
    }

    private fun configWindow() {
        val statusBarColor = MaterialColors.getColor(binding.root, R.attr.background)
        this.window?.addFlags((WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS))
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.window?.statusBarColor = statusBarColor
    }

}