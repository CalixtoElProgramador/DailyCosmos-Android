package com.listocalixto.dailycosmos.ui.main.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.APODFavoriteEntity
import com.listocalixto.dailycosmos.data.remote.apod_favorite.RemoteAPODFavoriteDataSource
import com.listocalixto.dailycosmos.databinding.FragmentFavoritesBinding
import com.listocalixto.dailycosmos.presentation.apod_favorite.APODFavoriteViewModel
import com.listocalixto.dailycosmos.presentation.apod_favorite.APODFavoriteViewModelFactory
import com.listocalixto.dailycosmos.repository.apod_favorite.APODFavoriteRepositoryImpl
import com.listocalixto.dailycosmos.ui.main.favorites.adapter.FavoritesAdapter

class FavoritesFragment : Fragment(R.layout.fragment_favorites),
    FavoritesAdapter.OnFavoriteClickListener {

    private lateinit var binding: FragmentFavoritesBinding
    private val viewModel by activityViewModels<APODFavoriteViewModel> {
        APODFavoriteViewModelFactory(APODFavoriteRepositoryImpl(RemoteAPODFavoriteDataSource()))
    }

    private lateinit var layoutManager: StaggeredGridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoritesBinding.bind(view)

        layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.rvFavorites.layoutManager = layoutManager

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
                }
                is Result.Failure -> {
                    binding.lottieLoading.visibility = View.GONE
                    Log.d("ViewModelFireStore", "Hubo un error... ${result.exception} ")
                }
            }
        })

    }

    override fun onFavoriteClick(favorite: APODFavoriteEntity) {
        val action = FavoritesFragmentDirections.actionFavoritesFragmentToDetailsFragment(
            favorite.copyright,
            favorite.date,
            favorite.explanation,
            favorite.hdurl,
            favorite.media_type,
            favorite.title,
            favorite.url,
            1,
        )
        findNavController().navigate(action)
    }

}