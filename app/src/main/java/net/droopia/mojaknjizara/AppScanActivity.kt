package net.droopia.mojaknjizara

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.provider.MediaStore
import android.util.Log
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.tbruyelle.rxpermissions3.RxPermissions
import net.droopia.mojaknjizara.adapters.ImageAdapter

class AppScanActivity : ScanActivity() {


    private lateinit var mUri: Uri
    private var bookId: Long = 0
    companion object {
        private val TAG = AppScanActivity::class.simpleName
        private var bookId: Long = 0
        fun start(context: Context, bookId: Long) {
//            this.bookId = bookId

            val intent = Intent(context, AppScanActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var alertDialogBuilder: android.app.AlertDialog.Builder? = null
    private var alertDialog: android.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookId = intent.extras?.getLong(EXTRA_ID)!!

        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_app_scan)
        addFragmentContentLayout()
    }

    override fun onError(error: DocumentScannerErrorModel) {
//        showAlertDialog(getString(R.string.error_label), error.errorMessage?.error, getString(R.string.ok_label))
        Log.d(TAG, "error: $error.errorMessage?.error")
    }

    override fun onSuccess(scannerResults: ScannerResults) {
//        var sRa = scannerResults.croppedImageFile?.absolutePath
//        var sRc = scannerResults.croppedImageFile?.canonicalPath
//        var sRp = scannerResults.croppedImageFile?.path
//        intent?.putExtra(EXTRA_ID, sR)
//        Log.d(TAG, "scannerResults.croppedImageFile.canonicalPath: $sRa")
//        Log.d(TAG, "scannerResults.croppedImageFile.absolutePath: $sRc")
//        Log.d(TAG, "scannerResults.croppedImageFile.path: $sRp")
//        intent.putExtra("scannerResults", sRp);
//        setResult(RESULT_OK, getIntent());

//        initViewPager(scannerResults)

        checkForStoragePermissions(scannerResults.croppedImageFile!!)

        intent.putExtra("mUri", mUri.toString());
        setResult(RESULT_OK, intent)
        finish();


    }

    override fun onClose() {
        Log.d(TAG, "onClose")
        finish()
    }

    fun onSaveButtonClicked(image: File) {
        Log.d(TAG, "save image")
        checkForStoragePermissions(image)
    }

    private fun checkForStoragePermissions(image: File) {
        RxPermissions(this)
            .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        saveImage(image)
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.STORAGE_PERMISSION_REFUSED_WITHOUT_NEVER_ASK_AGAIN))
                    }
                    else -> {
                        onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.STORAGE_PERMISSION_REFUSED_GO_TO_SETTINGS))
                    }
                }
            }
    }

    private fun saveImage(image: File) {
        showProgressBar()

        val date = Date()
        val formatter = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss:mm", Locale.getDefault())
        val dateFormatted = formatter.format(date)

        val to = File(Environment.getExternalStorageDirectory().absolutePath + "/" + DIRECTORY_DCIM + "/zynkphoto${dateFormatted}.jpg")

        val inputStream: InputStream = FileInputStream(image)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "zynkphoto${dateFormatted}.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM")
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            mUri = imageUri!!
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
            out.flush()
            out.close()
        }

        hideProgessBar()
//        showAlertDialog(getString(R.string.photo_saved), "", getString(R.string.ok_label))

    }

    private fun showProgressBar() {
//        progressLayoutApp.isVisible = true
    }

    private fun hideProgessBar() {
//        progressLayoutApp.isVisible = false
    }
//
    private fun initViewPager(scannerResults: ScannerResults) {
        val fileList = ArrayList<File>()

        scannerResults.originalImageFile?.let {
            Log.d(TAG, "ZDCoriginalPhotoFile size ${it.sizeInMb}")
        }

        scannerResults.croppedImageFile?.let {
            Log.d(TAG, "ZDCcroppedPhotoFile size ${it.sizeInMb}")
        }

        scannerResults.transformedImageFile?.let {
            Log.d(TAG, "ZDCtransformedPhotoFile size ${it.sizeInMb}")
        }

        scannerResults.originalImageFile?.let { fileList.add(it) }
        scannerResults.transformedImageFile?.let { fileList.add(it) }
        scannerResults.croppedImageFile?.let { fileList.add(it) }

//        val targetAdapter = ImageAdapter(this, fileList, this)
//        viewPagerTwo.adapter = targetAdapter
//        viewPagerTwo.isUserInputEnabled = false
//
//        previousButton.setOnClickListener {
//            viewPagerTwo.currentItem = viewPagerTwo.currentItem - 1
//            nextButton.isVisible = true
//            if (viewPagerTwo.currentItem == 0) {
//                previousButton.isVisible = false
//            }
//        }
//
//        nextButton.setOnClickListener {
//            viewPagerTwo.currentItem = viewPagerTwo.currentItem + 1
//            previousButton.isVisible = true
//            if (viewPagerTwo.currentItem == fileList.size - 1) {
//                nextButton.isVisible = false
//            }
//        }
    }
//
//
//
    private fun showAlertDialog(title: String?, message: String?, buttonMessage: String) {
        alertDialogBuilder = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonMessage) { dialog, which ->

            }
        alertDialog?.dismiss()
        alertDialog = alertDialogBuilder?.create()
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()
    }

    val File.size get() = if (!exists()) 0.0 else length().toDouble()
    val File.sizeInKb get() = size / 1024
    val File.sizeInMb get() = sizeInKb / 1024

}