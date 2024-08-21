package com.example.passwordmanager.cards

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R

// Обновление адаптера для обработки долгого нажатия и управления режимом выбора
class CardAdapter(
    private var cards: List<Card>,
    private val onItemClick: (Card) -> Unit,
    private val onItemLongClick: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private val selectedCards = mutableSetOf<Card>()
    private var isSelectionMode = false

    class CardViewHolder(
        view: View,
        private val onItemClick: (Card) -> Unit,
        private val onItemLongClick: (Card) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val cardNameTextView: TextView = view.findViewById(R.id.cardNameTextView)
        private val cardNumberTextView: TextView = view.findViewById(R.id.cardNumberTextView)
        private val cardView: com.google.android.material.card.MaterialCardView =
            view.findViewById(R.id.cardView)
        private lateinit var card: Card
        private var isInSelectionMode = false

        init {
            itemView.setOnClickListener {
                if (isInSelectionMode) {
                    toggleSelection()
                } else {
                    onItemClick(card)
                }
            }
            itemView.setOnLongClickListener {
                toggleSelection()
                true
            }
        }

        private fun toggleSelection() {
            cardView.isChecked = !cardView.isChecked
            onItemLongClick(card)
        }

        fun bind(card: Card, isSelected: Boolean, isInSelectionMode: Boolean) {
            this.card = card

            this.isInSelectionMode = isInSelectionMode

            cardNameTextView.text = card.cardName
            cardNumberTextView.text = card.cardNumber

            cardView.isChecked = isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card, selectedCards.contains(card), isSelectionMode)
    }

    override fun getItemCount() = cards.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateCards(newCards: List<Card>) {
        this.cards = newCards
        notifyDataSetChanged()
    }

    fun toggleSelection(card: Card) {
        if (selectedCards.contains(card)) {
            selectedCards.remove(card)
        } else {
            selectedCards.add(card)
        }
        isSelectionMode = selectedCards.isNotEmpty()
        notifyDataSetChanged()
    }

    fun getSelectedCards(): List<Card> {
        return selectedCards.toList()
    }

    fun clearSelection() {
        selectedCards.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        notifyDataSetChanged()
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedCards.clear()
        notifyDataSetChanged()
    }
}