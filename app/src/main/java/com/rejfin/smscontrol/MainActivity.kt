package com.rejfin.smscontrol

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rejfin.smscontrol.ui.CommandsFragment
import com.rejfin.smscontrol.ui.HomeFragment
import com.rejfin.smscontrol.ui.SettingsFragment
import com.rejfin.smscontrol.ui.other.PagerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(){
    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PreferenceManager.setDefaultValues(this, R.xml.commands_preference, false)
        loadTheme()

        // set userId for firebase crashlytics //
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if(pref.getString("userId","") == ""){
            val uuid = UUID.randomUUID()
            FirebaseCrashlytics.getInstance().setUserId(uuid.toString())
            pref.edit().putString("userId",uuid.toString()).commit()
        }

        // set view pager for fragments//
        val adapter = PagerViewAdapter(
            supportFragmentManager
        )
        adapter.addFragment(HomeFragment(),resources.getString(R.string.home))
        adapter.addFragment(CommandsFragment(),resources.getString(R.string.commands))
        adapter.addFragment(SettingsFragment(),resources.getString(R.string.settings))
        pager_view.adapter = adapter

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
                    }
                    2 -> {
                        bottom_navigation.menu.findItem(R.id.settings).isChecked = true
                        toolbar.visibility = View.VISIBLE
                        toolbar.title = getString(R.string.settings)
                    }
                }
            }
        })
    }

    // load user preferences //
    private fun loadTheme(){
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if(pref.contains("dark_mode")) {
            if (pref.getString("dark_mode", "") == "Light") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                return
            } else if (pref.getString("dark_mode", "") == "Dark") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                return
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
