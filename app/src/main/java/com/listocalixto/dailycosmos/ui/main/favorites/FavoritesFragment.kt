package com.listocalixto.dailycosmos.ui.main.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.FavoriteEntity
import com.listocalixto.dailycosmos.data.model.toAPOD
import com.listocalixto.dailycosmos.databinding.FragmentFavoritesBinding
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModel
import com.listocalixto.dailycosmos.ui.main.DetailsArgs
import com.listocalixto.dailycosmos.ui.main.MainViewModel
import com.listocalixto.dailycosmos.ui.main.favorites.adapter.FavoritesAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment(R.layout.fragment_favorites),
    FavoritesAdapter.OnFavoriteClickListener {

    private val viewModelShared by activityViewModels<MainViewModel>()
    private val viewModel by activityViewModels<APODFavoriteViewModel>()
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var layoutManager: StaggeredGridLayoutManager

    override fun onResume() {
        super.onResume()
        isBottomNavVisible()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoritesBinding.bind(view)
        layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.rvFavorites.layoutManager = layoutManager
        getAllFavorites()
        
        if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
            showDialogCreateAccount()
        }

    }

    private fun showDialogCreateAccount() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_an_account))
            .setIcon(R.drawable.ic_help)
            .setMessage(getString(R.string.dialog_message_create_an_account))
            .setNegativeButton(resources.getString(R.string.no_thanks)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                navigateToRegisterGuestActivity()
            }.show()
    }

    private fun navigateToRegisterGuestActivity() {
        findNavController().navigate(R.id.action_favoritesFragment_to_registerGuestActivity)
    }

    private fun getAllFavorites() {
        viewModel.getAPODFavorites().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    binding.lottieLoading.visibility = View.VISIBLE
                    Log.d("ViewModelFireStore", "Loading... ")
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    Log.d("ViewModelFireStore", "Results... ${result.data} ")
                    binding.rvFavorites.adapter =
                        FavoritesAdapter(result.data, this@FavoritesFragment)
                    isBottomNavVisible()
                    return@Observer
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                    isBottomNavVisible()
                }
            }
        })
    }

    private fun isBottomNavVisible() {
        if (!activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.isVisible!!) {
            activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.apply {
                animation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.slide_in_bottom
                )
                visibility = View.VISIBLE
            }
        }
    }

    override fun onFavoriteClick(favorite: FavoriteEntity) {
        viewModelShared.setArgsToDetails(
            DetailsArgs(favorite.toAPOD(1), null, -1)
        )
        findNavController().navigate(R.id.action_favoritesFragment_to_detailsFragment)
    }

}