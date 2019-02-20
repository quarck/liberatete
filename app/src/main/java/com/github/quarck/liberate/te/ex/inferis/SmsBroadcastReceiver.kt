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

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.telephony.SmsMessage
import android.util.Log
import kotlin.concurrent.thread

class SmsBroadcastReceiver : BroadcastReceiver()
{
	override fun onReceive(context: Context, intent: Intent)
	{
		val pdusBundle = intent.extras

		val pdus: Any? = pdusBundle.get("pdus") //  as Array<ByteArray>?
		val format: String? = intent.getStringExtra("format")

		Log.d(LOG_TAG, "SmsBroadcastReceiver.onReceive");

		//val pudsArray: Array<ByteArray>? = pdus as Array<*>
		if (pdus != null && (pdus is Array<*>?) && format != null)
		{
			val messages = SmsMessage.createFromPdu(pdus[0] as ByteArray, format)

			val password = MainActivity.getPassword(context)

			if (password != "")
			{
				val messageBody = messages.messageBody

				if (messageBody.length > password.length
						&& messageBody.startsWith(password))
				{
					val command =
							messageBody
									.substring(password.length + 1)
									.trim({ it <= ' ' })
									.toUpperCase()

					Log.i(LOG_TAG, "Received command: $command")
					handleCommand(context, command, messages);
					abortBroadcast()
				}
				else {
					Log.e(LOG_TAG, "Wrong password")
				}
			}
			else
			{
				Log.e(LOG_TAG, "Password is not set")
			}
		}
		else {
			Log.e(LOG_TAG, "Can't parse incoming SMS ${pdus != null} ${(pdus is Array<*>?)} ${format != null}")
		}
	}

	private fun handleCommand(context: Context, command: String, messages: SmsMessage)
	{
		when (command)
		{
			"WIPE" ->
				wipeEverything(context, messages, false)
			"FULLWIPE", "FULL WIPE" ->
				wipeEverything(context, messages, true)
			"REBOOT" ->
				rebootDevice(context, messages)
			"LOCATE" ->
				sendGPSCords(context, messages)
			"PING" ->
				handlePingCommand(context, messages)
			else ->
				SmsUtil.reply(messages, "Not recognized")
		}
	}

	private fun wipeEverything(context: Context, message: SmsMessage, fullWipe: Boolean)
	{
		val wipeThread =
				thread(false) {
					try {
						val lDPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
						lDPM.wipeData(if (fullWipe) DevicePolicyManager.WIPE_EXTERNAL_STORAGE else 0)
						Log.i(LOG_TAG, "Data wipe started, no exceptions")
					}
					catch (ex: Exception) {
						SmsUtil.reply(message, "Can't wipe: no permissions")
					}
				}

		SmsUtil.reply(message, (if (fullWipe) "FULL " else "") + "WIPE started", 3000)
		wipeThread.start()
	}

	private fun rebootDevice(context: Context, message: SmsMessage)  {
		SmsUtil.reply(message, "Rebooting", 2000)
		(context.getSystemService(Context.POWER_SERVICE) as PowerManager?)?.reboot(null)
	}

	private fun handlePingCommand(context: Context, message: SmsMessage) {
		SmsUtil.reply(message, "PONG")
	}

	private fun sendGPSCords(context: Context, message: SmsMessage) {
		SmsLocationReporter(message.originatingAddress).Start(context)
	}

	companion object {
		const val LOG_TAG = "LiberateTe"
	}
}
