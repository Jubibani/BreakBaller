package com.google.ar.sceneform.samples.gltf.library.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.ar.sceneform.samples.gltf.library.screens.ListFragment

class PracticeViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFragment.newInstance("Tab 1")
            1 -> ListFragment.newInstance("Tab 2")
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}