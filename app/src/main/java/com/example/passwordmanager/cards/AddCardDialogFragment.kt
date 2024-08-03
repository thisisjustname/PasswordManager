package com.example.passwordmanager.cards

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.passwordmanager.R
import com.example.passwordmanager.passwords.AddPasswordDialogFragment
import com.example.passwordmanager.passwords.Password
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddCardDialogFragment : DialogFragment() {

    interface AddCardListener {
        fun onCardAdded(card: Card)
    }

    private lateinit var listener: AddCardListener

    fun setListener(listener: AddCardListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_cards, null)
        val cardNameInput: TextView = dialogView.findViewById(R.id.cardNameInput)
        val cardTitleInput: TextView = dialogView.findViewById(R.id.addOrUpdate)
        val cardNumberInput: EditText = dialogView.findViewById(R.id.cardNumberInput)
        cardNumberInput.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val space = ' '

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val input = s.toString().replace(" ", "")
                val formatted = StringBuilder()

                for (i in input.indices) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(space)
                    }
                    formatted.append(input[i])
                }

                isUpdating = true
                cardNumberInput.setText(formatted.toString())
                cardNumberInput.setSelection(formatted.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        val cardCVVInput: EditText = dialogView.findViewById(R.id.cardCVVInput)
        val cardPinInput: EditText = dialogView.findViewById(R.id.cardPinInput)
        val addButton: Button = dialogView.findViewById(R.id.addButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        // Check if arguments are passed for editing
        arguments?.let {
            cardNameInput.setText(it.getString("cardName"))
            cardNumberInput.setText(it.getString("number"))
            cardCVVInput.setText(it.getString("cvv"))
            cardPinInput.setText(it.getString("pin"))
            cardTitleInput.setText("Update Card")
            addButton.setText("Update")
            cancelButton.visibility = View.GONE
        }

        addButton.setOnClickListener {
            val cardName = cardNameInput.text.toString()
            val number = cardNumberInput.text.toString()
            val cvv = cardCVVInput.text.toString()
            val pin = cardPinInput.text.toString()

            if (cardName.isEmpty()) {
                cardNameInput.error = "Input at least one character"
                return@setOnClickListener
            }

            if (number.length != 19) {
                cardNumberInput.error = "Card number must be 16 digits"
                return@setOnClickListener
            }

            if (cvv.length != 3) {
                cardCVVInput.error = "CVV must be 3 digits"
                return@setOnClickListener
            }

            if (pin.length != 4) {
                cardPinInput.error = "CVV must be 4 digits"
                return@setOnClickListener
            }


            val newCard = Card(cardName, number, cvv, pin)
            listener.onCardAdded(newCard)
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
        fun show(fragmentManager: FragmentManager, listener: AddCardListener) {
            val dialog = AddCardDialogFragment()
            dialog.setListener(listener)
            dialog.show(fragmentManager, "AddCardDialogFragment")
        }
    }
}