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
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
class SetPasswordActivity : Activity()
{
	lateinit var setPasswordStatusMessageLabel: TextView
	lateinit var passwordEditBox: EditText
	lateinit var passwordConfirmationBox: EditText

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_set_password)

		setPasswordStatusMessageLabel = findViewById<TextView>(R.id.setPasswordStatusMessageLabel)
		passwordEditBox = findViewById<EditText>(R.id.passwordEditBox)
		passwordConfirmationBox = findViewById<EditText>(R.id.passwordConfirmationBox)

		setPasswordStatusMessageLabel.text = getString(R.string.initial_instruction)
		setPasswordStatusMessageLabel.visibility = View.VISIBLE
	}

	fun setPassword(v: View)
	{
		val password1 = passwordEditBox.text.toString()
		val password2 = passwordConfirmationBox.text.toString()

		if (password1 == password2)
		{
			if (password1.length >= 4)
			{
				if (password1.length < 7)
				{
					AlertDialog
						.Builder(this)
						.setMessage(getString(R.string.password_is_shorter_than_7_sym))
						.setPositiveButton(getString(R.string.use_short),
							{ dialog: DialogInterface?, which: Int ->  commitAndFinish(password1) })
						.setNegativeButton(getString(R.string.cancel),
							{ dialog: DialogInterface?, which: Int ->  resumeEditing("") })
						.create()
						.show()
				}
				else
				{
					commitAndFinish(password1)
				}
			}
			else
			{
				resumeEditing(getString(R.string.password_too_short))
			}
		}
		else
		{
			resumeEditing(getString(R.string.password_didnt_match))
		}
	}

	private fun commitAndFinish(password: String)
	{
		MainActivity.setPassword(this, password)
		setPasswordStatusMessageLabel.text = ""
		finish()

	}

	private fun resumeEditing(reason: String)
	{
		setPasswordStatusMessageLabel.text = reason
		setPasswordStatusMessageLabel.visibility = View.VISIBLE
        passwordEditBox.requestFocus()
	}

	companion object {
		const val LOG_TAG = "LiberateTe"
	}
}
