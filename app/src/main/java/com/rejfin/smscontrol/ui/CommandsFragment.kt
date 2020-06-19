package com.rejfin.smscontrol.ui

import android.Manifest
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
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.ui.other.CustomPreferenceItem


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
            wifiOnPref!!.setAvailability(false)
            wifiOffPref!!.setAvailability(false)
        }

        // set click listener on pick sound properties //
        val soundUri = preferenceManager.findPreference<Preference>("sound_name")
        soundUri?.setOnPreferenceClickListener {
            val intent = Intent().setType("audio/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent,1)
            true
        }

        // check if command requiring root privileges have it //
        val rebootPref = preferenceManager.findPreference<CustomPreferenceItem>("restart")
        rebootPref?.setStateChangeListener(object :
            CustomPreferenceItem.OnStateChangeEventListener {
            override fun onStateChange() {
                if(!pref!!.getBoolean("root_status",false)){
                    showRootPermissionDialog()
                    rebootPref.setState(false)
                }
            }
        })

        // check if command requiring root privileges have it //
        val shutDownPref = preferenceManager.findPreference<CustomPreferenceItem>("shutdown")
        shutDownPref?.setStateChangeListener(object :
            CustomPreferenceItem.OnStateChangeEventListener {
            override fun onStateChange() {
                if(!pref!!.getBoolean("root_status",false)){
                    showRootPermissionDialog()
                    shutDownPref.setState(false)
                }
            }
        })

        // check if command requiring Root and READ_PHONE_STATE privileges have it //
        val mobileDataOnPref = preferenceManager.findPreference<CustomPreferenceItem>("mobile_data_on")
        mobileDataOnPref?.setStateChangeListener(object :
            CustomPreferenceItem.OnStateChangeEventListener {
            override fun onStateChange() {
                if(!pref!!.getBoolean("root_status",false)){
                    showRootPermissionDialog()
                    mobileDataOnPref.setState(false)
                    return
                }

                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
                    askForPermission(
                        2,
                        arrayOf(Manifest.permission.READ_PHONE_STATE),
                        getString(R.string.mobile_data_permission_info)
                    )
                    mobileDataOnPref.setState(false)
                }
            }
        })

        // check if command requiring Root and READ_PHONE_STATE privileges have it //
        val mobileDataOffPref = preferenceManager.findPreference<CustomPreferenceItem>("mobile_data_off")
        mobileDataOffPref?.setStateChangeListener(object :
            CustomPreferenceItem.OnStateChangeEventListener {
            override fun onStateChange() {
                if(!pref!!.getBoolean("root_status",false)){
                    showRootPermissionDialog()
                    mobileDataOffPref.setState(false)
                    return
                }

                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
                    askForPermission(
                        3,
                        arrayOf(Manifest.permission.READ_PHONE_STATE),
                        getString(R.string.mobile_data_permission_info)
                    )
                    mobileDataOffPref.setState(false)
                }
            }
        })

        // check if command requiring SEND_SMS privilege have it //
        val sendListCommandPref = preferenceManager.findPreference<CustomPreferenceItem>("command_list")
        sendListCommandPref?.setStateChangeListener(object :
            CustomPreferenceItem.OnStateChangeEventListener {
            override fun onStateChange() {
                if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED){
                    askForPermission(
                        4,
                        arrayOf(Manifest.permission.SEND_SMS),
                        getString(R.string.send_sms_info)
                    )
                    sendListCommandPref.setState(false)
                }
            }
        })

        // check if command requiring ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION privilege have it //
        val locationPref = preferenceManager.findPreference<CustomPreferenceItem>("location")
        locationPref?.setStateChangeListener(object :
            CustomPreferenceItem.OnStateChangeEventListener {
            override fun onStateChange() {
                if((ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) ||
                        ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                    askForPermission(
                        5,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                        getString(R.string.location_permission_info)
                    )
                    locationPref.setState(false)
                }
            }
        })

        val getInfoPref = preferenceManager.findPreference<CustomPreferenceItem>("get_info")
        getInfoPref?.setStateChangeListener(object : CustomPreferenceItem.OnStateChangeEventListener{
            override fun onStateChange() {
                if((ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)){
                    askForPermission(
                        6,
                        arrayOf(Manifest.permission.SEND_SMS),
                        getString(R.string.send_sms_get_info)
                    )
                    getInfoPref.setState(false)
                }
            }

        })
    }

    // function show dialog about the need for root //
    private fun showRootPermissionDialog(){
        MaterialAlertDialogBuilder(context)
            .setMessage(getString(R.string.root_needed))
            .setPositiveButton(
                R.string.understand
            ) { dialog, _ -> dialog?.dismiss() }
            .show()
    }

    // function ask user for permission or open settings where user can grant needed permission //
    private fun askForPermission(requestCode: Int, permissions: Array<String>, message: String){
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel)
            ) { dialog, _ -> dialog?.dismiss() }
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ ->
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, requestCode)
                }
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            // mobile data request code //
            2, 3 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(requestCode == 2){
                        preferenceManager.findPreference<CustomPreferenceItem>("mobile_data_on")?.setState(true)
                    }else if(requestCode == 3){
                        preferenceManager.findPreference<CustomPreferenceItem>("mobile_data_off")?.setState(true)
                    }
                }else{
                    showDeniedMessage()
                    preferenceManager.findPreference<CustomPreferenceItem>("mobile_data_on")?.setState(false)
                    preferenceManager.findPreference<CustomPreferenceItem>("mobile_data_off")?.setState(false)
                }
            }
            // sms request code //
            4,6 ->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(requestCode == 4){
                        preferenceManager.findPreference<CustomPreferenceItem>("command_list")?.setState(true)
                    }else if (requestCode == 6){
                        preferenceManager.findPreference<CustomPreferenceItem>("get_info")?.setState(true)
                    }

                }else{
                    showDeniedMessage()
                    preferenceManager.findPreference<CustomPreferenceItem>("command_list")?.setState(false)
                    preferenceManager.findPreference<CustomPreferenceItem>("get_info")?.setState(false)
                }
            }
            // location request code //
            5 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    preferenceManager.findPreference<CustomPreferenceItem>("location")?.setState(true)
                }else{
                    showDeniedMessage()
                    preferenceManager.findPreference<CustomPreferenceItem>("location")?.setState(false)
                }
            }
        }
    }

    // shows when the user will deny the necessary permissions //
    private fun showDeniedMessage(){
        MaterialAlertDialogBuilder(context)
            .setMessage(getString(R.string.permission_denied))
            .setPositiveButton(getString(R.string.understand)
            ) { dialog, _ -> dialog?.dismiss() }
            .show()
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
    // ex. 'select app to run' rely on 'run selected app' preference state //
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