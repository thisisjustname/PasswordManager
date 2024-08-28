package com.example.passwordmanager.documents

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.passwordmanager.R
import com.example.passwordmanager.SecureImageStorage

class DocumentAdapter(
    private val documents: List<Document>,
    private val onItemClick: (Document, View, TextView, List<ImageView>) -> Unit
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cardView)
        val nameTextView: TextView = itemView.findViewById(R.id.documentNameTextView)
        val thumbnailsGrid: ConstraintLayout = itemView.findViewById(R.id.thumbnailsGrid)
        val thumbnailContainers: List<ConstraintLayout> = listOf(
            itemView.findViewById(R.id.thumbnail1Container),
            itemView.findViewById(R.id.thumbnail2Container),
            itemView.findViewById(R.id.thumbnail3Container),
            itemView.findViewById(R.id.thumbnail4Container)
        )
        val thumbnails: List<ImageView> = listOf(
            itemView.findViewById(R.id.thumbnail1),
            itemView.findViewById(R.id.thumbnail2),
            itemView.findViewById(R.id.thumbnail3),
            itemView.findViewById(R.id.thumbnail4)
        )
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

        // Load thumbnails
        val secureStorage = SecureImageStorage(holder.itemView.context)
        val imageUris = document.fileNames.take(4).mapNotNull { fileName ->
            secureStorage.getDecryptedImage(fileName)?.let { Uri.fromFile(it) }
        }

        // Set visibility and layout for thumbnails based on count
        for (i in 0 until 4) {
            if (i < imageUris.size) {
                holder.thumbnailContainers[i].visibility = View.VISIBLE
                holder.thumbnails[i].visibility = View.VISIBLE
                holder.thumbnails[i].transitionName = "documentImage_${document.id}_$i"

                holder.thumbnails[i].load(imageUris[i]) {
                    crossfade(true)
                    placeholder(R.drawable.shimmer_placeholder)
                }
            } else {
                holder.thumbnailContainers[i].visibility = View.GONE
                holder.thumbnails[i].visibility = View.GONE
            }
        }

        // Adjust layout for different image counts
        when (imageUris.size) {
            1 -> {
                holder.thumbnailContainers[0].layoutParams =
                    (holder.thumbnailContainers[0].layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = 0
                        constrainedWidth = true
                        horizontalChainStyle = ConstraintLayout.LayoutParams.CHAIN_PACKED
                        matchConstraintPercentWidth = 0.8f
                    }
            }
            2 -> {
                holder.thumbnailContainers[1].visibility = View.VISIBLE
                holder.thumbnailContainers[2].visibility = View.GONE
                holder.thumbnailContainers[3].visibility = View.GONE
            }
            3 -> {
                holder.thumbnailContainers[2].visibility = View.VISIBLE
                holder.thumbnailContainers[2].layoutParams =
                    (holder.thumbnailContainers[2].layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = 0
                        constrainedWidth = true
                        horizontalChainStyle = ConstraintLayout.LayoutParams.CHAIN_PACKED
                        matchConstraintPercentWidth = 0.8f
                    }
                holder.thumbnailContainers[3].visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(document, holder.cardView, holder.nameTextView, holder.thumbnails)
        }
    }

    override fun getItemCount() = documents.size
}