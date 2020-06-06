package com.rejfin.smscontrol

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.preference.PreferenceManager
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService

class CommandManager {
    fun manage(context: Context, intent: Intent?){
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val message = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val messageBody = message[0].messageBody
        val senderNumber = message[0].originatingAddress

        var messageSecurityCode = ""
        var messageCommand = ""
        try{
            messageSecurityCode = messageBody.split(" ")[0].substringAfter("@")
            messageCommand = messageBody.split(" ")[1]
        }catch (e:IndexOutOfBoundsException){
            println("WRONG MESSAGE FORMAT")
        }

        // TODO lock broadcast after 5 bad code attempt maybe //
        // TODO replace println with function to write log to file //
        if(pref.getString("security_code",null) == messageSecurityCode && messageBody[0] == '@'){
            when(messageCommand){
                pref.getString("wifi_on", null) -> {
                    if(pref.getBoolean("wifi_on_state",false)){
                        setWifiState(context,true)
                    }
                }
                pref.getString("wifi_off", null) -> {
                    if(pref.getBoolean("wifi_off_state",false)){
                        setWifiState(context,false)
                    }
                }
                pref.getString("mobile_data_on", null) -> {
                    // TODO MOBILE DATA ON COMMAND
                }
                pref.getString("mobile_data_off", null) -> {
                    // TODO MOBILE DATA OFF COMMAND
                }
                pref.getString("sound_on", null) -> {
                    if(pref.getBoolean("sound_on_state",false)) {
                        setSoundLevel(context, 100)
                    }
                }
                pref.getString("sound_off", null) -> {
                    if(pref.getBoolean("sound_off_state",false)) {
                        setSoundLevel(context, 0)
                    }
                }
                pref.getString("sound_play", null) -> {
                    if(pref.getBoolean("sound_play_state",false)) {
                        playSound(context, pref)
                    }
                }
                pref.getString("sound_level",null) -> {
                    if(pref.getBoolean("sound_level_state",false)) {
                        // check if argument exist //
                        try {
                            setSoundLevel(context, messageBody.split(" ")[2].toIntOrNull())
                        }catch (e:IndexOutOfBoundsException) {
                            println("WRONG MESSAGE FORMAT, NO VOLUME ARGUMENT")
                        }
                    }
                }
                pref.getString("run_app", null) -> {
                    if(pref.getBoolean("run_app_state",false)) {
                        runSelectedApp(context, pref)
                    }
                }
                pref.getString("command_list", null) -> {
                    if(pref.getBoolean("command_list_state",false)) {
                        sendCommandList(context,pref,senderNumber!!)
                    }
                }
                // root commands //
                pref.getString("restart", null) -> {
                    // TODO RESTART COMMAND
                }
                pref.getString("shutdown", null) -> {
                    // TODO SHUTDOWN COMMAND
                }
                pref.getString("location", null) -> {
                    // TODO LOCATION COMMAND
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setWifiState(context:Context, state:Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wifiManager.isWifiEnabled = state
            } catch (e: Exception) {
                Toast.makeText(context,e.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setSoundLevel(context:Context,level:Int?){
        // return false if sms does not contain sound volume level //
        if(level == null){
            return
        }

        // prepare necessary variable //
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxLevelMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val maxLevelRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        val musicLevel = if(level>maxLevelMusic){
            maxLevelMusic
        }else{
            level
        }
        val ringLevel = if(level>maxLevelRing){
            maxLevelRing
        }else{
            level
        }

        // set sound volume //
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            musicLevel,
            0)
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING,
            ringLevel,
            0)
    }

    private fun playSound(context: Context,pref: SharedPreferences){
        // get audio uri //
        val audioUri = if (pref.getString("sound_uri",null).isNullOrEmpty()){
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }else{
            Uri.parse(pref.getString("sound_uri",null)!!)
        }

        //prepare intent //
        val intentService = Intent(context,
            PlayMusicService::class.java)
        intentService.putExtra("uri",audioUri.toString())

        // start service based on API version //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(context,intentService)
        } else {
            context.startService(intentService)
        }
    }

    private fun runSelectedApp(context:Context,pref:SharedPreferences){
        val appPackage = pref.getString("selected_app",null)
        if(!appPackage.isNullOrEmpty()){
            val intent = context.packageManager.getLaunchIntentForPackage(appPackage)
            if(intent != null){
                try{
                    context.startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    Toast.makeText(context,context.getString(R.string.start_app_error),Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(context,context.getString(R.string.start_app_error),Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(context,context.getString(R.string.start_app_not_selected),Toast.LENGTH_LONG).show()
        }
    }

    private fun sendCommandList(context:Context,pref:SharedPreferences,senderNumber:String){
        val allKeys = pref.all
        val filterArray = arrayListOf(
            "sound_uri","selected_app","dark_mode","sound_name","security_code","command_list","command_list_state")
        val stateMap = mutableMapOf<String,String>()
        val commandMap = mutableMapOf<String,String>()
        for (i in allKeys){
            if(!filterArray.contains(i.key)){
                if("state" in i.key){
                    stateMap[i.key] = i.value.toString()
                }else{
                    commandMap[i.key] = i.value.toString()
                }
            }
        }
        // check if state for all commands exist of not set to false //
        commandMap.keys.forEach {
            if (!stateMap.containsKey(it + "_state")) {
                stateMap[it + "_state"] = "false"
            }
        }
        var message = ""
        commandMap.keys.forEach {
            if(stateMap[it+"_state"] == "true") {
                message += "${commandMap[it]}\n"
            }
        }
        if(message.isEmpty()){
            message = context.getString(R.string.all_commands_disabled)
        }
        try{
            val smsManager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
                SmsManager.getSmsManagerForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId())
            }else{
                SmsManager.getDefault()
            }

            val messages = smsManager.divideMessage(message)
            for (mess in messages){
                smsManager.sendTextMessage(senderNumber,null,mess,null,null)
            }
        }catch(e:Exception){
            Toast.makeText(context,e.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }
}
