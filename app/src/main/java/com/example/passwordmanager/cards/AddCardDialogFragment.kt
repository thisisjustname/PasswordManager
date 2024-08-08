package com.example.passwordmanager.cards

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        val cardNameLayout: TextInputLayout = dialogView.findViewById(R.id.cardNameLayout)
        val cardNumberLayout: TextInputLayout = dialogView.findViewById(R.id.cardNumberLayout)
        val cardCVVLayout: TextInputLayout = dialogView.findViewById(R.id.cardCVVLayout)
        val cardPinLayout: TextInputLayout = dialogView.findViewById(R.id.cardPinLayout)

        // Check if arguments are passed for editing
        arguments?.let {
            cardNameInput.setText(it.getString("cardName"))
            cardNumberInput.setText(it.getString("number"))
            cardCVVInput.setText(it.getString("cvv"))
            cardPinInput.setText(it.getString("pin"))
            cardTitleInput.setText(R.string.update_card)
            addButton.setText(R.string.update)
            cancelButton.visibility = View.GONE
        }

        addButton.setOnClickListener {
            val cardName = cardNameInput.text.toString()
            val number = cardNumberInput.text.toString()
            val cvv = cardCVVInput.text.toString()
            val pin = cardPinInput.text.toString()

            var wasError = false

            if (cardName.isEmpty()) {
                cardNameLayout.isErrorEnabled = true
                cardNameLayout.error = getString(R.string.input_at_least)
                wasError = true
            } else {
                cardNameLayout.isErrorEnabled = false
            }

            if (number.length != 19) {
                cardNumberLayout.isErrorEnabled = true
                cardNumberLayout.error = getString(R.string.card_numbers)
                wasError = true
            } else {
                cardNumberLayout.isErrorEnabled = false
            }

            if (cvv.length != 3) {
                cardCVVLayout.isErrorEnabled = true
                cardCVVLayout.error = getString(R.string.cvv_length)
                wasError = true
            } else {
                cardCVVLayout.isErrorEnabled = false
            }

            if (pin.length != 4) {
                cardPinLayout.isErrorEnabled = true
                cardPinLayout.error = getString(R.string.pin_length)
                wasError = true
            } else {
                cardPinLayout.isErrorEnabled = false
            }

            if(wasError){
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