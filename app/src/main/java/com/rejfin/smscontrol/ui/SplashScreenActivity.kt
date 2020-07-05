package com.rejfin.smscontrol.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.rejfin.smscontrol.MainActivity
import com.rejfin.smscontrol.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        loadTheme()

        val intentMain = Intent(this,MainActivity::class.java)

        Handler().postDelayed(
            {
                startActivity(intentMain)
                finish()
            },2000
        )
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