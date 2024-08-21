package com.example.passwordmanager

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

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

        Glide.with(holder.imageView.context)
            .load(uri)
            .override(600, 600)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.imageView.stopShimmerAnimation()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.imageView.stopShimmerAnimation()
                    holder.imageView.setOnClickListener {
                        onItemClick?.invoke(uri, holder.imageView)
                    }
                    return false
                }
            })
            .into(holder.imageView)
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