package com.listocalixto.dailycosmos.ui.welcome

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.listocalixto.dailycosmos.R
import com.listocalixto.dailycosmos.databinding.FragmentParentWelcomeBinding
import com.listocalixto.dailycosmos.ui.welcome.adapter.WelcomeAdapter
import com.listocalixto.dailycosmos.ui.welcome.pages.WelcomeFragmentPage01
import com.listocalixto.dailycosmos.ui.welcome.pages.WelcomeFragmentPage02
import com.listocalixto.dailycosmos.ui.welcome.pages.WelcomeFragmentPage03

class WelcomeParentFragment : Fragment(R.layout.fragment_parent_welcome) {

    private lateinit var binding: FragmentParentWelcomeBinding
    private lateinit var adapter: WelcomeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentParentWelcomeBinding.bind(view)
        initAdapter()
        setupOnBoardingIndicators()
        setCurrentOnBoardingIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentOnBoardingIndicator(position)
                if (position == 2) {
                    binding.buttonNext.apply {
                        animation = AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.fade_in_main
                        )
                        visibility = View.VISIBLE
                    }
                } else {
                    if (binding.buttonNext.isVisible) {
                        binding.buttonNext.apply {
                            animation = AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.fade_out_main
                            )
                            visibility = View.GONE
                        }
                    }
                }
            }
        })

        binding.buttonNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < adapter.itemCount) {
                binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            } else {
                findNavController().navigate(R.id.action_welcomeParentFragment_to_authParentFragment)
            }
        }

    }

    private fun initAdapter() {
        val fragmentList =
            arrayListOf(WelcomeFragmentPage01(), WelcomeFragmentPage02(), WelcomeFragmentPage03())
        adapter = WelcomeAdapter(fragmentList, this)
        binding.viewPager.adapter = adapter
    }

    private fun setupOnBoardingIndicators() {
        val indicators = arrayOfNulls<ImageView>(adapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.onboarding_indicator_inactive
                )
            )
            indicators[i]!!.layoutParams = layoutParams
            binding.layoutOnBoardingIndicators.addView(indicators[i])
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCurrentOnBoardingIndicator(index: Int) {
        val childCount: Int = binding.layoutOnBoardingIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = binding.layoutOnBoardingIndicators.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.onboarding_indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.onboarding_indicator_inactive
                    )
                )
            }
        }
    }

}