package com.example.financeapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.financeapp.ui.DailyFragment
import com.example.financeapp.ui.MonthlyFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val dailyFragment = DailyFragment()
    private val monthlyFragment = MonthlyFragment()

    override fun getItemCount(): Int {
        return 2 // We have two tabs: Daily and Monthly
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> dailyFragment
            1 -> monthlyFragment
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    // Method to get reference to DailyFragment
    fun getDailyFragment(): DailyFragment {
        return dailyFragment
    }

    // Method to get reference to MonthlyFragment
    fun getMonthlyFragment(): MonthlyFragment {
        return monthlyFragment
    }
}
