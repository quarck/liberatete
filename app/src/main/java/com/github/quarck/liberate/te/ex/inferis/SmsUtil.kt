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

import android.telephony.SmsManager
import android.telephony.SmsMessage

object SmsUtil
{
	@JvmOverloads fun send(dst: String?, msg: String, sleepMillis: Long = 0)
	{
		try
		{
			if (dst != null)
			{
				val smsMgr = SmsManager.getDefault()

				smsMgr?.sendTextMessage(dst, null, msg, null, null)

				if (sleepMillis > 0)
					Thread.sleep(sleepMillis) // sleep for 3 seconds to give SMS chance to get delivered
			}
		}
		catch (ex: Exception)
		{
			// catch everything, since we must simply try sending, if it fails - it is not the biggest deal
		}

	}

	@JvmOverloads fun reply(originalMessage: SmsMessage, replyText: String, sleepMillis: Long = 0)
	{
		send(originalMessage.originatingAddress, replyText, sleepMillis)
	}
}
