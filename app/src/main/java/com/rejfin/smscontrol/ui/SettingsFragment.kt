package com.rejfin.smscontrol.ui

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
import androidx.preference.PreferenceManager
import com.rejfin.smscontrol.BuildConfig
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.helpers_class.RunCmdCommand
import kotlinx.coroutines.*

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
        // find right properties //
        val darkModePref = findPreference<Preference>("dark_mode") as ListPreference
        val aboutPref = findPreference<Preference>("about")
        val feedbackPref = findPreference<Preference>("feedback")
        val rootPref = findPreference<Preference>("root_status")
        val logPref = findPreference<Preference>("check_log")

        // set summary in 'About' and 'Root' preferences //
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        aboutPref?.summary = "SmsControl v${BuildConfig.VERSION_NAME} by Rejfin"
        rootPref?.summary = if (pref.getBoolean("root_status",false)){
            getString(R.string.root_granted)
        }else{
            getString(R.string.root_denied)
        }

        // set feedback listener, create chooser intent //
        feedbackPref?.setOnPreferenceClickListener {
            val intentEmail = Intent(Intent.ACTION_SENDTO)
            intentEmail.data = Uri.parse("mailto:")
            intentEmail.putExtra(Intent.EXTRA_EMAIL, Array(1) { "rejfin.dev@gmail.com" })
            intentEmail.putExtra(Intent.EXTRA_SUBJECT, "Feedback - SMSControl v${BuildConfig.VERSION_NAME}")
            intentEmail.putExtra(Intent.EXTRA_TEXT,"USER ID: ${pref.getString("userId","none")}")
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

        // check log //
        logPref?.setOnPreferenceClickListener {
            LogDataDialog().showDialog(parentFragmentManager)
            true
        }

        // try to obtain root privilege //
        rootPref?.setOnPreferenceClickListener {
            CoroutineScope(Dispatchers.Main).launch{
                var result = false
                val x = async(Dispatchers.IO) {
                    result = RunCmdCommand.commandAsync("su -c ls").await()
                }
                x.await()
                if(result){
                    it.summary = getString(R.string.root_granted)
                    pref.edit().putBoolean("root_status",true).apply()
                }else{
                    it.summary = getString(R.string.root_denied)
                    pref.edit().putBoolean("root_status",false).apply()
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