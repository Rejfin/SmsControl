package com.rejfin.smscontrol.helpers_class

import android.content.Context
import android.text.format.DateFormat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.rejfin.smscontrol.R
import java.io.FileNotFoundException
import java.lang.Exception
import java.util.*

object LogManager {
    // save data to log file //
    fun saveToLog(message:String,context: Context){
        try{
            val file = context.openFileOutput("Log.txt",Context.MODE_APPEND)
            val calendar = Calendar.getInstance()
            val date = DateFormat.format("dd.MM.yyyy HH:mm:ss",calendar.timeInMillis)
            file.write("$date -> $message\n".toByteArray())
            file.close()
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    // read data saved in log file //
    fun readFromLog(context: Context):String{
        return try{
            val file = context.openFileInput("Log.txt")
            val text = String(file.readBytes())
            file.close()
            text
        }catch (e:FileNotFoundException){
            context.getString(R.string.empty_log_file)
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().recordException(e)
            context.getString(R.string.error_read_logs)
        }
    }

    // clear log file //
    fun clearLogs(context: Context):Boolean{
        return try{
            context.deleteFile("Log.txt")
            true
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }
}