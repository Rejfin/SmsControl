package com.rejfin.smscontrol

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*

class CommandsFragment : PreferenceFragmentCompat(){

    private var pref:SharedPreferences? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.commands_preference, rootKey)
    }
}