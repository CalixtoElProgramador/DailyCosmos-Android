package com.listocalixto.dailycosmos.ui.main.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.firebase.auth.FirebaseAuth
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.core.Result
import com.listocalixto.dailycosmos.data.model.FavoriteEntity
import com.listocalixto.dailycosmos.data.model.toAPOD
import com.listocalixto.dailycosmos.databinding.FragmentFavoritesBinding
import com.listocalixto.dailycosmos.presentation.favorites.APODFavoriteViewModel
import com.listocalixto.dailycosmos.presentation.preferences.UtilsViewModel
import com.listocalixto.dailycosmos.ui.main.DetailsArgs
import com.listocalixto.dailycosmos.ui.main.MainViewModel
import com.listocalixto.dailycosmos.ui.main.favorites.adapter.FavoritesAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment(R.layout.fragment_favorites),
    FavoritesAdapter.OnFavoriteClickListener {

    private val viewModelShared by activityViewModels<MainViewModel>()
    private val viewModel by activityViewModels<APODFavoriteViewModel>()
    private val dataStoreUtils by activityViewModels<UtilsViewModel>()

    private var showDialogAgain = true

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var layoutManager: StaggeredGridLayoutManager

    override fun onResume() {
        super.onResume()
        isBottomNavVisible()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModelShared.isShowFavoritesDialogAgain().value?.let { answer ->
            showDialogAgain = answer
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoritesBinding.bind(view)
        viewModelShared.setAPODTranslated(null)
        layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.rvFavorites.layoutManager = layoutManager
        getAllFavorites()

        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.setOnItemReselectedListener { item -> smoothScrollToStart(item) }

        dataStoreUtils.isDialogShowAgain.observe(viewLifecycleOwner, { showAgain ->
            if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true && showAgain && showDialogAgain) {
                    showDialogCreateAccount()
            }
        })
    }

    private fun smoothScrollToStart(item: MenuItem) {
        when (item.itemId) {
            R.id.favoritesFragment -> {
                binding.rvFavorites.smoothScrollToPosition(0)
            }
        }
    }

    private fun showDialogCreateAccount() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_an_account))
            .setIcon(R.drawable.ic_help)
            .setMessage(getString(R.string.dialog_message_create_an_account))
            .setOnDismissListener {
                viewModelShared.setShowFavoritesDialogAgain(false)
            }
            .setNeutralButton(getString(R.string.no_thanks)) {_,_ ->
                viewModelShared.setShowFavoritesDialogAgain(false)
            }
            .setNegativeButton(getString(R.string.do_not_show_again)) { _, _ ->
                dataStoreUtils.setDialogShowAgain(false)
            }
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                navigateToRegisterGuestActivity()
            }.show()
    }

    private fun navigateToRegisterGuestActivity() {
        val activityNavHost = requireActivity().findViewById<View>(R.id.nav_host_activity)
        Navigation.findNavController(activityNavHost).navigate(R.id.action_mainParentFragment_to_registerParentFragment)
    }

    private fun getAllFavorites() {
        viewModel.getAPODFavorites().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    binding.lottieLoading.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.lottieLoading.visibility = View.GONE
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