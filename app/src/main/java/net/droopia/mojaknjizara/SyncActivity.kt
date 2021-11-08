package net.droopia.mojaknjizara

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.squareup.picasso.Picasso
import net.droopia.mojaknjizara.database.Book
import net.droopia.mojaknjizara.database.BookViewModel
import net.droopia.mojaknjizara.database.BookViewModelFactory
import net.droopia.mojaknjizara.database.BooksApplication
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

//https://www.geeksforgeeks.org/progressbar-in-kotlin/

class SyncActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null
    private var i = 0
    private var txtView: TextView? = null
    private val handler = Handler()



    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }

//    val unsyncedBook: LiveData<List<Book>>? = bookViewModel.toReadShelf

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync)

        // finding progressbar by its id
        progressBar = findViewById<ProgressBar>(R.id.progress_Bar) as ProgressBar

        // finding textview by its id
        txtView = findViewById<TextView>(R.id.text_view)

        // finding button by its id
        val btn = findViewById<Button>(R.id.show_button)


        bookViewModel.toReadShelf?.observe(this, { observable ->
            if (observable != null) {
                for (book: Book in observable) {
                    println(book)
                }
            }

            observable?.let { details ->
                println(details)
            }
        })


        // handling click on button
        btn.setOnClickListener {
            // Before clicking the button the progress bar will invisible
            // so we have to change the visibility of the progress bar to visible
            // setting the progressbar visibility to visible
            progressBar!!.visibility = View.VISIBLE

            i = progressBar!!.progress

            Thread(Runnable {


                // this loop will run until the value of i becomes 99
                while (i < 100) {
                    i += 1
                    // Update the progress bar and display the current value
                    handler.post(Runnable {
                        progressBar!!.progress = i
                        // setting current progress to the textview
                        txtView!!.text = i.toString() + "/" + progressBar!!.max
                    })
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

                // setting the visibility of the progressbar to invisible
                // or you can use View.GONE instead of invisible
                // View.GONE will remove the progressbar


            }).start()

            progressBar!!.visibility = View.INVISIBLE
        }
    }

    /**
     * Post book to WebBookList
     *
     * @param book Book object of currently viewed book
     */
    fun postBook(current: Book) {


        val urlDev = "http://10.0.2.2:8000/api/blogpost"

        val url = (
                PreferenceManager.getDefaultSharedPreferences(this).getString(
                    "remote_url", urlDev)?.toUri() ?: urlDev
                ).toString()

//        "marko1112@droopia.net"
        val apiToken = (
                PreferenceManager.getDefaultSharedPreferences(this).getString(
                    "api_token", urlDev)?.toUri() ?: urlDev
                ).toString()

        val picasoBitmap = Picasso.get().load(current.cover).get()
        val fileObject = bitmapToFile(picasoBitmap, "temp_file.jpg")!!

        val client = OkHttpClient()

        val JSONObjectString_2 = "{\"title\": \"${current.title}\", \"shortDescription\": \"${current.author}\", \"body\": \"${current.isbn13}\"}"
        val MEDIA_TYPE_JPG = "image/jpeg".toMediaType()
        val fileUri = Uri.parse(current.cover)
        val fileName = fileUri.lastPathSegment

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("metadata", JSONObjectString_2)
            .addFormDataPart("file", fileName, fileObject.asRequestBody(MEDIA_TYPE_JPG))
            .build()

        val request = Request.Builder()
            .header("X-AUTH-TOKEN", apiToken)
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response)
        }
    }

    /**
     * Convert Picasso loaded bitmap to File object
     * need File object to post it via .addFormDataPart on okhttp request
     * and this was most reliable way to get the image.
     * https://handyopinion.com/convert-bitmap-to-file-in-android-java-kotlin/
     *
     * @param bitmap Bitmap loaded with picasso
     * @param fileNameToSave String to save this temporary file
     */
    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, name: String) {
        val fos: OutputStream?
        fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, "$name.jpg")
            FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        Objects.requireNonNull(fos)!!.close()
    }

    private fun saveImage2(image: File, fileNameToSave: String) {
//        showProgressBar()

        val date = Date()
        val formatter = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss:mm", Locale.getDefault())
        val dateFormatted = formatter.format(date)


        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val to = File( getExternalFilesDir(Environment.DIRECTORY_PICTURES)  , "BookCoverPhoto-$timeStamp.jpg")


//        val to = File(Environment.getExternalStorageDirectory().absolutePath + "/" + DIRECTORY_DCIM + "/zynkphoto${dateFormatted}.jpg")

        val inputStream: InputStream = FileInputStream(image)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "BookCoverPhoto-$timeStamp.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM")
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//            mUri = imageUri!!
//
//            intent.putExtra("mUri", mUri.toString());
            val out = resolver.openOutputStream(imageUri!!)
            out?.write(image.readBytes())
            out?.flush()
            out?.close()
        } else {
            val out: OutputStream = FileOutputStream(to)

            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            inputStream.close()
//            mUri = Uri.fromFile(to)
//            intent.putExtra("mUri", mUri.toString());
            out.flush()
            out.close()


        }

//        hideProgessBar()
//        showAlertDialog(getString(R.string.photo_saved), "", getString(R.string.ok_label))
    }

}