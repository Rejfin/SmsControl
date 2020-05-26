package com.rejfin.smscontrol

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.rejfin.smscontrol.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}