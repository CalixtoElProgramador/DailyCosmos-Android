package com.listocalixto.dailycosmos.ui.welcome.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.listocalixto.dailycosmos.ui.welcome.WelcomeParentFragment

class WelcomeAdapter(
    list: ArrayList<Fragment>,
    parentFragment: WelcomeParentFragment
) : FragmentStateAdapter(parentFragment) {

    private val fragmentList = list

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}