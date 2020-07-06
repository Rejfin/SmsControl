package com.rejfin.smscontrol.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.rejfin.smscontrol.ForegroundNotification
import com.rejfin.smscontrol.receivers.LocationListener
import com.rejfin.smscontrol.R
import com.rejfin.smscontrol.helpers_class.SendSms
import kotlinx.coroutines.*
import java.util.*

class LocationService : Service(){
    private lateinit var notifyManager:NotificationManagerCompat
    private var lastKnownLocation:Location? = null
    var locationOne:Location? = null
    var locationTwo:Location? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notifyManager = NotificationManagerCompat.from(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val provider = intent!!.getStringArrayListExtra("providers")!!
        val senderNumber = intent.getStringExtra("number")!!
        val duration = intent.getIntExtra("duration",30)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val notify = ForegroundNotification()
            .createNotification(this,"A", NotificationManagerCompat.IMPORTANCE_LOW,getString(
                R.string.getting_location
            ))
        this.startForeground(2,notify)

        // check required permission //
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // try to get last known location from providers //
            if(provider.size!=0 && provider.contains("LAST_KNOWN")){
                val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                val listAvailableLocation = mutableListOf<Location?>(gpsLocation,networkLocation,passiveLocation)
                for (i in listAvailableLocation.size-1 downTo 0){
                    // remove nulls and location older than 10 min //
                    if(listAvailableLocation[i] == null || listAvailableLocation[i]!!.time < Date().time - 600000){
                        listAvailableLocation.removeAt(i)
                    }
                }
                // save best result from all providers //
                lastKnownLocation = if(listAvailableLocation.size > 0){
                    if(listAvailableLocation.size == 1){
                        listAvailableLocation[0]
                    }else{
                        if(listAvailableLocation[0]!!.accuracy > listAvailableLocation[1]!!.accuracy){
                            listAvailableLocation[1]
                        }else{
                            listAvailableLocation[0]
                        }
                    }
                }else{
                    null
                }
            }else{
                val locationListenerOne =
                    LocationListener()
                var locationListenerTwo: LocationListener? = null
                if(provider.size > 1) {
                    locationListenerTwo =
                        LocationListener()
                }
                locationManager.requestLocationUpdates(provider[0],0L,0f, locationListenerOne)
                locationListenerOne.setLocationChangeListener(object:
                    LocationListener.OnLocationUpdateListener {
                    override fun onLocationChange(location: Location) {
                        locationOne = location
                        locationManager.removeUpdates(locationListenerOne)
                    }
                })

                if(locationListenerTwo != null){
                    locationManager.requestLocationUpdates(provider[1],0L,0f, locationListenerTwo)
                    locationListenerTwo.setLocationChangeListener(object:
                        LocationListener.OnLocationUpdateListener {
                        override fun onLocationChange(location: Location) {
                            locationTwo = location
                            locationManager.removeUpdates(locationListenerTwo)
                        }
                    })
                }
            }

            if(provider.contains("LAST_KNOWN")){
                if(lastKnownLocation != null){
                    val message = "Location provider: ${lastKnownLocation!!.provider}\nAccuracy: ${lastKnownLocation!!.accuracy}m\nhttps://www.google.com/maps/search/?api=1&query=${lastKnownLocation!!.latitude},${lastKnownLocation!!.longitude}"
                    SendSms.sendSms(
                        this,
                        message,
                        senderNumber
                    )
                }else{
                    SendSms.sendSms(
                        this,
                        getString(R.string.location_not_available),
                        senderNumber
                    )
                }
                stopForeground(true)
                stopSelf()
            }else{
                // choose the best data from the available providers after 30s //
                CoroutineScope(Dispatchers.IO).launch {
                    val timer = async { delay(duration*1000L) }
                    timer.await()
                    val finalLocation:Location? = if(locationOne != null){
                        if(locationTwo != null){
                            if(locationOne!!.accuracy < locationTwo!!.accuracy){
                                locationOne
                            }else{
                                locationTwo
                            }
                        }else{
                            locationOne
                        }
                    }else if(locationTwo != null){
                        locationTwo
                    }else{
                        null
                    }

                    if(finalLocation != null){
                        val message = "Location provider: ${finalLocation.provider}\nAccuracy: ${finalLocation.accuracy}m\nhttps://www.google.com/maps/search/?api=1&query=${finalLocation.latitude},${finalLocation.longitude}"
                        SendSms.sendSms(
                            this@LocationService,
                            message,
                            senderNumber
                        )
                    }else{
                        SendSms.sendSms(
                            this@LocationService,
                            getString(R.string.location_not_available),
                            senderNumber
                        )
                    }
                    delay(3000)
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }
}