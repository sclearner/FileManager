package com.samnn.filemanager

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.samnn.filemanager.databinding.ActivityMediaViewBinding
import java.io.File

class MediaViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaViewBinding.inflate(layoutInflater)
        val path = intent.getStringExtra("path")
        val bitmap = BitmapFactory.decodeFile(path)
        binding.media.setImageBitmap(bitmap)
        binding.appbar.title = path?.let { File(it).name }

        setContentView(binding.root)
    }
}