package com.rejfin.smscontrol

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import androidx.preference.PreferenceManager
import android.provider.Telephony
import android.widget.Toast

class CommandManager {
    fun manage(context: Context, intent: Intent?){
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val message = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val messageBody = message[0].messageBody
        //val senderNumber = message[0].originatingAddress

        var messageSecurityCode = ""
        var messageCommand = ""
        try{
            messageSecurityCode = messageBody.split(" ")[0].substringAfter("@")
            messageCommand = messageBody.split(" ")[1]
        }catch (e:IndexOutOfBoundsException){
            println("WRONG MESSAGE FORMAT")
        }

        // TODO lock broadcast after 5 bad code attempt maybe //
        if(pref.getString("security_code",null) == messageSecurityCode && messageBody[0] == '@'){
            when(messageCommand){
                pref.getString("wifi_on", null) -> {
                    // TODO WIFI ON COMMAND
                }
                pref.getString("wifi_off", null) -> {
                    // TODO WIFI OFF COMMAND
                }
                pref.getString("mobile_data_on", null) -> {
                    // TODO MOBILE DATA ON COMMAND
                }
                pref.getString("mobile_data_off", null) -> {
                    // TODO MOBILE DATA OFF COMMAND
                }
                pref.getString("sound_on", null) -> {
                    // TODO SOUND ON COMMAND
                }
                pref.getString("sound_off", null) -> {
                    // TODO SOUND OFF COMMAND
                }
                pref.getString("sound_play", null) -> {
                    // TODO PLAY SOUND COMMAND
                }
                pref.getString("sound_level",null) -> {
                    // TODO SET SOUND LEVEL COMMAND
                }
                pref.getString("run_app", null) -> {
                    // TODO RUN APP COMMAND
                }
                pref.getString("command_list", null) -> {
                    // TODO COMMAND LIST COMMAND
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
}
