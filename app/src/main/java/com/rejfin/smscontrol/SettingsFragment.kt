package com.rejfin.smscontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
        // find right properties //
        val darkModePref = findPreference<Preference>("dark_mode") as ListPreference
        val aboutPref = findPreference<Preference>("about")
        val feedbackPref = findPreference<Preference>("feedback")

        // set summary in 'About' preference //
        aboutPref?.summary = "SmsControl v${BuildConfig.VERSION_NAME} by Rejfin"

        // set feedback listener, create chooser intent //
        feedbackPref?.setOnPreferenceClickListener {
            val intentEmail = Intent(Intent.ACTION_SENDTO)
            intentEmail.data = Uri.parse("mailto:")
            intentEmail.putExtra(Intent.EXTRA_EMAIL, Array(1) { "rejfin.dev@gmail.com" })
            intentEmail.putExtra(Intent.EXTRA_SUBJECT, "Feedback - SMSControl v${BuildConfig.VERSION_NAME}")
            try {
                startActivity(Intent.createChooser(intentEmail, "Send feedback"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context,
                    R.string.activity_not_found, Toast.LENGTH_LONG).show()
            }
            true
        }

        // set listener for dark mode preference //
        darkModePref.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == "Light") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else if (newValue == "Dark") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create options for dark mode preference based on current API level //
        val darkModePref = findPreference<Preference>("dark_mode") as ListPreference
        val entries:Array<String>
        val entryValue:Array<String>
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            entries = arrayOf("Dark","Light","Set by Battery Saver")
            entryValue = arrayOf("Dark","Light","Default")
        }else{
            entries = arrayOf("Dark","Light","System default")
            entryValue = arrayOf("Dark","Light","Default")
        }
        darkModePref.entries = entries
        darkModePref.entryValues = entryValue
    }
}