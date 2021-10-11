package net.droopia.mojaknjizara.adapters

import java.io.File

interface ImageAdapterListener {
    fun onSaveButtonClicked(image: File)
}