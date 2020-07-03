package com.rejfin.smscontrol.helpers_class

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalStateException

object RunCmdCommand {
    // command used to run console command //
    fun commandAsync(command: String):Deferred<Boolean> = runBlocking {
        async(Dispatchers.IO){
            var process:Process? = null
            var processResult = 0
            FirebaseCrashlytics.getInstance().log("command: $command")
            val processJob:Deferred<Boolean> = async(Dispatchers.IO) {
                try {
                    process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                    processResult = process!!.waitFor()
                    true
                } catch (e: IOException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    false
                }catch (e1: InterruptedException) {
                    FirebaseCrashlytics.getInstance().recordException(e1)
                    false
                }
            }

            if(!processJob.await()){
                return@async false
            }

            if (processResult != 0) { //error executing command
                Log.d("smsControl", "result code : $processResult")
                var line: String
                var result = false
                val bufferedReader =
                    BufferedReader(InputStreamReader(process!!.errorStream))
                try {
                    while (bufferedReader.readLine().also { line = it } != null) {
                        Log.d("smsControl", "Error: $line")
                        FirebaseCrashlytics.getInstance().log("Error: $line")
                    }
                } catch (e: IOException) {
                    result = false
                }catch (e: IllegalStateException){
                    result = false
                }
                result
            }else{
                //Command execution is OK
                val bufferedReader =
                    BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                try {
                    while (bufferedReader.readLine().also { line = it } != null) {
                        Log.d("smsControl", line!!)
                    }
                } catch (e: IOException) {
                    Log.d("smsControl", "OK Error: ${e.localizedMessage}")
                }
                true
            }
        }
    }
}