package com.listocalixto.dailycosmos.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        observeDestinationChange(navController)
    }

    private fun observeDestinationChange(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.pictureFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.appBarMain.visibility = View.GONE
                }
                R.id.todayFragment -> {

                    binding.appBarMain.animation = AnimationUtils.loadAnimation(
                        this,
                        R.anim.fade_out_main
                    )
                    binding.appBarMain.visibility = View.GONE
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                R.id.exploreFragment -> {



                    binding.appBarMain.visibility = View.VISIBLE
                    binding.topAppBar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.settingsActivity -> {
                                findNavController(R.id.nav_host_main).navigate(R.id.action_exploreFragment_to_settingsActivity)
                                true
                            }
                            else -> false
                        }
                    }
                }
                R.id.favoritesFragment -> {
                    binding.appBarMain.visibility = View.VISIBLE
                    binding.topAppBar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.settingsActivity -> {
                                findNavController(R.id.nav_host_main).navigate(R.id.action_favoritesFragment_to_settingsActivity)
                                true
                            }
                            else -> false
                        }
                    }
                }
                R.id.randomFragment -> {
                    binding.appBarMain.visibility = View.VISIBLE
                    binding.topAppBar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.settingsActivity -> {
                                findNavController(R.id.nav_host_main).navigate(R.id.action_randomFragment_to_settingsActivity)
                                true
                            }
                            else -> false
                        }
                    }
                }
                R.id.searchFragment -> {
                    binding.appBarMain.visibility = View.VISIBLE
                    binding.topAppBar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.settingsActivity -> {
                                findNavController(R.id.nav_host_main).navigate(R.id.action_searchFragment_to_settingsActivity)
                                true
                            }
                            else -> false
                        }
                    }
                }

                R.id.detailsFragment -> {
                    binding.appBarMain.visibility = View.GONE
                    binding.bottomNavigation.visibility = View.VISIBLE
                }

                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.appBarMain.visibility = View.VISIBLE
                }
            }
        }
    }
}