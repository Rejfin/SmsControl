package com.rejfin.smscontrol.receivers

import android.content.*
import android.provider.Telephony
import com.rejfin.smscontrol.CommandManager


class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            CommandManager().manage(context!!,intent)
        }
    }
}