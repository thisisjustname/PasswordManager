package com.example.passwordmanager.documents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.size.ViewSizeResolver
import com.example.passwordmanager.R
import com.example.passwordmanager.RoundedCornersTransformation
import com.facebook.shimmer.ShimmerFrameLayout

class DocumentAdapter(
    private val documents: List<Document>,
    private val onItemClick: (Document, View, TextView, List<ImageView>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var showShimmer = true

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_SHIMMER = 1
    }

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

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerLayout: ShimmerFrameLayout = itemView as ShimmerFrameLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.document_item, parent, false)
                DocumentViewHolder(view)
            }
            VIEW_TYPE_SHIMMER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.document_item_shimmer, parent, false)
                ShimmerViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DocumentViewHolder -> {
                val document = documents[position]
                bindDocumentViewHolder(holder, document)
            }
            is ShimmerViewHolder -> {
                holder.shimmerLayout.startShimmer()
            }
        }
    }

    private fun bindDocumentViewHolder(holder: DocumentViewHolder, document: Document) {
        holder.nameTextView.text = document.name
        holder.cardView.transitionName = "documentCard_${document.id}"
        holder.nameTextView.transitionName = "documentTitle_${document.id}"

        val imageUris = document.imageUris.take(4)
        for (i in 0 until 4) {
            if (i < imageUris.size) {
                holder.thumbnailContainers[i].visibility = View.VISIBLE
                holder.thumbnails[i].visibility = View.VISIBLE
                holder.thumbnails[i].transitionName = "documentImage_${document.id}_$i"

                holder.thumbnails[i].load(imageUris[i]) {
                    crossfade(true)
                    placeholder(R.drawable.shimmer_placeholder)
                    scale(Scale.FIT)
                    transformations(RoundedCornersTransformation(16f))
                }
            } else {
                holder.thumbnailContainers[i].visibility = View.GONE
                holder.thumbnails[i].visibility = View.GONE
            }
        }

        adjustLayoutForImageCount(holder, imageUris.size)

        holder.itemView.setOnClickListener {
            onItemClick(document, holder.cardView, holder.nameTextView, holder.thumbnails)
        }
    }

    private fun adjustLayoutForImageCount(holder: DocumentViewHolder, imageCount: Int) {
        when (imageCount) {
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
    }

    override fun getItemCount(): Int = if (showShimmer) 10 else documents.size

    override fun getItemViewType(position: Int): Int {
        return if (showShimmer) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }

    fun showShimmer() {
        showShimmer = true
        notifyDataSetChanged()
    }

    fun hideShimmer() {
        showShimmer = false
        notifyDataSetChanged()
    }
}