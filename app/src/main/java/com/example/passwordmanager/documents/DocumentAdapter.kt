package com.example.passwordmanager.documents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R

class DocumentAdapter(private val documents: List<Document>) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val documentImageView: ImageView = itemView.findViewById(R.id.documentImageView)
        val documentNameTextView: TextView = itemView.findViewById(R.id.documentNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.document_item, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = documents[position]
        holder.documentImageView.setImageResource(document.photoResId)
        holder.documentNameTextView.text = document.name
    }

    override fun getItemCount(): Int {
        return documents.size
    }
}