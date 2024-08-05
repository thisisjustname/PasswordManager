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
import android.view.animation.AnimationUtils
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
    private lateinit var searchBar: com.google.android.material.search.SearchBar
    private lateinit var searchView: com.google.android.material.search.SearchView
    private lateinit var searchAdapter: CardAdapter

    @SuppressLint("NotifyDataSetChanged")
    private fun loadCards() {
        val savedCards = secureStorage.getDecrypted("cards", Array<Card>::class.java)
        if (savedCards != null) {
            cards.clear()
            cards.addAll(savedCards)
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
        adapter = CardAdapter(cards, { card -> showCardInfoDialog(card) }, { card -> onCardLongClick(card) })
        recyclerView.adapter = adapter

        searchBar = view.findViewById(R.id.searchBar)
        searchView = view.findViewById(R.id.searchView)
        deleteButton = view.findViewById(R.id.deleteButton)
        deleteButton.visibility = View.GONE

        setupSearch()
        loadCards()

        searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    AddCardDialogFragment.show(childFragmentManager, this)
                    true
                }
                else -> false
            }
        }

        deleteButton.setOnClickListener {
            deleteSelectedCards()
        }
    }

    private fun setupSearch() {
        searchBar.setOnClickListener {
            searchView.show()
        }

        // Создаем отдельный RecyclerView для результатов поиска
        val searchRecyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        searchAdapter = CardAdapter(mutableListOf(), { card -> showCardInfoDialog(card) }, { card -> onCardLongClick(card) })
        searchRecyclerView.adapter = searchAdapter

        // Добавляем RecyclerView в SearchView
        searchView.addView(searchRecyclerView)

        searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterCards(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterCards(query: String) {
        val filteredCards = cards.filter { card ->
            card.cardName.contains(query, true) || card.cardNumber.contains(query, true)
        }
        searchAdapter.updateCards(filteredCards)
        searchAdapter.notifyDataSetChanged()
    }

    override fun onCardAdded(card: Card) {
        cards.add(card)
        adapter.notifyItemInserted(cards.size - 1)
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
        val prevVisibility = deleteButton.visibility
        deleteButton.visibility = if (adapter.getSelectedCards().isNotEmpty()) View.VISIBLE else View.GONE
        val scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up)
        val scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)
        if(prevVisibility != deleteButton.visibility) {
            if (deleteButton.visibility == View.VISIBLE) {
                deleteButton.startAnimation(scaleUp)
            } else{
                deleteButton.startAnimation(scaleDown)
            }
        }
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
            val dialogFragment = AddCardDialogFragment().apply {
                setListener(object : AddCardDialogFragment.AddCardListener {
                    override fun onCardAdded(updatedCard: Card) {
                        // Update the password in the list and notify the adapter
                        val index = cards.indexOfFirst { it.cardNumber == card.cardNumber && it.cardName == card.cardName }
                        if (index != -1) {
                            cards[index] = updatedCard
                            adapter.notifyItemChanged(index)
                            filterCards("") // Refresh the filtered list
                        }
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

    fun onBackPressed(): Boolean {
        if (adapter.getSelectedCards().isNotEmpty()) {
            adapter.exitSelectionMode()
            updateDeleteButtonVisibility()
            return true
        }
        return false
    }
}

data class Card(
    val cardName: String,
    val cardNumber: String,
    val cvv: String,
    val pin: String
)