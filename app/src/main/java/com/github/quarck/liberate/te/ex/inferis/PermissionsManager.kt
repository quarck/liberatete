//
//   Liberate Te Ex Inferis
//   Copyright (C) 2017 Sergey Parshin (s.parshin.sc@gmail.com)
//
//   This program is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//
//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with this program; if not, write to the Free Software Foundation,
//   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
//


package com.github.quarck.liberate.te.ex.inferis

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

object PermissionsManager {
    private fun Context.hasPermission(perm: String) =
            this@hasPermission.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED

    private fun Activity.shouldShowRationale(perm: String) =
            this@shouldShowRationale.shouldShowRequestPermissionRationale(perm)

    fun hasAllPermissions(context: Context) =
            context.hasPermission(Manifest.permission.RECEIVE_SMS) &&
                    context.hasPermission(Manifest.permission.SEND_SMS) &&
                    context.hasPermission(Manifest.permission.BROADCAST_SMS) &&
                    context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

    fun shouldShowRationale(activity: Activity) =
            activity.shouldShowRationale(Manifest.permission.RECEIVE_SMS)
                    || activity.shouldShowRationale(Manifest.permission.SEND_SMS)
                    || activity.shouldShowRationale(Manifest.permission.BROADCAST_SMS)
                    || activity.shouldShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || activity.shouldShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

    fun requestPermissions(activity: Activity) =
            activity.requestPermissions(
                    arrayOf(Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.BROADCAST_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 0)
}