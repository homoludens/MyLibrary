package net.droopia.mojaknjizara

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import net.droopia.mojaknjizara.databinding.ActivityMainBinding
import com.zynksoftware.documentscanner.ui.DocumentScanner
import android.graphics.Bitmap
import androidx.activity.viewModels
import net.droopia.mojaknjizara.database.BookViewModel
import net.droopia.mojaknjizara.api.Exporter
import net.droopia.mojaknjizara.api.Import
import net.droopia.mojaknjizara.database.BookViewModelFactory
import net.droopia.mojaknjizara.database.BooksApplication
import net.droopia.mojaknjizara.database.emptyBook

const val LOG_EDIT = "BookEdit"
const val LOG_SEARCH = "BookSearch"
const val TAG_NEW = "BookNew"
const val EXTRA_BOOK = "Bookintent"
const val LOG_TAG = "BookSearch"
const val EXTRA_ID= ""
const val LOG_BOOK_VIEW= "BookView"
const val TAG = "scan activitiy"

class MainActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels {
        BookViewModelFactory((application as BooksApplication).repository)
    }


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }


//        val configuration = DocumentScanner.Configuration()
//        configuration.imageQuality = 100
//        configuration.imageSize = 1000000 // 1 MB
//        configuration.imageType = Bitmap.CompressFormat.JPEG
//        DocumentScanner.init(this, configuration) // or simply DocumentScanner.init(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.search_scan_item -> {
                val intent = Intent(applicationContext, LibrarySearchActivity::class.java).apply {
                    putExtra(EXTRA_SCAN, true)
                }
                startActivity(intent)
                return true
            }
            R.id.search_menu_item -> {
                val intent = Intent(applicationContext, LibrarySearchActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_settings ->  {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_sync ->  {
                val intent = Intent(applicationContext, SyncActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}