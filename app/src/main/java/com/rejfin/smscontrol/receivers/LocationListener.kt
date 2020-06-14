package com.rejfin.smscontrol.receivers

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class LocationListener : LocationListener {
    private var mLocationListener: OnLocationUpdateListener? = null

    override fun onLocationChanged(location: Location?) {
        // save only locations that have an accuracy of less than 30m //
        if(location!!.accuracy < 30f){
            if(mLocationListener != null){
                mLocationListener!!.onLocationChange(location)
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    interface OnLocationUpdateListener{
        fun onLocationChange(location:Location)
    }

    fun setLocationChangeListener(listener: OnLocationUpdateListener){
        mLocationListener = listener
    }
}