package com.example.passwordmanager

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ImageAdapter(
    private val onItemClick: ((Uri, View) -> Unit)? = null
) : ListAdapter<Uri, ImageAdapter.ImageViewHolder>(IMAGE_COMPARATOR) {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShimmerShapeableImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = getItem(position)
        holder.imageView.transitionName = "image_$position"
        holder.imageView.startShimmerAnimation()

        holder.imageView.setOnClickListener {
            onItemClick?.invoke(uri, holder.imageView)
        }

        holder.imageView.load(uri) {
            size(600, 600)
            listener(
                onStart = {
                    holder.imageView.startShimmerAnimation()
                },
                onSuccess = { _, _ ->
                    holder.imageView.stopShimmerAnimation()
                    holder.imageView.setOnClickListener {
                        onItemClick?.invoke(uri, holder.imageView)
                    }
                },
                onError = { _, _ ->
                    holder.imageView.stopShimmerAnimation()
                }
            )
        }
    }

    companion object {
        private val IMAGE_COMPARATOR = object : DiffUtil.ItemCallback<Uri>() {
            override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean =
                oldItem.toString() == newItem.toString()

            override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean =
                oldItem == newItem
        }
    }
}