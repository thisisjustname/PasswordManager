package com.example.passwordmanager.passwords

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.passwordmanager.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class AddPasswordDialogFragment : DialogFragment() {

    private lateinit var generatePasswordButton: Button
    private lateinit var passwordGenerator: PasswordGenerator
    private lateinit var passwordInput: EditText

    interface AddPasswordListener {
        fun onPasswordAdded(password: Password)
    }

    private lateinit var listener: AddPasswordListener

    fun setListener(listener: AddPasswordListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_password, null)
        val passwordTitle: TextView = dialogView.findViewById(R.id.addOrUpdate)
        val addButton: Button = dialogView.findViewById(R.id.addButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        val siteNameLayout: TextInputLayout = dialogView.findViewById(R.id.siteNameLayout)
        val loginLayout: TextInputLayout = dialogView.findViewById(R.id.loginLayout)
        val emailLayout: TextInputLayout = dialogView.findViewById(R.id.emailLayout)
        val passwordLayout: TextInputLayout = dialogView.findViewById(R.id.passwordLayout)

        val siteNameInput: EditText = dialogView.findViewById(R.id.siteNameInput)
        val loginInput: EditText = dialogView.findViewById(R.id.loginInput)
        val emailInput: EditText = dialogView.findViewById(R.id.emailInput)
        passwordInput = dialogView.findViewById(R.id.passwordInput)

        generatePasswordButton = dialogView.findViewById(R.id.generatePasswordButton)
        passwordGenerator = PasswordGenerator()

        generatePasswordButton.setOnClickListener {
            showPasswordGeneratorDialog()
        }



        // Check if arguments are passed for editing
        arguments?.let {
            siteNameInput.setText(it.getString("siteName"))
            loginInput.setText(it.getString("login"))
            emailInput.setText(it.getString("email"))
            passwordInput.setText(it.getString("password"))
            passwordTitle.setText(R.string.update_password)
            addButton.setText(R.string.update)
            cancelButton.visibility = View.GONE
        }

        addButton.setOnClickListener {
            val siteName = siteNameInput.text.toString()
            val login = loginInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            var wasError = false

            if (siteName.isEmpty()) {
                siteNameLayout.isErrorEnabled = true
                siteNameLayout.error = getString(R.string.input_at_least)
                wasError = true
            } else {
                siteNameLayout.isErrorEnabled = false
            }

            if (login.isEmpty()) {
                loginLayout.isErrorEnabled = true
                loginLayout.error = getString(R.string.input_at_least)
                wasError = true
            } else {
                loginLayout.isErrorEnabled = false
            }

            if (email.isEmpty()) {
                emailLayout.isErrorEnabled = true
                emailLayout.error = getString(R.string.input_at_least)
                wasError = true
            } else {
                emailLayout.isErrorEnabled = false
            }

            if (password.isEmpty()) {
                passwordLayout.isErrorEnabled = true
                passwordLayout.error = getString(R.string.input_at_least)
                wasError = true
            } else {
                passwordLayout.isErrorEnabled = false
            }

            if (wasError){
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

    private fun showPasswordGeneratorDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_generate_password, null)
        val useSpecialChars: androidx.appcompat.widget.SwitchCompat = dialogView.findViewById(R.id.useSpecialChars)
        val useNumbers: androidx.appcompat.widget.SwitchCompat = dialogView.findViewById(R.id.useNumbers)
        val useUppercase: androidx.appcompat.widget.SwitchCompat = dialogView.findViewById(R.id.useUppercase)
        val lengthSlider: com.google.android.material.slider.Slider = dialogView.findViewById(R.id.lengthSlider)
        val generatedPasswordText: TextView = dialogView.findViewById(R.id.generatedPasswordText)
        val regenerateButton: Button = dialogView.findViewById(R.id.regenerateButton)
        val confirmButton: Button = dialogView.findViewById(R.id.confirmButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        fun updatePassword() {
            val password = passwordGenerator.generatePassword(
                length = lengthSlider.value.toInt(),
                useSpecialChars = useSpecialChars.isChecked,
                useNumbers = useNumbers.isChecked,
                useUppercase = useUppercase.isChecked
            )
            generatedPasswordText.text = password
        }

        useSpecialChars.setOnCheckedChangeListener { _, _ -> updatePassword() }
        useNumbers.setOnCheckedChangeListener { _, _ -> updatePassword() }
        useUppercase.setOnCheckedChangeListener { _, _ -> updatePassword() }
        lengthSlider.addOnChangeListener { _, _, _ -> updatePassword() }

        regenerateButton.setOnClickListener { updatePassword() }

        updatePassword() // Generate initial password

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        confirmButton.setOnClickListener {
            passwordInput.setText(generatedPasswordText.text)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, listener: AddPasswordListener) {
            val dialog = AddPasswordDialogFragment()
            dialog.setListener(listener)
            dialog.show(fragmentManager, "AddPasswordDialogFragment")
        }
    }
}