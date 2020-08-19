package com.rejfin.smscontrol.ui.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.ui.fragments.MainFragment
import java.util.*


class MainActivity : AppCompatActivity(){
    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PreferenceManager.setDefaultValues(this,
            R.xml.commands_preference, false)
        loadTheme()

        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            MainFragment()
        ).commit()

        // set userId for firebase crashlytics //
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        if (pref.getString("userId", "") == "") {
            val uuid = UUID.randomUUID()
            FirebaseCrashlytics.getInstance().setUserId(uuid.toString())
            pref.edit().putString("userId", uuid.toString()).commit()
        }else{
            FirebaseCrashlytics.getInstance().setUserId(pref.getString("userId","")!!)
        }
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
