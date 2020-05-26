package com.rejfin.smscontrol

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerViewAdapter(fm:FragmentManager): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList:MutableList<Fragment> = ArrayList()
    private val titleList:MutableList<String> = ArrayList()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getPageId(fragment:Fragment):Int{
        return fragmentList.indexOf(fragment)
    }

    override fun getCount(): Int {
        return 3
    }

    fun addFragment(fragment: Fragment,title:String){
        fragmentList.add(fragment)
        titleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titleList[position]
    }
}