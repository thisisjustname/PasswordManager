package com.example.passwordmanager.passwords

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.passwordmanager.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddPasswordDialogFragment : DialogFragment() {

    interface AddPasswordListener {
        fun onPasswordAdded(password: Password)
    }

    private lateinit var listener: AddPasswordListener

    fun setListener(listener: AddPasswordListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_password, null)
        val siteNameInput: EditText = dialogView.findViewById(R.id.siteNameInput)
        siteNameInput.inputType = InputType.TYPE_CLASS_TEXT
        val passwordTitle: TextView = dialogView.findViewById(R.id.addOrUpdate)
        val loginInput: EditText = dialogView.findViewById(R.id.loginInput)
        val emailInput: EditText = dialogView.findViewById(R.id.emailInput)
        val passwordInput: EditText = dialogView.findViewById(R.id.passwordInput)
        val addButton: Button = dialogView.findViewById(R.id.addButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        // Check if arguments are passed for editing
        arguments?.let {
            siteNameInput.setText(it.getString("siteName"))
            loginInput.setText(it.getString("login"))
            emailInput.setText(it.getString("email"))
            passwordInput.setText(it.getString("password"))
            passwordTitle.setText("Update Password")
            addButton.setText("Update")
            cancelButton.visibility = View.GONE
        }

        addButton.setOnClickListener {
            val siteName = siteNameInput.text.toString()
            val login = loginInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (siteName.isEmpty()) {
                siteNameInput.error = "Input at least 1 character."
                return@setOnClickListener
            }

            if (login.isEmpty()) {
                loginInput.error = "Input at least 1 character."
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailInput.error = "Input at least 1 character."
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Input at least 1 character."
                return@setOnClickListener
            }

            val newPassword = Password(siteName, login, email, password)
            listener.onPasswordAdded(newPassword)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, listener: AddPasswordListener) {
            val dialog = AddPasswordDialogFragment()
            dialog.setListener(listener)
            dialog.show(fragmentManager, "AddPasswordDialogFragment")
        }
    }
}