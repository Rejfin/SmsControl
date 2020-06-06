package com.rejfin.smscontrol

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputFilter
import android.text.InputType
import androidx.preference.*

class CommandsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var pref:SharedPreferences? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.commands_preference, rootKey)

        // get shared preference and set right state for some preference when they rely on others //
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        loadDependencyPreference()

        // populate list of app for 'pick app' command //
        populateAppList()

        // load saved sound that the user has chosen to play on command and set as summary //
        preferenceManager.findPreference<Preference>("sound_name")?.summary =
            pref!!.getString("sound_name","default ringtone")

        // set properties for security code entry //
        val textEditSecurityCode = preferenceManager
            .findPreference<EditTextPreference>("security_code")

        textEditSecurityCode?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
            val filterArray = arrayOfNulls<InputFilter>(1)
            filterArray[0] = InputFilter.LengthFilter(5)
            it.filters = filterArray
        }

        // if Android version >= 10, disable wifi category //
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val wifiOnPref = preferenceManager.findPreference<CustomPreferenceItem>("wifi_on")
            val wifiOffPref = preferenceManager.findPreference<CustomPreferenceItem>("wifi_off")
            wifiOnPref!!.setState(false,wifiOnPref)
            wifiOffPref!!.setState(false,wifiOffPref)
        }

        // set click listener on pick sound properties //
        val soundUri = preferenceManager.findPreference<Preference>("sound_name")
        soundUri?.setOnPreferenceClickListener {
            val intent = Intent().setType("audio/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent,1)
            true
        }
    }

    // create entry and values for "pick app" list //
    private fun populateAppList(){
        val appListPref = preferenceManager.findPreference<ListPreference>("selected_app")
        val appList:List<ApplicationInfo> = getListOfInstalledApps(requireContext())
        var entries = arrayOf<String>()
        var values = arrayOf<String>()
        appList.forEach {
            entries += requireContext().packageManager.getApplicationLabel(it).toString()
            values += it.packageName
        }
        appListPref!!.entries = entries
        appListPref.entryValues = values
    }

    //wait for user, to pick sound file //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                preferenceManager.findPreference<Preference>("sound_name")?.summary = getNameFromUri(data?.data!!)
                pref!!.edit().putString("sound_name",getNameFromUri(data.data!!)).apply()
                pref!!.edit().putString("sound_uri",data.data.toString()).apply()
            }
        }
    }

    //get a list of installed apps //
    private fun getListOfInstalledApps(context: Context) : List<ApplicationInfo>{
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
    }

    // register sharedPreference change listener //
    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    // unregister sharedPreference change listener //
    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // set preference state if they rely on other preference //
    private fun loadDependencyPreference() {
        preferenceManager.findPreference<ListPreference>("selected_app")!!.isEnabled =
            pref!!.getBoolean("run_app_state",false)
        preferenceManager.findPreference<Preference>("sound_name")!!.isEnabled =
            pref!!.getBoolean("sound_play_state",false)
    }

    override fun onSharedPreferenceChanged(preference: SharedPreferences?, key: String?) {
        when (key) {
            "sound_name" -> {
                preferenceManager.findPreference<EditTextPreference>(key)?.summary = preference?.getString(key,"")
            }
            "run_app_state" -> {
                preferenceManager.findPreference<Preference>("selected_app")!!.isEnabled =
                    preference!!.getBoolean(key,false)
            }
            "sound_play_state" -> {
                preferenceManager.findPreference<Preference>("sound_name")!!.isEnabled =
                    preference!!.getBoolean(key,false)
            }
        }
    }

    // function used to get name from picked sound //
    private fun getNameFromUri(uri:Uri): String{
        var name = ""
        uri.let { returnUri ->
            context?.contentResolver?.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
        }
        return name
    }
}