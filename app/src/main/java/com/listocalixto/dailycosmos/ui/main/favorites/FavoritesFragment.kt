package com.listocalixto.dailycosmos.ui.main.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.local.AppDatabase
import com.listocalixto.dailycosmos.data.local.favorites.LocalFavoriteDataSource
import com.listocalixto.dailycosmos.data.model.FavoriteEntity
import com.listocalixto.dailycosmos.data.model.toAPOD
import com.listocalixto.dailycosmos.data.remote.favorites.RemoteAPODFavoriteDataSource
import com.listocalixto.dailycosmos.databinding.FragmentFavoritesBinding
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModel
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModelFactory
import com.listocalixto.dailycosmos.domain.favorites.FavoritesRepoImpl
import com.listocalixto.dailycosmos.ui.main.details.DetailsArgs
import com.listocalixto.dailycosmos.ui.main.details.DetailsViewModel
import com.listocalixto.dailycosmos.ui.main.favorites.adapter.FavoritesAdapter

class FavoritesFragment : Fragment(R.layout.fragment_favorites),
    FavoritesAdapter.OnFavoriteClickListener {

    private val viewModelDetails by activityViewModels<DetailsViewModel>()
    private val viewModel by activityViewModels<APODFavoriteViewModel> {
        APODFavoriteViewModelFactory(
            FavoritesRepoImpl(
                RemoteAPODFavoriteDataSource(),
                LocalFavoriteDataSource(AppDatabase.getDatabase(requireContext()).favoriteDao())
            )
        )
    }

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var layoutManager: StaggeredGridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoritesBinding.bind(view)
        layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.rvFavorites.layoutManager = layoutManager
        getAllFavorites()

    }

    private fun getAllFavorites() {
        viewModel.getAPODFavorites().observe(viewLifecycleOwner, { result ->
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
        viewModelDetails.setArgs(
            DetailsArgs(
                favorite.toAPOD(1),
                null,
                -1
            )
        )
        findNavController().navigate(R.id.action_favoritesFragment_to_detailsFragment)
    }

}