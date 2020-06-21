package com.rejfin.smscontrol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rejfin.smscontrol.helpers_class.LogManager
import com.rejfin.smscontrol.helpers_class.RunCmdCommand
import com.rejfin.smscontrol.helpers_class.SendSms
import com.rejfin.smscontrol.services.LocationService
import com.rejfin.smscontrol.services.PlayMusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.reflect.Method


class CommandManager {
    fun manage(context: Context, intent: Intent?){
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val message = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val messageBody = message[0].messageBody
        val senderNumber = message[0].originatingAddress

        val messageSecurityCode: String
        val messageCommand: String
        try{
            messageSecurityCode = messageBody.split(" ")[0].substringAfter("@")
            messageCommand = messageBody.split(" ")[1]
        }catch (e:IndexOutOfBoundsException){
            LogManager.saveToLog("[INFO] Usual sms",context)
            return
        }

        if(pref.getString("security_code",null) == messageSecurityCode && messageBody[0] == '@'){
            LogManager.saveToLog("[INFO] Command: $messageCommand",context)
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
                    if(pref.getBoolean("mobile_data_on_state",false)) {
                        setMobileDataEnabled(context, true)
                    }
                }
                pref.getString("mobile_data_off", null) -> {
                    if(pref.getBoolean("mobile_data_off_state",false)) {
                        setMobileDataEnabled(context, false)
                    }
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
                            LogManager.saveToLog("[ERROR] Wrong message format, no volume argument",context)
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
                pref.getString("location", null) -> {
                    if(pref.getBoolean("location_state",false)) {
                        getCurrentLocation(context,senderNumber!!)
                    }
                }
                pref.getString("restart", null) -> {
                    if(pref.getBoolean("restart_state",false)) {
                        rebootPhone(context)
                    }
                }
                pref.getString("shutdown", null) -> {
                    if(pref.getBoolean("shutdown_state",false)) {
                        shutdownPhone(context)
                    }
                }
                pref.getString("bluetooth_on", null) -> {
                    if(pref.getBoolean("bluetooth_on_state",false)) {
                        bluetoothSetState(true)
                    }
                }
                pref.getString("bluetooth_off", null) -> {
                    if(pref.getBoolean("bluetooth_off_state",false)) {
                        bluetoothSetState(false)
                    }
                }
                pref.getString("sync_on",null) -> {
                    if(pref.getBoolean("sync_on_state",false)){
                        ContentResolver.setMasterSyncAutomatically(true)
                    }
                }
                pref.getString("sync_off",null) -> {
                    if(pref.getBoolean("sync_off_state",false)){
                        ContentResolver.setMasterSyncAutomatically(false)
                    }
                }
                pref.getString("get_info",null)->{
                    if(pref.getBoolean("get_info_state",false)){
                        SendSms.sendSms(context,getInfo(context),senderNumber!!)
                    }
                }
                else ->{
                    LogManager.saveToLog("[INFO] Unknown command: $messageCommand",context)
                }
            }
        }else{
            LogManager.saveToLog("[ERROR] Wrong security code: $messageSecurityCode",context)
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
                FirebaseCrashlytics.getInstance().recordException(e)
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
            "sound_uri","selected_app","dark_mode","sound_name","security_code","command_list","command_list_state","root_state")
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
        SendSms.sendSms(context,message,senderNumber)
    }

    private fun rebootPhone(context:Context) = runBlocking{
        val job = launch(Dispatchers.IO) {
            // it's time for the phone to save the message as received, otherwise a boot loop may appear //
            delay(3000)
            if(!runBlocking {RunCmdCommand.commandAsync("su -c reboot now").await()}){
                Toast.makeText(context,context.getString(R.string.unexpected_error),Toast.LENGTH_LONG).show()
            }
        }
        job.join()
    }

    private fun shutdownPhone(context:Context) = runBlocking{
        val job = launch {
            // it's time for the phone to save the message as received, otherwise a boot loop may appear //
            delay(3000)
            if(!RunCmdCommand.commandAsync("su 0 -c reboot -p").await()){
                Toast.makeText(context,context.getString(R.string.unexpected_error),Toast.LENGTH_LONG).show()
            }
        }
        job.join()
    }

    @SuppressLint("MissingPermission")
    private fun setMobileDataEnabled(context: Context, enable: Boolean) {
        try{
            val command = StringBuilder()
            command.append("su -c service call phone ")
            command.append(getTransactionCode(context) + " ")
            if (Build.VERSION.SDK_INT >= 22) {
                val manager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                var id = 0
                if (manager.activeSubscriptionInfoCount > 0){
                    id = manager.activeSubscriptionInfoList[0].subscriptionId
                }
                command.append("i32 ")
                command.append("$id ")
            }
            command.append("i32 ")
            command.append(if (enable) "1" else "0")
            command.append("\n")
            runBlocking {RunCmdCommand.commandAsync(command.toString()).await()}
        }catch(e: IOException){
            Toast.makeText(context,e.localizedMessage,Toast.LENGTH_LONG).show()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    // setMobileDataEnabled required this function //
    private fun getTransactionCode(context:Context): String? {
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val telephonyManagerClass =
                Class.forName(telephonyManager.javaClass.name)
            val getITelephonyMethod =
                telephonyManagerClass.getDeclaredMethod("getITelephony")
            getITelephonyMethod.isAccessible = true
            val iTelephonyStub = getITelephonyMethod.invoke(telephonyManager)
            val iTelephonyClass = Class.forName(iTelephonyStub.javaClass.name)
            val stub = iTelephonyClass.declaringClass
            val field = stub!!.getDeclaredField("TRANSACTION_setDataEnabled")
            field.isAccessible = true
            return field.getInt(null).toString()
        } catch (e: java.lang.Exception) {
            if (Build.VERSION.SDK_INT >= 22){
                return "86"
            } else if (Build.VERSION.SDK_INT == 21){
                return "83"
            }
        }
        return ""
    }

    private fun getCurrentLocation(context:Context,senderNumber: String){
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val intentService = Intent(context, LocationService::class.java)
        val providers = arrayListOf<String>()

        // check available location providers //
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            providers.add(LocationManager.GPS_PROVIDER)
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            providers.add(LocationManager.NETWORK_PROVIDER)
        }
        // if none provider available or any of them are disabled try to enable it using ROOT command //
        if(providers.isNullOrEmpty() || !providers.contains(LocationManager.GPS_PROVIDER) || !providers.contains(LocationManager.NETWORK_PROVIDER)) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            if(pref.getBoolean("root_status",false)){
                if (runBlocking {RunCmdCommand.commandAsync("su -c settings put secure location_providers_allowed +gps,network").await()}) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        providers.add(LocationManager.GPS_PROVIDER)
                    }
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        providers.add(LocationManager.NETWORK_PROVIDER)
                    }
                }
            }
        }

         // if none provider available try to get last known location //
        if(providers.isNullOrEmpty()){
            providers.add("LAST_KNOWN")
        }

        intentService.putStringArrayListExtra("providers", providers)
        intentService.putExtra("number", senderNumber)
        // start service based on API version //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(context,intentService)
        } else {
            context.startService(intentService)
        }
    }

    private fun bluetoothSetState(state:Boolean){
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (state) {
            bluetoothAdapter.enable()
        }else{
            bluetoothAdapter.disable()
        }
    }

    private fun getInfo(context: Context):String{
        var message: String
        //WIFI
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiState = if(wifiManager.isWifiEnabled){
            "on"
        }else{
            "off"
        }
        message = "Wifi: $wifiState"
        if(wifiManager.isWifiEnabled){
            val wifiSSID = wifiManager.connectionInfo.ssid
            message += "\nSSID: $wifiSSID"
        }

        //mobileData
        var mobileDataState: Boolean?
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        try {
            val cmClass = Class.forName(cm!!.javaClass.name)
            val method: Method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true
            mobileDataState = method.invoke(cm) as Boolean
        } catch (e: java.lang.Exception) {
            mobileDataState = null
        }

        val mobileenabled = if(mobileDataState!= null && mobileDataState){
            "on"
        }else{
            "off"
        }
        message += "\nMobileData: $mobileenabled"

        //bluetooth
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothState = if(bluetoothAdapter.isEnabled){
            "on"
        }else{
            "off"
        }
        message += "\nBluetooth: $bluetoothState"

        //sync
        val syncState = if(ContentResolver.getMasterSyncAutomatically()){
            "on"
        }else{
            "off"
        }
        message += "\nSync: $syncState"

        //battery
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val batteryState = if(plugged == BatteryManager.BATTERY_PLUGGED_AC ||
            plugged == BatteryManager.BATTERY_PLUGGED_USB ||
            plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS){
            "charging"
        }else{
            "unplugged"
        }

        message += "\nBattery: $batteryLevel%, $batteryState"

        //location
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsState = if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            "on"
        }else{
            "off"
        }
        val networkState = if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            "on"
        }else{
            "off"
        }
       message += "\nLocation:\n-GPS: $gpsState\n-Network: $networkState"

        //battery saver
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batterySaverState = if(powerManager.isPowerSaveMode){
            "on"
        }else{
            "off"
        }
        message += "\nPower saver: $batterySaverState"

        return message
    }
}
