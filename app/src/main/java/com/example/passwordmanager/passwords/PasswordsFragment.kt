package com.example.passwordmanager.passwords
import DataManager
import com.example.passwordmanager.R


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PasswordsFragment : Fragment(), AddPasswordDialogFragment.AddPasswordListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PasswordAdapter
    private val passwords = mutableListOf<Password>()
    private val filteredPasswords = mutableListOf<Password>()
    private lateinit var secureStorage: DataManager
    private var isSelectionMode = false
    private lateinit var deleteButton: Button

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPasswords() {
        val savedPasswords = secureStorage.getDecrypted("passwords", Array<Password>::class.java)
        if (savedPasswords != null) {
            passwords.clear()
            passwords.addAll(savedPasswords)
            filteredPasswords.addAll(savedPasswords)
            adapter.notifyDataSetChanged()
        }
    }

    private fun savePasswords() {
        secureStorage.saveEncrypted("passwords", passwords.toTypedArray())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_passwords_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        secureStorage = DataManager(requireContext())

        recyclerView = view.findViewById(R.id.passwordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PasswordAdapter(filteredPasswords, { password -> showPasswordInfoDialog(password) }, { password -> onPasswordLongClick(password) })
        recyclerView.adapter = adapter
        loadPasswords()

        val searchEditText: EditText = view.findViewById(R.id.searchEditText)
        val searchCriteriaSpinner: Spinner = view.findViewById(R.id.searchCriteriaSpinner)
        val addButton: Button = view.findViewById(R.id.addPasswordButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        deleteButton.visibility = View.GONE

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterPasswords(s.toString(), searchCriteriaSpinner.selectedItem.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addButton.setOnClickListener {
            AddPasswordDialogFragment.show(childFragmentManager, this)
        }

        deleteButton.setOnClickListener {
            deleteSelectedPasswords()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterPasswords(query: String, criteria: String) {
        filteredPasswords.clear()
        for (password in passwords) {
            if ((criteria == "Site Name" && password.siteName.contains(query, true)) ||
                (criteria == "Login" && password.login.contains(query, true)) ||
                (criteria == "Email" && password.email.contains(query, true)) ||
                (criteria == "Password" && password.password.contains(query, true))) {
                filteredPasswords.add(password)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onPasswordAdded(password: Password) {
        passwords.add(password)
        filteredPasswords.add(password)
        adapter.notifyItemInserted(filteredPasswords.size - 1)
        savePasswords()
    }

    private fun onPasswordLongClick(password: Password) {
        adapter.toggleSelection(password)
        updateDeleteButtonVisibility()
    }

    private fun deleteSelectedPasswords() {
        val selectedPasswords = adapter.getSelectedPasswords()
        passwords.removeAll(selectedPasswords)
        filteredPasswords.removeAll(selectedPasswords)
        adapter.clearSelection()
        adapter.notifyDataSetChanged()
        savePasswords()
        updateDeleteButtonVisibility()
    }

    private fun updateDeleteButtonVisibility() {
        deleteButton.visibility = if (adapter.getSelectedPasswords().isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showPasswordInfoDialog(password: Password) {
        if (adapter.getSelectedPasswords().isNotEmpty()) {
            adapter.toggleSelection(password)
            updateDeleteButtonVisibility()
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_password_info, null)
        val siteName: TextView = dialogView.findViewById(R.id.siteName)
        val loginTextView: TextView = dialogView.findViewById(R.id.loginTextView)
        val emailTextView: TextView = dialogView.findViewById(R.id.emailTextView)
        val passwordTextView: TextView = dialogView.findViewById(R.id.passwordTextView)
        val copyLoginButton: Button = dialogView.findViewById(R.id.copyLoginButton)
        val copyEmailButton: Button = dialogView.findViewById(R.id.copyEmailButton)
        val copyPasswordButton: Button = dialogView.findViewById(R.id.copyPasswordButton)
        val okButton: Button = dialogView.findViewById(R.id.okButton)
        val editButton: Button = dialogView.findViewById(R.id.editButton)

        loginTextView.text = password.login
        emailTextView.text = password.email
        passwordTextView.text = password.password
        siteName.text = password.siteName

        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        copyLoginButton.setOnClickListener {
            val clip = ClipData.newPlainText("Login", password.login)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Login copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        copyEmailButton.setOnClickListener {
            val clip = ClipData.newPlainText("Email", password.email)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        copyPasswordButton.setOnClickListener {
            val clip = ClipData.newPlainText("Password", password.password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        editButton.setOnClickListener {
            // Open AddPasswordDialogFragment with existing password data for editing
            val dialogFragment = AddPasswordDialogFragment().apply {
                setListener(object : AddPasswordDialogFragment.AddPasswordListener {
                    override fun onPasswordAdded(updatedPassword: Password) {
                        // Update the password in the list and notify the adapter
                        val index = passwords.indexOfFirst { it.siteName == password.siteName && it.login == password.login }
                        if (index != -1) {
                            passwords[index] = updatedPassword
                            filterPasswords("", "") // Refresh the filtered list
                        }
                        filteredPasswords.add(updatedPassword)
                        adapter.notifyItemInserted(filteredPasswords.size - 1)
                        loginTextView.text = updatedPassword.login
                        emailTextView.text = updatedPassword.email
                        passwordTextView.text = updatedPassword.password
                        siteName.text = updatedPassword.siteName
                        savePasswords()
                    }
                })
                arguments = Bundle().apply {
                    putString("siteName", password.siteName)
                    putString("login", password.login)
                    putString("email", password.email)
                    putString("password", password.password)
                }
            }
            dialogFragment.show(childFragmentManager, "EditPasswordDialogFragment")
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

data class Password(
    val siteName: String,
    val login: String,
    val email: String,
    val password: String
)