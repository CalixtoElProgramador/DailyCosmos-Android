package com.listocalixto.dailycosmos.ui.auth.welcome.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.listocalixto.dailycosmos.ui.auth.welcome.WelcomeFragment

class WelcomeAdapter(
    list: ArrayList<Fragment>,
    fragment: WelcomeFragment
) : FragmentStateAdapter(fragment) {

    private val fragmentList = list

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}