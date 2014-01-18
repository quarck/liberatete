/*
 * Copyright (c) 2014, Sergey Parshin, quarck@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.github.quarck.liberate.te.ex.inferis

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class SmsLocationReporter(private val requestedByAddr: String ) : LocationListener {

    private var locationManager: LocationManager? = null

    private var lastPositionUpdate: Long = 0
    private var lastAccuracy = 100000.0f

    private var isGpsEnabled = false
    private var isNetworkEnabled = false

    private var totalSentMsgs = 0

    fun Start(context: Context)
    {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

            lastPositionUpdate = System.currentTimeMillis() // a bit of a hack to make sender don't sent within first 20 seconds until it receives good location

            if (isGpsEnabled)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 10.0f, this)

            if (isNetworkEnabled)
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 10.0f, this)

            if (!isGpsEnabled && !isNetworkEnabled)
                SmsUtil.send(requestedByAddr, "Sorry, all network location providers are disabled!")
        }
        catch (ex: SecurityException) {
            SmsUtil.send(requestedByAddr, "No permission for location access")
        }
    }

    override fun onLocationChanged(location: Location)
    {
        val accuracy = location.accuracy

        val provider = location.provider

        val gotAccurateLocation = accuracy < 10.0

        val now = System.currentTimeMillis()

        var sendUpdate = false
        var terminate = false

        if (!isGpsEnabled || gotAccurateLocation)
        // if gps disabled or if got very accurate GPS fix - send straight away and disable
        {
            sendUpdate = true
            terminate = true
        }
        else if (now - lastPositionUpdate > 20000L && accuracy < 0.5f * lastAccuracy)
        // if we've got significantly improved fix since the last update sent 20 secs ago - send it...
        {
            sendUpdate = true
        }
        else if (now - lastPositionUpdate > 360000L)
        // no position improvements in the last 5 minutes? terminate
        {
            terminate = false
        }

        if (sendUpdate)
        {
            val sb = StringBuilder()

            val isGpsLocation = (provider === LocationManager.GPS_PROVIDER)
            sb.append(if (isGpsLocation) "GPS" else "Net")

            sb.append("LOC: ")
            sb.append(location.latitude)
            sb.append(" ")
            sb.append(location.longitude)
            sb.append(" A: ")
            sb.append(location.altitude)

            val speed = location.speed.toDouble()
            if (speed > 10.0f)
            {
                sb.append(" S: ")
                sb.append(speed)
            }

            sb.append(" acc: ")
            sb.append(accuracy)

            if (!isGpsEnabled)
                sb.append(" [GPS off]")

            SmsUtil.send(requestedByAddr, sb.toString())
            totalSentMsgs++

            lastAccuracy = accuracy
            lastPositionUpdate = now
        }

        if (terminate || totalSentMsgs >= 5)
        {
            locationManager?.removeUpdates(this)
            locationManager = null
        }
    }

    override fun onProviderDisabled(arg0: String)
    {
    }

    override fun onProviderEnabled(arg0: String)
    {
    }

    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle)
    {
    }
}