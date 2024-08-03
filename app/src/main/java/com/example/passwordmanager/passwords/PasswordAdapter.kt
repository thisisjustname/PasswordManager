package com.example.passwordmanager.passwords

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R

class PasswordAdapter(
    private var passwords: List<Password>,
    private val onItemClick: (Password) -> Unit,
    private val onItemLongClick: (Password) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    private val selectedPasswords = mutableSetOf<Password>()
    private var isSelectionMode = false

    class PasswordViewHolder(itemView: View, private val onItemClick: (Password) -> Unit, private val onItemLongClick: (Password) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val siteNameTextView: TextView = itemView.findViewById(R.id.siteNameTextView)
        private val loginTextView: TextView = itemView.findViewById(R.id.loginTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.selectionCheckBox)
        private lateinit var password: Password

        init {
            itemView.setOnClickListener {
                onItemClick(password)
            }
            itemView.setOnLongClickListener {
                onItemLongClick(password)
                true
            }
        }

        fun bind(password: Password, isSelected: Boolean, isSelectionMode: Boolean) {
            this.password = password
            siteNameTextView.text = password.siteName
            loginTextView.text = password.login
            emailTextView.text = password.email
            checkBox.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            checkBox.isChecked = isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.password_item, parent, false)
        return PasswordViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val password = passwords[position]
        holder.bind(password, selectedPasswords.contains(password), isSelectionMode)
    }

    override fun getItemCount(): Int {
        return passwords.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePasswords(newPasswords: List<Password>) {
        this.passwords = newPasswords
        notifyDataSetChanged()
    }

    fun toggleSelection(password: Password) {
        if (selectedPasswords.contains(password)) {
            selectedPasswords.remove(password)
        } else {
            selectedPasswords.add(password)
        }
        isSelectionMode = selectedPasswords.isNotEmpty()
        notifyDataSetChanged()
    }

    fun getSelectedPasswords(): List<Password> {
        return selectedPasswords.toList()
    }

    fun clearSelection() {
        selectedPasswords.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        notifyDataSetChanged()
    }
}