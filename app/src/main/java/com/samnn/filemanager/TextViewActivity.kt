package com.samnn.filemanager

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.samnn.filemanager.databinding.ActivityTextViewBinding
import java.io.File
import java.io.InputStream

class TextViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextViewBinding
    private lateinit var file: File
    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appbar)

        path = intent.getStringExtra("path")
        file = path?.let { File(it) }!!
        val inputStream: InputStream = file.inputStream()
        val reader = inputStream.reader()
        val content = reader.readText()
        reader.close()

        binding.content.setText(content)
        binding.appbar.title = file.name
        binding.appbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        binding.appbar.setNavigationOnClickListener {
            if (binding.appbar.title != file.name) {
                val newFile = File("${file.parent}/${binding.appbar.title}")
                if (!newFile.exists()) file.renameTo(newFile)
            }
            if (file.parent == cacheDir.path) {
                file.delete()
            }
            this.finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.document_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v("Clicked", item.toString())
        return when (item.itemId) {
            R.id.save -> {
                save()
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun save() {
        val outputStream = file.outputStream()
        val writer = outputStream.writer()
        writer.write(binding.content.text.toString())
        writer.close()
    }
}