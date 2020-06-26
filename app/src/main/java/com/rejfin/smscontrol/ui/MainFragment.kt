package com.rejfin.smscontrol.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.ui.other.PagerViewAdapter
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*


class MainFragment : Fragment() {
    private lateinit var adapter:PagerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = PagerViewAdapter(childFragmentManager)
        val view = inflater.inflate(R.layout.fragment_main,container,false)
        setHasOptionsMenu(true)
        view.toolbar.inflateMenu(R.menu.dialog_menu_logs_blacklist)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dialog_menu_logs_blacklist, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set view pager for fragments//
        toolbar.menu.findItem(R.id.item_one)?.isVisible = false
        adapter.addFragment(HomeFragment(),resources.getString(R.string.home))
        adapter.addFragment(CommandsFragment(),resources.getString(R.string.commands))
        adapter.addFragment(SettingsFragment(),resources.getString(R.string.settings))
        pager_view.adapter = adapter

        toolbar.setOnMenuItemClickListener {
            if(it.itemId == R.id.help_item){
                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,HelpFragment()).addToBackStack("HELP").commit()
            }
            true
        }


        // set listener for bottom navigation bar //
        bottom_navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    pager_view.currentItem = 0
                }
                R.id.commands -> {
                    pager_view.currentItem = 1
                }
                R.id.settings -> {
                    pager_view.currentItem = 2
                }
            }
            true
        }

        // when user swipe fragments set property bottom navigation localization //
        pager_view.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> {
                        bottom_navigation.menu.findItem(R.id.home).isChecked = true
                        toolbar.visibility = View.INVISIBLE
                    }
                    1 -> {
                        bottom_navigation.menu.findItem(R.id.commands).isChecked = true
                        toolbar.visibility = View.VISIBLE
                        toolbar.title = getString(R.string.commands)
                        toolbar.menu.findItem(R.id.help_item)?.isVisible = true
                    }
                    2 -> {
                        bottom_navigation.menu.findItem(R.id.settings).isChecked = true
                        toolbar.visibility = View.VISIBLE
                        toolbar.title = getString(R.string.settings)
                        toolbar.menu.findItem(R.id.help_item)?.isVisible = false
                    }
                }
            }
        })
    }
}