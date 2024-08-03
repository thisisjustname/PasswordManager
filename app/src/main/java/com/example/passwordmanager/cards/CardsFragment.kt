package com.example.passwordmanager.cards

import DataManager
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.passwords.AddPasswordDialogFragment
import com.example.passwordmanager.passwords.Password
import com.example.passwordmanager.passwords.PasswordAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchBar

class CardsFragment : Fragment(), AddCardDialogFragment.AddCardListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdapter
    private val cards = mutableListOf<Card>()
    private val filteredCards = mutableListOf<Card>()
    private lateinit var secureStorage: DataManager
    private var isSelectionMode = false
    private lateinit var deleteButton: Button

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCards() {
        val savedPasswords = secureStorage.getDecrypted("cards", Array<Card>::class.java)
        if (savedPasswords != null) {
            cards.clear()
            cards.addAll(savedPasswords)
            filteredCards.addAll(savedPasswords)
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveCards() {
        secureStorage.saveEncrypted("cards", cards.toTypedArray())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_cards_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        secureStorage = DataManager(requireContext())

        recyclerView = view.findViewById(R.id.cardsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CardAdapter(filteredCards, { card -> showCardInfoDialog(card) }, { card -> onCardLongClick(card) })
        recyclerView.adapter = adapter
        loadCards()

        val searchEditText: EditText = view.findViewById(R.id.searchEditText)
        val addButton: Button = view.findViewById(R.id.addCardButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        deleteButton.visibility = View.GONE

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterCards(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        addButton.setOnClickListener {
            AddCardDialogFragment.show(childFragmentManager, this)
        }

        deleteButton.setOnClickListener {
            deleteSelectedCards()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterCards(query: String) {
        filteredCards.clear()
        for (card in cards) {
            if ((card.cardName.contains(query, true)) ||
                (card.cardNumber.contains(query, true))) {
                filteredCards.add(card)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCardAdded(card: Card) {
        cards.add(card)
        filteredCards.add(card)
        adapter.notifyItemInserted(filteredCards.size - 1)
        saveCards()
    }

    private fun onCardLongClick(card: Card) {
        adapter.toggleSelection(card)
        updateDeleteButtonVisibility()
    }

    private fun deleteSelectedCards() {
        val selectedCards = adapter.getSelectedCards()
        cards.removeAll(selectedCards)
        filteredCards.removeAll(selectedCards)
        adapter.clearSelection()
        adapter.notifyDataSetChanged()
        saveCards()
        updateDeleteButtonVisibility()
    }

    private fun updateDeleteButtonVisibility() {
        deleteButton.visibility = if (adapter.getSelectedCards().isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showCardInfoDialog(card: Card) {
        if (adapter.getSelectedCards().isNotEmpty()) {
            adapter.toggleSelection(card)
            updateDeleteButtonVisibility()
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_card_info, null)
        val cardName: TextView = dialogView.findViewById(R.id.cardName)
        val cardNumber: TextView = dialogView.findViewById(R.id.cardNumberTextView)
        val cardCvv: TextView = dialogView.findViewById(R.id.cardCVVTextView)
        val cardPin: TextView = dialogView.findViewById(R.id.cardPinView)
        val copyNumberButton: Button = dialogView.findViewById(R.id.copyNumberButton)
        val copyCVVButton: Button = dialogView.findViewById(R.id.copyCVVButton)
        val copyPinButton: Button = dialogView.findViewById(R.id.copyPinButton)
        val okButton: Button = dialogView.findViewById(R.id.okButton)
        val editButton: Button = dialogView.findViewById(R.id.editButton)

        cardName.text = card.cardName
        cardNumber.text = card.cardNumber
        cardCvv.text = card.cvv
        cardPin.text = card.pin

        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        copyNumberButton.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textToCopy = card.cardNumber.replace(" ", "")
            val clip = ClipData.newPlainText("Card Number", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Number copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        copyCVVButton.setOnClickListener {
            val clip = ClipData.newPlainText("CVV", card.cvv)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "CVV copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        copyPinButton.setOnClickListener {
            val clip = ClipData.newPlainText("Pin", card.pin)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Pin copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        editButton.setOnClickListener {
            // Open AddPasswordDialogFragment with existing password data for editing
            val dialogFragment = AddCardDialogFragment().apply {
                setListener(object : AddCardDialogFragment.AddCardListener {
                    override fun onCardAdded(updatedCard: Card) {
                        // Update the password in the list and notify the adapter
                        val index = cards.indexOfFirst { it.cardNumber == card.cardNumber && it.cardName == card.cardName }
                        if (index != -1) {
                            cards[index] = updatedCard
                            filterCards("") // Refresh the filtered list
                        }
                        filteredCards.add(updatedCard)
                        adapter.notifyItemInserted(filteredCards.size - 1)
                        cardName.text = updatedCard.cardName
                        cardNumber.text = updatedCard.cardNumber
                        cardCvv.text = updatedCard.cvv
                        cardPin.text = updatedCard.pin
                        saveCards()
                    }
                })
                arguments = Bundle().apply {
                    putString("cardName", card.cardName)
                    putString("number", card.cardNumber)
                    putString("cvv", card.cvv)
                    putString("pin", card.pin)
                }
            }
            dialogFragment.show(childFragmentManager, "EditCardDialogFragment")
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

data class Card(
    val cardName: String,
    val cardNumber: String,
    val cvv: String,
    val pin: String
)