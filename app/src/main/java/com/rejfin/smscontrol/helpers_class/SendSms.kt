package com.rejfin.smscontrol.helpers_class

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics

object SendSms {
    // function used to send sms //
    fun sendSms(context: Context, message:String, number:String){
        try{
            val smsManager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
                SmsManager.getSmsManagerForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId())
            }else{
                SmsManager.getDefault()
            }

            val messages = smsManager.divideMessage(message)
            for (mess in messages){
                smsManager.sendTextMessage(number,null,mess,null,null)
            }
        }catch(e:Exception){
            Toast.makeText(context,e.localizedMessage, Toast.LENGTH_LONG).show()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}