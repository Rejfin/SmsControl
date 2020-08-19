package com.rejfin.smscontrol.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.rejfin.smscontrol.R
import kotlinx.android.synthetic.main.help_commands_layout.*

class HelpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.help_commands_layout,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        tv_wifi_on.text = pref.getString("wifi_on","wifiOn")
        tv_wifi_off.text = pref.getString("wifi_off","wifiOff")
        tv_bluetooth_on.text = pref.getString("bluetooth_on","bluetoothOn")
        tv_bluetooth_off.text = pref.getString("bluetooth_off","bluetoothOff")
        tv_sound_on.text = pref.getString("sound_on","soundOn")
        tv_sound_off.text = pref.getString("sound_off","soundOff")
        tv_play_sound.text = pref.getString("sound_play","playSound")
        tv_sound_level.text = pref.getString("sound_level","setSoundLevel")
        tv_sync_on.text = pref.getString("sync_on","syncOn")
        tv_sync_off.text = pref.getString("sync_off","syncOff")
        tv_command_list.text = pref.getString("command_list","commandList")
        tv_get_info.text = pref.getString("get_info","getInfo")
        tv_location.text = pref.getString("location","location")
        tv_run_app.text = pref.getString("run_app","runApp")
        tv_mobile_data_on.text = pref.getString("mobile_data_on","mobileDataOn")
        tv_mobile_data_off.text = pref.getString("mobile_data_off","mobileDataOff")
        tv_restart.text = pref.getString("restart","restart")
        tv_shutdown.text = pref.getString("shutdown","shutdown")
    }
}