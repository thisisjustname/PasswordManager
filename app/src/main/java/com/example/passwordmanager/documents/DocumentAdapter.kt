package com.example.passwordmanager.documents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R

class DocumentAdapter(
    private val documents: List<Document>,
    private val onItemClick: (Document, View, TextView) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val nameTextView: TextView = itemView.findViewById(R.id.documentNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.document_item, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = documents[position]
        holder.nameTextView.text = document.name

        holder.cardView.transitionName = "documentCard_${document.id}"
        holder.nameTextView.transitionName = "documentTitle_${document.id}"

        holder.itemView.setOnClickListener {
            onItemClick(document, holder.cardView, holder.nameTextView)
        }
    }

    override fun getItemCount() = documents.size
}