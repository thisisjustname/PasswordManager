package com.example.passwordmanager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.passwordmanager.cards.CardsFragment
import com.example.passwordmanager.documents.DocumentsFragment
import com.example.passwordmanager.passwords.PasswordsFragment


class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PasswordsFragment()
            1 -> CardsFragment()
            2 -> DocumentsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}