package com.rejfin.smscontrol

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalStateException

class RunCmdCommand {
    fun command(command: String): Boolean {
        val process = try {
            Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        val result = try {
            process.waitFor()
        } catch (e1: InterruptedException) {
            e1.printStackTrace()
            return false
        }

        if (result != 0) { //error executing command
            Log.d("smsControl", "result code : $result")
            var line: String
            val bufferedReader =
                BufferedReader(InputStreamReader(process.errorStream))
            try {
                while (bufferedReader.readLine().also { line = it } != null) {
                    Log.d("smsControl", "Error: $line")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }catch (e: IllegalStateException){
                e.printStackTrace()
                return false
            }
            return false
        }

        //Command execution is OK
        val bufferedReader =
            BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        try {
            while (bufferedReader.readLine().also { line = it } != null) {
                Log.d("smsControl", line!!)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }
}