package net.droopia.mojaknjizara.adapters

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

import net.droopia.mojaknjizara.R
import java.io.File

class ImageViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val photoImageView = itemView.findViewById<ImageView>(R.id.mImageView)
    private val saveButton = itemView.findViewById<TextView>(R.id.mImageView)

    fun bindData(context: Context?, image: File, listener: ImageAdapterListener, position: Int, size: Int) {
//        photoImageView.setImageFile(image)

        Picasso.get().load(image).into(photoImageView, object : Callback {
            override fun onSuccess() {
                println("crop loaded")
            }

            override fun onError(e: Exception) {
                println("crop errored")
            }
        })

        saveButton.setOnClickListener {
            listener.onSaveButtonClicked(image)
        }
    }
}

//private fun PhotoView.setImageFile(image: File) {
//
//
//
//
////    Glide.with(this).load(image)
////        .diskCacheStrategy(DiskCacheStrategy.NONE)
////        .skipMemoryCache(true)
////        .into(this)
//}
