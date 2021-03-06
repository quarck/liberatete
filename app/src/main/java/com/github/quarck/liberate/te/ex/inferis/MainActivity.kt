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

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MyDeviceAdminReceiver : DeviceAdminReceiver()

class MainActivity : Activity()
{
	private val devicePolicyManager: DevicePolicyManager by lazy {
		getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
	}
	private val deviceAdminComponentName: ComponentName by lazy {
		ComponentName(this, MyDeviceAdminReceiver::class.java)
	}

    private lateinit var buttonEnableDeviceAdmin: TextView
    private lateinit var buttonSetPassword: TextView


	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

        buttonEnableDeviceAdmin = findViewById<TextView>(R.id.buttonEnableDeviceAdmin)
        buttonSetPassword = findViewById<TextView>(R.id.buttonSetPassword)

		buttonSetPassword.setOnClickListener(this::stepOneSetPassword)
		buttonEnableDeviceAdmin.setOnClickListener(this::stepTwoEnableAdmin)
	}

    private fun checkAndRequestPermissions() {
        if (!PermissionsManager.hasAllPermissions(this)) {
            if (PermissionsManager.shouldShowRationale(this)) {
                AlertDialog
                        .Builder(this)
                        .setMessage(getString(R.string.permissions_rationale))
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.ok)) { _: DialogInterface?, _: Int ->
							PermissionsManager.requestPermissions(this)
						}
						.setNegativeButton(getString(android.R.string.cancel)) { _: DialogInterface?, _: Int ->
							this@MainActivity.finish()
						}
						.create()
                        .show()
            } else {
                PermissionsManager.requestPermissions(this)
            }
        }
    }

	override fun onResume()
	{
		super.onResume()
		updateControls()
		checkAndRequestPermissions();
	}

	private fun updateControls()
	{
		checkAndRequestPermissions();

		val passwordConfigured = isPasswordConfigured;

        buttonEnableDeviceAdmin.isEnabled = passwordConfigured;
        buttonSetPassword.setText(if (passwordConfigured) R.string.step1done else R.string.step1)

		val adminActive = isActiveAdmin;

        buttonEnableDeviceAdmin.setText(if (adminActive) R.string.step2done else R.string.step2)
	}

	fun stepOneSetPassword(v: View)
	{
        Log.d(LOG_TAG, "stepOneSetPassword")

		val intent = Intent(this, SetPasswordActivity::class.java)
		startActivity(intent)
	}

	fun stepTwoEnableAdmin(v: View)
	{
		enableAdmin()
	}

	private val isPasswordConfigured: Boolean
		get() = "" != getPassword(this)


	private val isActiveAdmin: Boolean
		get() = devicePolicyManager?.isAdminActive(deviceAdminComponentName) ?: false


	protected fun enableAdmin()
	{
		Log.d(LOG_TAG, "enableAdmin")
		// Launch the activity to have the user enable our admin.

		val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName)
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
			getString(R.string.enable_admin))

		startActivityForResult(intent, 1)
	}

	companion object
	{
		val SHARED_PREF = "main_pref"
		val PASSWORD_KEY = "pwd"
		val LAUNCHER_DISABLED_KEY = "ld"

		fun Context.setPrefs(fn: SharedPreferences.Editor.() -> Unit): Unit
		{
			val prefs = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.fn();
			editor.apply()
		}

		private fun <T> Context.getPrefs(fn: SharedPreferences.() -> T): T
		{
			val prefs = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			return prefs.fn()
		}

		fun setPassword(ctx: Context, strPassword: String)
			= ctx.setPrefs { putString(PASSWORD_KEY, strPassword) }

		fun getPassword(ctx: Context): String
			= ctx.getPrefs { getString(PASSWORD_KEY, "") }

		fun setLauncherDisabled(ctx: Context)
			= ctx.setPrefs { putBoolean(LAUNCHER_DISABLED_KEY, true) }

		fun getLauncherDisabled(ctx: Context): Boolean
			= ctx.getPrefs { getBoolean(LAUNCHER_DISABLED_KEY, false) }

        const val LOG_TAG = "LiberateTe"
    }
}
